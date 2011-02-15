package hog;

import java.util.*;

import april.jmat.*;


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
        while (nClassifiers-- > 0) {
            wt = LinAlg.normalizeL1(wt);

            //
            // Choose the best of 250 linear svms
            //
            Random rand = new Random();
            LinearSVM best = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
            for (int i=0; i<250; ++i) {
                LinearSVM c = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
                best = (c.getError() < best.getError()) ? c : best;
            }

            classifiers.add(best);

            //
            //  Update weights based on mis-classification
            //
            final double eps = best.getError();
            final double rho =(1-eps)/eps;
            alpha.add(Math.log(rho));

            for (int i=0; i<wt.length; ++i) {
                wt[i] *= best.wasMisClassified(i) ? rho : 1;
            }
        }
    }

    public int predict(float[] x)
    {
        double sumAlphaH = 0;

        for (int i=0; i<alpha.size(); ++i) {
            sumAlphaH += alpha.get(i) * classifiers.get(i).predict(x);
        }

        return sumAlphaH >= 0 ? 1 : -1;
    }
}

