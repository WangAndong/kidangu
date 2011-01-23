package hog;

import april.image.*;
import april.jmat.*;

public class HOGDescriptor
{


    static HOGDescriptor[][] computeFor(FloatImage im, int C, int B)
    {
        assert (C%2==1);

        FloatImage gim = gradientImage(im);

        //
        // Compute histograms for cells
        //
        HOGDescriptor[][] hog = new HOGDescriptor[gim.width/C][gim.height/C];
        double[] gauss1D = LinAlg.copyDoubles(SigProc.makeGaussianFilter(C/2, C));
        double[][] gauss2D = LinAlg.outerProduct(gauss1D, gauss1D);

        for (int j=0; j<gim.height; j+=C) {
            for (int i=0; i<gim.width; i+=C) {

                for (int y=0; y<C; ++y) {
                    for (int x=0; j<C; ++j) {
                        float theta = gim.get(i+x, j+y);
                        hog[i/C][j/C] = 2;
                    }
                }
            }
        }
    }

    private static FloatImage gradientImage(FloatImage im)
    {
        FloatImage gim = new FloatImage(im.width-2, im.height-2);
        final int W = im.width;
        final int H = im.height;

        for (int y=0; y<H; ++y) {
            for (int x=0; x<W; ++x) {

                float a00 = isValid(x-1, y-1, W, H) ? im.get(x-1, y-1) : 0;
                float a01 = isValid(x-1, y  , W, H) ? im.get(x-1, y  ) : 0;
                float a02 = isValid(x-1, y+1, W, H) ? im.get(x-1, y+1) : 0;
                float a10 = isValid(x  , y-1, W, H) ? im.get(x  , y-1) : 0;
                float a11 = isValid(x  , y  , W, H) ? im.get(x  , y  ) : 0;
                float a12 = isValid(x  , y  , W, H) ? im.get(x  , y+1) : 0;
                float a20 = isValid(x+1, y-1, W, H) ? im.get(x+1, y-1) : 0;
                float a21 = isValid(x+1, y  , W, H) ? im.get(x+1, y  ) : 0;
                float a22 = isValid(x+1, y+1, W, H) ? im.get(x+1, y+1) : 0;

                float Gx = 3*(a02 - a00) + 10*(a12 - a10) + 3*(a22 - a20);
                float Gy = 3*(a20 - a00) + 10*(a21 - a01) + 3*(a22 - a02);

                float theta = (float) Math.atan2(Gy, Gx);
                gim.set(x, y, theta);
            }
        }
        return gim;
    }

    private static boolean isValid(int x, int y, int W, int H)
    {
        return x >=0 && y>=0 && x<W && y<H;
    }
}
