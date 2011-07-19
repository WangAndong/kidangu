package bayer;

import java.util.*;

import april.image.*;


public class MedianInterpolate
{
    static FloatImage interpolateGreen(FloatImage fim)
    {
        final int W = fim.width;
        final int H = fim.height;

        FloatImage gim = new FloatImage(W, H);

        for (int y=0; y<H; ++y)
            for (int x=0; x<W; ++x) {
                if (isGreenPosition(x, y)) {
                    gim.set(x, y, fim.get(x, y));

                } else {
                    ArrayList<Float> vals = new ArrayList<Float>();
                    for (int j=atleast(y-10, 0); j<atmost(y+10, H-1); ++j)
                        for (int i=atleast(x-10, 0); i<atmost(x+10, W-1); ++i) {
                            if (isGreenPosition(i, j))
                                vals.add(fim.get(i, j));
                        }

                    float median = 0;
                    if (vals.size()%2==0) {
                        median = 0.5f*vals.get(vals.size()/2) + 0.5f*vals.get(vals.size()/2-1);
                    } else {
                        median = vals.get(vals.size()/2);
                    }
                    gim.set(x, y, median);
                }
            }

        return gim;
    }

    static boolean isGreenPosition(int x, int y)
    {
        return (y%2 == x%2);
    }

    static int atleast(int v, int min)
    {
        return v >= min ? v : 0;
    }

    static int atmost(int v, int max)
    {
        return v <= max ? v : max;
    }
}
