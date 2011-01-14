package franco.surf;

import java.util.*;

import edu.emory.mathcs.jtransforms.fft.*;


public class FFTObjectPool
{
    static HashMap<Long, FloatFFT_2D> fft = new HashMap<Long, FloatFFT_2D>();

    public static FloatFFT_2D getFFTObject(int width, int height)
    {
        long id = ((long) width << 32) | height;
        FloatFFT_2D f = fft.get(id);

        if (f == null) {
            f = new FloatFFT_2D(width, height);
            fft.put(id, f);
        }
        return f;
    }

    public static void reset()
    {
        fft.clear();
    }
}
