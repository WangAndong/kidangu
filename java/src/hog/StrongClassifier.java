package hog;

import java.util.*;
import java.util.concurrent.*;

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

        int nClassifiers = 18;
        while (nClassifiers-- > 0) {
            wt = LinAlg.normalizeL1(wt);

            //
            // Choose the best of 250 linear SVMs
            //
            Random rand = new Random();
            LinearSVM best = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
            for (int i=0; i<250; ++i) {
                LinearSVM c = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
                best = (c.getTrainError() < best.getTrainError()) ? c : best;
            }

            classifiers.add(best);
            System.out.print('.');

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
            //  while maintaining acceptable false postive rate.
            //
            final double biasStep = 1e-4;
            while ( getPositiveRate(ds, true) < minTPR &&
                    getPositiveRate(ds,false) < maxFPR ) {
                bias -= biasStep;
            }

            bias += biasStep; /* Undo the change that caused constraints to break */

            if (getPositiveRate(ds, true) >= minTPR)
                break;
        }

        System.out.println();
        System.out.printf("TPR: %.4f\n", getPositiveRate(ds, true));
        System.out.printf("FPR: %.4f\n", getPositiveRate(ds, false));
        System.out.println(alpha.size() + " Linear SVM classifiers");

        if (getPositiveRate(ds, false) > maxFPR) {
            throw new ConvergenceFailure("Strong classifier failed to converge "
                    + "(" + getPositiveRate(ds, false) + " > " + maxFPR + ")" );
        }
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