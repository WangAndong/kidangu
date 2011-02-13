package hog;

import java.awt.image.*;

import april.image.*;

public class HOGBlocks
{
    final IntegralHOG ihog;

    public static class Descriptor
    {
        final float[] v;
        final int blockWidth;
        final int blockHeight;

        Descriptor(float[] v, int blockWidth, int blockHeight)
        {
            this.v = v;
            this.blockWidth = blockWidth;
            this.blockHeight = blockHeight;
        }
    }

    public HOGBlocks(BufferedImage im)
    {
        float[] pixels = FloatImage.imageToFloats(im);
        ihog = new IntegralHOG(new FloatImage(im.getWidth(), im.getHeight(), pixels));
    }

    public Descriptor getDescriptor(int x, int y, int blockWidth, int blockHeight)
    {
        int bw2 = blockWidth / 2;
        int bh2 = blockHeight / 2;

        // TODO: Verify the offsets
        float[] hog1 = ihog.hog(x    , y,     x+bw2-1  , y+bh2-1);
        float[] hog2 = ihog.hog(x+bw2, y,     x+2*bw2-1, y+bh2-1);
        float[] hog3 = ihog.hog(x    , y+bh2, x+bw2-1  , y+2*bh2-1);
        float[] hog4 = ihog.hog(x+bw2, y+bh2, x+2*bw2-1, y+2*bh2-1);

        final int N = hog1.length;
        float[] v = new float[4*N];
        for (int i=0; i<hog1.length; ++i) {
            v[i]     = hog1[i];
            v[N+i]   = hog2[i];
            v[2*N+i] = hog3[i];
            v[3*N+i] = hog4[i];
        }

        return new Descriptor(v, blockWidth, blockHeight);
    }

    public static void main(String args[])
    {
        int i = 0;

        for (int w : new int[] {})

        System.out.println(i);
    }
}
