package hog;

import java.awt.*;
import java.awt.image.*;

public class Util
{
    /** Prefer this over BufferedImage.getSubImage because the underlying pixel
     *  data is shared when using BufferedImage.getSubImage */
    public static BufferedImage copySubImage(BufferedImage im, int x, int y, int w, int h)
    {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        BufferedImage cropped = im.getSubimage(x, y, w, h);

        Graphics g = out.getGraphics();
        g.drawImage(cropped, 0, 0, null);
        g.dispose();

        return out;
    }

    public static void printProgress(int completed, int max)
    {
        int blocks = (int) (((float)completed/max)*40);

        System.out.print('[');

        for (int i=0; i<blocks; ++i)
            System.out.print('=');

        for (int i=blocks+1; i<40; ++i)
            System.out.print('.');

        System.out.printf(" %2d%% ]", (int)((float)completed/max*100));
    }

    static void printBlocks(int n, char c)
    {
        for (int i=0; i<c; ++i)
            System.out.print('=');
    }

    static void printBlocks(int n, char c, String prefix, String suffix)
    {
        System.out.print(prefix);
        for (int i=0; i<n; ++i)
            System.out.print('=');
        System.out.print(suffix);
    }

    /** true if two values differ by less than 1E-5 */
    public static boolean equalsF(double a, double b)
    {
        return Math.abs(a-b) < 1e-5;
    }
}
