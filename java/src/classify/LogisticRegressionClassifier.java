package classify;

import java.util.*;

import april.jmat.*;


public class LogisticRegressionClassifier
{
    double[] w;

    public static LogisticRegressionClassifier train(List<double[]> x0, List<double[]> x1, double eps)
    {
        final int N = x0.size() + x1.size();
        final int M = x0.get(0).length;

        Matrix X = new Matrix(N, M+1);
        int r = 0;
        for (double[] d : x0) {
            DenseVec v = new DenseVec(d);
            v.resize(M+1);
            v.set(M, 1);
            X.setRow(r, v);
        }
        for (double[] d : x1) {
            DenseVec v = new DenseVec(d);
            v.resize(M+1);
            v.set(M, 1);
            X.setRow(r++, v);
        }

        double[] y = new double[N];
        for (int i=x0.size(); i<N; ++i)
            y[i] = 1;

        double[] w = new double[M+1];
        for (int i=0; i<M+1; ++i)
            w[i] = 0.1;

        while (true) {
            double[] s = X.times(w);
            double[] gradient = X.transposeTimes(LinAlg.subtract(s, y));

            if (LinAlg.normF(gradient) < eps)
                break;
            else
                LinAlg.printTranspose(gradient);

            LinAlg.plusEquals(w, LinAlg.scale(gradient, -1e-4));
        }

        return new LogisticRegressionClassifier(w);
    }

    private LogisticRegressionClassifier(double[] w)
    {
        this.w = w;
    }

    public double classify(double[] v)
    {
        return 1.0/(1.0+Math.exp(-LinAlg.dotProduct(v, w)));
    }

    DenseVec sigmoidForEach(DenseVec v)
    {
        DenseVec s = new DenseVec(v.size());
        for (int i=0; i<s.size(); ++i)
            s.set(i, 1.0/(1.0+Math.exp(-v.get(i))));

        return s;
    }

    public double[] getFeatureWeights()
    {
        return w;
    }

    public static void main(String[] args)
    {
        ArrayList<double[]> x0 = new ArrayList<double[]>();
        x0.add(new double[] {-0, -0});
        x0.add(new double[] {-0.1, -0});
        x0.add(new double[] {-0.1, -0.1});

        ArrayList<double[]> x1 = new ArrayList<double[]>();
        x1.add(new double[] {1, 1});
        x1.add(new double[] {1, 1.1});
        x1.add(new double[] {1.1, 1.1});

        LogisticRegressionClassifier lrc = LogisticRegressionClassifier.train(x0, x1, 1e-8);
        LinAlg.printTranspose(lrc.w);
    }
}
