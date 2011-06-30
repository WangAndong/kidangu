package distort;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

import april.image.*;
import april.jmat.*;
import april.util.*;


public class HoughTransform
{
    FloatImage xform;

    public HoughTransform(BufferedImage im)
    {
        float[] pixels = FloatImage.imageToFloats(im);
        FloatImage fim = new FloatImage(im.getWidth(), im.getHeight(), pixels);

        transform(fim);
    }

    public HoughTransform(FloatImage fim)
    {
        transform(fim);
    }

    void transform(FloatImage fim)
    {
        /* Filter with a gaussian of width sigma */
        final double sigma = 1;
        int N = round(6*sigma);
        N = N | 1;
        float[] g = SigProc.makeGaussianFilter(sigma, N);
        FloatImage sim = fim.filterFactoredCentered(g, g);

        /* Convolve with the gradient kernel */
        final float[] k = new float[] {-1,0,1};
        FloatImage xgim = sim.filterHorizontalCentered(k);
        FloatImage ygim = sim.filterVerticalCentered(k);

        final int W = fim.width;
        final int H = fim.height;
        int maxR = round(LinAlg.magnitude(new int[] {W, H}));

        xform = new FloatImage(maxR, round(Math.PI*101));

        for (int j = 0; j < H; ++j)
            for (int i = 0; i < W; ++i) {
                float[] xy = new float[] {xgim.d[j*W+i], ygim.d[j*W+i]};

                double theta = Math.abs(MathUtil.atan2(xy[0], xy[1]));
                double mag = LinAlg.magnitude(xy);

                /* Update accumulator */
//                final double pi8 = Math.PI/8;
//                for (double t=theta-pi8; t<=theta+pi8; t+=1) {
//                    final double clamped = clampT(t)*100;
//                    double r = i*Math.cos(clamped) + j*Math.sin(clamped);
//                    float v = xform.get(round(r), round(clamped));
//                    xform.set(round(r), round(clamped), v + (float)mag);
//                }
                for (double t=0; t<=Math.PI; t+=0.01) {
                    double r = i*Math.cos(t) + j*Math.sin(t);
                    float v = xform.get(round(r), round(t*100));
                    xform.set(round(r), round(t*100), v + (float)mag);
                }
            }
    }

    static int round(double v)
    {
        return (int) Math.round(v);
    }

    static double clampT(double v)
    {
        v = (v > Math.PI) ? v - Math.PI : v;
        v = (v < 0) ? Math.PI + v : v;
        return v;
    }

    public static void main(String[] args) throws IOException
    {
        JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        final BufferedImage im = ImageIO.read(new File(args[0]));
        HoughTransform ht = new HoughTransform(im);

        JImage jim1 = new JImage(im);
        JImage jim2 = new JImage(ht.xform.normalize().makeImage());

        jf.add(jim1, BorderLayout.WEST);
        jf.add(jim2, BorderLayout.CENTER);

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }
}
