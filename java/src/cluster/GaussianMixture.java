package cluster;

import java.util.*;

import april.jmat.*;

public class GaussianMixture
{
    ArrayList<double[]> data = new ArrayList<double[]>();

    ArrayList<Double> pi = new ArrayList<Double>();
    ArrayList<MultiGaussian> mixture = new ArrayList<MultiGaussian>();


    public GaussianMixture(ArrayList<double[]> data, ArrayList<double[]> means)
    {
        this.data = data;
        ArrayList<ArrayList<double[]>> clusters =
            KMeansClusterer.clusterWithNearestMean(data, means);

        for(ArrayList<double[]> cluster : clusters) {
            pi.add((double)cluster.size() / data.size());
            mixture.add(estimateFromSamples(cluster));
        }
    }

    public ArrayList<MultiGaussian> estimate()
    {
        final int K = pi.size();
        final int N = data.size();

        double logLikelihood = getLogLikelihood(mixture, pi, data);

        int iter = 0;
        while (true) {
            /*  EXPECTATION */

            /* Compute how much is each gaussian component responsible for each
               data point. This is a soft assignment of data points to clusters */
            double[][] share = new double[K][N];
            for (int i=0; i<N; ++i) {
                double[] r = new double[K];
                for (int j=0; j<K; ++j) {
                    r[j] = pi.get(j) * mixture.get(j).prob(data.get(i));
                }

                r = LinAlg.normalizeL1(r);
                for (int c=0; c<K; ++c) {
                    share[c][i] = r[c];
                }
            }

            /*  MAXIMIZATION */

            ArrayList<MultiGaussian> mix = new ArrayList<MultiGaussian>();
            for (int j=0; j<K; ++j) {
                MultiGaussian g = estimateFromSamples(data, share[j]);
                mix.add(g);
            }

            for (int j=0; j<K; ++j) {
                pi.set(j, LinAlg.normL1(share[j])/N);
            }

            /*  Check for convergence */
            double newLogLikelihood = getLogLikelihood(mix, pi, data);
            if (Math.abs(logLikelihood - newLogLikelihood) < 1e-6)
                break;

            logLikelihood = newLogLikelihood;
            mixture = mix;

            System.out.println("iteration " + ++iter + " logLikelihood: " + logLikelihood);
        }

        return mixture;
    }

    static MultiGaussian estimateFromSamples(List<double[]> data)
    {
        MultiGaussianEstimator mge = new MultiGaussianEstimator(data.get(0).length);
        for (double[] v : data)
            mge.observe(v);

        return mge.getEstimate();
    }

    static MultiGaussian estimateFromSamples(List<double[]> data, double[] wt)
    {
        MultiGaussianEstimator mge = new MultiGaussianEstimator(data.get(0).length);
        for (int i=0; i<data.size(); ++i)
            mge.observeWeighted(data.get(i), wt[i]);

        return mge.getEstimate();
    }

    static double getLogLikelihood(ArrayList<MultiGaussian> mix,
            ArrayList<Double> pi,
            ArrayList<double[]> data)
    {
        final int K = pi.size();
        final int N = data.size();

        double ll = 0;
        for (int i=0; i<N; ++i) {
            double sumP = 0;
            for (int j=0; j<K; ++j) {
                final MultiGaussian g = mix.get(j);
                final double[] v = data.get(i);
                final double p = g.prob(v);
                sumP += (pi.get(j) * p);
            }

            ll += Math.log(sumP);
        }

        return ll;
    }


    public static void main(String[] args)
    {
        MultiGaussian g1 = new MultiGaussian(LinAlg.diag(new double[] {1, 2, 3}), new double[3]);
        MultiGaussian g2 = new MultiGaussian(LinAlg.diag(new double[] {1, 2, 3}), new double[] {2,2,2});

        ArrayList<double[]> data = new ArrayList<double[]>();
        Random r1 = new Random();
        Random r2 = new Random();
        for (int i=0; i<1000; ++i) {
            data.add(g1.sample(r1));
            data.add(g2.sample(r2));
        }

        ArrayList<double[]> means = new ArrayList<double[]>();
        means.add(new double[] {0.5, 0.5, 0.5});
        means.add(new double[] {1.5, 1.5, 1.5});

        GaussianMixture gmm = new GaussianMixture(data, means);
        ArrayList<MultiGaussian> mix = gmm.estimate();

        for (MultiGaussian g : mix) {
            LinAlg.printTranspose(g.getMean());
            LinAlg.print(g.getCovariance().copyArray());
            System.out.println();
        }
    }
}
