package distort.gen;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import april.image.*;
import april.jmat.*;
import april.tag.*;
import april.util.*;


public class MosaicMatch
{
    TagFamily tf = new Tag36h11();
    TagDetector td = new TagDetector(tf);

    final FloatImage foim;
    ArrayList<int[]> pixels;

    public MosaicMatch(BufferedImage oim)
    {
        final int W = oim.getWidth();
        final int H = oim.getHeight();

        /* Find pixels that lie within tags */
        ArrayList<TagDetection> detections =  td.process(oim, new double[] {W*0.5, H*0.5});
        pixels = new ArrayList<int[]>();
        for (TagDetection td : detections) {
            double[] a = td.p[0];
            double[] b = td.p[1];
            double[] c = td.p[2];
            double[] d = td.p[3];

            int lft = (int) Math.floor(min( min( min(a[0],b[0]), c[0] ), d[0]));
            int rgt = (int) Math.ceil (max( max( max(a[0],b[0]), c[0] ), d[0]));
            int btm = (int) Math.floor(min( min( min(a[1],b[1]), c[1] ), d[1]));
            int top = (int) Math.ceil (max( max( max(a[1],b[1]), c[1] ), d[1]));

            lft = Math.max(lft, 0);
            rgt = Math.min(rgt, W);
            btm = Math.max(btm, 0);
            top = Math.min(top, H);

            for (int y=btm; y<=top; ++y)
                for (int x=lft; x<=rgt; ++x) {
                    double[] xy = new double[] { x, y };

                    double ab = isLeftOf(a, b, xy);
                    double bc = isLeftOf(b, c, xy);
                    double cd = isLeftOf(c, d, xy);
                    double da = isLeftOf(d, a, xy);

                    if (ab <= 0 && bc <= 0 && cd <= 0 && da <= 0) {
                        pixels.add(new int[] {x, y});
                    }
                }
        }

        foim = new FloatImage(W, H, FloatImage.imageToFloats(oim));
    }

    public double getMeanSqErr(BufferedImage gim)
    {
        final int W = gim.getWidth();
        final int H = gim.getHeight();
        FloatImage fgim = new FloatImage(W, H, FloatImage.imageToFloats(gim));

        double mse = 0;
        for (int[] p : pixels)
            mse += LinAlg.sq(foim.get(p[0], p[1]) - fgim.get(p[0], p[1])) / pixels.size();

        return mse;
    }

    static double min(double a, double b)
    {
        return Math.min(a, b);
    }

    static double max(double a, double b)
    {
        return Math.max(a, b);
    }

    /** >0 if c left of line a-b. =0 if collinear. <0 if c right of line a-b */
    static double isLeftOf(double[] a, double[] b, double[] c)
    {
        return ((c[1]-a[1]) * (b[0]-a[0])) - ((c[0]-a[0]) * (b[1]-a[1]));
    }

    public static void main(String[] args) throws IOException
    {
        final BufferedImage oim = ImageIO.read(new File(args[0]));
        MosaicMatch mm = new MosaicMatch(oim);

        for (int[] p : mm.pixels)
            oim.setRGB(p[0], p[1], Color.white.getRGB());

        JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        JImage jim = new JImage(oim);
        jf.add(jim, BorderLayout.CENTER);

        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(800, 800);
        jf.setVisible(true);
    }
}
