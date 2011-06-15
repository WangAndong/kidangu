package hog.test;

import hog.*;
import hog.HOGBlocks.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import april.config.*;
import april.jmat.*;
import april.util.*;


class ClassifierTest
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 2) {
            System.out.println("USAGE: hog.test.ClassifierTest <config> <img>");
            System.exit(-1);
        }

        Config cfg = new ConfigFile(args[0]);
        Cascade cascade = new Cascade(cfg.getChild("cascade"));

        BufferedImage im = ImageIO.read(new File(args[1]));

//        /* Get the centered 64x128 pixels */
//        int W = im.getWidth();
//        int H = im.getHeight();
//        im = Util.copySubImage(im, W/2-32, H/2-64, 64, 128);

        HOGBlocks hogBlocks = new HOGBlocks(im);

        ArrayList<int[]> rects = new ArrayList<int[]>();
        for (int j=0; j<im.getHeight()-128; ++j) {
            for (int i=0; i<im.getWidth()-64; ++i) {
                if (cascade.predict(hogBlocks, i, j) == 1) {
                    rects.add(new int[] {i, j, 64, 128});
                    System.out.println("" + i + "," + j);
                }
            }
        }

        Graphics2D g = im.createGraphics();
        g.setColor(Color.green);
        for (int[] r : rects) {
            g.drawRect(r[0], r[1], 64, 128);
        }
        g.dispose();

        JFrame jf = new JFrame();
        jf.add(new JImage(im));
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(800, 800);
        jf.setVisible(true);
    }
}

class Cascade
{
    ArrayList<Level> levels = new ArrayList<Level>();

    public Cascade(Config cfg)
    {
        final int nlevels = cfg.requireInt("nlevels");
        for (int i=1; i<=nlevels; ++i) {
            levels.add(new Level(cfg.getChild("level" + i)));
        }
    }

    public double predict(HOGBlocks hogBlocks, int x, int y)
    {
        for (Level l: levels) {
            if (l.predict(hogBlocks, x, y) == -1)
                return -1;
        }

        return 1;
    }
}

class Level
{
    final double bias;
    ArrayList<Classifier> classifiers = new ArrayList<Classifier>();

    public Level(Config cfg)
    {
        this.bias = cfg.requireDouble("bias");

        for (String key : cfg.getKeys()) {
            if (!key.startsWith("svm"))
                continue;

            classifiers.add(new Classifier(cfg.getChild(key)));
        }
    }

    public double predict(HOGBlocks hogBlocks, int x, int y)
    {
        double thresh = 0;
        double vote = 0;

        for (Classifier c : classifiers) {
            double p = c.predict(hogBlocks, x, y);
            vote += p*c.weight;
            thresh += c.weight;
        }

        return vote >= thresh + bias ? 1 : -1;
    }
}

class Classifier
{
    final double weight;
    final int featureIndex;
    final int[] labels;
    final double[] hyperplane;

    public Classifier(Config cfg)
    {
        this.weight = cfg.requireDouble("weight");
        this.featureIndex = cfg.requireInt("featureIndex");
        this.hyperplane = cfg.requireDoubles("plane");
        this.labels = cfg.requireInts("labels");
    }

    public double predict(HOGBlocks hogBlocks, int x, int y)
    {
        // TODO: Can cache descriptors for performance. The
        // featureIndex can be repeated in a Cascade.
        final DescriptorInfo d = PedDetector.descriptorInfo.get(featureIndex);
        float[] f = hogBlocks.getDescriptor( x+d.x, y+d.y, d.width, d.height);

        double proj = LinAlg.dotProduct(LinAlg.copyDoubles(f), hyperplane);
        return proj > 0 ? labels[0] : labels[1];
    }
}
