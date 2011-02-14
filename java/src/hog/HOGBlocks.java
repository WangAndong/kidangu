package hog;

import hog.ped.*;

import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;


import april.image.*;
import april.jmat.*;
import april.util.*;

public class HOGBlocks
{
    final IntegralHOG ihog;

    public static class DescriptorInfo
    {
        final int x;
        final int y;
        final int width;
        final int height;

        public DescriptorInfo(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public HOGBlocks(BufferedImage im)
    {
        float[] pixels = FloatImage.imageToFloats(im);
        ihog = new IntegralHOG(new FloatImage(im.getWidth(), im.getHeight(), pixels));
    }

    public float[] getDescriptor(DescriptorInfo d)
    {
        return getDescriptor(d.x, d.y, d.width, d.height);
    }

    public float[] getDescriptor(int x, int y, int blockWidth, int blockHeight)
    {
        assert blockWidth%2 == 0;
        assert blockHeight%2 == 0;

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

        float norm = ihog.norm(x, y, x+blockWidth-1, blockHeight-1);
        return LinAlg.scale(v, 1.0/norm);
    }

    public static ArrayList<float[]> getDescriptors(BufferedImage im, ArrayList<DescriptorInfo> dinfo)
    {
        HOGBlocks hogBlocks = new HOGBlocks(im);
        ArrayList<float[]> descs = new ArrayList<float[]>();


        for (DescriptorInfo ifo : dinfo)
            descs.add(hogBlocks.getDescriptor(ifo));

        return descs;
    }

    public static void main(String args[]) throws IOException
    {
        BufferedImage im = ImageIO.read(new File("/home/rpradeep/Desktop/hog.png"));
        HOGBlocks hogBlocks = new HOGBlocks(im);

        while (true) {
            Tic tic = new Tic();
            hogBlocks.getDescriptors(im, PedestrianDetector.descriptorInfo);
            System.out.printf("time=%.3fs\n", tic.toc());
        }
    }
}