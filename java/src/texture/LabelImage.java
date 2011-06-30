package texture;

import hog.*;

import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

import april.image.*;
import april.jmat.*;
import april.util.*;


public class LabelImage
{
    static final double[] pi = new double[] { 0.0755563408936634, 0.835491160441368, 0.0888865530041938 };

    static final double[][] mu = new double[][] {
            { 0.00449504301034074, 2.45927075708645, 7.77890444604994, },
            { 0.00710786848476895, 2.19493779976828, 2.33950045018019, },
            { 0.0429003362796575, 1.386147376258, 0.321622260237665, },
    };

    static final double[][][] sigma = new double[][][] {
            { { 18.1720663672776, 0.109605981318401, 0.00510777018174265, },
                    { 0.109605981318401, 11.6309572490123, -0.273970374274359, },
                    { 0.00510777018174265, -0.273970374274359, 0.235393369502977, },
            },
            { { 190.86143285805, 3.25510467762875, -0.0573722740170464, },
                    { 3.25510467762875, 49.9201867955938, -7.7052026862445, },
                    { -0.0573722740170464, -7.7052026862445, 1.75032890072936, },
            },
            { { 21.0026809310123, 1.28092063266654, 0.283503507409221, },
                    { 1.28092063266654, 9.45349393058473, -4.36543802243189, },
                    { 0.283503507409221, -4.36543802243189, 14.8257996298491, },
            },
    };

    static final MultiGaussian[] gmm = new MultiGaussian[] {
            new MultiGaussian(sigma[0], mu[0]),
            new MultiGaussian(sigma[1], mu[1]),
            new MultiGaussian(sigma[2], mu[2]),
    };

    public static double[] getMembership(double[] x)
    {
        double[] m = new double[pi.length];

        for (int i = 0; i < m.length; ++i)
            m[i] = gmm[i].prob(x);

        return m;
    }

    public static void main(String[] args) throws IOException
    {
        for (String arg : args) {
            System.out.println(arg + "\n");

            BufferedImage im = ImageUtil.conformImageToInt(ImageIO.read(new File(arg)));
            FloatImage iim = new FloatImage(im.getWidth(), im.getHeight(), FloatImage.imageToFloats(im));
            CoLevels co = new CoLevels(iim);

            final int W = co.width;
            final int H = co.height;

            int S = 16;
            for (int j = 0; j < H - S; j += S) {
                for (int i = 0; i < W - S; i += S) {
                    double[] v = co.getFeatures(i, j, i + S - 1, j + S - 1);
                    double[] m = getMembership(v);
                    System.out.printf("%d ", LinAlg.maxIdx(m));
                }
                System.out.println();
            }

            System.out.println();
        }
    }
}
