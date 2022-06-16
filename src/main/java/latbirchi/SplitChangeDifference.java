package latbirchi;

public class SplitChangeDifference {

    private boolean split;
    private boolean clusterAnomaly;
    private byte[] intraClusterAnomaly;
    private byte[] interClusterAnomaly;
    private byte[] changeDifferences;

    public SplitChangeDifference(boolean split, byte[] changeDifferences, boolean clusterAnomaly, byte[] intraClusterAnomaly, byte[] interClusterAnomaly) {
        this.split = split;
        this.changeDifferences = changeDifferences;
        this.clusterAnomaly = clusterAnomaly;
        this.intraClusterAnomaly = intraClusterAnomaly;
        this.interClusterAnomaly = interClusterAnomaly;
    }

    public boolean isSplit() {
        return split;
    }

    public byte[] getChangeDifferences() {
        return changeDifferences;
    }

    public boolean isClusterAnomaly() {
        return clusterAnomaly;
    }

    public void setClusterAnomaly(boolean clusterAnomaly) {
        this.clusterAnomaly = clusterAnomaly;
    }

    public byte[] isIntraClusterAnomaly() {
        return intraClusterAnomaly;
    }

    public void setIntraClusterAnomaly(byte[] intraClusterAnomaly) {
        this.intraClusterAnomaly = intraClusterAnomaly;
    }

    public byte[] isInterClusterAnomaly() {
        return interClusterAnomaly;
    }

    public void setInterClusterAnomaly(byte[] interClusterAnomaly) {
        this.interClusterAnomaly = interClusterAnomaly;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public void setChangeDifferences(byte[] changeDifferences) {
        this.changeDifferences = changeDifferences;
    }
}
