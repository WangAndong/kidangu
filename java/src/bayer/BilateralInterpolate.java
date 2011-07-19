package bayer;

import april.image.*;
import april.jmat.*;


public class BilateralInterpolate
{
    static final float sigmaS = 1f;
    static final float sigmaI = 0.1f;

    static FloatImage interpolateRed(FloatImage fim, FloatImage iim)
    {
        final int W = fim.width;
        final int H = fim.height;

        FloatImage rim = new FloatImage(W, H);

        for (int y=0; y<H; ++y)
            for (int x=0; x<W; ++x) {
                if (isRedPosition(x, y)) {
                    rim.set(x, y, fim.get(x, y));

                } else {
                    float sumW = 0;
                    float sum = 0;

                    for (int j=atleast(y-2, 0); j<atmost(y+2, H-1); ++j)
                        for (int i=atleast(x-2, 0); i<atmost(x+2, W-1); ++i) {
                            if (isRedPosition(i, j)) {
                                float w = weight(0, sigmaS, dist(x, y, i, j));
                                w *= weight(iim.get(x, y), sigmaI, iim.get(i, j));

                                sum += w*fim.get(i, j);
                                sumW += w;
                            }
                        }

                    rim.set(x, y, sum/sumW);
                }
            }

        return rim;
    }

    static FloatImage interpolateBlue(FloatImage fim, FloatImage iim)
    {
        final int W = fim.width;
        final int H = fim.height;

        FloatImage bim = new FloatImage(W, H);

        for (int y=0; y<H; ++y)
            for (int x=0; x<W; ++x) {
                if (isBluePosition(x, y)) {
                    bim.set(x, y, fim.get(x, y));

                } else {
                    float sumW = 0;
                    float sum = 0;

                    for (int j=atleast(y-2, 0); j<atmost(y+2, H-1); ++j)
                        for (int i=atleast(x-2, 0); i<atmost(x+2, W-1); ++i) {
                            if (isBluePosition(i, j)) {
                                float w = weight(0, sigmaS, dist(x, y, i, j));
                                w *= weight(iim.get(x, y), sigmaI, iim.get(i, j));

                                sum += w*fim.get(i, j);
                                sumW += w;
                            }
                        }

                    bim.set(x, y, sum/sumW);
                }
            }

        return bim;
    }

    static FloatImage interpolateGreen(FloatImage fim, FloatImage iim)
    {
        final int W = fim.width;
        final int H = fim.height;

        FloatImage gim = new FloatImage(W, H);

        for (int y=0; y<H; ++y)
            for (int x=0; x<W; ++x) {
                if (isGreenPosition(x, y)) {
                    gim.set(x, y, fim.get(x, y));

                } else {
                    float sumW = 0;
                    float sum = 0;

                    for (int j=atleast(y-2, 0); j<atmost(y+2, H-1); ++j)
                        for (int i=atleast(x-2, 0); i<atmost(x+2, W-1); ++i) {
                            if (isGreenPosition(i, j)) {
                                float w = weight(0, sigmaS, dist(x, y, i, j));
                                w *= weight(iim.get(x, y), sigmaI, iim.get(i, j));

                                sum += w*fim.get(i, j);
                                sumW += w;
                            }
                        }

                    gim.set(x, y, sum/sumW);
                }
            }

        return gim;
    }

    static boolean isRedPosition(int x, int y)
    {
        return y%2==0 && x%2==1;
    }

    static boolean isGreenPosition(int x, int y)
    {
        return (y%2 == x%2);
    }

    static boolean isBluePosition(int x, int y)
    {
        return y%2==1 && x%2==0;
    }

    static int atleast(int v, int min)
    {
        return v >= min ? v : 0;
    }

    static int atmost(int v, int max)
    {
        return v <= max ? v : max;
    }

    static float weight(float mu, float sigma, float v)
    {
        return (float) Math.exp(-0.5*LinAlg.sq((v-mu)/sigma));
    }

    static float dist(int x, int y, int i, int j)
    {
        return (float) Math.sqrt((x-i)*(x-i) + (y-j)*(y-j));
    }
}
