package hog;

import java.util.*;


/** A DataSet has a certain number of instances. Each instance is labelled
 * -1 or 1. Each instance has a fixed number of features. */
public interface DataSet
{
    /** Number of instances in this dataset */
    public int numInstances();

    /** Number of features stored for each instance.
     * (Should be same for all instances) */
    public int numFeatures();

    /** Return an array consisting of all features of the i'th instance */
    public ArrayList<float[]> getInstance(int idx);

    /** Return an array consisting of the i'th feature of all instances */
    public ArrayList<float[]> getFeatureOfInstances(int idx);

    /** Get the label assigned to an instance */
    public int getLabel(int idx);

    /** Get all stored labels. Correspondence is by index */
    public ArrayList<Integer> getLabels();
}
