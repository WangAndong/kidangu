package hog;

import java.util.*;


public class RejectionCascade
{
    ArrayList<StrongClassifier> cascade = new ArrayList<StrongClassifier>();

    public RejectionCascade(DataSet ds, double minLevelTPR, double maxFPR)
    {
        double FPR = 1.0;
        do {
            StrongClassifier sc;
            try {
                sc = new StrongClassifier(ds, minLevelTPR, 0.7);
            }
            catch (ConvergenceFailure e) {
                e.printStackTrace();
                break;
            }

            cascade.add(sc);
            FPR *= sc.getFalsePositiveRate(ds);

            System.out.println("NFO: Added strong classifier");
            printDescription(minLevelTPR, FPR);

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

        } while (FPR > maxFPR);
    }

    int predict(ArrayList<float[]> instance)
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
        System.out.println("------------------------------------");
        System.out.println("CASCADE DESCRIPTION");
        System.out.println("------------------------------------");

        System.out.println(cascade.size() + " levels");

        for (StrongClassifier sc1: cascade)
            Util.printBlocks(sc1.numClassifiers(), '=', "[", "]");
        System.out.println();

        System.out.println("False Positive Rate = " + FPR);
        System.out.println("Detection Rate = " + Math.pow(minTPR, cascade.size()));

        System.out.println("------------------------------------");
    }
}
