package bayer;

import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

import april.graph.*;
import april.image.*;


public class MixtureDebayer
{
    public static void main(String[] args) throws IOException
    {
        final BufferedImage im = ImageIO.read(new File(args[0]));
        FloatImage rim = BayerUtil.maskRed(new FloatImage(im, 16));
        FloatImage gim = BayerUtil.maskGreen(new FloatImage(im, 8));
        FloatImage bim = BayerUtil.maskBlue(new FloatImage(im, 0));
    }
}
