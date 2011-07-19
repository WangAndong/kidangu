package bayer;

import april.image.*;
import april.jmat.*;
import april.util.*;


public class MRFInterpolate
{
    static float dataCost(float oldV, float newV)
    {
        return (float) -(0.01*Math.min(LinAlg.sq(oldV-newV), 1500.0));
    }

    static float neighborCost(float thisV, float thatV)
    {
        return (float) -Math.min(LinAlg.sq(thisV-thatV), 3.5);
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

    static FloatImage interpolateGreen(FloatImage fim)
    {
        final int W = fim.width;
        final int H = fim.height;

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
                    float n = 0;

                    if (i>0) {
                        plusEquals(v, data.getMsg(i-1, j));
                        ++n;
                    }
                    if (i<W-1) {
                        plusEquals(v, data.getMsg(i+1, j));
                        ++n;
                    }
                    if (j>0) {
                        plusEquals(v, data.getMsg(i, j-1));
                        ++n;
                    }
                    if (j<H-1) {
                        plusEquals(v, data.getMsg(i, j+1));
                        ++n;
                    }

                    scale(v, 1/n);
                }
                else {
                    for (int k=0; k<v.length; ++k)
                        v[k] = dataCost(fim.get(i, j)*255, k);
                }
            }

        /* One message passing iteration */
        Tic tic = new Tic();
        for (int j = 1; j < H-1; ++j) {
            System.out.printf("%d\r", j);
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

        System.out.println();
        for (int j = 1; j < H-1; ++j) {
            System.out.printf("%d\r", j);
            for (int i = 1; i < W-1; ++i) {
                left.map[i][j] = msg(data.getMsg(i, j),
                        left.getMsg(i+1, j), up.getMsg(i, j+1), down.getMsg(i, j-1));

                right.map[i][j] = msg(data.getMsg(i, j),
                        right.getMsg(i-1, j), up.getMsg(i, j+1), down.getMsg(i, j-1));

                down.map[i][j] = msg(data.getMsg(i, j),
                        right.getMsg(i-1, j), left.getMsg(i+1, j), down.getMsg(i, j-1));

                up.map[i][j] = msg(data.getMsg(i, j),
                        right.getMsg(i-1, j), left.getMsg(i+1, j), up.getMsg(i, j+1));
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

        /* Normalize to fit the intensity spectrum of the input image */
        float min = LinAlg.min(fim.d);
        float max = LinAlg.max(fim.d);
        imout = imout.normalize();

        for (int i=0; i<imout.d.length; ++i)
            imout.d[i] = min + (max-min)*imout.d[i];

        return imout;
    }

    static void plusEquals(float[] a, float[] b)
    {
        for (int i=0; i<a.length; ++i)
            a[i] += b[i];
    }

    static void scale(float[] a, float v)
    {
        for (int i=0; i<a.length; ++i)
            a[i] /= v;
    }
}
