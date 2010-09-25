package franco.model;

import java.io.*;
import java.util.*;

import lcm.lcm.*;
import april.config.*;
import april.jmat.*;
import april.lcmtypes.*;
import april.util.*;
import april.velodyne.*;

public class PointCloud implements LCMSubscriber
{
    final double[] sensorPos;
    final double[] sensorQuat;
    ArrayList<double[]> points = new ArrayList<double[]>();
    int prevPointCount = 0;
    final Model model;

    static final VelodyneCalibration calibrationData = VelodyneCalibration.makeMITCalibration();


    public PointCloud(Config config, Model model)
    {
        this.sensorPos = ConfigUtil.getPosition(config, "velodyne");
        this.sensorQuat = ConfigUtil.getQuaternion(config, "velodyne");
        this.model = model;

        LCM.getSingleton().subscribe("VELODYNE", this);
    }

    public synchronized double[] get(int index)
    {
        return points.get(index);
    }

    public synchronized int size()
    {
        return points.size();
    }

    public synchronized int getUpdateBeginIndex()
    {
        return prevPointCount;
    }

    public synchronized ArrayList<double[]> getUpdatedPoints()
    {
        return new ArrayList<double[]>(points.subList(prevPointCount, points.size()));
    }

    @Override
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        if (!channel.equals("VELODYNE"))
            return;

        try {
            processVelodyneMessage(new velodyne_t(ins));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processVelodyneMessage(final velodyne_t packet)
    {
        pose_t pose = PoseTracker.getSingleton().get();
        if (pose == null)
            return;

        Velodyne parser = new Velodyne(calibrationData, packet.data);
        Velodyne.Sample sample = new Velodyne.Sample();

        double B2G[][] = LinAlg.quatPosToMatrix(pose.orientation, pose.pos);
        double S2B[][] = LinAlg.quatPosToMatrix(sensorQuat, sensorPos);
        double T[][] = LinAlg.matrixAB(B2G, S2B);

        synchronized (this) {
            while (parser.next(sample)) {
                points.add(LinAlg.transform(T, sample.xyz));
            }
        }

        // Notify about update
        for (ModelUpdateListener l : model.listeners)
            l.pointCloudUpdated();
    }
}
