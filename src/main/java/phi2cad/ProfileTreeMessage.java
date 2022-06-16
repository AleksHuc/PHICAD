package phi2cad;

import latbirchi.CF;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class ProfileTreeMessage {

    private boolean groundTruth;
    private String ipAddress;
    private String direction;
    private ArrayList<CF> list;
    private LocalDateTime timestamp;
    double[] detections;

    public ProfileTreeMessage(boolean groundTruth, String ipAddress, String direction, ArrayList<CF> list, LocalDateTime timestamp, double[] detections) {
        this.groundTruth = groundTruth;
        this.ipAddress = ipAddress;
        this.direction = direction;
        this.list = list;
        this.timestamp = timestamp;
        this.detections = detections;
    }

    public boolean isGroundTruth() {
        return groundTruth;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDirection() {
        return direction;
    }

    public ArrayList<CF> getCFList() {
        return list;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double[] getDetections() {
        return detections;
    }
}
