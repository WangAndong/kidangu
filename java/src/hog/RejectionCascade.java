package hog;

import java.io.*;
import java.util.*;


public class RejectionCascade
{
    ArrayList<BoostedClassifier> cascade = new ArrayList<BoostedClassifier>();
    ArrayList<double[]> roc = new ArrayList<double[]>();

    static final double MIN_LEVEL_TPR = 0.9975; /** Min TPR At each cascade level */
    static final double MAX_LEVEL_FPR = 0.7; /** Max FPR At each cascade level */


    public RejectionCascade(DataSet ds, double targetFPR)
    {
        System.out.println();
        double fpr = 1.0, tpr = 1.0;

        BoostedClassifier.PredictionStats ps = null;
        while (fpr > targetFPR) {
            System.out.println("----------------------------------------");
            System.out.println("  LEVEL " + (cascade.size()+1));
            System.out.println("----------------------------------------\n");

            BoostedClassifier bc = new BoostedClassifier(ds, 0);

            while (true) {
                bc.addWeakClassifier();

                ps = bc.predict(ds);
                System.out.printf("Boosted classifier has fpr:%.4f tpr:%.4f\n", ps.fpRate, ps.tpRate);

                /* Tune classifier to achieve the required TPR */
                if (ps.tpRate < MIN_LEVEL_TPR) {
                    System.out.println("  tuning ...");
                    bc.tune(ps, MIN_LEVEL_TPR);
                    ps = bc.predict(ds);
                    System.out.printf("  tuned classifier has fpr:%.4f tpr:%.4f\n", ps.fpRate, ps.tpRate);
                }

                /* Do we have the required ROC ? */
                if (ps.fpRate < MAX_LEVEL_FPR && ps.tpRate >= MIN_LEVEL_TPR) {
                    System.out.println("Boosted classifier meets required FPR\n");
                    cascade.add(bc);
                    break;
                } else {
                    System.out.println("Boosted classifier does not meet required FPR or TPR");
                }
            }

            roc.add(new double[] {ps.tpRate, ps.fpRate});
            tpr *= ps.tpRate;
            fpr *= ps.fpRate;
            printDescription();

            /* Save classifier to file */
            try {
                this.save(new PrintStream(new File("classifier.config")));
                System.out.println("Saved classifier to file: classifier.config");
            }
            catch (FileNotFoundException e) {
                System.out.println("ERR: Could not save classifier to file");
                e.printStackTrace();
            }

            /* Next cascade level has to deal with false detections only */
            ArrayList<Integer> selected = new ArrayList<Integer>();
            int nNegative = 0;
            for (int i=0; i<ds.numInstances(); ++i) {
                if (ds.getLabel(i)==1) {
                    selected.add(i);
                }
                else /* Negative instance */ {
                    if (this.predict(ds.getInstance(i))==1) {
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
        for (BoostedClassifier sc : cascade) {
            if (sc.predict(instance) == -1)
                return -1;
        }

        /* We passed all the rejectors */
        return 1;
    }

    void printDescription()
    {
        System.out.println("\nHere's a description of the cascade till this level...");

        double finalFPR = 1.0, finalTPR = 1.0;
        for (int i=0; i<cascade.size(); ++i) {
            BoostedClassifier bc = cascade.get(i);
            double[] roc = this.roc.get(i);

            finalTPR *= roc[0];
            finalFPR *= roc[1];

            System.out.printf("  ");
            Util.printBlocks(bc.numClassifiers(), '=', "[", "]");
            System.out.printf(" tpr:%.4f fpr:%.4f\n", roc[0], roc[1]);
        }
        System.out.println();

        System.out.println("False Positive Rate = " + finalFPR);
        System.out.println("Detection Rate = " + finalTPR);
    }

    public void save(PrintStream out)
    {
        out.println("cascade {");
        for (int i=0; i<cascade.size(); ++i) {
            out.printf("level%d {\n", i+1);
            cascade.get(i).save(out);
            out.println("}\n\n");
        }
        out.println("}");
    }
}
