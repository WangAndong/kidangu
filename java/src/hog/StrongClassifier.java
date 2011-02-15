package hog;

import java.io.*;
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

    public StrongClassifier(DataSet ds, int nClassifiers)
    {
        double[] wt = new double[ds.numInstances()];

        //
        // Initialize weights
        //
        final float nPositive = Collections.frequency(ds.getLabels(), 1);
        final float nNegative = Collections.frequency(ds.getLabels(), -1);
        for (int i=0; i<ds.numInstances(); ++i) {
            wt[i] = (ds.getLabel(i) == 1) ? (1/nPositive) : (1/nNegative);
        }

        //
        // Add weak classifiers
        //
        double fpRate = 1.0;
        while (nClassifiers-- > 0) {
            wt = LinAlg.normalizeL1(wt);

            //
            // Choose the best of 250 linear svms
            //
            Random rand = new Random();
            LinearSVM best = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
            for (int i=0; i<250; ++i) {
                LinearSVM c = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
                best = (c.getTrainError() < best.getTrainError()) ? c : best;
            }

            classifiers.add(best);

            //
            //  Update weights based on mis-classification
            //
            final double eps = best.getTrainError();
            final double rho =(1-eps)/eps;
            alpha.add(Math.log(rho));

            for (int i=0; i<wt.length; ++i) {
                wt[i] *= best.wasMisClassified(i) ? rho : 1;
            }

            //
            //  Analyze performance
            //
        }
    }

    /** returns {-1, 1} */
    public int predict(ArrayList<float[]> instance)
    {
        double sumAlphaH = 0;
        for (int i=0; i<alpha.size(); ++i)
            sumAlphaH += alpha.get(i) * classifiers.get(i).predict(instance);

        return sumAlphaH >= 0 ? 1 : -1;
    }
}

