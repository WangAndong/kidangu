package bayer;

import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

import april.image.*;


public class Debayer2
{
    public static void main(String[] args) throws IOException
    {
        final BufferedImage im = ImageIO.read(new File(args[0]));
        FloatImage byim = BayerUtil.bayerImage(im);
        FloatImage mask = BayerUtil.getChannelMask(byim.width, byim.height);
    }

    static void interpolate(FloatImage rim, FloatImage gim, FloatImage bim)
    {

    }

    static double similarity(int i, int j, int x, int y, FloatImage obs)
    {
        if (isObserved(obs.get(x, y))) {
            return bilateralWeight(i, j, x, y, obs);

        } else /* hidden. need to infer */ {
            int v0 = Math.max(0, y-1);
            int v1 = Math.min(obs.height, y+1);
            int h0 = Math.max(0, x-1);
            int h1 = Math.min(obs.width, x+1);

            double similarity = 0;
            double sumWt = 0;

            for (int v=v0; v<=v1; ++v)
                for (int h=h0; h<=h1; ++h)
                    if (isObserved(obs.get(h, v))) {
                        double sim = bilateralWeight(i, j, h, v, obs);
                        double wt = spaceGaussian((h-i)*(h-i)+(v-j)*(v-j));
                        similarity += wt*sim;
                        sumWt += wt;
                    }

            similarity /= sumWt;
            return similarity;
        }
    }

    static boolean isObserved(float v)
    {
        return !Float.isNaN(v);
    }

    static double bilateralWeight(int i, int j, int x, int y, FloatImage im)
    {
        return spaceGaussian((x-i)*(x-i) + (y-j)*(y-j)) *
            rangeGaussian(im.get(i, j) - im.get(i, j));
    }

    static double rangeGaussian(double z)
    {
        final double sigma = 0.2f;
        return Math.exp(-0.5*(z*z)/(sigma*sigma));
    }

    static double spaceGaussian(double zsqr)
    {
        final double sigma = 1f;
        return Math.exp(-0.5*(zsqr)/(sigma*sigma));
    }

    static double projection(int i, int j, int x, int y, int v, int h)
    {
        int a = Math.abs(x-i), b = Math.abs(y-j);
        int c = Math.abs(v-i), d = Math.abs(h-j);

        return a*c + b*d;
    }
}
