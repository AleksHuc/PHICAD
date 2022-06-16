package utils;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;

public class FlowSAXHandler extends DefaultHandler {
    private TreeMap<LocalDateTime,ArrayList<Flow>> flowList = null;
    private Flow flow = null;
    private StringBuilder data = null;

    public TreeMap<LocalDateTime,ArrayList<Flow>> getFlowList() {
        return flowList;
    }

    boolean bAppName;
    boolean bTotalSourceBytes;
    boolean bTotalDestinationBytes;
    boolean bTotalDestinationPackets;
    boolean bTotalSourcePackets;
    boolean bSourcePayloadAsBase64;
    boolean bDestinationPayloadAsBase64;
    boolean bDestinationPayloadAsUTF;
    boolean bDirection;
    boolean bSourceTCPFlagsDescription;
    boolean bDestinationTCPFlagsDescription;
    boolean bSource;
    boolean bProtocolName;
    boolean bSourcePort;
    boolean bDestination;
    boolean bDestinationPort;
    boolean bStartDateTime;
    boolean bStopDateTime;
    boolean bTag;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.startsWith("Testbed")) {
            flow = new Flow();

            if (flowList == null) {
                flowList = new TreeMap<>();
            }
        } else if (qName.equalsIgnoreCase("appName")) {
            bAppName = true;
        } else if (qName.equalsIgnoreCase("totalSourceBytes")) {
            bTotalSourceBytes = true;
        } else if (qName.equalsIgnoreCase("totalDestinationBytes")) {
            bTotalDestinationBytes = true;
        } else if (qName.equalsIgnoreCase("totalDestinationPackets")) {
            bTotalDestinationPackets = true;
        } else if (qName.equalsIgnoreCase("totalSourcePackets")) {
            bTotalSourcePackets = true;
        } else if (qName.equalsIgnoreCase("sourcePayloadAsBase64")) {
            bSourcePayloadAsBase64 = true;
        } else if (qName.equalsIgnoreCase("destinationPayloadAsBase64")) {
            bDestinationPayloadAsBase64 = true;
        } else if (qName.equalsIgnoreCase("destinationPayloadAsUTF")) {
            bDestinationPayloadAsUTF = true;
        } else if (qName.equalsIgnoreCase("direction")) {
            bDirection = true;
        } else if (qName.equalsIgnoreCase("sourceTCPFlagsDescription")) {
            bSourceTCPFlagsDescription = true;
        } else if (qName.equalsIgnoreCase("destinationTCPFlagsDescription")) {
            bDestinationTCPFlagsDescription = true;
        } else if (qName.equalsIgnoreCase("source")) {
            bSource = true;
        } else if (qName.equalsIgnoreCase("protocolName")) {
            bProtocolName = true;
        } else if (qName.equalsIgnoreCase("sourcePort")) {
            bSourcePort = true;
        } else if (qName.equalsIgnoreCase("destination")) {
            bDestination = true;
        } else if (qName.equalsIgnoreCase("destinationPort")) {
            bDestinationPort = true;
        } else if (qName.equalsIgnoreCase("startDateTime")) {
            bStartDateTime = true;
        } else if (qName.equalsIgnoreCase("stopDateTime")) {
            bStopDateTime = true;
        } else if (qName.equalsIgnoreCase("Tag")) {
            bTag = true;
        }

        data = new StringBuilder();
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (bAppName) {
            flow.setAppName(data.toString());
            bAppName = false;
        } else if (bTotalSourceBytes) {
            flow.setTotalSourceBytes(Integer.parseInt(data.toString()));
            bTotalSourceBytes = false;
        } else if (bTotalDestinationBytes) {
            flow.setTotalDestinationBytes(Integer.parseInt(data.toString()));
            bTotalDestinationBytes = false;
        } else if (bTotalDestinationPackets) {
            flow.setTotalDestinationPackets(Integer.parseInt(data.toString()));
            bTotalDestinationPackets = false;
        } else if (bTotalSourcePackets) {
            flow.setTotalSourcePackets(Integer.parseInt(data.toString()));
            bTotalSourcePackets = false;
        } else if (bSourcePayloadAsBase64) {
            flow.setSourcePayloadAsBase64(data.toString());
            bSourcePayloadAsBase64 = false;
        } else if (bDestinationPayloadAsBase64) {
            flow.setDestinationPayloadAsBase64(data.toString());
            bDestinationPayloadAsBase64 = false;
        } else if (bDestinationPayloadAsUTF) {
            flow.setDestinationPayloadAsUTF(data.toString());
            bDestinationPayloadAsUTF = false;
        } else if (bDirection) {
            flow.setDirection(data.toString());
            bDirection = false;
        } else if (bSourceTCPFlagsDescription) {
            flow.setSourceTCPFlagsDescription(data.toString());
            bSourceTCPFlagsDescription = false;
        } else if (bDestinationTCPFlagsDescription) {
            flow.setDestinationTCPFlagsDescription(data.toString());
            bDestinationTCPFlagsDescription = false;
        } else if (bSource) {
            flow.setSource(data.toString());
            bSource = false;
        } else if (bProtocolName) {
            flow.setProtocolName(data.toString());
            bProtocolName = false;
        } else if (bSourcePort) {
            flow.setSourcePort(Integer.parseInt(data.toString()));
            bSourcePort = false;
        } else if (bDestination) {
            flow.setDestination(data.toString());
            bDestination = false;
        } else if (bDestinationPort) {
            flow.setDestinationPort(Integer.parseInt(data.toString()));
            bDestinationPort = false;
        } else if (bStartDateTime) {
            flow.setStartDateTime(data.toString());
            bStartDateTime = false;
        } else if (bStopDateTime) {
            flow.setStopDateTime(data.toString());
            bStopDateTime = false;
        } else if (bTag) {
            flow.setTag(data.toString());
            bTag = false;
        }

        if (qName.startsWith("Testbed")) {

            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'H:m:s");
            String startT =  flow.getStartDateTime();
            LocalDateTime startLDT = LocalDateTime.parse(startT, format);

            flowList.computeIfAbsent(startLDT, k -> new ArrayList<>());
            flowList.get(startLDT).add(flow);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        data.append(new String(ch, start, length));
    }
}
