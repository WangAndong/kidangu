package franco.view;

import java.util.*;

import april.jmat.*;
import april.vis.*;
import franco.model.*;

public class PointCloudView implements ModelUpdateListener
{
    final Model model;
    final VisCanvas vc;
    VisWorld.Buffer vb;
    VisData vd = new VisData();
    ColorMapper colorMapper = ColorMapper.makeJet(-5, 7);
    volatile boolean fPointCloudUpdated;

    public PointCloudView(Model model, VisCanvas vc, int pointSize)
    {
        this.model = model;
        this.vc = vc;
        this.vb = vc.getWorld().getBuffer("pointcloud");
        this.vd.add(new VisDataPointStyle(new CachedColorizer(), pointSize));

        model.addListener(this);
        new RenderThread().start();
    }

    @Override
    public void pointCloudUpdated()
    {
        // Color points and append to data
        PointCloud pc = model.getPointCloud();
        Random random = new Random();
        for (int i=pc.getUpdateBeginIndex(); i<pc.size(); ++i) {
            if (random.nextDouble() > 0.999) {
                double[] p = LinAlg.resize(pc.get(i), 4);
                p[3] = colorMapper.map(p[2]);

                vd.add(p);
            }
        }

        fPointCloudUpdated = true;
    }

    class RenderThread extends Thread
    {
        public RenderThread()
        {
            setDaemon(true);
        }

        @Override
        public void run()
        {
            try {
                while (true) {
                    if (fPointCloudUpdated) {
                        vb.addBuffered(vd);
                        vb.switchBuffer();
                    }

                    Thread.sleep(16);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
