package hog;


import hog.HOGBlocks.*;

import java.io.*;
import java.util.*;


public class PedDetector
{
    /** Information about location and size of descriptors in a window. Each descriptor
     * describes the feature in PedDataSet at the corresponding feature index */
    public static final ArrayList<HOGBlocks.DescriptorInfo> descriptorInfo = new ArrayList();

    static {
        /* Initialize descriptor information */
        for (int w=12; w<64; w+=4) {
            int step = 0;

            if (w >= 48)
                step = 8;
            else if (w >= 32)
                step = 6;
            else
                step = 4;

            /* 1:1 blocks */
            int h = w;
            for (int y=0; y+h<128; y+=step)
                for (int x=0; x+w<64; x+=step)
                    descriptorInfo.add(new DescriptorInfo(x, y, w, h));

            /* 1:2 blocks */
            h = 2*w;
            for (int y=0; y+h<128; y+=step)
                for (int x=0; x+w<64; x+=step)
                    descriptorInfo.add(new DescriptorInfo(x, y, w, h));

            /* 2:1 blocks */
            h = w/2;
            for (int y=0; y+h<128; y+=step)
                for (int x=0; x+w<64; x+=step)
                    descriptorInfo.add(new DescriptorInfo(x, y, w, h));
        }
    }

    public static void main(String[] args) throws IOException
    {
        PedDataSet ds = new PedDataSet(
                new File("/home/rpradeep/studio/INRIAPerson/Train/pos"),
                new File("/home/rpradeep/studio/INRIAPerson/Train/neg"),
                true, new FilenameFilter() {
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".png");
                    }
        });

        RejectionCascade rc = new RejectionCascade(ds, 0.1);
    }
}
