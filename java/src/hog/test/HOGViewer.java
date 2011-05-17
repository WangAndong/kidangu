package hog.test;

import hog.*;
import hog.HOGBlocks.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import april.jmat.*;
import april.util.*;
import april.vis.*;

public class HOGViewer
{
    public static void main(String[] args) throws IOException
    {
        JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        final VisCanvas vc = new VisCanvas(new VisWorld());
        vc.setBackground(Color.black);
        jf.add(vc, BorderLayout.CENTER);

        final BufferedImage oim = ImageIO.read(new File(args[0]));
        final int oH = oim.getHeight();
        final int oW = oim.getWidth();

        final BufferedImage im = Util.copySubImage(oim, oW/2-32, oH/2-64, 64, 128);
        final int H = im.getHeight();
        final int W = im.getWidth();

        ParameterGUI pg = new ParameterGUI();
        pg.addIntSlider("bsize", "lg2(Block Size)", 2, 6, 3);
        pg.addDoubleSlider("contrast", "contrast", 0, 50, 10);
        pg.addIntSlider("alpha", "Image alpha", 0, 255, 10);
        pg.addBoolean("fnorm", "Normalize", true);
        jf.add(pg, BorderLayout.SOUTH);

        final MyParamListener listener = new MyParamListener(vc, im, H, W);
        pg.addListener(listener);

        jf.setSize(800, 800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);

        listener.parameterChanged(pg, "bsize");
    }

    private static final class MyParamListener implements ParameterListener
    {
        private final VisCanvas vc;
        private final BufferedImage im;
        private final int h;
        private final int w;

        private MyParamListener(VisCanvas vc, BufferedImage im, int h, int w)
        {
            this.vc = vc;
            this.im = im;
            this.h = h;
            this.w = w;
        }

        @Override
        public void parameterChanged(ParameterGUI pg, String name)
        {
            /* Show image */
            final VisWorld.Buffer vbim = vc.getWorld().getBuffer("image");
            VisDataGrid vdg = new VisDataGrid(0, 0, w, h, 1, 1, true);

            final int ALPHA = pg.gi("alpha");
            for (int y = 0; y < h; ++y)
                for (int x = 0; x < w; ++x) {
                    Color c = ColorUtil.setAlpha(new Color(im.getRGB(x, y)), ALPHA);
                    vdg.set(x, y, 0, c);
                }

            vbim.addBuffered(vdg);
            vbim.switchBuffer();

            /* Show HOG quivers */
            ArrayList<DescriptorInfo> dinfo = new ArrayList<DescriptorInfo>();
            int BSIZE = 1 << pg.gi("bsize");
            for (int j=0; j<h; j+=BSIZE)
                for (int i=0; i<w; i+=BSIZE) {
                    dinfo.add(new DescriptorInfo(i, j, BSIZE, BSIZE));
                }

            ArrayList<float[]> descs = HOGBlocks.getDescriptors(im, dinfo, pg.gb("fnorm"));
            final VisWorld.Buffer vb = vc.getWorld().getBuffer("content");
            ColorMapper cm = ColorMapper.makeGray(0, pg.gd("contrast"));

            for (int i=0 ;i<dinfo.size(); ++i) {
                DescriptorInfo inf = dinfo.get(i);
                double[] desc = LinAlg.copyDoubles(descs.get(i));
                double[] hog1 = LinAlg.select(desc, 0, desc.length/4-1);
                double[] hog2 = LinAlg.select(desc, desc.length/4, 2*desc.length/4-1);
                double[] hog3 = LinAlg.select(desc, 2*desc.length/4, 3*desc.length/4-1);
                double[] hog4 = LinAlg.select(desc, 3*desc.length/4, desc.length-1);

                double w4 = inf.width/4.0;
                double h4 = inf.height/4.0;

                vb.addBuffered(new VisQuiver(inf.x + w4,   inf.y + h4,   BSIZE/4, hog1, cm));
                vb.addBuffered(new VisQuiver(inf.x + 3*w4, inf.y + h4,   BSIZE/4, hog2, cm));
                vb.addBuffered(new VisQuiver(inf.x + w4,   inf.y + 3*h4, BSIZE/4, hog3, cm));
                vb.addBuffered(new VisQuiver(inf.x + 3*w4, inf.y + 3*h4, BSIZE/4, hog4, cm));
            }

            vb.switchBuffer();
        }
    }
}

class VisQuiver extends VisChain
{
    public VisQuiver(double x, double y, double r, double[] hog, ColorMapper cm)
    {
        double[] begin = new double[] {x, y, 0.1};
        int N = hog.length;

        for (int i=0; i<hog.length; ++i) {
            double theta = (2*Math.PI * i/N) - Math.PI;
            double[] end = new double[] {x+r*Math.cos(theta), y+r*Math.sin(theta), 0.1};
            final Color color = cm.mapColor(hog[i]);

            this.add(new VisLineSegment(begin, end, color));
        }
    }
}
