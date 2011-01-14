package franco.surf;

import java.awt.image.*;
import java.util.*;

import edu.emory.mathcs.jtransforms.fft.*;

import april.image.*;

public class ComplexImage implements Cloneable
{
    final int W;
    final int H;
    final float d[];


    public ComplexImage(int width, int height, float pixels[])
    {
        assert (pixels.length == 2*width*height);

        this.W = width;
        this.H = height;
        this.d = pixels;
    }

    public ComplexImage(int width, int height)
    {
        this.W = width;
        this.H = height;
        this.d = new float[2*width*height];
    }

    public ComplexImage(BufferedImage im)
    {
        this.W = im.getWidth();
        this.H = im.getHeight();
        this.d = new float[2*W*H];

        /* Convert from source format into complex values */
        switch(im.getType())
        {
        case BufferedImage.TYPE_INT_RGB:
            int rgb[] = ((DataBufferInt) (im.getRaster().getDataBuffer())).getData();

            for (int i=0; i<rgb.length; i++) {
                int r = ((rgb[i] & 0xFF0000) >>> 16);
                int g = ((rgb[i] & 0xFF00) >>> 8);
                int b = (rgb[i] & 0xFF);

                d[2*i] = clamp((0.3f*r + 0.59f*g + 0.11f*b)/255f, 0, 1);
            }
            break;

        case BufferedImage.TYPE_INT_BGR:
            int bgr[] = ((DataBufferInt) (im.getRaster().getDataBuffer())).getData();

            for (int i=0; i<bgr.length; i++) {
                int b = ((bgr[i] & 0xFF0000) >>> 16);
                int g = ((bgr[i] & 0xFF00) >>> 8);
                int r = (bgr[i] & 0xFF);

                d[2*i] = clamp((0.3f*r + 0.59f*g + 0.11f*b)/255f, 0, 1);
            }
            break;

        case BufferedImage.TYPE_BYTE_GRAY:
            byte v[] = ((DataBufferByte) (im.getRaster().getDataBuffer())).getData();

            for (int i=0; i<v.length; i++) {
                d[2*i] = clamp(v[i]/255f, 0, 1);
            }
            break;

        default:
            System.err.println("ComplexImage: Slow image conversion");

            for (int y=0; y<H; ++y) {
                for (int x=0; x<W; ++x) {
                    int c = im.getRGB(x, y);
                    int b = ((c & 0xFF0000) >>> 16);
                    int g = ((c & 0xFF00) >>> 8);
                    int r = (c & 0xFF);

                    d[2*(y*W+x)] = clamp((0.3f*r + 0.59f*g + 0.11f*b)/255f, 0, 1);
                }
            }
            break;
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        return new ComplexImage(W, H, Arrays.copyOf(d, d.length));
    }

    public int getWidth()
    {
        return W;
    }

    public int getHeight()
    {
        return H;
    }

    public float[] getData()
    {
        return d;
    }

    public ComplexImage getDFT()
    {
        float[] c = Arrays.copyOf(d, d.length);

        FloatFFT_2D fft = FFTObjectPool.getFFTObject(W, H);
        fft.complexForward(c);

        return new ComplexImage(W, H, c);
    }

    public ComplexImage getInverseDFT()
    {
        float[] c = Arrays.copyOf(d, d.length);

        FloatFFT_2D fft = FFTObjectPool.getFFTObject(W, H);
        fft.complexInverse(c, true);

        return new ComplexImage(W, H, c);
    }

    public ComplexImage convolve(ComplexImage kernel)
    {
        assert (kernel.d.length == d.length);

        float[] a = this.getDFT().getData();
        float[] b = kernel.getDFT().getData();
        float[] r = new float[d.length];

        /* multiplication of FFTs */
        for (int i=0; i<a.length; i+=2) {
            r[i] = a[i]*b[i] - a[i+1]*b[i+1];
            r[i+1] = a[i+1]*b[i] + a[i]*b[i+1];
        }

        FloatFFT_2D fft = FFTObjectPool.getFFTObject(W, H);
        fft.complexInverse(r, true);

        return new ComplexImage(W, H, r);
    }

    public FloatImage makeFloatImage()
    {
        float[] f = new float[W*H];

        for (int i=0; i<f.length; i++)
            f[i] = (float) Math.sqrt(d[2*i]*d[2*i]+d[2*i+1]*d[2*i+1]);

        return new FloatImage(W, H, f);
    }

    private static float clamp(float v, float min, float max)
    {
        if (v < min)
            return min;
        else if (v > max)
            return max;
        else
            return v;
    }
}
