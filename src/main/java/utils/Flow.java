package utils;

public class Flow {
    private String appName;
    private int totalSourceBytes;
    private int totalDestinationBytes;
    private int totalDestinationPackets;
    private int totalSourcePackets;
    private String sourcePayloadAsBase64;
    private String destinationPayloadAsBase64;
    private String destinationPayloadAsUTF;
    private String direction;
    private String sourceTCPFlagsDescription;
    private String destinationTCPFlagsDescription;
    private String source;
    private String protocolName;
    private int sourcePort;
    private String destination;
    private int destinationPort;
    private String startDateTime;
    private String stopDateTime;
    private String Tag;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getTotalSourceBytes() {
        return totalSourceBytes;
    }

    public void setTotalSourceBytes(int totalSourceBytes) {
        this.totalSourceBytes = totalSourceBytes;
    }

    public int getTotalDestinationBytes() {
        return totalDestinationBytes;
    }

    public void setTotalDestinationBytes(int totalDestinationBytes) {
        this.totalDestinationBytes = totalDestinationBytes;
    }

    public int getTotalDestinationPackets() {
        return totalDestinationPackets;
    }

    public void setTotalDestinationPackets(int totalDestinationPackets) {
        this.totalDestinationPackets = totalDestinationPackets;
    }

    public int getTotalSourcePackets() {
        return totalSourcePackets;
    }

    public void setTotalSourcePackets(int totalSourcePackets) {
        this.totalSourcePackets = totalSourcePackets;
    }

    public String getSourcePayloadAsBase64() {
        return sourcePayloadAsBase64;
    }

    public void setSourcePayloadAsBase64(String sourcePayloadAsBase64) {
        this.sourcePayloadAsBase64 = sourcePayloadAsBase64;
    }

    public String getDestinationPayloadAsBase64() {
        return destinationPayloadAsBase64;
    }

    public void setDestinationPayloadAsBase64(String destinationPayloadAsBase64) {
        this.destinationPayloadAsBase64 = destinationPayloadAsBase64;
    }

    public String getDestinationPayloadAsUTF() {
        return destinationPayloadAsUTF;
    }

    public void setDestinationPayloadAsUTF(String destinationPayloadAsUTF) {
        this.destinationPayloadAsUTF = destinationPayloadAsUTF;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getSourceTCPFlagsDescription() {
        return sourceTCPFlagsDescription;
    }

    public void setSourceTCPFlagsDescription(String sourceTCPFlagsDescription) {
        this.sourceTCPFlagsDescription = sourceTCPFlagsDescription;
    }

    public String getDestinationTCPFlagsDescription() {
        return destinationTCPFlagsDescription;
    }

    public void setDestinationTCPFlagsDescription(String destinationTCPFlagsDescription) {
        this.destinationTCPFlagsDescription = destinationTCPFlagsDescription;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getStopDateTime() {
        return stopDateTime;
    }

    public void setStopDateTime(String stopDateTime) {
        this.stopDateTime = stopDateTime;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String tag) {
        Tag = tag;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "appName='" + appName + '\'' +
                ", totalSourceBytes=" + totalSourceBytes +
                ", totalDestinationBytes=" + totalDestinationBytes +
                ", totalDestinationPackets=" + totalDestinationPackets +
                ", totalSourcePackets=" + totalSourcePackets +
                ", sourcePayloadAsBase64='" + sourcePayloadAsBase64 + '\'' +
                ", destinationPayloadAsBase64='" + destinationPayloadAsBase64 + '\'' +
                ", destinationPayloadAsUTF='" + destinationPayloadAsUTF + '\'' +
                ", direction='" + direction + '\'' +
                ", sourceTCPFlagsDescription='" + sourceTCPFlagsDescription + '\'' +
                ", destinationTCPFlagsDescription='" + destinationTCPFlagsDescription + '\'' +
                ", source='" + source + '\'' +
                ", protocolName='" + protocolName + '\'' +
                ", sourcePort=" + sourcePort +
                ", destination='" + destination + '\'' +
                ", destinationPort=" + destinationPort +
                ", startDateTime='" + startDateTime + '\'' +
                ", stopDateTime='" + stopDateTime + '\'' +
                ", Tag='" + Tag + '\'' +
                '}';
    }
}
