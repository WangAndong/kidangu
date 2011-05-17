package hog;

import hog.SVMSearchThread.*;

import java.util.*;
import java.util.concurrent.atomic.*;

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

    public StrongClassifier(DataSet ds, double minTPR, double maxFPR) throws ConvergenceFailure, InterruptedException
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

        double FPR = 1.0;
        while (FPR > maxFPR) {
            wt = LinAlg.normalizeL1(wt);

            //
            // Choose the best of 250 linear SVMs. Do this on four threads
            // to maximize CPU utilization.
            //
            final AtomicInteger doneCount = new AtomicInteger();
            OnProgressCallBack ocb = new OnProgressCallBack() {
                @Override
                public synchronized void progress()
                {
                    System.out.print("Finding SVM " + alpha.size() + ": ");
                    Util.printProgress(doneCount.get()+1, 250);
                    System.out.print('\r');
                }
            };

            ArrayList<SVMSearchThread> threads = new ArrayList<SVMSearchThread>();

            for (int i=0; i<4; ++i) {
                threads.add(new SVMSearchThread(ds, wt, 62, doneCount, ocb));
                threads.get(i).start();
            }

            for (Thread t : threads) {
                t.join();
            }
            System.out.println();

            /* Find the best SVM found amongst the 4 threads */
            LinearSVM best = null;
            for (SVMSearchThread sst : threads) {
                if (best==null || sst.getBest().getTrainError() < best.getTrainError())
                    best = sst.getBest();
            }
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
            //  We do this adjusting the bias based on the number of
            //  false negatives.
            //
            PredictionResult pr = getPredictionResults(ds);

            System.out.println("TPR = " + getTruePositiveRate(pr));
            if (getTruePositiveRate(pr) < minTPR) {
                System.out.println("Adjusting Bias ...");

                Collections.sort(pr.falseNegatives);
                int targetFN = (int) Math.ceil(pr.truePositives.size()*(1-minTPR));
                targetFN = Math.min(targetFN, pr.falseNegatives.size()-1);

                bias = pr.falseNegatives.get(targetFN);
                System.out.println("bias = " + bias);
            }

            pr = getPredictionResults(ds);
            FPR = getFalsePositiveRate(pr); /* Udpate FPR for the new bias setting */

            //
            //  Output some information
            //
            System.out.print("Currently has " + alpha.size() + " Classifiers ");
            System.out.printf("\t\t(TPR:%.4f, FPR:%.4f)\n\n", getTruePositiveRate(pr), FPR);
        }
    }

    public double getTruePositiveRate(DataSet ds)
    {
        return getTruePositiveRate(getPredictionResults(ds));
    }

    public double getTruePositiveRate(PredictionResult pr)
    {
        double a = ((double)pr.falseNegatives.size()) / pr.truePositives.size();
        return 1 / (1 + a);
    }

    public double getFalsePositiveRate(DataSet ds)
    {
        return getFalsePositiveRate(getPredictionResults(ds));
    }

    public double getFalsePositiveRate(PredictionResult pr)
    {
        double a = ((double)pr.trueNegatives.size()) / pr.falsePositives.size();
        return 1 / (1 + a);
    }

    public int numClassifiers()
    {
        return alpha.size();
    }

    PredictionResult getPredictionResults(DataSet ds)
    {
        PredictionResult pr = new PredictionResult();

        for (int i=0; i<ds.numInstances(); ++i) {
            double p = weightedPrediction(ds.getInstance(i));
            double t = ds.getLabel(i);

            if (p>=bias && t==1)
                pr.truePositives.add(p);
            else if (p>=bias && t==-1)
                pr.falsePositives.add(p);
            else if (p<bias && t==-1)
                pr.trueNegatives.add(p);
            else if (p<bias && t==1)
                pr.falseNegatives.add(p);
            else
                throw new RuntimeException("Unexpected case: p=" + p + " t=" + t);
        }

        return pr;
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

    static class PredictionResult
    {
        ArrayList<Double> truePositives = new ArrayList<Double>();
        ArrayList<Double> trueNegatives = new ArrayList<Double>();
        ArrayList<Double> falsePositives = new ArrayList<Double>();
        ArrayList<Double> falseNegatives = new ArrayList<Double>();
    }

    static class ConvergenceFailure extends Exception
    {
        public ConvergenceFailure(String msg)
        {
            super(msg);
        }
    }
}

class SVMSearchThread extends Thread
{
    DataSet ds;
    double[] wt;
    AtomicInteger doneCount;
    final int tries;
    LinearSVM best;

    OnProgressCallBack onProgress;

    public SVMSearchThread(DataSet ds, double[] wt, int tries,
            AtomicInteger doneCount, OnProgressCallBack ocb)
    {
        this.ds = ds;
        this.wt = wt;
        this.doneCount = doneCount;
        this.tries = tries;
        this.onProgress = ocb;
    }

    @Override
    public void run()
    {
        Random rand = new Random();

        best = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
        for (int i=0; i<tries-1; ++i) {
            LinearSVM c = LinearSVM.train(ds, wt, rand.nextInt(ds.numFeatures()));
            best = (c.getTrainError() < best.getTrainError()) ? c : best;

            doneCount.getAndIncrement();
            onProgress.progress();
        }
    }

    public LinearSVM getBest()
    {
        return best;
    }

    interface OnProgressCallBack
    {
        void progress();
    }
}