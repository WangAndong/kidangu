package franco.model;

import java.io.*;

import lcm.lcm.*;
import april.lcmtypes.*;

public class PoseSimulator implements LCMSubscriber
{
    int i = 0;
    double lastKnownTheta = -1;

    public PoseSimulator()
    {
        LCM.getSingleton().subscribe("VELODYNE", this);
    }

    @Override
    public void messageReceived(LCM arg0, String arg1, LCMDataInputStream ins)
    {
        try {
            velodyne_t velodyne = new velodyne_t(ins);

            pose_t pose = new pose_t();
            pose.utime = velodyne.utime;
            pose.orientation = new double[] {1, 0, 0, 0};
            pose.pos = new double[] {-0.25*i, 0, 0};

            LCM.getSingleton().publish("POSE", pose);

            // TODO: Publish pose every velodyne sweep
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
