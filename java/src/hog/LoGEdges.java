package hog;

import april.image.*;
import april.jmat.*;

public class LoGEdges
{
    static float[] makeKernel(double sigma)
    {
        int size = (int) (sigma*6 + 0.5);
        size = size%2==0? size+1: size; /*Kernels are odd-sized*/

        float[] kernel = new float[size*size];

        for (int y=0; y<size; ++y)
            for (int x=0; x<size; ++x)
                kernel[y*size+x] = (float) LoG(x-size/2.0, y-size/2.0, sigma);

        float sum = 0;
        for (float k : kernel)
            sum += k;

        return LinAlg.scale(kernel, 1.0/sum);
    }

    FloatImage detect(FloatImage im, float[] LoGKernel)
    {
        //
        //  Compute response with the LoG kernel first
        //
        FloatImage cim = new FloatImage(im.width, im.height);
        final int ksize = (int) Math.sqrt(LoGKernel.length);
        final int R = ksize/2;

        for (int y=0; y<im.height; ++y)
            for (int x=0; x<im.width; ++x) {
                float v = 0;

                for(int j=y-R; j<=y+R; ++j)
                    for (int i=x-R; i<=x+R; ++i)
                        if (j>=0 && j<im.height && i>=0 && i<im.width)
                            v += im.get(i, j) * LoGKernel[(j+R)*ksize+(i+R)];

                cim.set(x, y, v);
            }

        //
        //  Compute zero crossings
        //
        FloatImage zim = new FloatImage(im.width, im.height);

        for (int y=1; y<im.height-1; ++y)
            for (int x=1; x<im.width-1; ++x) {
                /* Offset 2x2 neighborhood */
                float xy1 = cim.get(x, y-1);
                float xy = cim.get(x, y);
                if (xy1*xy < 0) {
                    if (Math.abs(xy1) < Math.abs(xy))
                        zim.set(x, y-1, 1);
                    else
                        zim.set(x, y, 1);

                    break;
                }

                float x1y = cim.get(x, y-1);
                if (x1y*xy < 0) {
                    if (Math.abs(x1y) < Math.abs(xy))
                        zim.set(x-1, y, 1);
                    else
                        zim.set(x, y, 1);

                    break;
                }
            }


        return cim;
    }

    private static double LoG(double x, double y, double sigma)
    {
        double sigma2 = sigma*sigma;
        double z = (x*x + y*y) / (2*sigma2);
        return -(1-z)*Math.exp(-z) / (Math.PI*sigma2*sigma2);
    }

    public static void main(String[] args)
    {

    }
}
