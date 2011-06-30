package cluster;

import java.util.*;

import april.jmat.*;


public class KMeansClusterer
{
    ArrayList<double[]> data;
    ArrayList<double[]> means;

    public KMeansClusterer(ArrayList<double[]> data, ArrayList<double[]> means)
    {
        this.data = data;
        this.means = means;
    }

    public ArrayList<double[]> estimateCenters()
    {
        final int K = means.size();

        while (true) {
            ArrayList<ArrayList<double[]>> clusters = MStep();
            ArrayList<double[]> means = EStep(clusters);

            /* Check for convergence */
            final double eps = 1e-6;

            boolean converged = true;
            for (int c=0; c<K; ++c) {
                if (LinAlg.distance(this.means.get(c), means.get(c)) > eps) {
                    converged = false;
                    break;
                }
            }

            if (converged)
                break;

            this.means = means;
        }

        return means;
    }

    ArrayList<double[]> EStep(ArrayList<ArrayList<double[]>> clust)
    {
        final int K = means.size();
        ArrayList<double[]> means = new ArrayList<double[]>();

        for (int c=0; c<K; ++c) {
            means.add(c, LinAlg.centroid(clust.get(c)));
        }

        return means;
    }

    ArrayList<ArrayList<double[]>> MStep()
    {
        return clusterWithNearestMean(data, means);
    }

    static ArrayList<ArrayList<double[]>> clusterWithNearestMean(
            ArrayList<double[]> data,
            ArrayList<double[]> means)
    {
        final int K = means.size();
        ArrayList<ArrayList<double[]>> clust = new ArrayList<ArrayList<double[]>>();
        for (int c=0; c<K; ++c)
            clust.add(new ArrayList<double[]>());

        for (int i=0; i<data.size(); ++i) {
            double[] dist = new double[K];
            for (int j=0; j<K; ++j) {
                dist[j] = LinAlg.squaredDistance(data.get(i), means.get(j));
            }

            final int nearest = LinAlg.minIdx(dist);
            clust.get(nearest).add(data.get(i));
        }

        return clust;
    }
}
