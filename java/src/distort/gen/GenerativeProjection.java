package distort.gen;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.*;
import javax.swing.*;

import lcm.lcm.*;

import april.jmat.*;
import april.lcmtypes.*;
import april.util.*;
import april.vis.*;

import magic.vis.chart.*;


public class GenerativeProjection implements LCMSubscriber
{
    final VisWorld.Buffer vb;

    public GenerativeProjection(String modelFile) throws IOException
    {
        JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        VisCanvas vc = new VisCanvas(new VisWorld());
        vc.setBackground(Color.black);
        jf.add(vc, BorderLayout.CENTER);

        vb = vc.getWorld().getBuffer("projection");

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);

        LCM.getSingleton().subscribe("HOMOGRAPHY", this);
    }

    @Override
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        try {
            raw_t raw = new raw_t(ins);
            DataInputStream dins = new DataInputStream(new ByteArrayInputStream(raw.buf));

            double[][] h = new double[3][3];
            for (int i=0; i<3; ++i)
                for (int j=0; j<3; ++j)
                    h[i][j] = dins.readDouble();

            Homography hmg = new Homography(h);

            ArrayList<double[]> points = new ArrayList<double[]>();
            points.add(hmg.project(-.1, -.1));
            points.add(hmg.project(+.1, -.1));
            points.add(hmg.project(+.1, +.1));
            points.add(hmg.project(-.1, +.1));
            points.add(hmg.project(-.1, -.1));
            points.add(hmg.project(+.1, +.1));
            points.add(hmg.project(-.1, +.1));
            points.add(hmg.project(+.1, -.1));

            VisPlot plot = new VisPlot();
            plot.setXAxis(ChartAxis.makeXAxis(-600, 600, 50));
            plot.setYAxis(ChartAxis.makeYAxis(-600, 600, 50));
            plot.addData(new XYDataSeries(points, new VisDataLineStyle(Color.cyan, 2)));
            vb.addBuffered(plot);
            vb.switchBuffer();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException
    {
        new GenerativeProjection(args[0]);
    }
}

class Homography
{
    final double[][] h;

    public Homography(double[][] h)
    {
        this.h = h;
    }

    public double[] project(double worldx, double worldy)
    {
        double ixy[] = new double[2];
        ixy[0] = h[0][0] * worldx + h[0][1] * worldy + h[0][2];
        ixy[1] = h[1][0] * worldx + h[1][1] * worldy + h[1][2];
        double z = h[2][0] * worldx + h[2][1] * worldy + h[2][2];
        ixy[0] = ixy[0] / z;
        ixy[1] = ixy[1] / z;
        return ixy;
    }
}
