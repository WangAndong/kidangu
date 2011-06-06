package hog;

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

    public BoostedClassifier(DataSet ds)
    {
        this.ds = ds;
        wt = new double[ds.numInstances()];

        /* Initialize weights */
        nPositive = Collections.frequency(ds.getLabels(), 1);
        nNegative = Collections.frequency(ds.getLabels(), -1);
        for (int i=0; i<ds.numInstances(); ++i) {
            wt[i] = (ds.getLabel(i) == 1) ? (1.0/nPositive) : (1.0/nNegative);
        }

        addWeakClassifier();
    }

    public LinearSVM addWeakClassifier()
    {
        wt = LinAlg.normalizeL1(wt);
        Random rand = new Random();

        LinearSVM best = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
        for (int i=1; i<250; ++i) {
            LinearSVM c = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
            best = (c.getTrainError() < best.getTrainError()) ? c : best;
        }

        /* Update alpha for classifier */
        double eps = best.getTrainError();
        assert (eps < 1);

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

        assert (count[1][1] + count[1][0] == nPositive);
        assert (count[0][1] + count[0][0] == nNegative);

        double tpr = ((double)count[1][1]) / (count[1][1] + count[1][0]);
        double fpr = ((double)count[0][1]) / (count[0][1] + count[0][0]);

        return new PredictionStats(tpr, fpr, posInstVotes);
    }

    /** Adjust bias to get required true positive rate */
    public void tune(PredictionStats ps, double truePositiveRate)
    {
        Collections.sort(ps.posInstVotes);

        /* Build a histogram of the votes */
        TreeMap<Double, Integer> hist = new TreeMap<Double, Integer>();
        for (Double v: ps.posInstVotes) {
            Integer count = hist.get(v);
            if (count == null)
                count = 0;

            hist.put(v, ++count);
        }

        final double thresh = getCombinedThreshold();

        System.out.printf("%.4f\t", thresh);
        for (Double v: hist.keySet()) {
            System.out.printf("%.4f:%d, ", v, hist.get(v));
        }
        System.out.println();

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
}
