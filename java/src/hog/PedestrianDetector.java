package hog;


import hog.HOGBlocks.*;

import java.util.*;

public class PedestrianDetector
{
    /** What block do we compute descriptors from */
    public static final ArrayList<HOGBlocks.DescriptorInfo> descriptorInfo = new ArrayList<HOGBlocks.DescriptorInfo>();

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

}
