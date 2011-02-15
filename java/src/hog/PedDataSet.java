package hog;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;


public class PedDataSet implements DataSet
{
    ArrayList<ArrayList<float[]>> data = new ArrayList<ArrayList<float[]>>();
    ArrayList<Integer> labels = new ArrayList<Integer>();

    public PedDataSet(File pos, File neg, FilenameFilter filt) throws IOException
    {
        this(pos, neg, false, filt);
    }

    public PedDataSet(File pos, File neg, boolean fVerbose, FilenameFilter filt) throws IOException
    {
        if (fVerbose) {
            System.out.println("DataSet: Loading positive examples");
        }
        processFolder(pos, fVerbose, filt, 1);

        if (fVerbose) {
            System.out.println("DataSet: Loading negative examples");
        }
        processFolder(neg, fVerbose, filt, -1);
    }

    private void processFolder(File dir, boolean fVerbose, FilenameFilter filt, int lbl) throws IOException
    {
        File[] files = dir.listFiles(filt);

        for (int i=0; i<files.length; ++i) {
            BufferedImage im = ImageIO.read(files[i]);

            /* Get the centered 64x128 pixels */
            int W = im.getWidth();
            int H = im.getHeight();
            im = copySubImage(im, W/2-32, H/2-64, 64, 128);

            data.add(HOGBlocks.getDescriptors(im, PedDetector.descriptorInfo));
            labels.add(lbl);

            if (fVerbose) {
                printProgress(i, files.length);
                System.out.print('\r');
            }
        }
    }

    @Override
    public int numInstances()
    {
        return data.size();
    }

    @Override
    public int numFeatures()
    {
        return 1;
    }

    @Override
    public ArrayList<float[]> getInstance(int idx)
    {
        return data.get(idx);
    }

    @Override
    public ArrayList<float[]> getFeatureOfInstances(int idx)
    {
        ArrayList<float[]> features = new ArrayList<float[]>();

        for (ArrayList<float[]> d : data)
            features.add(d.get(idx));

        return features;
    }

    @Override
    public int getLabel(int idx)
    {
        return labels.get(idx);
    }

    @Override
    public ArrayList<Integer> getLabels()
    {
        return labels;
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
        new PedDataSet(new File("/home/rpradeep/studio/INRIAPerson/Train/pos"),
                new File("/home/rpradeep/studio/INRIAPerson/Train/neg"),
                true, new FilenameFilter() {
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".png");
                    }
        });
    }
}
