package texture;

import java.util.*;


public class DataNormalizer
{
    double[] lb; /** lower bound on feature values */
    double[] ub; /** upper bound on feature values */

    public DataNormalizer()
    {}

    public DataNormalizer(double[] lowerBound, double[] upperBound)
    {
        this.lb = lowerBound;
        this.ub = upperBound;
    }

    public double[] getLowerBounds()
    {
        return lb;
    }

    public double[] getUpperBounds()
    {
        return ub;
    }

    public void observeLimits(List<double[]> data)
    {
        final int dim = data.get(0).length;

        if (lb == null && ub == null) {
            lb = new double[dim];
            Arrays.fill(lb, Double.POSITIVE_INFINITY);

            ub = new double[dim];
            Arrays.fill(ub, Double.NEGATIVE_INFINITY);
        }

        for (double[] d : data) {
            for (int i=0; i<dim; ++i) {
                lb[i] = Math.min(d[i], lb[i]);
                ub[i] = Math.max(d[i], ub[i]);
            }
        }
    }

    public ArrayList<double[]> normalize(List<double[]> data)
    {
        ArrayList<double[]> n = new ArrayList<double[]>();
        for (double[] v : data)
            n.add(normalize(v));

        return n;
    }

    public double[] normalize(double[] v)
    {
        if (lb == null || ub == null) {
            throw new IllegalStateException("Bounds unknown for normalization");
        }

        double[] r = new double[v.length];
        for (int i=0; i<v.length; ++i) {
            double mean = 0.5*(ub[i] + lb[i]);
            r[i] = 2* (v[i] - mean) / (ub[i] - lb[i]);
        }

        return r;
    }

    public static void main(String[] args)
    {
        DataNormalizer dn = new DataNormalizer();
        double[][] data = new double[][] {
                {-2, 3},
                {2, 4},
        };

        dn.observeLimits(Arrays.asList(data));
        ArrayList<double[]> n = dn.normalize(Arrays.asList(data));
    }
}
