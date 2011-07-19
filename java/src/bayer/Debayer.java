package bayer;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

import april.image.*;
import april.util.*;


public class Debayer
{
    public static void main(String[] args) throws IOException
    {
        final BufferedImage im = ImageIO.read(new File(args[0]));
        FloatImage rim = BayerUtil.maskRed(new FloatImage(im, 16));
        FloatImage gim = BayerUtil.maskGreen(new FloatImage(im, 8));
        FloatImage bim = BayerUtil.maskBlue(new FloatImage(im, 0));

        gim = MedianInterpolate.interpolateGreen(gim);
        FloatImage iim = gim;
        rim = BilateralInterpolate.interpolateRed(rim, iim);
        bim = BilateralInterpolate.interpolateBlue(bim, iim);
        gim = BilateralInterpolate.interpolateGreen(gim, bim);

        JFrame jf = new JFrame("Bayer");
        jf.setLayout(new BorderLayout());

        JImage jim = new JImage(FloatImage.makeImage(rim, gim, bim));
        jf.add(jim, BorderLayout.CENTER);

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }
}
