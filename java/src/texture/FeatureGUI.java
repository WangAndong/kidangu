package texture;

import hog.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import april.image.*;
import april.util.*;


public class FeatureGUI
{
    private static final class SelectionController extends MouseAdapter
    {
        final DataModel model;
        MouseEvent start;

        public SelectionController(DataModel model)
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
            double[] v = co.getFeatures(sel.x, sel.y, sel.x+sel.width, sel.y+sel.height);
            for (double d : v)
                sb.append(String.format("%.4f, ", d));
            model.appendOutput(sb.toString() + "\n");

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
        pg.addButtons(">>", ">>", "classify", "Classify");
        pg.addListener(new ParameterListener() {
            ImageStore store = new ImageStore(args, model);

            public void parameterChanged(ParameterGUI pg, String name) {
                if (name.equals(">>")) {
                    final BufferedImage rim = store.getRandomImage();
                    model.setImage(Util.copySubImage(rim, 0, 2*rim.getHeight()/3, rim.getWidth(), rim.getHeight()/3));
                } else {
                    BufferedImage im = model.getImage();
                    FloatImage iim = new FloatImage(im.getWidth(), im.getHeight(), FloatImage.imageToFloats(im));
                    CoLevels co = new CoLevels(iim);

                    Graphics g = im.getGraphics();
                    for (int y=0; y<im.getHeight()-16; y+=16)
                        for (int x=0; x<im.getWidth()-16; x+=16) {
                            double[] m = co.getFeatures(x, y, x+15, y+15);
                            if (m[2] > 2)
                                g.setColor(Color.red);
                            else
                                g.setColor(Color.green);

                            g.drawRect(x+2, y+2, 14, 14);
                        }

                    g.dispose();
                    model.setImage(im);
                }
            }
        });
        jf.add(pg, BorderLayout.NORTH);

        final SelectionController sc = new SelectionController(model);
        imp.addMouseListener(sc);
        imp.addMouseMotionListener(sc);

        jf.setSize(350, 500);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
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
    DataModel model;

    public ImageStore(String[] files, DataModel model)
    {
        this.files = files;
        this.model = new DataModel();
    }

    public BufferedImage getRandomImage()
    {
        BufferedImage im = null;

        try {
            im = ImageIO.read(new File(files[rand.nextInt(files.length)]));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return im;
    }
}
