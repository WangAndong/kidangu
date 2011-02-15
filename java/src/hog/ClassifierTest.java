package hog;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import april.jmat.*;
import april.jmat.geom.*;
import april.util.*;
import april.vis.*;
import april.vis.VisText.*;


public class ClassifierTest
{
    public static void main(String[] args)
    {
        testStrongClassifier();
        //testLinearSVM();
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
                XORDataSet ds = new XORDataSet();
                double[] Wts = LinAlg.normalizeL1(new double[] {1, 1, 1, pg.gd("w")});

                LinearSVM svm = LinearSVM.train(ds, Wts, 0);
                jf.setTitle("Error: " + svm.err);

                VisWorld.Buffer vb = vc.getWorld().getBuffer("classifier");

                // Show points with original labels
                VisData vd1 = new VisData(new VisDataPointStyle(new ClassColorizer(), 20));

                for (int i=0; i<ds.data.size(); ++i) {
                    float[] d = ds.data.get(i);
                    vd1.add(new double[] {d[0], d[1], 0, ds.getLabel(i)});

                    vb.addBuffered(new VisText(
                            new double[] {d[0], d[1], 5}, ANCHOR.TOP_LEFT, "   "+Wts[i]));
                }
                vb.addBuffered(vd1);

                // Show the separating line
                vb.addBuffered(getSeparatingLine(svm));

                // Show points with classification labels
                VisData vd2 = new VisData(new VisDataPointStyle(new ClassColorizer2(), 14));

                for (int i=0; i<ds.data.size(); ++i) {
                    float[] d = ds.data.get(i);
                    int l = svm.predict(d);
                    System.out.println(l);
                    vd2.add(new double[] {d[0], d[1], 0.1, l});
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
        pg.addIntSlider("n", "Classifiers", 0, 250, 2);
        jf.add(pg, BorderLayout.SOUTH);

        final GaussianMixtureDataSet ds = new GaussianMixtureDataSet();

        pg.addListener(new ParameterListener() {
            public void parameterChanged(ParameterGUI pg, String name)
            {
                StrongClassifier sc = new StrongClassifier(ds, pg.gi("n"));

                VisWorld.Buffer vb = vc.getWorld().getBuffer("classifier");

                // Show points with original labels
                VisData vd1 = new VisData(new VisDataPointStyle(new ClassColorizer(), 20));

                for (int i=0; i<ds.data.size(); ++i) {
                    float[] d = ds.data.get(i);
                    vd1.add(new double[] {d[0], d[1], 0, ds.getLabel(i)});
                }
                vb.addBuffered(vd1);

                // Show points with classification labels
                VisData vd2 = new VisData(new VisDataPointStyle(new ClassColorizer(), 12));

                for (int i=0; i<ds.data.size(); ++i) {
                    float[] d = ds.data.get(i);
                    int l = sc.predict(d);
                    vd2.add(new double[] {d[0], d[1], 0.1, l});
                }
                vb.addBuffered(vd2);

                // Show Lines
                for (int i=0; i<sc.classifiers.size(); ++i)
                    vb.addBuffered(getSeparatingLine(sc.classifiers.get(i)));

                vb.switchBuffer();
            }
        });

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }

    private static VisData getSeparatingLine(LinearSVM svm)
    {
        // Show the separating line
        double[] w = svm.svm.getFeatureWeights();
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
        return p[p.length-1] == 1 ? 0xff0000FF: 0xffFF0000;
    }
}

class ClassColorizer2 implements Colorizer
{
    @Override
    public int colorize(double[] p)
    {
        return p[p.length-1] == 1 ? 0xff000088: 0xff880000;
    }
}

class XORDataSet implements DataSet
{
    ArrayList<float[]> data = new ArrayList<float[]>();
    ArrayList<Integer> labels = new ArrayList<Integer>();

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
}

class GaussianMixtureDataSet implements DataSet
{
    ArrayList<float[]> data = new ArrayList<float[]>();
    ArrayList<Integer> labels = new ArrayList<Integer>();

    public GaussianMixtureDataSet()
    {
        Random g1 = new Random();
        Random g2 = new Random();
        Random b = new Random();

        for (int i=0 ;i<15; ++i) {
            if (b.nextBoolean()) {
                double[] p = new double[] {g1.nextGaussian(), g1.nextGaussian()};
                data.add(LinAlg.copyFloats(p));
                labels.add(1);
            } else {
                double[] p = new double[] {.5+g2.nextGaussian(), .5+g2.nextGaussian()};
                data.add(LinAlg.copyFloats(p));
                labels.add(-1);
            }
        }
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
}
