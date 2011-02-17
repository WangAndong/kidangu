package hog;

import java.util.*;

import april.jmat.*;


/**
 * Constructs a StrongClassifier by using the weighted vote of a set of linear
 * SVMs (weak classifier). The StrongClassifier is trained using AdaBoost
 * (as described in Pattern Recognition and Machine Learning, C. M. Bishop)
 */
public class StrongClassifier
{
    ArrayList<LinearSVM> classifiers = new ArrayList<LinearSVM>();
    ArrayList<Double> alpha = new ArrayList<Double>();
    double bias = 0;

    public StrongClassifier(DataSet ds, double minTPR, double maxFPR) throws ConvergenceFailure
    {
        double[] wt = new double[ds.numInstances()];

        //
        // Initialize weights
        //
        final int nPositive = Collections.frequency(ds.getLabels(), 1);
        final int nNegative = Collections.frequency(ds.getLabels(), -1);
        for (int i=0; i<ds.numInstances(); ++i) {
            wt[i] = (ds.getLabel(i) == 1) ? (1.0/nPositive) : (1.0/nNegative);
        }

        //
        // Add weak classifiers
        //

        double FPR = 1.0, prevTPR = -1;
        do {
            wt = LinAlg.normalizeL1(wt);

            //
            // Choose the best of 250 linear SVMs
            //
            Random rand = new Random();
            LinearSVM best = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
            for (int i=0; i<250; ++i) {
                System.out.println("Finding SVM: ");
                Util.printProgress(i, 250);
                System.out.print('\r');

                LinearSVM c = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
                best = (c.getTrainError() < best.getTrainError()) ? c : best;
            }
            System.out.println();

            classifiers.add(best);

            //
            //  Update weights based on misclassification
            //
            final double eps = best.getTrainError();
            final double rho =(1-eps)/eps;
            alpha.add(Math.log(rho));

            for (int i=0; i<wt.length; ++i) {
                wt[i] *= best.wasMisClassified(i) ? rho : 1;
            }

            //
            //  Adjust bias to bring down false negative rate.
            //  while maintaining acceptable false positive rate.
            //
            final double biasStep = 1e-4;
            while ( getTruePositiveRate(ds) < minTPR ) {
                bias -= biasStep;
            }

            bias += biasStep; /* Undo the change that caused constraints to break */
            FPR = getFalsePositiveRate(ds); /* Udpate FPR for the new bias setting */

            //
            //  If the false positive rate becomes 1.0, this means that there is
            //  no classification going on and this classifier is just letting all
            //  data pass through (negatives can't be separated from the positives
            //  at the required TPR). Time to complain.
            //
            if (FPR == 1) {
                throw new ConvergenceFailure("StrongClassifier failed to converge");
            }

            //
            //  If the TPR didn't improve, we don't hope to do better.
            //  Time to say bye after removing the classifier we just added.
            //
            if (getTruePositiveRate(ds) == prevTPR) {
                alpha.remove(alpha.size()-1);
                classifiers.remove(classifiers.size()-1);
                System.out.println("NFO: No improvement. Removed classifier");
                break;
            }

            prevTPR = getTruePositiveRate(ds);

            //
            //  Output some information
            //
            Util.printBlocks(alpha.size(), '=', "[", "]");
            System.out.println(" " + alpha.size() + " Classifiers ");
            System.out.printf(" (TPR:%.4f, FPR:%.4f)", prevTPR, FPR);
            System.out.print('\n');

        } while( FPR < maxFPR );
    }

    public double getTruePositiveRate(DataSet ds)
    {
        return getPositiveRate(ds, true);
    }

    public double getFalsePositiveRate(DataSet ds)
    {
        return getPositiveRate(ds, false);
    }

    public int numClassifiers()
    {
        return alpha.size();
    }

    private double getPositiveRate(DataSet ds, boolean tORf)
    {
        double nFalsePostives = 0;
        double nTruePostives = 0;
        double nPositives = 0;
        double nNegatives = 0;

        for (int i=0; i<ds.numInstances(); ++i) {
            int p = predict(ds.getInstance(i));

            if (ds.getLabel(i)==1)
                ++nPositives;
            else
                ++nNegatives;

            if (p==1 && (ds.getLabel(i)!= 1))
                ++nFalsePostives;
            else if (p==1 && (ds.getLabel(i)== 1))
                ++nTruePostives;
        }

        if (tORf==true)
            return nTruePostives/nPositives;
        else
            return nFalsePostives/nNegatives;
    }

    /** returns {-1, 1} */
    public int predict(ArrayList<float[]> instance)
    {
        return weightedPrediction(instance) >= bias ? 1 : -1;
    }

    private double weightedPrediction(ArrayList<float[]> instance)
    {
        double sumAlphaH = 0;
        for (int i=0; i<alpha.size(); ++i)
            sumAlphaH += alpha.get(i) * classifiers.get(i).predict(instance);

        return sumAlphaH;
    }
}

class ConvergenceFailure extends Exception
{
    public ConvergenceFailure(String msg)
    {
        super(msg);
    }
}