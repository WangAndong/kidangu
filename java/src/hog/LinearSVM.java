package hog;

import java.util.*;

import liblinear.*;

import april.jmat.*;


/**
 *  Wrapper for LibLinear.Model
 */
public class LinearSVM
{
    static final double eps = 1e-10;

    final int featureIdx;
    final Model svm;
    final double trainingErr;
    ArrayList<Boolean> misClassified;

    private LinearSVM(int fIdx, Model svm, ArrayList<Boolean> misClassified, double err)
    {
        this.featureIdx = fIdx;
        this.svm = svm;
        this.trainingErr = err;
        this.misClassified = misClassified;
    }

    /** Weights should be normalized (i.e. sum(wts)==1) */
    public static LinearSVM train(DataSet ds, double[] wts, int featureIdx)
    {
        assert (Util.equalsF(LinAlg.normL1(wts), 1));

        Problem p = constructProblem(ds, wts, featureIdx);
        Parameter param = new Parameter(SolverType.L2R_L2LOSS_SVC_DUAL, 1, eps);

        Linear.disableDebugOutput();
        Model svm = Linear.train(p, param);

        double err = 0;
        ArrayList<Boolean> misClassified = new ArrayList<Boolean>();
        for (int i=0; i<p.x.length; ++i) {
            boolean m = (Linear.predict(svm, p.x[i]) != p.y[i]);
            misClassified.add(m);
            err += m ? wts[i] : 0;
        }

        return new LinearSVM(featureIdx, svm, misClassified, err);
    }

    public double getTrainError()
    {
        return trainingErr;
    }

    public double[] getFeatureWeights()
    {
        return svm.getFeatureWeights();
    }

    /** returns {-1,1} */
    public double predict(ArrayList<float[]> instance)
    {
        float[] x = instance.get(featureIdx);
        return Linear.predict(svm, convertToFeatureNode(x, svm.getBias()));
    }

    public boolean wasMisClassified(int instanceIndex)
    {
        return misClassified.get(instanceIndex);
    }

    /*
     * Utility functions for data format conversion
     */

    static FeatureNode[][] convertToFeatureNodes(ArrayList<float[]> x)
    {
        FeatureNode[][] nodes = new FeatureNode[x.size()][];

        for (int i=0; i<x.size(); ++i) {
            nodes[i] = convertToFeatureNode(x.get(i), 1);
        }

        return nodes;
    }

    private static FeatureNode[] convertToFeatureNode(float[] xn, double bias)
    {
        /* NOTE: we don't take advantage of any sparsity */
        FeatureNode[] nodes = new FeatureNode[xn.length+1];

        for (int j=0; j<xn.length; ++j)
            nodes[j] = new FeatureNode(j+1, xn[j]);

        /* Bias term as the last term */
        if (bias != 0) {
            nodes[xn.length] = new FeatureNode(xn.length+1, bias);
        }

        return nodes;
    }

    static Problem constructProblem(DataSet ds, double[] wts, int featureIdx)
    {
        ArrayList<float[]> features = ds.getFeatureOfInstances(featureIdx);
        FeatureNode[][] nodes = convertToFeatureNodes(features);

        int[] y = new int[ds.numInstances()];
        for (int i=0; i<y.length; ++i) {
            y[i] = ds.getLabel(i);
        }

        Problem p = new Problem();
        p.bias = 1;
        p.l = nodes.length;
        p.n = features.get(0).length + 1;
        p.x = nodes;
        p.y = y;
        p.W = wts;

        return p;
    }
}
