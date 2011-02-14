package hog.ped;

import hog.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

public class DataSet
{
    ArrayList<float[]> features = new ArrayList<float[]>();

    public DataSet(File folder, FilenameFilter filter) throws IOException
    {
        this(folder, false, filter);
    }

    public DataSet(File folder, boolean fProgress, FilenameFilter filter) throws IOException
    {
        File[] files = folder.listFiles(filter);

        for (int i=0; i<files.length; ++i) {
            BufferedImage im = ImageIO.read(files[i]);

            /* Get the centered 64x128 pixels */
            int W = im.getWidth();
            int H = im.getHeight();
            im = copySubImage(im, W/2-32, H/2-64, 64, 128);

            features.addAll(HOGBlocks.getDescriptors(im, PedestrianDetector.descriptorInfo));

            if (fProgress) {
                printProgress(i, files.length);
                System.out.print('\r');
            }
        }
    }

    /** Prefer this over BufferedImage.getSubImage because the underlying pixel
     *  data is shared when using BufferedImage.getSubImage */
    static BufferedImage copySubImage(BufferedImage im, int x, int y, int w, int h)
    {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        BufferedImage cropped = im.getSubimage(x, y, w, h);

        Graphics g = out.getGraphics();
        g.drawImage(cropped, 0, 0, null);
        g.dispose();

        return out;
    }

    static void printProgress(int completed, int max)
    {
        int blocks = (int) (((float)completed/max)*40);

        System.out.print('[');

        for (int i=0; i<blocks; ++i)
            System.out.print('=');

        for (int i=blocks+1; i<40; ++i)
            System.out.print('.');

        System.out.printf(" %2d%% ]", (int)((float)completed/max*100));
    }

    public static void main(String[] args) throws IOException
    {
        new DataSet(new File("/home/rpradeep/studio/INRIAPerson/Train/pos"), true, new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".png");
            }
        });
    }
}
