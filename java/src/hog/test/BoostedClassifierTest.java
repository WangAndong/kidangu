package hog.test;

import hog.*;
import hog.BoostedClassifier.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import april.jmat.*;
import april.jmat.geom.*;
import april.util.*;
import april.vis.*;


public class BoostedClassifierTest
{
    public static void main(String[] args)
    {
        final JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        VisCanvas vc = new VisCanvas(new VisWorld());
        vc.setBackground(Color.black);
        jf.add(vc, BorderLayout.CENTER);

        ParameterGUI pg = new ParameterGUI();
        pg.addButtons("add", "Add", "tune", "Tune");
        pg.addDoubleSlider("bias", "Bias", -100, 100, 0);
        jf.add(pg, BorderLayout.SOUTH);

        final ConcentricCircleDataSet cds = new ConcentricCircleDataSet();

        final VisWorld.Buffer vb = vc.getWorld().getBuffer("content");
        final BoostedClassifier bc = new BoostedClassifier(cds, 1);

        while (true) {
            System.out.println(bc.numClassifiers());
            bc.addWeakClassifier();
            PredictionStats ps = bc.predict(cds);

            if (ps.tpRate < 0.9975)
                bc.tune(ps, 0.9975);

            ps = bc.predict(cds);

            if (ps.fpRate > 0.3)
                break;
            System.out.println(ps.fpRate);

            pg.sd("bias", bc.getBias());
        }

        final ParameterListener pl = new ParameterListener() {
            public void parameterChanged(ParameterGUI pg, String name)
            {
                if (name.equals("add")) {
                    bc.addWeakClassifier();
                }

                if (name.equals("bias")) {
                    bc.setBias(pg.gd("bias"));
                }

                if (name.equals("tune")) {
                    final PredictionStats ps = bc.predict(cds);

                    if (ps.tpRate < 0.9975)
                        bc.tune(ps, 0.9975);

                    pg.sd("bias", bc.getBias());
                }

//                for (int i=0; i<bc.numClassifiers(); ++i) {
//                    LinearSVM svm = bc.getClassifier(i);
//                    vb.addBuffered((makeSeparatingLineVisualization(svm)));
//                }

                final PredictionStats ps = bc.predict(cds);
                jf.setTitle("TPR: " + ps.tpRate + " FPR: " + ps.fpRate + " N: " + bc.numClassifiers());

                vb.addBuffered(cds.getVisualization(bc));
                vb.switchBuffer();
            }
        };
        pg.addListener(pl);

//        for (int i=0; i<bc.numClassifiers(); ++i) {
//            LinearSVM svm = bc.getClassifier(i);
//            vb.addBuffered((makeSeparatingLineVisualization(svm)));
//        }

        final PredictionStats ps = bc.predict(cds);
        jf.setTitle("TPR: " + ps.tpRate + " FPR: " + ps.fpRate + " N: " + bc.numClassifiers());

        vb.addBuffered(cds.getVisualization(bc));
        vb.switchBuffer();

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }

    private static VisData makeSeparatingLineVisualization(LinearSVM svm)
    {
        double[] w = LinAlg.normalizeL1(svm.getFeatureWeights());
        GLine2D line = new GLine2D(-w[0]/w[1], -w[2]/w[1]);
        VisData vdLine = new VisData(new VisDataLineStyle(Color.gray, 2));

        vdLine.add(line.getPointOfCoordinate(-100));
        vdLine.add(line.getPointOfCoordinate(100));

        return vdLine;
    }
}

class ConcentricCircleDataSet implements DataSet
{
    private ArrayList<float[]> data = new ArrayList<float[]>();
    private ArrayList<Integer> labels = new ArrayList<Integer>();

    public ConcentricCircleDataSet()
    {
        Random r = new Random();
        Random theta = new Random();

        for (int i=0; i<300; ++i) {
            float x = (r.nextFloat()-0.5f) * 5;
            float noise = (float) (r.nextGaussian()*0.5f);

            data.add(new float[] {
                    (float) x,
                    (float) x*x + noise - 0.5f,
                    });
            labels.add(+1);
        }

        r = new Random();
        theta = new Random();

//        for (int i=0; i<2000; ++i) {
//            float rsample = (float) r.nextGaussian()*0.05f + 2f;
//            float tsample = (float) (theta.nextFloat() * Math.PI);
//
//            data.add(new float[] {
//                    (float) (rsample*Math.cos(tsample)),
//                    (float) (rsample*Math.sin(tsample))
//                    });
//            labels.add(-1);
//        }

        for (int i=0; i<300; ++i) {
            float x = (r.nextFloat()-0.5f) * 5;
            float noise = (float) (r.nextGaussian()*0.5f);

            data.add(new float[] {
                    (float) x,
                    (float) 0.3*x*x + noise - 3
                    });
            labels.add(-1);
        }
    }

    private ConcentricCircleDataSet(ArrayList<float[]> data, ArrayList<Integer> labels)
    {
        this.data = data;
        this.labels = labels;
    }

    @Override
    public ArrayList<float[]> getInstance(int idx)
    {
        ArrayList<float[]> inst = new ArrayList<float[]>();
        for (float v: data.get(idx)) {
            inst.add(new float[] {v});
        }

        return inst;
    }

    @Override
    public ArrayList<float[]> getFeatureOfInstances(int idx)
    {
        ArrayList<float[]> ftrs = new ArrayList<float[]>();
        for (int i=0; i<data.size(); ++i)
            ftrs.add(new float[] {data.get(i)[idx]});

        return ftrs;
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
        return 2;
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

        return new ConcentricCircleDataSet(sdata, slabel);
    }

    VisObject getVisualization(BoostedClassifier bc)
    {
        VisData vdP = new VisData(new VisDataPointStyle(new CachedColorizer(), 7));
        VisData vd = new VisData(new VisDataPointStyle(new CachedColorizer(), 5));

        double[] wt = bc.getDataWeights();
        double min = LinAlg.min(wt);
        double max = LinAlg.max(wt);
        for (int i=0 ;i<wt.length; ++i)
            wt[i] = (wt[i]-min) / (max-min);

        Color orange = new Color(128,255,0);

        for (int i=0; i<data.size(); ++i) {
            float[] p = data.get(i);
            double[] d = new double[] {p[0], p[1], 0, 0};
            if (labels.get(i) == 1) {
                d[3] = ColorUtil.setAlpha(orange, (int) (50+205*(wt[i]))).getRGB();
            }
            else {
                d[3] = ColorUtil.setAlpha(Color.cyan, (int) (50+205*(wt[i]))).getRGB();
            }
            vd.add(d);

            d = LinAlg.copy(d);
            if (bc.predict(this.getInstance(i))!=this.getLabel(i)) {
                d[3] = Color.blue.getRGB();
            } else {
                d[3] = Color.TRANSLUCENT;
            }
            vdP.add(d);
        }

        return new VisChain(vdP, vd);
    }
}
