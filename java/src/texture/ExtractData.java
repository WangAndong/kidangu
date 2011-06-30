package texture;

import hog.*;

import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

import cluster.*;

import april.image.*;
import april.jmat.*;
import april.util.*;


public class ExtractData
{
    public static void main(String[] args) throws IOException
    {
        ArrayList<double[]> data = new ArrayList<double[]>();

        /* Sample instances from input images */
        System.out.println("Sampling instances ...");

        for (String name : args) {
            BufferedImage im = ImageUtil.conformImageToInt(ImageIO.read(new File(name)));
            im = Util.copySubImage(im, 0, 1*im.getHeight()/2, im.getWidth(), im.getHeight()/2);
            FloatImage iim = new FloatImage(im.getWidth(), im.getHeight(), FloatImage.imageToFloats(im));

            CoLevels co = new CoLevels(iim);
            data.addAll(sampleInstances(co));
        }

        data = normalize(data);

        ArrayList<double[]> means = new ArrayList<double[]>();
        Random r = new Random();
        for (int i=0; i<20; ++i)
            means.add(data.get(r.nextInt(data.size())));

        /* Cluster the instances using the K-Means algorithm first */
        System.out.println("Estimating cluster means ...");

        KMeansClusterer kmc = new KMeansClusterer(data, means);
        means = kmc.estimateCenters();

        GaussianMixture gmm = new GaussianMixture(data, means);
        ArrayList<MultiGaussian> mix = gmm.estimate();

        /* We have the cluster centers now */
        System.out.println("Clusters: ");

        for (MultiGaussian g : mix) {
            LinAlg.printTranspose(g.getMean());
            LinAlg.print(g.getCovariance().copyArray());
            System.out.println();
        }
    }

    static ArrayList<double[]> normalize(ArrayList<double[]> data)
    {
        final int dim = data.get(0).length;
        final int N = data.size();

        double[] Ex = new double[dim];
        double[] Ex2 = new double[dim];

        for (int i=0; i<N; ++i) {
            final double[] z = data.get(i);
            LinAlg.plusEquals(Ex, LinAlg.scale(z, 1.0/N));
            LinAlg.plusEquals(Ex2, LinAlg.scale(sq(z), 1.0/N));
        }

        double[] sigma = sqrt(LinAlg.subtract(Ex2, sq(Ex)));
        ArrayList<double[]> normalized = new ArrayList<double[]>();
        for (double[] v : data) {
            double[] z = LinAlg.subtract(v, Ex);
            for (int i=0; i<dim; ++i)
                z[i] /= sigma[i];
            normalized.add(z);
        }

        return normalized;
    }

    static double[] sq(double[] v)
    {
        double[] r = new double[v.length];
        for (int i=0; i<v.length; ++i)
            r[i] = v[i]*v[i];

        return r;
    }

    static double[] sqrt(double[] v)
    {
        double[] r = new double[v.length];
        for (int i=0; i<v.length; ++i)
            r[i] = Math.sqrt(v[i]);

        return r;
    }

    static ArrayList<double[]> sampleInstances(CoLevels co)
    {
        final int W = co.width;
        final int H = co.height;

        int S = 16;
        Random rx = new Random();
        Random ry = new Random();

        ArrayList<double[]> data = new ArrayList<double[]>();
        for (int n=0; n<300; ++n) {
            int i = rx.nextInt(W-S);
            int j = ry.nextInt(H-S);

            double[] m = co.getFeatures(i, j, i+S-1, j+S-1);
            data.add(m);
        }

        return data;
    }
}
