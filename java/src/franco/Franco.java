package franco;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import april.config.*;
import april.util.*;
import april.vis.*;
import franco.model.*;
import franco.opts.*;
import franco.view.*;

public class Franco
{
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        //
        //  Set up program options
        //
        GetOpt options = new GetOpt();
        options.addString((char) 0, "config", null, "config file to use");
        options.parse(args);

        Config config = new ConfigFile(options.getString("config"));

        ProgramOptions.setOptions(options);
        ProgramOptions.setConfig(config);

        //
        //  Create the model and setup the views
        //
        PoseSimulator ps = new PoseSimulator();
        Model theModel = new Model();

        VisCanvas vc = new VisCanvas(new VisWorld());
        vc.setBackground(new Color(0,16,16));

        JFrame jf = new JFrame("Franco");
        jf.add(vc);
        jf.setSize(1000,800);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);

        PointCloudView pcview = new PointCloudView(theModel, vc, 1);
    }
}
