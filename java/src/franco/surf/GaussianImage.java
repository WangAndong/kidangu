package franco.surf;


public class GaussianImage
{
    static float Gaussian(double x, double y, double s)
    {
        double _2ss = 2.0*s*s;
        return (float) ((1/(Math.PI*_2ss)) * Math.exp((-(x*x+y*y))/_2ss));
    }

    public static ComplexImage get(int W, int H, double sigma)
    {
        float[] d = new float[2*W*H];

        for (int y=0; y<H; ++y) {
            for (int x=0; x<W; ++x) {
                d[2*(y*W+x)] = Gaussian(x-W/2.0, y-H/2.0, sigma);
            }
        }

        return new ComplexImage(W, H, d);
    }
}
