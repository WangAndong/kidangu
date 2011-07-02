package distort.gen;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.*;
import javax.swing.*;

import lcm.lcm.*;

import april.jmat.*;
import april.lcmtypes.*;
import april.util.*;
import april.vis.*;


public class GenerativeProjection implements LCMSubscriber
{
    JFrame jf;
    JImage jim;
    Model model;

    public GenerativeProjection(String modelFile) throws IOException
    {
        BufferedImage mim = ImageIO.read(new File(modelFile));
        model = new Model(ImageUtil.conformImageToInt(mim));

        jf = new JFrame();
        jf.setLayout(new BorderLayout());

        jim = new JImage();
        jf.add(jim, BorderLayout.NORTH);

        VisCanvas vc = new VisCanvas(new VisWorld());
        jf.add(vc, BorderLayout.SOUTH);

        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(640, 480);
        jf.setVisible(true);

        LCM.getSingleton().subscribe("HOMOGRAPHY", this);
    }

    @Override
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        try {
            raw_t raw = new raw_t(ins);
            DataInputStream dins = new DataInputStream(new ByteArrayInputStream(raw.buf));

            double[][] h = new double[3][3];
            for (int i=0; i<3; ++i)
                for (int j=0; j<3; ++j)
                    h[i][j] = dins.readDouble();

            System.out.println("Generating image ...");
            BufferedImage im = project(new Homography(h));
            jim.setImage(im);
            jim.repaint();
            jf.invalidate();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    BufferedImage project(Homography h)
    {
        /* Project points from the model into image space */
        ArrayList<double[]> proj = new ArrayList<double[]>();
        for (double[] xyc : model.getSamples()) {
            double[] pxy = LinAlg.resize(h.project(xyc[0], xyc[1]), 3);
            pxy[2] = xyc[2];
            proj.add(pxy);
        }

        /* Grid the projected points for efficient search */
        final double x0 = -600, y0 = -600, x1 = 600, y1 = 600;
        Gridder<double[]> grid = new Gridder<double[]>(x0, y0, x1, y1, 2);

        for (double[] v : proj) {
            double[] xyrgb = new double[5];
            xyrgb[0] = v[0];
            xyrgb[1] = v[1];

            int c = (int) v[2];
            xyrgb[2] = ((c & 0xFF0000) >>> 16) / 255.0;
            xyrgb[3] = ((c & 0xFF00) >>> 8) /255.0;
            xyrgb[4] = (c & 0xFF) / 255.0;

            grid.add(v[0], v[1], xyrgb);
        }

        /* Interpolate the resulting image */
        BufferedImage im = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
        final int W = im.getWidth();
        final int H = im.getHeight();

        for (int y = -H/2; y < H/2; ++y)
            for (int x = -W/2; x < W/2; ++x) {
                Iterable<double[]> nbors = grid.find(x, y, 2);
                im.setRGB(x+W/2, y+H/2, interpolate(new double[] {x,y}, nbors, 2));
            }

        return im;
    }

    static int interpolate(double[] xy, Iterable<double[]> neighbors, double range)
    {
        double sumWt = 0;
        double[] rgb = new double[3];

        for (double[] xyrgb : neighbors) {
             double z = LinAlg.squaredDistance(xy, xyrgb) / (range*range);
             double wt = Math.exp(-z);
             LinAlg.plusEquals(rgb, LinAlg.scale(LinAlg.copy(xyrgb, 2, 3), wt));

             sumWt += wt;
        }

        if (sumWt < 1e-8) {
            return Color.TRANSLUCENT;
        }

        rgb = LinAlg.scale(rgb, 1/sumWt);
        return new Color((float)rgb[0], (float)rgb[1], (float)rgb[2]).getRGB();
    }

    public static void main(String[] args) throws IOException
    {
        new GenerativeProjection(args[0]);
    }
}

class Model
{
    final BufferedImage im;

    final double width = 0.2;
    final double height = 0.2;

    public Model(BufferedImage im)
    {
        this.im = im;
    }

    public List<double[]> getSamples()
    {
        final int W = im.getWidth();
        final int H = im.getHeight();
        final int pixels[] = ((DataBufferInt) (im.getRaster().getDataBuffer())).getData();

        ArrayList<double[]> samples = new ArrayList<double[]>();

        for (int y = 0; y < H; ++y)
            for (int x = 0; x < W; ++x) {
                double[] xy = xform(x, y);
                double[] pxyc = LinAlg.resize(xy, 3);
                pxyc[2] = pixels[y*W+x] | (0xFF << 24);
                samples.add(pxyc);
            }

        return samples;
    }

    double[] xform(double x, double y)
    {
        final double W = im.getWidth();
        final double H = im.getHeight();

        return new double[] {(x-W/2)/W * width, (y-H/2)/H * height};
    }
}

class Homography
{
    final double[][] h;

    public Homography(double[][] h)
    {
        this.h = h;
    }

    public double[] project(double worldx, double worldy)
    {
        double ixy[] = new double[2];
        ixy[0] = h[0][0] * worldx + h[0][1] * worldy + h[0][2];
        ixy[1] = h[1][0] * worldx + h[1][1] * worldy + h[1][2];
        double z = h[2][0] * worldx + h[2][1] * worldy + h[2][2];
        ixy[0] = ixy[0] / z;
        ixy[1] = ixy[1] / z;
        return ixy;
    }
}
