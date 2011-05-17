package hog;

import hog.StrongClassifier.*;

import java.util.*;
import java.util.concurrent.*;


public class RejectionCascade
{
    ArrayList<StrongClassifier> cascade = new ArrayList<StrongClassifier>();

    static final double MIN_LEVEL_TPR = 0.9975; /** Min TPR At each cascade level */
    static final double MAX_LEVEL_FPR = 0.7; /** Max FPR At each cascade level */

    public RejectionCascade(DataSet ds, double targetFPR)
    {
        double FPR = 1.0;
        while (FPR > targetFPR) {
            System.out.println();

            StrongClassifier sc = null;
            try {
                sc = new StrongClassifier(ds, MIN_LEVEL_TPR, MAX_LEVEL_FPR);
            }
            catch (ConvergenceFailure e) {
                e.printStackTrace();
                break;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            catch (ExecutionException e) {
                e.printStackTrace();
                break;
            }

            cascade.add(sc);
            FPR *= sc.getFalsePositiveRate(ds);

            System.out.println("CASCADE: Added this strong classifier");
            printDescription(MIN_LEVEL_TPR, FPR);

            ArrayList<Integer> selected = new ArrayList<Integer>();
            int nNegative = 0;
            for (int i=0; i<ds.numInstances(); ++i) {
                if (ds.getLabel(i)==1) {
                    selected.add(i);
                }
                else /* Negative instance */ {
                    // TODO: wont sc.predict suffice here?
                    if (predict(ds.getInstance(i))==1) {
                        /* Wrong prediction. so keep it */
                        selected.add(i);
                        ++nNegative;
                    }
                }
            }

            if (nNegative==0) {
                System.out.println("WRN: Removed all Negatives. Are we overfitting?");
                break;
            }

            ds = ds.select(selected);
        }
    }

    public int predict(ArrayList<float[]> instance)
    {
        for (StrongClassifier sc : cascade) {
            if (sc.predict(instance) == -1)
                return -1;
        }

        /* We passed all the rejectors */
        return 1;
    }

    void printDescription(double minTPR, double FPR)
    {
        System.out.println();
        System.out.println("***************************************************");
        System.out.println("* CASCADE DESCRIPTION");
        System.out.println("***************************************************");

        System.out.println(cascade.size() + " levels");

        for (StrongClassifier sc1: cascade) {
            Util.printBlocks(sc1.numClassifiers(), '=', "[", "]");
            System.out.println();
        }
        System.out.println();

        System.out.println("False Positive Rate = " + FPR);
        System.out.println("Detection Rate = " + Math.pow(minTPR, cascade.size()));

        System.out.println("***************************************************");
    }
}
