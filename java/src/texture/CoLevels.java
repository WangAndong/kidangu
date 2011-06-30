package texture;

import april.image.*;


public class CoLevels
{
    final FloatImage iim;
    final int width;
    final int height;

    /** @param iim intensity image */
    public CoLevels(FloatImage iim)
    {
        this.iim = iim;
        this.width = iim.width;
        this.height = iim.height;
    }

    /** Features returned (in order): energy entropy contrast */
    public double[] getFeatures(int x0, int y0, int x1, int y1)
    {
        int[][] co = new int[32][32];

        for(int y=y0; y<=y1; ++y)
            for(int x=x0; x<x1; ++x) {
                int p = (int) (iim.get(x, y)*255);
                int q = (int) (iim.get(x+1, y)*255);

                ++co[p/8][q/8];
                ++co[q/8][p/8];
            }

        final double Z = 1024; /* Bogo normalization */

        double energy = 0;
        double entropy = 0;
        double contrast = 0;

        for (int j=0; j<32; ++j)
            for (int i=0; i<32; ++i) {
                double p = co[i][j] / Z;
                energy += p*p;
                entropy += (p==0) ? 0 : -p*Math.log(p);
                contrast += (i-j)*(i-j)*p;
            }

        return new double[] {energy, entropy, contrast};
    }
}
