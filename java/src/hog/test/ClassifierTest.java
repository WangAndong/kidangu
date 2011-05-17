package hog.test;

import hog.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import april.jmat.*;
import april.jmat.geom.*;
import april.util.*;
import april.vis.*;
import april.vis.VisText.*;


class ClassifierTest
{
    public static void main(String[] args)
    {
        //testStrongClassifier();
        testLinearSVM();
    }

    private static void testLinearSVM()
    {
        final JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        final VisCanvas vc = new VisCanvas(new VisWorld());
        vc.getViewManager().interfaceMode = 1.9;
        jf.add(vc, BorderLayout.CENTER);

        ParameterGUI pg = new ParameterGUI();
        pg.addDoubleSlider("w", "Weight", 0, 1, 0.1);
        jf.add(pg, BorderLayout.SOUTH);

        pg.addListener(new ParameterListener() {
            public void parameterChanged(ParameterGUI pg, String name)
            {
                GaussianMixtureDataSet ds = new GaussianMixtureDataSet();
                double[] Wts = new double[ds.numInstances()];
                Arrays.fill(Wts, 1);
                //double[] Wts = //LinAlg.normalizeL1(new double[] {1, 1, 1, pg.gd("w")});

                LinearSVM svm = LinearSVM.train(ds, Wts, 0);
                jf.setTitle("Error: " + svm.getTrainError());

                VisWorld.Buffer vb = vc.getWorld().getBuffer("classifier");

                // Show points with original labels
                VisData vd1 = new VisData(new VisDataPointStyle(new ClassColorizer(), 20));

                for (int i=0; i<ds.numInstances(); ++i) {
                    float[] d = ds.getInstance(i).get(0);
                    vd1.add(new double[] {d[0], d[1], 0, ds.getLabel(i)});
                }
                vb.addBuffered(vd1);

                // Show the separating line
                vb.addBuffered(getSeparatingLine(svm));

                // Show points with classification labels
                VisData vd2 = new VisData(new VisDataPointStyle(new ClassColorizer2(), 14));

                for (int i=0; i<ds.numInstances(); ++i) {
                    float[] d = ds.getInstance(i).get(0);
                    double l = svm.predict(ds.getInstance(i));
                    System.out.println(l);
                    vd2.add(new double[] {d[0], d[1], 0.1, l});

                    vb.addBuffered(new VisText(
                            new double[] {d[0], d[1], 5}, ANCHOR.TOP_LEFT, "   "+String.format("%.2f", l)));
                }
                vb.addBuffered(vd2);

                vb.switchBuffer();
            }
        });

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }

    private static void testStrongClassifier()
    {
        final JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        final VisCanvas vc = new VisCanvas(new VisWorld());
        vc.getViewManager().interfaceMode = 1.9;
        jf.add(vc, BorderLayout.CENTER);

        ParameterGUI pg = new ParameterGUI();
        pg.addDoubleSlider("fpr", "False Positive Rate", 0, 1, 0.1);
        jf.add(pg, BorderLayout.SOUTH);

        final GaussianMixtureDataSet ds = new GaussianMixtureDataSet();

        pg.addListener(new ParameterListener() {
            public void parameterChanged(ParameterGUI pg, String name)
            {
                VisWorld.Buffer vb = vc.getWorld().getBuffer("classifier");

                // Show points with original labels
                VisData vd1 = new VisData(new VisDataPointStyle(new ClassColorizer(), 20));

                for (int i=0; i<ds.numInstances(); ++i) {
                    float[] d = ds.getInstance(i).get(0);
                    vd1.add(new double[] {d[0], d[1], 0, ds.getLabel(i)});
                }
                vb.addBuffered(vd1);
                vb.switchBuffer();

                RejectionCascade rc = new RejectionCascade(ds, pg.gd("fpr"));

                // Show points with classification labels
                VisData vd2 = new VisData(new VisDataPointStyle(new ClassColorizer(), 12));

                for (int i=0; i<ds.numInstances(); ++i) {
                    float[] d = ds.getInstance(i).get(0);
                    int l = rc.predict(ds.getInstance(i));
                    vd2.add(new double[] {d[0], d[1], 0.1, l});
                }
                vb.addBuffered(vd1);
                vb.addBuffered(vd2);

                vb.switchBuffer();
            }
        });

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }

    private static VisData getSeparatingLine(LinearSVM svm)
    {
        double[] w = svm.getFeatureWeights();
        GLine2D line = new GLine2D(-w[0]/w[1], -w[2]/w[1]);
        VisData vdLine = new VisData(new VisDataLineStyle(Color.gray, 2));

        vdLine.add(line.getPointOfCoordinate(-100));
        vdLine.add(line.getPointOfCoordinate(100));

        return vdLine;
    }
}

class ClassColorizer implements Colorizer
{
    @Override
    public int colorize(double[] p)
    {
        return p[p.length-1] >= 0 ? 0xff0088FF: 0xffFF8800;
    }
}

class ClassColorizer2 implements Colorizer
{
    @Override
    public int colorize(double[] p)
    {
        return p[p.length-1] >= 0 ? 0xff004488: 0xff884400;
    }
}

class XORDataSet implements DataSet
{
    private ArrayList<float[]> data = new ArrayList<float[]>();
    private ArrayList<Integer> labels = new ArrayList<Integer>();

    public XORDataSet()
    {
        data.add(new float[] { 2 , 2});
        data.add(new float[] {-2, -2});
        data.add(new float[] { 2, -2});
        data.add(new float[] {-2,  2});

        labels.add(1);
        labels.add(1);
        labels.add(-1);
        labels.add(-1);
    }

    @Override
    public ArrayList<float[]> getInstance(int idx)
    {
        return new ArrayList<float[]>(Arrays.asList(data.get(idx)));
    }

    @Override
    public ArrayList<float[]> getFeatureOfInstances(int idx)
    {
        return data;
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
    public DataSet select(ArrayList<Integer> indices)
    {
        throw new RuntimeException("Not implemented");
    }
}

class GaussianMixtureDataSet implements DataSet
{
    private ArrayList<float[]> data = new ArrayList<float[]>();
    private ArrayList<Integer> labels = new ArrayList<Integer>();

    public GaussianMixtureDataSet()
    {
        Random g1 = new Random();
        Random g2 = new Random();
        Random b = new Random();

        for (int i=0 ;i<150; ++i) {
            if (b.nextBoolean()) {
                double[] p = new double[] {g1.nextGaussian(), g1.nextGaussian()};
                data.add(LinAlg.copyFloats(p));
                labels.add(1);
            } else {
                double[] p = new double[] {2+g2.nextGaussian(), 2+g2.nextGaussian()};
                data.add(LinAlg.copyFloats(p));
                labels.add(-1);
            }
        }
    }

    private GaussianMixtureDataSet(ArrayList<float[]> data, ArrayList<Integer> labels)
    {
        this.data = data;
        this.labels = labels;
    }

    @Override
    public ArrayList<float[]> getInstance(int idx)
    {
        return new ArrayList<float[]>(Arrays.asList(data.get(idx)));
    }

    @Override
    public ArrayList<float[]> getFeatureOfInstances(int idx)
    {
        return data;
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
    public DataSet select(ArrayList<Integer> indices)
    {
        ArrayList<float[]> sdata = new ArrayList<float[]>();
        ArrayList<Integer> slabel = new ArrayList<Integer>();

        for (int i : indices) {
            sdata.add(data.get(i));
            slabel.add(labels.get(i));
        }

        return new GaussianMixtureDataSet(sdata, slabel);
    }
}
