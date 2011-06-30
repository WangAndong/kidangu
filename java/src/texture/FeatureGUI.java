package texture;

import hog.*;

import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import april.image.*;
import april.jmat.*;


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

            BufferedImage im = model.im;
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

    public static void main(String[] args) throws IOException
    {
        JFrame jf = new JFrame();
        jf.setLayout(new BorderLayout());

        DataModel model = new DataModel();
        BufferedImage im = ImageIO.read(new File(args[0]));
        model.im = Util.copySubImage(im, 0, 1*im.getHeight()/2, im.getWidth(), im.getHeight()/2);

        ImageView imp = new ImageView(model);
        jf.add(imp, BorderLayout.CENTER);

        OutputView ov = new OutputView(model);
        jf.add(ov, BorderLayout.SOUTH);

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
    BufferedImage im;
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
        if (model.im != null) {
            g.drawImage(model.im, 0, 0, null);
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
