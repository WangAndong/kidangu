package texture;

import hog.*;

import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

import april.image.*;
import april.util.*;


public class ExtractData
{
    public static void main(String[] args) throws IOException
    {
        for (String name : args) {
            BufferedImage im = ImageUtil.conformImageToInt(ImageIO.read(new File(name)));
            FloatImage iim = new FloatImage(im.getWidth(), im.getHeight(), FloatImage.imageToFloats(im));

            CoLevels co = new CoLevels(iim);
            printFeatures(co);
        }
    }

    static void printFeatures(CoLevels co)
    {
        final int W = co.width;
        final int H = co.height;

        int S = 16;
        for (int j=0; j<H-S; j+=S)
            for (int i=0; i<W-S; i+=S) {
                double[] m = co.getFeatures(i, j, i+S-1, j+S-1);
                System.out.printf("%.8f,%.8f,%.8f\n", m[0], m[1], m[2]);
            }
    }
}
