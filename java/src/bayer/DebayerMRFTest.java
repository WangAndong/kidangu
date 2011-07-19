package bayer;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import april.image.*;
import april.jmat.*;
import april.util.*;


public class DebayerMRFTest
{
    static float dataCost(float oldV, float newV)
    {
        return (float) -(0.1*Math.min(LinAlg.sq(oldV-newV), 10000.0));
    }

    static float neighborCost(float thisV, float thatV)
    {
        return (float) -Math.min(LinAlg.sq(thisV-thatV), 20.0);
    }

    static float[] msg(float[] msg1, float[] msg2, float[] msg3, float[] msg4)
    {
        final int K = msg1.length;

        float[] maxmargin = new float[K];

        for (int i=0; i<K; ++i) {
            float[] p = new float[K];
            for (int j=0; j<K; ++j)
                p[j] = neighborCost(i, j) + msg1[j] + msg2[j] + msg3[j] + msg4[j];

            maxmargin[i] = LinAlg.max(p);
        }

        double avg = 0;
        for (int i=0; i<K; ++i)
            avg += maxmargin[i] / K;

        for (int i=0; i<K; ++i)
            maxmargin[i] -= avg;

        return maxmargin;
    }

    public static void main(String[] args) throws IOException
    {
        final String title = "Image restoration";
        final JFrame jf = new JFrame(title);
        final BufferedImage im = ImageIO.read(new File(args[0]));

        final int W = im.getWidth();
        final int H = im.getHeight();
        final FloatImage fim = bayerImage(new FloatImage(im, 8)); /* bayered green channel */
        final FloatImage original = fim.copy();

        final MessageGrid data = new MessageGrid(W, H, 256);
        final MessageGrid right = new MessageGrid(W, H, 256);
        final MessageGrid left = new MessageGrid(W, H, 256);
        final MessageGrid up = new MessageGrid(W, H, 256);
        final MessageGrid down = new MessageGrid(W, H, 256);

        /* compute data costs */
        for (int j=0; j<H; ++j)
            for (int i=0; i<W; ++i) {
                boolean missing = (j%2==0 && i%2==1) || (j%2==1 && i%2==0);

                float[] v = data.getMsg(i, j);
                if (missing) {
                    Arrays.fill(v, 1);
                }
                else {
                    for (int k=0; k<v.length; ++k)
                        v[k] = dataCost(fim.get(i, j)*255, k);
                }
            }

        /* View initial data costs just for visualization kicks */
        FloatImage kim = new FloatImage(W, H);
        if (true) {
            for (int j = 0; j < H; ++j)
                for (int i = 0; i < W; ++i)
                    kim.set(i, j, LinAlg.maxIdx(data.getMsg(i, j)));
        }

        final JImage jim = new JImage(kim.normalize().makeImage());
        jf.add(jim, BorderLayout.CENTER);

        ParameterGUI pg = new ParameterGUI();
        pg.addButtons("iter", "Iterate");
        jf.add(pg, BorderLayout.SOUTH);

        final ParameterListener listener = new ParameterListener() {
            public void parameterChanged(ParameterGUI pg, String name)
            {
                /* One message passing iteration */
                Tic tic = new Tic();
                for (int j = 1; j < H-1; ++j) {
                    System.out.print(".");
                    for (int i = 1; i < W-1; ++i) {
                        up.map[i][j] = msg(data.getMsg(i, j),
                                right.getMsg(i-1, j), left.getMsg(i+1, j), up.getMsg(i, j+1));

                        down.map[i][j] = msg(data.getMsg(i, j),
                                right.getMsg(i-1, j), left.getMsg(i+1, j), down.getMsg(i, j-1));

                        right.map[i][j] = msg(data.getMsg(i, j),
                                right.getMsg(i-1, j), up.getMsg(i, j+1), down.getMsg(i, j-1));

                        left.map[i][j] = msg(data.getMsg(i, j),
                                left.getMsg(i+1, j), up.getMsg(i, j+1), down.getMsg(i, j-1));

                    }
                }

                /* Read out the max marginal beliefs */
                FloatImage imout = new FloatImage(W, H);

                for (int j = 1; j < H-1; ++j)
                    for (int i = 1; i < W-1; ++i) {
                        float[] v = new float[256];
                        plusEquals(v, up.getMsg(i, j+1));
                        plusEquals(v, down.getMsg(i, j-1));
                        plusEquals(v, left.getMsg(i+1, j));
                        plusEquals(v, right.getMsg(i-1, j));
                        plusEquals(v, data.getMsg(i, j));

                        imout.set(i, j, LinAlg.maxIdx(v));
                    }

                imout = imout.normalize();

                double mse = 0;
                for (int i=0; i<imout.d.length; ++i) {
                    mse += LinAlg.sq(imout.d[i] - original.d[i]) / imout.d.length;
                }

                System.out.printf("%.16f\n", mse);
                jf.setTitle(title + " " + tic.toc());
                jim.setImage(imout.makeImage());
                jim.repaint();
            }
        };
        pg.addListener(listener);

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);

        for (;;) {
            listener.parameterChanged(pg, "iter");
        }
    }

    static FloatImage bayerImage(FloatImage fim)
    {
        int W = fim.width;
        int H = fim.height;

        for (int y=0; y<H; y+=2)
            for (int x=1; x<W; x+=2)
                fim.set(x, y, 0);

        for (int y=1; y<H; y+=2)
            for (int x=0; x<W; x+=2)
                fim.set(x, y, 0);

        return fim;
    }

    static void copyToFrom(float[] dest, float[] src)
    {
        for (int i=0; i<dest.length; ++i)
            dest[i] = src[i];
    }

    static void plusEquals(float[] a, float[] b)
    {
        for (int i=0; i<a.length; ++i)
            a[i] += b[i];
    }

    static double chi2(float[] a, float[] b)
    {
        double c = 0;
        for (int i=0; i<a.length; ++i)
            c += LinAlg.sq((a[i] - b[i])) / a.length;

        return c;
    }
}

class MessageGrid
{
    public final int width;
    public final int height;
    final public float[][][] map;

    public MessageGrid(int width, int height, int labels)
    {
        this.width = width;
        this.height = height;
        map = new float[width][height][labels];
    }

    public MessageGrid(int width, int height, int labels, float initialValue)
    {
        this.width = width;
        this.height = height;

        map = new float[width][height][labels];
        for (int i=0; i<width; ++i)
            for (int j=0; j<height; ++j)
                Arrays.fill(map[i][j], initialValue);
    }

    public float[] getMsg(int i, int j)
    {
        return map[i][j];
    }
}
