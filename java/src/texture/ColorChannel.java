package texture;

import java.awt.image.*;

import april.image.*;


public class ColorChannel
{
    final FloatImage fim;
    final int width;
    final int height;

    public ColorChannel(BufferedImage im)
    {
        fim = new FloatImage(im.getWidth(), im.getHeight(), FloatImage.imageToFloats(im));
        this.width = fim.width;
        this.height = fim.height;
    }

    public ColorChannel(FloatImage fim)
    {
        this.fim = fim;
        this.width = fim.width;
        this.height = fim.height;
    }

    /**
     * mean and variance of pixel values from x0,y0 (inclusive) to x1,y1 (inclusive)
     */
    public double[] getMoments(int x0, int y0, int x1, int y1)
    {
        double sumX = 0;
        double sumX2 = 0;

        for (int y=y0; y<y1; ++y)
            for (int x=x0; x<x1; ++x) {
                float p = fim.get(x, y);
                sumX += p; sumX2 += p*p;
            }

        int count = (y1-y0+1)*(x1-x0+1);
        double Ex = sumX/count;
        double Ex2 = sumX2/count;
        return new double[] {Ex, Ex2 - Ex*Ex};
    }
}
