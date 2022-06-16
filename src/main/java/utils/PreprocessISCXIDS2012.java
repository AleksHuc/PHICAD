package utils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class PreprocessISCXIDS2012 {

    static public void main(String[] args) {

        String legend = "Flow ID, Source IP, Source Port, Destination IP, Destination Port, Protocol, Timestamp, Flow Duration, Total Fwd Packets, Total Backward Packets, Total Length of Fwd Packets, Total Length of Bwd Packets, Label";

        File folder = new File("data/ISCX-IDS-2012/Original");

        HashMap<String,AtomicLong> protocols = new HashMap<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'H:m:s");

        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.getName().startsWith("Testbed")) {
                System.out.println(fileEntry);
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();
                    FlowSAXHandler handler = new FlowSAXHandler();
                    saxParser.parse(fileEntry, handler);

                    TreeMap<LocalDateTime,ArrayList<Flow>> flowList = handler.getFlowList();
                    int counter = 0;
                    String[] fileNameSplit = fileEntry.getName().split("\\.");
                    PrintWriter pw = new PrintWriter(new FileWriter("data/ISCX-IDS-2012/" + fileNameSplit[0] + ".csv"));
                    pw.write( legend + "\n");

                    for(Map.Entry<LocalDateTime,ArrayList<Flow>> entry : flowList.entrySet()) {

                        for (Flow flow : entry.getValue()) {

                            String startT = flow.getStartDateTime();
                            String endT = flow.getStopDateTime();

                            LocalDateTime startLDT = LocalDateTime.parse(startT, format);
                            LocalDateTime endLDT = LocalDateTime.parse(endT, format);
                            String timestamp = String.format("%02d", startLDT.getDayOfMonth()) + "/" + String.format("%02d", startLDT.getMonthValue()) + "/" + startLDT.getYear() + " " + String.format("%02d", startLDT.getHour()) + ":" + String.format("%02d", startLDT.getMinute()) + ":" + String.format("%02d", startLDT.getSecond());

                            Duration duration = Duration.between(startLDT, endLDT);

                            String protocol = flow.getProtocolName();

                            if (!protocols.containsKey(protocol)) {
                                protocols.put(protocol, new AtomicLong());
                            }

                            protocols.get(protocol).getAndIncrement();

                            if (protocol.equals("tcp_ip") || protocol.equals("udp_ip")) {

                                if(protocol.equals("tcp_ip")) {
                                    protocol = "0";
                                } else {
                                    protocol = "1";
                                }

                                pw.write(counter + "," + flow.getSource() + "," + flow.getSourcePort() + "," + flow.getDestination() + "," + flow.getDestinationPort() + "," + protocol + "," + timestamp + "," + duration.getSeconds() + "," + flow.getTotalSourcePackets() + "," + flow.getTotalDestinationPackets() + "," + flow.getTotalSourceBytes() + "," + flow.getTotalDestinationBytes() + "," + flow.getTag() + "\n");

                                counter++;
                            }
                        }
                    }

                    pw.flush();
                    pw.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (Map.Entry<String, AtomicLong> entry : protocols.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue().get());
        }
    }
}
