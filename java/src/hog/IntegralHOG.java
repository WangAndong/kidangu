package hog;

import april.image.*;
import april.jmat.*;

import magic.camera.util.*;


/**
 * Efficiently compute the unsigned HOG for any rectangular region within an image
 * */
public class IntegralHOG
{
    final FloatImage xgim;
    final FloatImage ygim;

    static final int CBINS = 9;
    final IntegralImageF iim[] = new IntegralImageF[CBINS];

    public IntegralHOG(FloatImage fim)
    {
        /* Filter with a gaussian of width sigma */
        final double sigma = 1;
        int N = (int) (6*sigma + 0.5);
        N = N | 1;
        float[] g = SigProc.makeGaussianFilter(sigma, N);
        FloatImage sim = fim.filterFactoredCentered(g, g);

        /* Convolve with the gradient kernel */
        final float[] k = new float[] {-1,0,1};
        xgim = sim.filterHorizontalCentered(k);
        ygim = sim.filterVerticalCentered(k);

        /* 9 images. One for each orientation bin */
        float bin[][] = new float[CBINS][fim.d.length];

        /* Store magnitude information in orientation bins */
        for (int i=0; i<fim.d.length; ++i) {
            float[] xy = new float[] {xgim.d[i], ygim.d[i]};

            float theta = (float) Math.abs(MathUtil.atan2(xy[0], xy[1]));
            int idx = (int) (CBINS*theta / Math.PI);

            bin[idx][i] = LinAlg.magnitude(xy);
        }

        /* Create integral images for each bin */
        for(int i=0; i<CBINS; ++i)
            iim[i] = new IntegralImageF(fim.width, fim.height, bin[i]);
    }

    /** Sum of pixel values from x0,y0 (inclusive) to x1,y1 (inclusive) */
    float[] hog(int x0, int x1, int y0, int y1)
    {
        float[] h = new float[CBINS];

        /* Assemble the histogram from the integral image for each bin*/
        for (int i=0; i<9; ++i)
            h[i] = (float) iim[i].sum(x0, y0, x1, y1);

        return h;
    }
}
