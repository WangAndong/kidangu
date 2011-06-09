package hog;

import java.io.*;
import java.util.*;

import april.jmat.*;

/**
 * Constructs a Boosted Classifier using the weighted vote of a set of linear
 * SVMs (weak classifier). The BoostedClassifier is trained using AdaBoost
 * (as described in Robust Real-time object detection, Viola and Jones, 2001).
 */
public class BoostedClassifier
{
    DataSet ds;
    final int nPositive;
    final int nNegative;

    double[] wt;
    ArrayList<LinearSVM> classifiers = new ArrayList<LinearSVM>();
    ArrayList<Double> alpha = new ArrayList<Double>();
    double bias = 0;


    public BoostedClassifier(DataSet ds, int nClassifiers)
    {
        this.ds = ds;
        wt = new double[ds.numInstances()];

        /* Initialize weights */
        nPositive = Collections.frequency(ds.getLabels(), 1);
        nNegative = Collections.frequency(ds.getLabels(), -1);
        for (int i=0; i<ds.numInstances(); ++i) {
            wt[i] = (ds.getLabel(i) == 1) ? (1.0/nPositive) : (1.0/nNegative);
        }

        while (nClassifiers-- != 0) {
            addWeakClassifier();
        }
    }

    public LinearSVM addWeakClassifier()
    {
        wt = LinAlg.normalizeL1(wt);

        LinearSVM best = LinearSVM.train(ds, wt, 0);
        for (int i=0; i<ds.numFeatures(); ++i) {
            System.out.printf("Weak classifier #%d: ", classifiers.size()+1);
            Util.printProgress(i, ds.numFeatures());
            System.out.print('\r');

            LinearSVM c = LinearSVM.train(ds, wt, i);
            best = (c.getTrainError() < best.getTrainError()) ? c : best;
        }

        /* Update alpha for classifier */
        double eps = best.getTrainError();
        assert (eps < 1);
        System.out.printf("\nBest feature has training error %.4f\n", eps);

        double alpha = Math.log((1-eps)/eps);
        this.alpha.add(alpha);

        /* Update data weights for next classifier */
        double beta = eps / (1-eps);
        for (int i=0; i<ds.numInstances(); ++i) {
            wt[i] *= best.wasMisClassified(i) ? 1 : beta;
        }

        classifiers.add(best);
        return best;
    }

    public LinearSVM getClassifier(int index)
    {
        return classifiers.get(index);
    }

    public int numClassifiers()
    {
        return classifiers.size();
    }

    public double[] getDataWeights()
    {
        return wt;
    }

    double getCombinedVote(ArrayList<float[]> instance)
    {
        double vote = 0;
        for (int i=0; i<classifiers.size(); ++i) {
            double prediction = classifiers.get(i).predict(instance);
            vote += alpha.get(i) * (prediction == 1 ? 1 : 0);
        }

        return vote;
    }

    double getCombinedThreshold()
    {
        double thresh = 0;
        for (int i=0; i<classifiers.size(); ++i) {
            thresh += alpha.get(i);
        }

        return 0.5*thresh;
    }

    double predict(double vote, double thresh)
    {
        return vote >= thresh + bias ? 1 : -1;
    }

    /** returns 1,-1 */
    public double predict(ArrayList<float[]> instance)
    {
        final double vote = getCombinedVote(instance);
        final double thresh = getCombinedThreshold();
        return predict(vote, thresh);
    }

    public PredictionStats predict(DataSet ds)
    {
        int count[][] = new int[2][2];

        ArrayList<Double> posInstVotes = new ArrayList<Double>();
        final double thresh = getCombinedThreshold();

        for (int i=0; i<ds.numInstances(); ++i) {
            double vote = getCombinedVote(ds.getInstance(i));
            int prediction = this.predict(vote, thresh) == 1 ? 1: 0;
            int label = ds.getLabel(i)==1 ? 1 : 0;
            ++count[label][prediction];

            if (label==1) {
                posInstVotes.add(vote);
            }
        }

        double tpr = ((double)count[1][1]) / (count[1][1] + count[1][0]);
        double fpr = ((double)count[0][1]) / (count[0][1] + count[0][0]);

        return new PredictionStats(tpr, fpr, posInstVotes);
    }

    /** Adjust bias to get required true positive rate */
    public void tune(PredictionStats ps, double truePositiveRate)
    {
        Collections.sort(ps.posInstVotes);

        final double thresh = getCombinedThreshold();
        int index = (int) (ps.posInstVotes.size()*(1-truePositiveRate));
        index = index < 0 ? 0 : index;

        this.bias = ps.posInstVotes.get(index) - thresh;
    }

    public static class PredictionStats
    {
        public final double tpRate;
        public final double fpRate;
        public final ArrayList<Double> posInstVotes;

        PredictionStats(double tpr, double fpr, ArrayList<Double> posInstVotes)
        {
            this.tpRate = tpr;
            this.fpRate = fpr;
            this.posInstVotes = posInstVotes;
        }
    }

    public void setBias(double v)
    {
        this.bias = v;
    }

    public double getBias()
    {
        return this.bias;
    }

    public void save(PrintStream out)
    {
        out.println("bias = " + bias + ";");

        out.println("classifiers {");
        for (int i=0; i<classifiers.size(); ++i) {
            out.printf("svm%d {\n", i+1);
            out.println("weight = " + alpha.get(i) + ";");
            classifiers.get(i).save(out);
            out.println("}\n");
        }
        out.println("}");
    }
}
