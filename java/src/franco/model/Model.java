package franco.model;

import java.util.*;

import franco.opts.*;

public class Model
{
    final PointCloud pointCloud;
    ArrayList<ModelUpdateListener> listeners = new ArrayList<ModelUpdateListener>();


    public Model()
    {
        this.pointCloud = new PointCloud(ProgramOptions.getConfig(), this);
    }

    public void addListener(ModelUpdateListener l)
    {
        listeners.add(l);
    }

    public PointCloud getPointCloud()
    {
        return pointCloud;
    }
}
