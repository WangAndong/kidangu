package hog;

import java.util.*;


public interface DataSet
{
    public int numInstances();
    public int numFeatures();
    public ArrayList<float[]> getFeatureOfInstances(int idx);
    public int getLabel(int idx);
    public ArrayList<Integer> getLabels();
}
