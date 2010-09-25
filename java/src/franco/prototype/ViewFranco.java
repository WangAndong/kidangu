package franco.prototype;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

import lcm.lcm.*;
import lcm.logging.*;
import lcm.logging.Log.Event;
import april.config.*;
import april.jmat.*;
import april.lcmtypes.*;
import april.util.*;
import april.velodyne.*;
import april.viewer.*;
import april.vis.*;

/*
 *
 * This is experimental code and not designed for readability or maintenance.
 * I plan to rewrite this stuff once the algorithms have been finalized.
 *
 */

/** Views velodyne data. **/
public class ViewFranco implements ViewObject, LCMSubscriber
{
    Viewer viewer;
    String name;
    Config config;

    LCM lcm = LCM.getSingleton();
    PoseTracker pt = PoseTracker.getSingleton();
    String channel;

    double spos[], squat[];
    final static VelodyneCalibration calib = VelodyneCalibration.makeMITCalibration();

    ImageRepository imgRepository;
    ClassificationLabelStore labelStore;
    BufferedImage imgCrossingSignal;
    State state = new State();
    Statistics statistics;

    final static ColorMapper colorMap = ColorMapper.makeJetWhite(-3, 7);

    final static boolean MAKE_MOVIE = false;

    private final class MyVisCanvasEventHandler extends VisCanvasEventAdapter
    {
        @Override
        public boolean keyTyped(VisCanvas vc, KeyEvent e)
        {
            if (e.getKeyChar() == 'c') {
                state.intersectionLineObjectCollection.clear();
                final double PEG_HEIGHT = 1.5;

                final double[] firstIntersectionPoint = state.intersectionPoints.get(0);
                final double[] lastIntersectionPoint = state.intersectionPoints
                        .get(state.intersectionPoints.size() - 1);
                final double[] medianIntersectionPoint = state.intersectionPoints
                        .get(state.intersectionPoints.size() / 2);

                final double paralellogramWidth = 10;

                final double[] bottomLeft = new double[] { firstIntersectionPoint[0] - paralellogramWidth / 2,
                        firstIntersectionPoint[1] - (state.bestSlope * -paralellogramWidth / 2) };
                final double[] bottomRight = new double[] { firstIntersectionPoint[0] + paralellogramWidth / 2,
                        firstIntersectionPoint[1] - (state.bestSlope * paralellogramWidth / 2) };
                final double[] topLeft = new double[] { lastIntersectionPoint[0] - paralellogramWidth / 2,
                        lastIntersectionPoint[1] - (state.bestSlope * -paralellogramWidth / 2) };
                final double[] topRight = new double[] { lastIntersectionPoint[0] + paralellogramWidth / 2,
                        lastIntersectionPoint[1] - (state.bestSlope * paralellogramWidth / 2) };

                Color intersectionColor = new Color(255, 0, 0);
                state.intersectionLineObjectCollection.add(new VisLineSegment(bottomLeft[0], bottomLeft[1], PEG_HEIGHT,
                        bottomRight[0], bottomRight[1], PEG_HEIGHT, intersectionColor.darker(), 4));
                state.intersectionLineObjectCollection.add(new VisLineSegment(topLeft[0], topLeft[1], PEG_HEIGHT,
                        topRight[0], topRight[1], PEG_HEIGHT, intersectionColor.brighter(), 4));

                final int[] medianIntersectionPointOrientation = new int[] {
                        geometricCounterClockWise(bottomLeft, bottomRight, medianIntersectionPoint),
                        geometricCounterClockWise(topLeft, topRight, medianIntersectionPoint) };

                ArrayList<double[]> interiorPoints = new ArrayList<double[]>();

                for (double x = Nearest(bottomRight[0], 0.25); x >= Nearest(topLeft[0], 0.25); x -= 0.25) {
                    for (double y = Nearest(bottomLeft[1], Statistics.BIN_WIDTH); y <= Nearest(bottomRight[1],
                            Statistics.BIN_WIDTH); y += Statistics.BIN_WIDTH) {

                        final int[] pointOrientation = new int[] {
                                geometricCounterClockWise(bottomLeft, bottomRight, new double[] { x, y }),
                                geometricCounterClockWise(topLeft, topRight, new double[] { x, y }) };

                        // Check if point within parallelogram
                        if ((pointOrientation[0] == medianIntersectionPointOrientation[0])
                                && (pointOrientation[1] == medianIntersectionPointOrientation[1]))
                            ;
                        else
                            continue;

                        interiorPoints.add(new double[] { x, y });
                    }
                }

                double maxDeviation = Double.MIN_VALUE;
                double dangerX = Double.POSITIVE_INFINITY;
                double dangerY = Double.POSITIVE_INFINITY;
                double[][] dangerLine = null;

                final Color dangerColor = new Color(128, 128, 128);
                for (double[] p : interiorPoints) {
                    for (double[] q : interiorPoints) {
                        if (euclideanDistance(p, q) > 15 && euclideanDistance(p, q) < 18) {
                            final double slope = (q[1] - p[1]) / (q[0] - p[0]);
                            final double pz = state.worldModel.get("" + p[0] + "," + p[1]);
                            final double qz = state.worldModel.get("" + q[0] + "," + q[1]);

                            // state.intersectionLineObjectCollection.add(new VisLineSegment(p[0], p[1], PEG_HEIGHT *
                            // 50,
                            // q[0], q[1], PEG_HEIGHT * 50, searchLineColor, 4));

                            // Search on the line
                            for (double x = p[0]; x >= q[0]; x -= 0.25) {
                                final double y = (x - p[0]) * slope + p[1];
                                final double y1 = Nearest(y, Statistics.BIN_WIDTH);
                                final double y2 = y1 + Statistics.BIN_WIDTH;

                                final double Ez = (y - p[1]) * ((qz - pz) / (q[1] - p[1])) + pz;
                                final Double z1 = state.worldModel.get("" + x + "," + y1);
                                final Double z2 = state.worldModel.get("" + x + "," + y2);

                                if (z1 != null && maxDeviation < z1 - Ez) {
                                    maxDeviation = z1 - Ez;
                                    dangerX = x;
                                    dangerY = y1;
                                    dangerLine = new double[][] { new double[] { p[0], p[1], pz },
                                            new double[] { q[0], q[1], qz } };
                                }

                                if (z2 != null && maxDeviation < z2 - Ez) {
                                    maxDeviation = z2 - Ez;
                                    dangerX = x;
                                    dangerY = y1;
                                    dangerLine = new double[][] { new double[] { p[0], p[1], pz },
                                            new double[] { q[0], q[1], qz } };
                                }
                            }
                        }
                    }
                }

                System.out.println(maxDeviation);
                state.intersectionLineObjectCollection.add(new VisBox(dangerX - 0.075, dangerY - 0.075, 0, 0.15, 0.15,
                        PEG_HEIGHT * 2, dangerColor));
                for (double h = -PEG_HEIGHT; h < PEG_HEIGHT; h += 0.01) {
                    state.intersectionLineObjectCollection.add(new VisLineSegment(dangerLine[0][0], dangerLine[0][1],
                            h, dangerLine[1][0], dangerLine[1][1], h, dangerColor, 4));
                }

                refreshCanvas(Long.MAX_VALUE);
            } else if (e.getKeyChar() == 'e') {
                final long startTime = System.currentTimeMillis();
                state.intersectionLineObjectCollection.clear();

                // Decide color quantization range of image
                double minHeight = Double.POSITIVE_INFINITY;
                double maxHeight = Double.NEGATIVE_INFINITY;

                for (ArrayList<Double> list : state.imageData)
                    for (double height : list) {
                        minHeight = Math.min(minHeight, height);
                        maxHeight = Math.max(maxHeight, height);
                    }

                // Create color map based on height range
                final ColorMapper grayMapper = ColorMapper.makeGray(minHeight, maxHeight);

                // Create image from height data
                System.err.println("Creating height Image ...");
                int height = state.imageData.size();
                int width = state.imageData.get(0).size();

                BufferedImage _out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                int out[] = ((DataBufferInt) (_out.getRaster().getDataBuffer())).getData();

                for (int y = 0; y + 1 < height; y++) {
                    for (int x = 0; x + 1 < width; x++) {
                        out[y * width + x] = grayMapper.map(state.imageData.get(y).get(x));
                    }
                }

                // Remove noise
                System.err.println("Removing noise ...");
                BufferedImage denoisedImage = null;
                try {
                    denoisedImage = BeliefPropagationImageRestorer.restore(_out);
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                    return true;
                }

                // Calculate Edges from the de-noised Image
                // First find the edge threshold
                System.err.println("Detecting optimal edge threshold ...");
                height = denoisedImage.getHeight();
                width = denoisedImage.getWidth();

                ArrayList<Double> edgeHeightCollection = new ArrayList<Double>();
                for (int y = 0; y < height; ++y) {
                    for (int x = 0; x < width; ++x) {

                        double edgeHeight = 0;
                        if (x > 0 && x < width - 2 && y > 0 && y < height - 2) {
                            double tmp1 = Math.abs(decodeHeightFromImage(denoisedImage, x, y)
                                    - decodeHeightFromImage(denoisedImage, x + 1, y + 1));
                            double tmp2 = Math.abs(decodeHeightFromImage(denoisedImage, x + 1, y)
                                    - decodeHeightFromImage(denoisedImage, x, y + 1));
                            edgeHeight = tmp1 + tmp2;
                        }

                        edgeHeightCollection.add(edgeHeight);
                    }
                }

                Collections.sort(edgeHeightCollection);
                final double edgeThreshold = edgeHeightCollection.get((int) (edgeHeightCollection.size() * 0.91));

                final ArrayList<double[]> pointsOfInterest = new ArrayList<double[]>();
                final HashSet<String> edgePoints = new HashSet<String>();
                System.err.println("Detecting edges ...");

                final Color ROAD_COLOR = new Color(160,160,160);
                final Color NON_ROAD_COLOR = Color.red.darker();

                for (int y = 0; y < height; ++y) {
                    for (int x = 0; x < width; ++x) {
                        final double value = decodeHeightFromImage(denoisedImage, x, y);

                        double edgeHeight = 0;
                        if (x > 0 && x < width - 2 && y > 0 && y < height - 2) {
                            double tmp1 = Math.abs(decodeHeightFromImage(denoisedImage, x, y)
                                    - decodeHeightFromImage(denoisedImage, x + 1, y + 1));
                            double tmp2 = Math.abs(decodeHeightFromImage(denoisedImage, x + 1, y)
                                    - decodeHeightFromImage(denoisedImage, x, y + 1));
                            edgeHeight = tmp1 + tmp2;
                        }

                        final double xx = -x * 0.25;
                        final double yy = (y - height / 2) * 0.25;

                        if (edgeHeight < edgeThreshold) {
                            final double[] firstIntersectionPoint = state.intersectionPoints.get(0);
                            final double[] lastIntersectionPoint = state.intersectionPoints
                                    .get(state.intersectionPoints.size() - 1);

                            if (state.worldModel.containsKey("" + xx + "," + yy)
                                    && (euclideanDistance(firstIntersectionPoint, new double[] { xx, yy }) < 15 || euclideanDistance(
                                            lastIntersectionPoint, new double[] { xx, yy }) < 15)) {
                                state.intersectionLineObjectCollection.add((new VisBox(xx - 0.05, yy + 0.05, 5, 0.2,
                                        0.2, value + edgeHeight * 2, ROAD_COLOR)));

                                pointsOfInterest.add(new double[] { xx, yy });
                            }
                        } else {
                            state.intersectionLineObjectCollection.add((new VisBox(xx - 0.05, yy + 0.05, 5, 0.2, 0.2,
                                    value + edgeHeight * 5, NON_ROAD_COLOR)));
                            edgePoints.add("" + xx + "," + yy);
                        }
                    }
                }

                System.err.println("Found " + pointsOfInterest.size() + " points of interest...");
                System.err.println("Begin traffic simulation...");

                HashSet<String> intersectionPoints = new HashSet<String>();
                for (double p[] : state.intersectionPoints)
                    intersectionPoints.add("" + p[0] + "," + p[1]);

                Random rng = new Random();
                final Color TRAFFIC_COLOR = Color.cyan.darker().darker().darker();
                final Color FAILURE_LINE_COLOR = Color.magenta.darker();

                state.failureStart = null;
                state.failureEnd = null;

                for (int simulations = 0; simulations < 5000;) {
                    final double[] a = pointsOfInterest.get(rng.nextInt(pointsOfInterest.size()));
                    final double[] b = pointsOfInterest.get(rng.nextInt(pointsOfInterest.size()));
                    final double m = (a[1] - b[1]) / (a[0] - b[0]);
                    final double az = state.worldModel.get("" + a[0] + "," + a[1]);
                    final double bz = state.worldModel.get("" + b[0] + "," + b[1]);
                    final double CLEARANCE = 0.5;
                    final int TRUCK_LENGTH = 12;

                    if (Math.abs(m) < Math.tan(Math.PI / 6)) // Should be significantly off the direction of motion
                        continue;

                    boolean fClearPath = false; // Does not pass through an edge
                    boolean fThroughIntersection = false;
                    boolean fBottomOut = false;

                    if (Math.abs(euclideanDistance(a, b) - TRUCK_LENGTH) <= 1) {
                        ++simulations;
                        // System.err.println("simulation#" + simulations);

                        final double epsilon = 0.25;

                        if (Math.abs(a[0] - b[0]) > Math.abs(a[1] - b[1])) {
                            // x has greater separation

                            final double delta = Math.signum(b[0] - a[0]) * epsilon;

                            for (double x = a[0]; x != b[0]; x += delta) {
                                double y = a[1] + (x - a[0]) * m;
                                y = Math.floor(y / epsilon) * epsilon;

                                fClearPath = !edgePoints.contains("" + x + "," + y);
                                if (!fClearPath)
                                    break;

                                fThroughIntersection |= intersectionPoints.contains("" + x + "," + y);

                                final double predictedz = ((x - a[0]) / (a[0] - b[0])) * (az - bz) + az;
                                final double actualz = state.worldModel.get("" + x + "," + y);
                                if (actualz >= predictedz + CLEARANCE && state.failureStart == null /* remove */) {
                                    fBottomOut = true;
                                    state.failurePoint = new double[] { x, y };
                                    System.out.println("FailurePoint actualz=" + actualz + " predictedz=" + predictedz);
                                }
                            }
                        } else {
                            // y has greater or equal separation

                            final double delta = Math.signum(b[1] - a[1]) * epsilon;

                            for (double y = a[1]; y != b[1]; y += delta) {
                                double x = a[0] + (y - a[1]) / m;
                                x = Math.floor(x / epsilon) * epsilon;

                                fClearPath = !edgePoints.contains("" + x + "," + y);
                                if (!fClearPath)
                                    break;

                                fThroughIntersection |= intersectionPoints.contains("" + x + "," + y);

                                final double predictedz = ((x - a[0]) / (a[0] - b[0])) * (az - bz) + az;
                                final double actualz = state.worldModel.get("" + x + "," + y);
                                if (actualz >= predictedz + CLEARANCE && state.failureStart == null /* remove */) {
                                    fBottomOut = true;
                                    state.failurePoint = new double[] { x, y };
                                    System.out.println("FailurePoint actualz=" + actualz + " predictedz=" + predictedz);
                                }
                            }
                        }

                        if (fClearPath && fThroughIntersection) {
//                            state.intersectionLineObjectCollection.add((new VisBox(a[0] - 0.05, a[1] + 0.05, 5, 0.2,
//                                    0.2, 1, TRAFFIC_COLOR)));
//                            state.intersectionLineObjectCollection.add((new VisBox(b[0] - 0.05, b[1] + 0.05, 5, 0.2,
//                                    0.2, 1, TRAFFIC_COLOR)));

                            if (fBottomOut) {
                                state.intersectionLineObjectCollection.add(new VisLineSegment(a[0], a[1], 5.5, b[0],
                                        b[1], 5.5, FAILURE_LINE_COLOR, 3));
                                state.failureStart = a;
                                state.failureEnd = b;

//                                state.intersectionLineObjectCollection.add((new VisBox(state.failurePoint[0] - 0.05,
//                                        state.failurePoint[1] + 0.05, 5, 0.2,
//                                        0.2, 2, Color.magenta)));

                                fBottomOut = false;
                            }
                        }
                    }
                }
                System.err.println("Took " + (System.currentTimeMillis() - startTime) + " ms");

                // Refresh screen
                System.err.println("Rendering ...");
                refreshCanvas(Long.MAX_VALUE);

            } else if (e.getKeyChar() == 'p' || e.getKeyChar() == 'P') {
                state.intersectionLineObjectCollection.clear();
                final double[] a = state.failureStart;
                final double[] b = state.failureEnd;
                final double az = state.worldModel.get("" + a[0] + "," + a[1]);
                final double bz = state.worldModel.get("" + b[0] + "," + b[1]);
                final double slope = (a[1] - b[1]) / (a[0] - b[0]);
                final double WHEEL_RADIUS = 0.5;

                if (a == null || b == null)
                    return true;

                double[] start = a[1] < 0 ? new double[] { a[0], a[1], az } : new double[] { b[0], b[1], bz };
                double[] stop = a[1] >= 0 ? new double[] { a[0], a[1], az } : new double[] { b[0], b[1], bz };

                start[1] -= 1;
                start[0] -= (1 / slope);
                stop[1] += 1;
                stop[0] += (1 / slope);

                for (double z = WHEEL_RADIUS; z < 3; z += 0.01)
                    state.intersectionLineObjectCollection.add(new VisLineSegment(start[0], start[1], start[2] + z,
                            stop[0], stop[1], stop[2] + z,
                            Color.red.darker().darker(), 4));

                if (e.getKeyChar() == 'P')
                    for (double z = WHEEL_RADIUS - 0.25; z < WHEEL_RADIUS; z += 0.1)
                        state.intersectionLineObjectCollection.add(new VisLineSegment(a[0], a[1], az + z,
                                b[0], b[1], bz + z,
                                Color.DARK_GRAY.darker(), 4));

                VisChain wheel1 = new VisChain();
                wheel1.add(LinAlg.translate(a[0] - 0.15, a[1], az + WHEEL_RADIUS));
                wheel1.add(LinAlg.rotateX(Math.PI / 2));
                wheel1.add(LinAlg.rotateY(Math.atan(slope)));
                wheel1.add(new VisCylinder(WHEEL_RADIUS, 0.5, Color.darkGray.darker()));
                state.intersectionLineObjectCollection.add(wheel1);

                VisChain wheel2 = new VisChain();
                wheel2.add(LinAlg.translate(b[0] - 0.15, b[1], bz + WHEEL_RADIUS));
                wheel2.add(LinAlg.rotateX(Math.PI / 2));
                wheel2.add(LinAlg.rotateY(Math.atan(slope)));
                wheel2.add(new VisCylinder(WHEEL_RADIUS, 0.5, Color.darkGray.darker()));
                state.intersectionLineObjectCollection.add(wheel2);

                // Refresh screen
                System.err.println("Rendering ...");
                refreshCanvas(Long.MAX_VALUE);

            } else if (e.getKeyChar() == 't') {
                VisChain c = new VisChain();
                c.add(LinAlg.translate(10, 10, 0));
                c.add(LinAlg.rotateY(Math.PI / 2));
                c.add(new VisCircle(1, new VisDataFillStyle(Color.yellow)));
                state.intersectionLineObjectCollection.add(c);

                // Refresh screen
                System.err.println("Rendering ...");
                refreshCanvas(Long.MAX_VALUE);
            }

            return true;
        }

        private double decodeHeightFromImage(BufferedImage img, int x, int y)
        {
            return new Color(img.getRGB(x, y)).getRed() / 255.0;
        }

        @Override
        public String getName()
        {
            return "ViewFranco event handler";
        }
    }

    class State
    {
        long lastFrameTime = 0;
        double lastTheta = Double.MIN_VALUE;
        int lastKnownClassification = 0;
        ArrayList<double[]> intersectionPoints = new ArrayList<double[]>();;
        boolean atIntersection = false;

        double simulatedMotionOffset = 0;
        // This is used to simulate motion on the x-axis. Can be removed once we
        // start using applanix data.

        ArrayList<VisObject> renderedObjectCollection = new ArrayList<VisObject>();
        ArrayList<VisObject> intersectionLineObjectCollection = new ArrayList<VisObject>();
        SizeLimitedDataBuffer<double[]> boundaryPoints = new SizeLimitedDataBuffer<double[]>(1000);
        HashMap<String, Double> worldModel = new HashMap<String, Double>(); // Bad perf. But easy
        ArrayList<ArrayList<Double>> imageData = new ArrayList<ArrayList<Double>>();

        double[] bestSegmentStart = null;
        double[] bestSegmentEnd = null;
        double bestSlope = 0;
        double[] failureStart = null;
        double[] failureEnd = null;
        double[] failurePoint = null;
        int bestKnownBalance = Integer.MAX_VALUE;

        final static double BIN_WIDTH = 0.5;
        final static int ROI_BEGIN = -30;
        final static int ROI_END = 30;
    }

    public ViewFranco(Viewer viewer, String name, Config config) throws IOException
    {
        this.viewer = viewer;
        this.name = name;
        this.config = config;
        this.channel = config.getString("channel", "VELODYNE");

        // sensor position in robot frame
        this.spos = ConfigUtil.getPosition(config.getRoot(), channel);
        this.squat = ConfigUtil.getQuaternion(config.getRoot(), channel);

        final String imgRepositoryPath = config.getString("image_repository_path");
        this.imgRepository = new ImageRepository(imgRepositoryPath);
        this.labelStore = new ClassificationLabelStore(imgRepositoryPath);
        this.statistics = new Statistics(spos, squat, imgRepositoryPath);

        this.viewer.getVisCanvas().setBackground(new Color(0, 16, 16));
        this.viewer.getVisCanvas().addEventHandler(new MyVisCanvasEventHandler());

        try {
            imgCrossingSignal = javax.imageio.ImageIO.read(new File(config.getString("railway_crossing_artwork")));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        lcm.subscribe("VELODYNE", this);
    }

    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        try {
            messageReceivedEx(channel, ins);
        }
        catch (IOException ex) {
            System.out.println("Exception: " + ex);
        }
    }

    /**
     * Checks the orientation of point c with respect to the line segment from a to b
     *
     * @param a
     * @param b
     * @param c
     * @return +1 if c is reached in a counter clockwise fashion from a and b. 0 if c is collinear with a and b. -1
     *         otherwise.
     */
    public static int geometricCounterClockWise(double[] a, double[] b, double[] c)
    {
        double area2 = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
        return (int) Math.signum(area2);
    }

    public static double euclideanDistance(double[] a, double[] b)
    {
        return Math.sqrt(Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2));
    }

    public static double Nearest(double a, double b)
    {
        return Math.floor(a / b) * b;
    }

    void messageReceivedEx(String channel, LCMDataInputStream ins) throws IOException
    {
        if (!channel.equals(this.channel))
            return;

        velodyne_t vdata = new velodyne_t(ins);

        // Write out frames at 30 frames per second
        if (MAKE_MOVIE) {
            if (vdata.utime - state.lastFrameTime > 333333) {
                viewer.getVisCanvas().movieMakeFrame();
                state.lastFrameTime = vdata.utime;
            }
        }

        Velodyne v = new Velodyne(calib, vdata.data);
        Velodyne.Sample vs = new Velodyne.Sample();

        while (v.next(vs)) {
            statistics.process(vdata.utime, vs);

            if (vs.ctheta > 5 && state.lastTheta < 1) {
                state.simulatedMotionOffset -= 0.25;

                final int classificationLabel = labelStore.getAt(vdata.utime);
                // final double PEG_HEIGHT = 1.5;
                // final Color intersectionColor = new Color(255, 128, 0);

                System.out.printf("@%s %d: ", "" + vdata.utime, classificationLabel);
                System.out.printf("processing data when velodyne sweep angle transitioned from %2f to %2f\n",
                        state.lastTheta, vs.ctheta);

                ArrayList<double[]> previousData = statistics.getDataAtPreviousTimeStep();
                ArrayList<double[]> data = statistics.getData();
                statistics.reset();

                //
                // Process the statistics data
                //

                state.imageData.add(new ArrayList<Double>());

                for (int i = 0; i < data.size(); ++i) {
                    double stat[] = data.get(i);
                    // double prevStat[] = previousData.get(i);

                    // final double groundHeightAtIntersection = data.get(data.size() / 2)[1];

                    // Update the worldModel
                    state.worldModel.put("" + state.simulatedMotionOffset + "," + statistics.getStartY(i), stat[1]);
                    // System.out.printf("" + state.simulatedMotionOffset + "," + statistics.getStartY(i) + " ");

                    // Add data to the image
                    state.imageData.get(state.imageData.size() - 1).add(stat[1]);

                    // Add data to the occupancy grid
                    // Remember that we are not interested in objects that start after a height of 5
                    state.renderedObjectCollection.add(new VisBox(state.simulatedMotionOffset, statistics.getStartY(i),
                            stat[1] - 0.25, State.BIN_WIDTH, statistics.getEndY(i) - statistics.getStartY(i), 0.25,
                            colorMap.mapColor(stat[1])));

                    double edgeHeight = 0;
                    if (i < data.size() - 1) {
                        double tmp1 = Math.abs(previousData.get(i)[1] - data.get(i + 1)[1]);
                        double tmp2 = Math.abs(previousData.get(i + 1)[1] - data.get(i)[1]);
                        edgeHeight = tmp1 + tmp2;
                    }

                    // final Color nonRoadColor = new Color(128, 0, 0);
                    if (edgeHeight < 0.4) {
                        state.boundaryPoints.add(new double[] { state.simulatedMotionOffset, statistics.getStartY(i) });
                    }
                }

                // If we are at/reached a crossing ...
                if (classificationLabel == 2) {

                    // if (state.lastKnownClassification != 2) {
                    // state.renderedObjectCollection.add(new VisImage(new
                    // VisTexture(imgCrossingSignal), new double[] { state.simulatedMotionOffset + 5, -5 },
                    // new double[]
                    // { state.simulatedMotionOffset + 10, 5 }, 0.25, false));
                    // }

                    state.atIntersection = true;
                    state.intersectionPoints.add(new double[] { state.simulatedMotionOffset, 0 });

                    // Mark intersection points
                    // state.renderedObjectCollection.add(new VisBox(state.simulatedMotionOffset - 0.075, -0.075, 2,
                    // 0.15,
                    // 0.15, PEG_HEIGHT, intersectionColor));
                }

                // If we left a crossing ...
                if (classificationLabel != 2 && state.lastKnownClassification == 2) {
                    // state.atIntersection = false;
                    // state.intersectionPoints.clear();
                }

                state.lastKnownClassification = classificationLabel;
            }

            state.lastTheta = vs.ctheta;
        }

        refreshCanvas(vdata.utime);
    }

    private void refreshCanvas(long utime)
    {
        //
        // Add stuff to the vis buffer
        //

        // VisWindow wnd = new VisWindow(VisWindow.ALIGN.TOP_LEFT, 320, 240, new double[] { 320 + 15, 0 + 15 },
        // new double[] { 0 + 15, 240 + 15 });
        //
        // BufferedImage bestMatchedImage = imgRepository.getAt(utime);
        // if (bestMatchedImage != null)
        // wnd.add(new VisImage(bestMatchedImage));

        // Show a histogram of statistics from regions of interest (current and past). Essentially this is a 3-D
        // histogram made out of 2-D histograms over time. Figured that people call this a "Occupancy grid".
        VisWorld.Buffer vb = viewer.getVisWorld().getBuffer(channel);

        for (VisObject b : state.renderedObjectCollection)
            vb.addBuffered(b);

        for (VisObject b : state.intersectionLineObjectCollection)
            vb.addBuffered(b);

        // vb.addBuffered(wnd);
        vb.switchBuffer();
    }

    public static void main(String args[]) throws IOException
    {
        if (args.length < 1) {
            System.err.println("USAGE: ViewFranco [-svm] range bin-width history-size configFile lcm-log-files");
            return;
        }

        int currentArg = 0;
        boolean fSvmFormat = false;

        if (args[0].equalsIgnoreCase("-svm")) {
            fSvmFormat = true;
            currentArg++;
        }

        final int range = Integer.parseInt(args[currentArg++]);
        final double binWidth = Double.parseDouble(args[currentArg++]);
        final int historySize = Integer.parseInt(args[currentArg++]);

        Statistics.ROI_BEGIN = -range;
        Statistics.ROI_END = range;
        Statistics.BIN_WIDTH = binWidth;

        System.err.printf("INFO: Parameters set to range=-%d to %d, width=%4f, history=%d\n", range, range, binWidth,
                historySize);

        Config config = new ConfigFile(args[currentArg]).getChild("viewer").getChild("franco");
        currentArg++;

        String channel = config.getString("channel", "VELODYNE");
        String repositoryPath = config.getString("image_repository_path");
        ClassificationLabelStore imgRepository = new ClassificationLabelStore(repositoryPath);

        // sensor position in robot frame
        double[] spos = ConfigUtil.getPosition(config.getRoot(), channel);
        double[] squat = ConfigUtil.getQuaternion(config.getRoot(), channel);

        for (int argIndex = currentArg; argIndex < args.length; ++argIndex) {
            Log log = new Log(args[argIndex], "r");
            double lastTheta = Double.MIN_VALUE;
            SizeLimitedDataBuffer<Double> dataBuffer = new SizeLimitedDataBuffer<Double>(
                    (int) ((Statistics.ROI_END - Statistics.ROI_BEGIN) / Statistics.BIN_WIDTH) * historySize);

            Statistics statistics = new Statistics(spos, squat, repositoryPath);

            try {
                while (true) {
                    Event e = log.readNext();
                    if (!channel.equals(e.channel))
                        continue;

                    velodyne_t vdata = new velodyne_t(log.readNext().data);
                    Velodyne v = new Velodyne(calib, vdata.data);
                    Velodyne.Sample vs = new Velodyne.Sample();

                    while (v.next(vs)) {
                        statistics.process(vdata.utime, vs);

                        if (vs.ctheta > 5 && lastTheta < 1) {
                            final int classificationLabel = imgRepository.getAt(vdata.utime);

                            ArrayList<double[]> data = statistics.getData();
                            statistics.reset();

                            for (double[] stat : data)
                                dataBuffer.add(stat[0]);

                            if (dataBuffer.isAtMaximumCapacity()) {
                                System.out.print(classificationLabel);
                                System.out.print(fSvmFormat ? ' ' : ':');

                                for (int i = 0; i < dataBuffer.size(); ++i) {
                                    if (fSvmFormat)
                                        System.out.printf("%d:%4f ", i + 1, dataBuffer.get(i));
                                    else
                                        System.out.printf("%4f,", dataBuffer.get(i));
                                }
                                System.out.println();
                            }
                        }

                        lastTheta = vs.ctheta;
                    }
                }
            }
            catch (EOFException e) {
                // thats OK
            }
            catch (IOException e) {
                System.err.println(e.toString());
                System.err.println("Resuming...");
            }
        }
    }

    private final class WindowOpenCloseListener extends WindowAdapter
    {
        public void windowOpened(WindowEvent arg0)
        {
            if (MAKE_MOVIE) {
                try {
                    viewer.getVisCanvas().movieBegin("/home/pradeep/Videos/demo-" + new Date() + ".ppmsg.gz", false);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed to create movie");
                }
            }
        }

        public void windowClosed(WindowEvent arg0)
        {
            if (MAKE_MOVIE) {
                try {
                    viewer.getVisCanvas().movieEnd();
                }
                catch (IOException e) {
                    System.err.println("Failed to complete movie");
                    e.printStackTrace();
                }
            }
        }
    }
}

class Statistics
{
    static double BIN_WIDTH = 0.25;
    static int ROI_BEGIN = -15;
    static int ROI_END = 15;

    VelodyneCalibration calib = VelodyneCalibration.makeMITCalibration();
    double spos[], squat[];
    static pose_t pose;

    ArrayList<IntervalStatisticsCollector> roiCollection = new ArrayList<IntervalStatisticsCollector>();
    ArrayList<double[]> previousTimeStepStatistics = new ArrayList<double[]>();

    static {
        // TODO: Use applanix info instead of fudged pose_t's.
        pose = new pose_t();

        pose.pos = new double[] { 0, 0.0, 3.0 };
        pose.vel = new double[3];
        pose.rotation_rate = new double[3];
        pose.orientation = new double[] { 0.9998173755898185, -0.0018885086762571626, -0.019014471105958535,
                -3.144713468615474E-4 };
        pose.accel = new double[3];
    }

    public Statistics(double spos[], double squat[], String imageRepositoryPath) throws IOException
    {
        // sensor position in robot frame
        this.spos = spos;
        this.squat = squat;

        for (double i = ROI_BEGIN; i < ROI_END; i += BIN_WIDTH) {
            // Need to explicitly filter out unwanted data above the tracks (near the velodyne).
            // Everywhere else we limit the ranges of the z-axis from -20 to 100
            if (Math.abs(i) > 1)
                roiCollection.add(new IntervalStatisticsCollector(i, i + BIN_WIDTH, -20, 100));
            else
                roiCollection.add(new IntervalStatisticsCollector(i, i + BIN_WIDTH, -20, 0.5));

            previousTimeStepStatistics.add(new double[] { -1.0, -1.0 });
        }
    }

    public double getStartY(int index)
    {
        return roiCollection.get(index).getStartY();
    }

    public double getEndY(int index)
    {
        return roiCollection.get(index).getEndY();
    }

    public void reset()
    {
        for (IntervalStatisticsCollector roi : roiCollection)
            roi.clear();
    }

    public void process(long utime, Velodyne.Sample vs)
    {
        double B2G[][] = LinAlg.quatPosToMatrix(pose.orientation, pose.pos);
        double S2B[][] = LinAlg.quatPosToMatrix(squat, spos);
        double T[][] = LinAlg.matrixAB(B2G, S2B);

        // Transform points and process them
        double[] transformed = LinAlg.transform(T, vs.xyz);

        // Do processing for other regions of interest. We choose to be efficient here by calculating the bin
        // that this point will belong to
        int startROI = (int) Math.floor((transformed[1] / BIN_WIDTH) + roiCollection.size() / 2);
        startROI = startROI < 0 ? 0 : startROI;

        for (int i = startROI; i < roiCollection.size(); ++i) {
            if (!roiCollection.get(i).process(transformed))
                break;
        }
    }

    public ArrayList<double[]> getData()
    {
        ArrayList<double[]> data = new ArrayList<double[]>();

        for (int i = 0; i < roiCollection.size(); ++i) {
            final IntervalStatisticsCollector roi = roiCollection.get(i);
            double stat[] = roi.getStatistic();

            if (stat[0] == -1 && stat[1] == -1 || stat[0] > 5) {
                // No data points were recorded by the velodyne for this bin. So impute!
                // Some times the range of statistics start much higher than what is useful. This happens when
                // there is an overhanging leaf or object so that the statistics for that bin starts much away
                // from the ground level. Again we want to impute in this case to avoid leaving holes on the
                // ground.
                stat = previousTimeStepStatistics.get(i);
            }

            // log data for classification
            data.add(stat);
        }

        previousTimeStepStatistics = data;
        return data;
    }

    public ArrayList<double[]> getDataAtPreviousTimeStep()
    {
        return new ArrayList<double[]>(previousTimeStepStatistics);
    }
}

/**
 * IntervalStatisticsCollector encapsulates the processing done for each point in the velodyne data. Here it is used to
 * calculate statistics about points in different regions.
 *
 * @author rpradeep
 */
class IntervalStatisticsCollector
{
    ArrayList<Double> data = new ArrayList<Double>();
    double startY = 0;
    double endY = 0;
    double startZ = Double.NEGATIVE_INFINITY;
    double endZ = Double.POSITIVE_INFINITY;

    public IntervalStatisticsCollector(double startY, double endY)
    {
        this.startY = startY;
        this.endY = endY;
    }

    public IntervalStatisticsCollector(double startY, double endY, double startZ, double endZ)
    {
        this.startY = startY;
        this.endY = endY;
        this.startZ = startZ;
        this.endZ = endZ;
    }

    public boolean process(double[] p)
    {
        if (p[1] >= startY && p[1] < endY && p[2] >= startZ && p[2] < endZ) {
            data.add(p[2]);
            return true;
        }

        return false;
    }

    public void clear()
    {
        data.clear();
    }

    public double getStartY()
    {
        return this.startY;
    }

    public double getEndY()
    {
        return this.endY;
    }

    public double[] getStatistic()
    {
        if (data.size() == 0)
            return new double[] { -1, -1 };

        Collections.sort(data);

        int discontinuity = 1;
        if (data.size() >= 1) {
            for (discontinuity = 1; discontinuity < data.size()
                    && (data.get(discontinuity) - data.get(discontinuity - 1) < 1); ++discontinuity)
                ;
        }

        // { min-value, max-value before separation }
        final int decile8 = (int) Math.ceil((discontinuity - 1) * 0.8);
        final int decile2 = (int) Math.floor((discontinuity - 1) * 0.2);
        return new double[] { data.get(decile8), data.get(decile2) };
    }
}

/**
 *
 * @author rpradeep
 *
 */
class ClassificationLabelStore
{
    Long[] _classificationStartTimes;
    Integer[] _classificationLabels;

    public ClassificationLabelStore(String repositoryPath) throws IOException
    {
        loadClassificationLabels(repositoryPath);
    }

    private void loadClassificationLabels(String repositoryPath) throws IOException
    {
        BufferedReader infile = new BufferedReader(new FileReader(repositoryPath + File.separator + "class.txt"));

        ArrayList<Long> startTimes = new ArrayList<Long>();
        ArrayList<Integer> labels = new ArrayList<Integer>();

        while (true) {
            final String line = infile.readLine();
            if (line == null)
                break;
            if (line.length() == 0) // ignore blank lines
                continue;

            String[] information = line.split("@");
            labels.add(Integer.parseInt(information[0]));
            startTimes.add(Long.parseLong(information[1]));
        }

        _classificationStartTimes = startTimes.toArray(new Long[0]);
        _classificationLabels = labels.toArray(new Integer[0]);

        // check if the data is sorted
        for (int i = 1; i < _classificationStartTimes.length; ++i)
            assert _classificationStartTimes[i] > _classificationStartTimes[i - 1];
    }

    public int getAt(final long timeStamp)
    {
        int nearest = Arrays.binarySearch(_classificationStartTimes, timeStamp);
        if (nearest < 0)
            nearest = Math.abs(nearest) - 2;

        if (nearest >= _classificationLabels.length)
            return _classificationLabels[_classificationLabels.length - 1];
        if (nearest < _classificationLabels.length && nearest >= 0)
            return _classificationLabels[nearest];
        else
            return 0;
    }
}

/**
 * ImageRepository is a key value type container class that processes a given directory of images and sorts the list of
 * images according to time-stamp. (the time-stamp does not refer to the file attribute). It then answers queries for
 * images given a time and returns the image that is nearest and before the given time. Caches all the images from the
 * directory in memory for better performance (accompanied by increased memory usage).
 *
 * @author pradeep
 */
class ImageRepository
{
    BufferedImage[] _preloadedImages;
    Long[] _listPictureFileTimeStamps;

    public ImageRepository(String repositoryPath)
    {
        loadImages(repositoryPath);
    }

    public BufferedImage getAt(final long timeStamp)
    {
        int nearest = Arrays.binarySearch(_listPictureFileTimeStamps, timeStamp);
        if (nearest < 0)
            nearest = Math.abs(nearest) - 2;

        if (nearest >= _preloadedImages.length)
            return _preloadedImages[_preloadedImages.length - 1];
        if (nearest < _preloadedImages.length && nearest >= 0)
            return _preloadedImages[nearest];
        else
            return _preloadedImages[0];
    }

    private void loadImages(String repositoryPath)
    {
        File d = new File(repositoryPath);
        String[] listPictureFileNames = d.list(new FilenameFilter() {
            public boolean accept(File dir, String name)
                            {
                return name.endsWith(".jpg");
            }
        });

        Arrays.sort(listPictureFileNames, String.CASE_INSENSITIVE_ORDER);

        // Store the time-stamps separately
        ArrayList<Long> listTimeStamps = new ArrayList<Long>();
        for (int i = 0; i < listPictureFileNames.length; ++i) {
            // TODO: Let the caller supply this logic as an IImageTimeStampProvider closure
            String lastPart = listPictureFileNames[i].split(" ")[2];
            String timeStamp = lastPart.split("\\.")[0];

            long t = Long.parseLong(timeStamp);
            listTimeStamps.add(t);
        }

        _listPictureFileTimeStamps = listTimeStamps.toArray(new Long[0]);

        // Check if we have the files sorted by time-stamp
        for (int i = 1; i < _listPictureFileTimeStamps.length; ++i) {
            assert _listPictureFileTimeStamps[i] > _listPictureFileTimeStamps[i - 1];
            System.err.printf("image repository: delay from frame%d to frame%d = %d microseconds\n", i - 1, i,
                    _listPictureFileTimeStamps[i] - _listPictureFileTimeStamps[i - 1]);
        }

        System.err.println("Caching images from repository in memory...");
        readyImageCache(repositoryPath, listPictureFileNames);
        System.err.println("In memory repository cache built.");
    }

    private void readyImageCache(String repositoryPath, String[] listPictureFileNames)
    {
        if (_preloadedImages == null) {
            _preloadedImages = new BufferedImage[listPictureFileNames.length];

            for (int i = 0; i < listPictureFileNames.length; ++i) {
                final String filePath = repositoryPath + "/" + listPictureFileNames[i];
                try {
                    _preloadedImages[i] = javax.imageio.ImageIO.read(new File(filePath));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        listPictureFileNames = null; // We shouldn't need it here after
    }
}

/**
 * SizeLimitedDataBuffer maintains a buffer of fixed capacity with double data. Every time a new data point is added and
 * the buffer is full, the oldest one is removed to make space.
 *
 * @author rpradeep
 *
 */
class SizeLimitedDataBuffer<T>
{
    LinkedList<T> q = new LinkedList<T>();
    int maxCapacity;

    public SizeLimitedDataBuffer(int capacity)
    {
        maxCapacity = capacity;
    }

    public boolean isAtMaximumCapacity()
    {
        return q.size() == maxCapacity;
    }

    public int size()
    {
        return q.size();
    }

    public void add(T d)
    {
        q.add(d);

        if (q.size() > maxCapacity)
            q.remove(0);
    }

    public T get(int index)
    {
        return q.get(index);
    }

    public ArrayList<T> getCopyOfBuffer()
    {
        return new ArrayList<T>(q);
    }
};

class BeliefPropagationImageRestorer
{
    @SuppressWarnings("serial")
    static public class ExternalInvocationException extends RuntimeException
    {
        public ExternalInvocationException(String message)
        {
            super(message);
        }
    }

    static BufferedImage restore(BufferedImage img) throws IOException, InterruptedException
    {
        ImageIO.write(img, "png", new File("hackyImageRestorer-data.png"));

        final Runtime rt = Runtime.getRuntime();

        // Convert to pgm
        System.err.println("Converting to .pgm ...");
        Process p = rt.exec("convert hackyImageRestorer-data.png hackyImageRestorer-data.pgm");
        p.waitFor();

        if (p.exitValue() != 0)
            throw new ExternalInvocationException("Failed to convert image to .pgm");

        // Restore image
        System.err.println("Applying felzenswalb restoration ...");
        p = rt.exec("bp-restore hackyImageRestorer-data.pgm hackyImageRestorer-restored.pgm");
        // p = rt.exec("cp hackyImageRestorer-data.pgm hackyImageRestorer-restored.pgm");
        p.waitFor();

        if (p.exitValue() != 0)
            throw new ExternalInvocationException("bp-restore: Failed to restore .pgm image\n");

        // Convert back to png
        System.err.println("Converting back to .png ...");
        p = rt.exec("convert hackyImageRestorer-restored.pgm -flop -rotate -90 hackyImageRestorer-restored.png");
        p.waitFor();

        if (p.exitValue() != 0)
            throw new ExternalInvocationException("Failed to convert image back to .png");

        // We are done. Return an in memory object of the restore image
        return ImageIO.read(new File("hackyImageRestorer-restored.png"));
    }
}
