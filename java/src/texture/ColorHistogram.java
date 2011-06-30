package texture;

import java.awt.image.*;

import april.jmat.*;

import magic.camera.util.*;


public class ColorHistogram
{
    static final int NBINS = 8;
    IntegralImage[] iimr = new IntegralImage[NBINS];
    IntegralImage[] iimg = new IntegralImage[NBINS];
    IntegralImage[] iimb = new IntegralImage[NBINS];

    public final int width;
    public final int height;

    public ColorHistogram(BufferedImage im)
    {
        final int W = im.getWidth();
        final int H = im.getHeight();
        final int pixels[] = ((DataBufferInt) (im.getRaster().getDataBuffer())).getData();

        this.width = W;
        this.height = H;

        /* Color value planes. Each color channel has NBINS different planes */
        int[][] rPlane = new int[NBINS][W*H];
        int[][] gPlane = new int[NBINS][W*H];
        int[][] bPlane = new int[NBINS][W*H];

        /* Enter data into the planes */
        for (int y = 0; y < H; ++y)
            for (int x = 0; x < W; ++x) {
                final int n = y*W+x;
                final int r = (pixels[n] & 0xFF0000) >>> 16;
                final int g = (pixels[n] & 0xFF00) >>> 8;
                final int b = (pixels[n] & 0xFF);

                int BINW = 256/NBINS;
                rPlane[r/BINW][n]++;
                gPlane[g/BINW][n]++;
                bPlane[b/BINW][n]++;
            }

        /* Construct integral images for each plane */
        for (int i=0; i<NBINS; ++i) {
            iimr[i] = new IntegralImage(W, H, rPlane[i]);
            iimg[i] = new IntegralImage(W, H, gPlane[i]);
            iimb[i] = new IntegralImage(W, H, bPlane[i]);
        }
    }

    public double[][] getHistogram(int x0, int y0, int x1, int y1)
    {
        double[][] h = new double[3][NBINS];

        for (int i=0; i<NBINS; ++i) {
            h[0][i] = iimr[i].sum(x0, y0, x1, y1);
            h[1][i] = iimg[i].sum(x0, y0, x1, y1);
            h[2][i] = iimb[i].sum(x0, y0, x1, y1);
        }

        return new double[][] {
                LinAlg.normalizeL1(h[0]),
                LinAlg.normalizeL1(h[1]),
                LinAlg.normalizeL1(h[2])
                };
    }
}
