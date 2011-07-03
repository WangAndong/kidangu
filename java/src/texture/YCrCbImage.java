package texture;

import java.awt.image.*;

import april.jmat.*;

import magic.camera.util.color.*;


public class YCrCbImage
{
    float[][] YCbCr;
    final int width;
    final int height;

    public YCrCbImage(BufferedImage src)
    {
        final int W = src.getWidth();
        final int H = src.getHeight();

        YCbCr = new float[W*H][3];
        this.width = W;
        this.height = H;

        final int in[] = ((DataBufferInt) (src.getRaster().getDataBuffer())).getData();

        for (int y = 0; y < H; ++y) {
            for (int x = 0; x < W; ++x) {
                YCbCr[y*W+x] = ColorSpace.RGBtoYCbCr(in[y*W + x]);
            }
        }
    }

    public double[] getAverageColor(int x0, int y0, int x1, int y1)
    {
        double[] m = new double[3];
        int N = (x1-x0)*(y1-y0);

        for(int y=y0; y<=y1; ++y)
            for(int x=x0; x<x1; ++x) {
                final double[] v = LinAlg.copyDoubles(YCbCr[y*width+x]);
                LinAlg.plusEquals(m, LinAlg.scale(v, 1.0/N));
            }

        return m;
    }
}
