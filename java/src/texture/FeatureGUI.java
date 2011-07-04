package texture;

import hog.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import libsvm.*;

import april.image.*;
import april.jmat.*;
import april.util.*;

import magic.camera.util.color.*;


public class FeatureGUI
{
    public static void main(final String[] args) throws IOException
    {
        JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        final DataModel model = new DataModel();
        BufferedImage im = ImageIO.read(new File(args[0]));
        model.setImage(Util.copySubImage(im, 0, 2*im.getHeight()/3, im.getWidth(), im.getHeight()/3));

        ImageView imp = new ImageView(model);
        jf.add(imp, BorderLayout.CENTER);

        OutputView ov = new OutputView(model);
        jf.add(ov, BorderLayout.SOUTH);

        ParameterGUI pg = new ParameterGUI();
        pg.addButtons("Random", "Random");
        pg.addButtons("classify", "Classify", "CrCb", "CrCb");
        pg.addButtons("Play", "Play", "Step >", "Step >", "< Step", "< Step", "Reset", "Reset");
        pg.addListener(new ParamListener(model, args));
        jf.add(pg, BorderLayout.NORTH);

        final SelectionListener sc = new SelectionListener(model);
        imp.addMouseListener(sc);
        imp.addMouseMotionListener(sc);

        jf.setSize(350, 500);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }

    private static final class ParamListener implements ParameterListener
    {
        private final DataModel model;
        ImageStore store;

        private ParamListener(DataModel model, String[] args)
        {
            this.model = model;
            store = new ImageStore(args, model);
        }

        public void parameterChanged(ParameterGUI pg, String name) {
            if (name.equals("Random")) {
                final BufferedImage rim = store.getRandom();
                model.setImage(Util.copySubImage(rim, 0, 2*rim.getHeight()/3, rim.getWidth(), rim.getHeight()/3));

            } else if (name.equals("classify")) {
                BufferedImage im = model.getImage();
                model.setImage(labelImage(im));

            } else if (name.equals("CrCb")) {
                BufferedImage src = model.getImage();

                final int W = src.getWidth();
                final int H = src.getHeight();
                BufferedImage dest = new BufferedImage(W, H, src.getType());

                final int in[] = ((DataBufferInt) (src.getRaster().getDataBuffer())).getData();
                final int out[] = ((DataBufferInt) (dest.getRaster().getDataBuffer())).getData();

                for (int y = 0; y < H; ++y) {
                    for (int x = 0; x < W; ++x) {
                        float[] YCbCr = ColorSpace.RGBtoYCbCr(in[y*W + x]);
                        out[y*W + x] = ColorSpace.YCbCrtoRGB(0.5f, YCbCr[1], YCbCr[2]);
                    }
                }

                model.setImage(dest);

            } else if (name.equals("Play")) {
                new MovieThread(store, model).start();

            } else if (name.equals("Step >")) {
                BufferedImage im = store.getNext();
                if (im != null)
                    model.setImage(labelImage(im, 2*im.getHeight()/4));

            } else if (name.equals("< Step")) {
                BufferedImage im = store.getPrevious();
                model.setImage(im);

            } else if (name.equals("Reset")) {
                store.reset();
            }
        }
    }

    private static final class SelectionListener extends MouseAdapter
    {
        final DataModel model;
        MouseEvent start;

        public SelectionListener(DataModel model)
        {
            this.model = model;
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            start = e;
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            if (start == null)
                return;

            setSelection(start.getPoint(), e.getPoint());
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (start == null)
                return;

            setSelection(start.getPoint(), e.getPoint());

            BufferedImage im = model.getImage();
            FloatImage iim = new FloatImage(im.getWidth(), im.getHeight(), FloatImage.imageToFloats(im));

            CoLevels co = new CoLevels(iim);
            Rectangle sel = model.selection;

            StringBuilder sb = new StringBuilder();
            sb.append("{ ");

            double[] v = co.getFeatures(sel.x, sel.y, sel.x+sel.width, sel.y+sel.height);
            for (double d : v)
                sb.append(String.format("%.4f, ", d));

            YCrCbImage cim = new YCrCbImage(im);
            v = cim.getAverageColor(sel.x, sel.y, sel.x+sel.width, sel.y+sel.height);
            for (double d : v)
                sb.append(String.format("%.4f, ", d));

            model.appendOutput(sb.toString() + "},\n");

            start = null;
        }

        void setSelection(Point p, Point q)
        {
            int x = p.x;
            int y = p.y;
            int width = q.x - p.x;
            int height = q.y - p.y;

            if (width < 0) {
                x = x + width;
                width = -width;
            }

            if (height < 0) {
                y = y + height;
                height = -height;
            }

            width = 16;
            height = 16;

            model.setSelection(new Rectangle(x, y, width, height));
        }
    }

    static class MovieThread extends Thread
    {
        final DataModel model;
        final ImageStore store;

        public MovieThread(ImageStore ims, DataModel m)
        {
            this.model = m;
            this.store = ims;
        }

        @Override
        public void run()
        {
            while (true) {
                BufferedImage im = store.getNext();
                if (im == null)
                    return;

                model.setImage(labelImage(im, 2*im.getHeight()/4));
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static BufferedImage labelImage(BufferedImage im)
    {
        return labelImage(im, 0);
    }

    static BufferedImage labelImage(BufferedImage im, int startY)
    {
        FloatImage iim = new FloatImage(im.getWidth(), im.getHeight(), FloatImage.imageToFloats(im));
        CoLevels co = new CoLevels(iim);
        YCrCbImage cim = new YCrCbImage(im);

        DataNormalizer dn = new DataNormalizer(
                new double[] { 0.0024, 0.8204, 0.0801, 0.1996, 0.0475, -0.1752, },
                new double[] { 0.1640, 3.0033, 11.6133, 0.8483, 0.3341, -0.0475, }
                );

        Graphics g = im.getGraphics();
        for (int y=startY; y<im.getHeight()-16; y+=16)
            for (int x=0; x<im.getWidth()-16; x+=16) {
                double[] texture = co.getFeatures(x, y, x+15, y+15);
                double[] color = cim.getAverageColor(x, y, x+15, y+15);

                double[] f = LinAlg.resize(texture, texture.length + color.length);
                f = dn.normalize(f);

                for (int i=0; i<color.length; ++i)
                    f[texture.length + 1] = color[i];

                if (Classifier.predict(f) < 0)
                    g.setColor(Color.orange);
                else
                    g.setColor(Color.cyan);

                g.drawRect(x+2, y+2, 14, 14);
            }

        g.dispose();
        return im;
    }
}

class DataModel
{
    StringBuilder output = new StringBuilder();
    private BufferedImage im;
    Rectangle selection;

    ArrayList<Listener> listeners = new ArrayList<Listener>();

    public void appendOutput(String s)
    {
        output.append(s);
        for (Listener l : listeners)
            l.outputChanged();
    }

    public void setSelection(Rectangle r)
    {
        this.selection = r;
        for (Listener l : listeners)
            l.selectionChanged();
    }

    public void setImage(BufferedImage im)
    {
        this.im = im;
        for (Listener l : listeners)
            l.imageChanged();
    }

    public BufferedImage getImage()
    {
        return im;
    }

    public static interface Listener
    {
        public void outputChanged();
        public void selectionChanged();
        public void imageChanged();
    }
}

class ImageView extends JPanel implements DataModel.Listener
{
    final DataModel model;

    public ImageView(DataModel m)
    {
        this.model = m;
        model.listeners.add(this);
    }

    @Override
    public void paint(Graphics g)
    {
        if (model.getImage() != null) {
            g.drawImage(model.getImage(), 0, 0, null);
        }
        if (model.selection != null) {
            Rectangle sel = model.selection;

            g.setColor(Color.green);
            g.drawRect(sel.x, sel.y, sel.width, sel.height);

            g.setColor(Color.yellow.darker());
            g.fillOval(sel.x-3, sel.y-3, 7, 7);
            g.fillOval(sel.x+sel.width-3, sel.y+sel.height-3, 7, 7);

            g.setColor(Color.yellow);
            g.fillOval(sel.x-2, sel.y-2, 3, 3);
            g.fillOval(sel.x+sel.width-2, sel.y+sel.height-2, 3, 3);
        }
    }

    @Override
    public void imageChanged()
    {
        this.repaint();
    }

    @Override
    public void outputChanged()
    {
    }

    @Override
    public void selectionChanged()
    {
        this.repaint();
    }
}

class OutputView extends TextArea implements DataModel.Listener
{
    final DataModel model;

    public OutputView(DataModel m)
    {
        this.model = m;
        this.model.listeners.add(this);
    }

    @Override
    public void imageChanged()
    {
    }

    @Override
    public void outputChanged()
    {
        this.setText(model.output.toString());
        this.repaint();
    }

    @Override
    public void selectionChanged()
    {
    }
}

class ImageStore
{
    String[] files;
    Random rand = new Random();
    int currentIndex = 0;

    DataModel model;

    public ImageStore(String[] files, DataModel model)
    {
        this.files = files;
        this.model = new DataModel();
    }

    public void reset()
    {
        currentIndex = 0;
    }

    public BufferedImage getPrevious()
    {
        if (currentIndex > 0) {
            --currentIndex;
        }

        return getCurrent();
    }

    public BufferedImage getNext()
    {
        if (currentIndex < files.length-1) {
            ++currentIndex;
            return getCurrent();
        }

        return null;
    }

    public BufferedImage getCurrent()
    {
        try {
            BufferedImage im = ImageIO.read(new File(files[currentIndex]));
            im = ImageUtil.conformImageToInt(im);
            return im;
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }

    public BufferedImage getRandom()
    {
        currentIndex = rand.nextInt(files.length);
        return getCurrent();
    }
}

class Classifier
{
    static svm_model model;

    static {
        try {
            model = svm.svm_load_model("/home/rpradeep/studio/libsvm-3.0/tools/texture.model");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    static public double predict(double[] x)
    {
        svm_node[] nodes = new svm_node[x.length];
        for (int i=0; i<x.length; i++) {
            nodes[i] = new svm_node();
            nodes[i].index = i+1;
            nodes[i].value = x[i];
        }

        return svm.svm_predict(model, nodes);
    }
}
