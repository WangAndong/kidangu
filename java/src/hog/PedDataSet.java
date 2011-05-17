package hog;

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

    private PedDataSet(ArrayList<ArrayList<float[]>> data, ArrayList<Integer> labels)
    {
        this.data = data;
        this.labels = labels;
    }

    private void processFolder(File dir, boolean fVerbose, FilenameFilter filt, int lbl) throws IOException
    {
        File[] files = dir.listFiles(filt);

        for (int i=0; i<files.length; ++i) {
            BufferedImage im = ImageIO.read(files[i]);

            /* Get the centered 64x128 pixels */
            // TODO: Do this in the HOG stage to avoid boundary effects
            int W = im.getWidth();
            int H = im.getHeight();
            im = Util.copySubImage(im, W/2-32, H/2-64, 64, 128);

            data.add(HOGBlocks.getDescriptors(im, PedDetector.descriptorInfo));
            labels.add(lbl);

            if (fVerbose) {
                Util.printProgress(i+1, files.length);
                System.out.print('\r');
            }
        }
        System.out.println();
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

    @Override
    public PedDataSet select(ArrayList<Integer> indices)
    {
        ArrayList<ArrayList<float[]>> sdata = new ArrayList<ArrayList<float[]>>();
        ArrayList<Integer> slabels = new ArrayList<Integer>();

        for (int i : indices) {
            sdata.add(data.get(i));
            slabels.add(labels.get(i));
        }

        return new PedDataSet(sdata, slabels);
    }

    public static void main(String[] args) throws IOException
    {
        new PedDataSet(
                new File("/home/rpradeep/studio/inria-person/train_64x128_H96/pos"),
                new File("/home/rpradeep/studio/inria-person/train_64x128_H96/neg"),
                true, new FilenameFilter() {
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".png");
                    }
        });
    }
}
