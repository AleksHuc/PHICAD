package phi2cad;

import latbirchi.CF;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class ProfileUpdate {

    private ArrayList<CF> list;
    private LocalDateTime timeStamp;
    private double[] clusteringVector;
    private double[] centroid;
    private double[] detections;

    public ProfileUpdate(ArrayList<CF> list, LocalDateTime timeStamp, double[] clusteringVector, double[] centroid, double[] detections) {
        this.list = list;
        this.timeStamp = timeStamp;
        this.clusteringVector = clusteringVector;
        this.centroid = centroid;
        this.detections = detections;
    }

    public ArrayList<CF> getCFList() {
        return list;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public double[] getClusteringVector() {
        return clusteringVector;
    }

    public double[] getCentroid() {
        return centroid;
    }

    public double[] getDetections() {
        return detections;
    }
}
