package bayer;

import java.awt.image.*;

import april.image.*;


public class BayerUtil
{
    static FloatImage maskRed(FloatImage rim)
    {
        int W = rim.width;
        int H = rim.height;

        for (int y=0; y<H; y+=2)
            for (int x=0; x<W; x+=2)
                rim.set(x, y, 0);

        for (int y=1; y<H; y+=2)
            for (int x=0; x<W; ++x)
                rim.set(x, y, 0);

        return rim;
    }

    static FloatImage maskGreen(FloatImage gim)
    {
        int W = gim.width;
        int H = gim.height;

        for (int y=0; y<H; y+=2)
            for (int x=1; x<W; x+=2)
                gim.set(x, y, 0);

        for (int y=1; y<H; y+=2)
            for (int x=0; x<W; x+=2)
                gim.set(x, y, 0);

        return gim;
    }

    static FloatImage maskBlue(FloatImage bim)
    {
        int W = bim.width;
        int H = bim.height;

        for (int y=0; y<H; y+=2)
            for (int x=0; x<W; ++x)
                bim.set(x, y, 0);

        for (int y=1; y<H; y+=2)
            for (int x=1; x<W; x+=2)
                bim.set(x, y, 0);

        return bim;
    }

    static FloatImage getChannelMask(int W, int H)
    {
        FloatImage mask = new FloatImage(W, H);

        for (int y=0; y<H; ++y)
            for (int x=0; x<W; ++x)
                if (y%2==0) {
                    if (x%2==0)
                        mask.set(x, y, 1);
                    else
                        mask.set(x, y, 0);
                } else {
                    if (x%2==0)
                        mask.set(x, y, 3);
                    else
                        mask.set(x, y, 1);
                }

        return mask;
    }

    static FloatImage bayerImage(BufferedImage im)
    {
        FloatImage rim = BayerUtil.maskRed(new FloatImage(im, 16));
        FloatImage gim = BayerUtil.maskGreen(new FloatImage(im, 8));
        FloatImage bim = BayerUtil.maskBlue(new FloatImage(im, 0));

        FloatImage bayered = rim;
        plusEquals(bayered.d, gim.d);
        plusEquals(bayered.d, bim.d);

        return bayered;
    }

    private static void plusEquals(float[] a, float[] b)
    {
        for (int i=0; i<a.length; ++i)
            a[i] += b[i];
    }
}
