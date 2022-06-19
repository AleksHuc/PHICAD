package tbirchi;

/**
 * The class implements an object that presents whether certain incremental cluster feature should be split and the corresponding anomaly predictions from the current data point analysis.
 */
public class SplitChangeDifference {

    private boolean split;
    private boolean clusterAnomaly;
    private byte[] intraClusterAnomaly;
    private byte[] interClusterAnomaly;
    private byte[] changeDifferences;

    /**
     * The constructor creates a new object with the corresponding parameters.
     * @param split boolean value that presents whether certain incremental cluster feature should be split or not.
     * @param changeDifferences byte array that presents the changes detected in cluster centroid movements.
     * @param clusterAnomaly boolean value that presents whether an anomalous size of a cluster was detected.
     * @param intraClusterAnomaly byte array that presents the changes detected in the distance of a new data point and cluster centroid.
     * @param interClusterAnomaly byte array that presents the changes detected in the distance between clusters.
     */
    public SplitChangeDifference(boolean split, byte[] changeDifferences, boolean clusterAnomaly, byte[] intraClusterAnomaly, byte[] interClusterAnomaly) {
        this.split = split;
        this.changeDifferences = changeDifferences;
        this.clusterAnomaly = clusterAnomaly;
        this.intraClusterAnomaly = intraClusterAnomaly;
        this.interClusterAnomaly = interClusterAnomaly;
    }

    /**
     * The method returns a flag that determine whether the incremental cluster feature node split should occur.
     * @return boolean value that presents whether certain incremental cluster feature node should be split or not.
     */
    public boolean isSplit() {
        return split;
    }

    /**
     * The method returns the changes detected in cluster centroid movements.
     * @return byte array that presents the changes detected in cluster centroid movements.
     */
    public byte[] getChangeDifferences() {
        return changeDifferences;
    }

    /**
     * The method returns a flag that determines whether the incremental cluster feature is anomalous or not.
     * @return  boolean value that presents whether certain incremental cluster feature is anomalous or not.
     */
    public boolean isClusterAnomaly() {
        return clusterAnomaly;
    }

    /**
     * The method sets the flag for anomalous or normal incremental cluster feature to the value of the input parameter.
     * @param clusterAnomaly boolean value that presents whether the incremental cluster feature is anomalous or not.
     */
    public void setClusterAnomaly(boolean clusterAnomaly) {
        this.clusterAnomaly = clusterAnomaly;
    }

    /**
     * The method returns the changes detected in the distance of a new data point and cluster centroid.
     * @return byte array that presents the changes detected in the distance of a new data point and cluster centroid.
     */
    public byte[] isIntraClusterAnomaly() {
        return intraClusterAnomaly;
    }

    /**
     * The method sets the changes detected in the distance of a new data point and cluster centroid to the value of the input parameter.
     * @param intraClusterAnomaly byte array that presents the changes detected in the distance of a new data point and cluster centroid.
     */
    public void setIntraClusterAnomaly(byte[] intraClusterAnomaly) {
        this.intraClusterAnomaly = intraClusterAnomaly;
    }

    /**
     * The method returns the changes detected in the distance between clusters.
     * @return byte array that presents the changes detected in the distance between clusters.
     */
    public byte[] isInterClusterAnomaly() {
        return interClusterAnomaly;
    }

    /**
     * The method set the changes detected in the distance between clusters to the value of the input parameter.
     * @param interClusterAnomaly byte array that presents the changes detected in the distance between clusters.
     */
    public void setInterClusterAnomaly(byte[] interClusterAnomaly) {
        this.interClusterAnomaly = interClusterAnomaly;
    }

    /**
     * The method sets a flag that determine whether the incremental cluster feature node split should occur.
     * @param split boolean value that presents whether certain incremental cluster feature should be split or not.
     */
    public void setSplit(boolean split) {
        this.split = split;
    }

    /**
     * The method sets the he changes detected in cluster centroid movements to the value of the input parameter.
     * @param changeDifferences byte array that presents the changes detected in cluster centroid movements.
     */
    public void setChangeDifferences(byte[] changeDifferences) {
        this.changeDifferences = changeDifferences;
    }
}
