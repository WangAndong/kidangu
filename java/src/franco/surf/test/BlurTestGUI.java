package franco.surf.test;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import edu.emory.mathcs.jtransforms.dct.*;
import edu.emory.mathcs.jtransforms.fft.*;
import franco.surf.*;

import april.image.*;
import april.util.*;

public class BlurTestGUI
{
    public static void main(String[] args) throws IOException
    {
        final JFrame jf = new JFrame("Blur Test");
        jf.setLayout(new BorderLayout());

        final ComplexImage im = new ComplexImage(ImageIO.read(new File(args[0])));
        final FloatImage fim = im.makeFloatImage();

        final JImage jim = new JImage(fim.makeImage());
        jf.add(jim, BorderLayout.CENTER);

        ParameterGUI pg = new ParameterGUI();
        pg.addDoubleSlider("blur", "Blur radius", 0, 500, 0);
        jf.add(pg, BorderLayout.SOUTH);

        pg.addListener(new ParameterListener() {
            @Override
            public void parameterChanged(ParameterGUI pg, String name)
            {
                int W = im.getWidth();
                int H = im.getHeight();

                FloatImage gauss = GaussianImage.get(W, H, pg.gd("blur")).makeFloatImage();

                FloatFFT_2D fft = new FloatFFT_2D(W, H);
                float[] a = new float[2*fim.d.length];
                for (int i=0; i<fim.d.length; ++i)
                    a[2*i] = fim.d[i];

                Tic tic = new Tic();
                fft.complexForward(a);

                float[] b = new float[2*gauss.d.length];
                for (int i=0; i<gauss.d.length; ++i)
                    b[2*i] = gauss.d[i];

                fft.complexForward(b);
                jf.setTitle(tic.toc() + " s");

                a=b;

                float[] r = new float[a.length];
                for (int i=0; i<a.length; i+=2) {
                    r[i] = a[i]*b[i] - a[i+1]*b[i+1];
                    r[i+1] = a[i+1]*b[i] + a[i]*b[i+1];
                }

                fft.complexInverse(r, true);

                jim.setImage(new ComplexImage(W, H, r).makeFloatImage().normalize().makeImage());
                jim.repaint();
            }
        });

        jf.setSize(600,800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }
}
