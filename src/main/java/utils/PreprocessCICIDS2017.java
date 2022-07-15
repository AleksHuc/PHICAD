package utils;


import org.jfree.data.xy.XYSeries;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static utils.Utils.parseTimestamp;


/**
 * The class presents the preprocessing of CICIDS2017 dataset.
 */
public class PreprocessCICIDS2017 {

    /**
     * The method that preprocesses the dataset.
     * @param args String array of general input parameters.
     */
    public static void main(String args[]) {
        ArrayList<File> arrayOfFiles = new ArrayList<>();

        HashMap<String,AtomicLong> protocols = new HashMap<>();

        arrayOfFiles.add(new File("data/CIC-IDS-2017/Original/Monday-WorkingHours.pcap_ISCX.csv"));                              // 3.7.2017: 13:55:58 - 22:01:34
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Original/Tuesday-WorkingHours.pcap_ISCX.csv"));                             // 4.7.2017: 13:53:32 - 22:00:31
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Original/Wednesday-workingHours.pcap_ISCX.csv"));                           // 5.7.2017: 13:42:42 - 22:10:19
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Original/Thursday-WorkingHours-Morning-WebAttacks.pcap_ISCX.csv"));         // 6.7.2017: 13:58:58 - 22:04:44
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Original/Thursday-WorkingHours-Afternoon-Infilteration.pcap_ISCX.csv"));    //
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Original/Friday-WorkingHours-Morning.pcap_ISCX.csv"));                      // 7.7.2017: 13:59:39 - 22:02:41
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Original/Friday-WorkingHours-Afternoon-PortScan.pcap_ISCX.csv"));           //
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Original/Friday-WorkingHours-Afternoon-DDos.pcap_ISCX.csv"));               //

        DateTimeFormatter[] formats = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("d/M/yyyy H:m"),
                DateTimeFormatter.ofPattern("d/M/yyyy H:m:s")
        };

        for (File currentFile : arrayOfFiles) {
            try {
                String firstLine = null;
                System.out.println(currentFile);
                FileReader fr = new FileReader(currentFile);
                BufferedReader br = new BufferedReader(fr);

                TreeMap<LocalDateTime,ArrayList<String>> orderedProfiles = new TreeMap<>();

                LocalDateTime lowerStart = null;
                LocalDateTime lowerEnd = null;
                LocalDateTime upperStart = null;
                LocalDateTime upperEnd = null;

                HashMap<String, XYSeries> hours = new HashMap<>();

                long counter = 0L;
                for(String line; (line = br.readLine()) != null; ) {

                    if(counter > 0) {

                        String[] sFlow = line.split(",");

                        if (sFlow.length > 0) {

                            if (!hours.containsKey(sFlow[84])) {
                                hours.put(sFlow[84], new XYSeries(sFlow[84]));
                            }

                            LocalDateTime timestamp = parseTimestamp(sFlow[6], formats);

//                            hours.get(sFlow[84]).add(counter, timestamp.getHour());

                            if (counter == 1) {
                                lowerStart = parseTimestamp(timestamp.getDayOfMonth() + "/" + timestamp.getMonthValue() + "/" + timestamp.getYear() + " 08:40:00", formats);
                                lowerEnd = parseTimestamp(timestamp.getDayOfMonth() + "/" + timestamp.getMonthValue() + "/" + timestamp.getYear() + " 12:59:59", formats);
                                upperStart = parseTimestamp(timestamp.getDayOfMonth() + "/" + timestamp.getMonthValue() + "/" + timestamp.getYear() + " 01:00:00", formats);
                                upperEnd = parseTimestamp(timestamp.getDayOfMonth() + "/" + timestamp.getMonthValue() + "/" + timestamp.getYear() + " 05:59:59", formats);
                            }

                            if ((timestamp.isAfter(lowerStart) && timestamp.isBefore(lowerEnd)) || timestamp.isEqual(lowerEnd)) {
//                                timestamp = timestamp.plusHours(5);
                            }
                            else if ((timestamp.isAfter(upperStart) && timestamp.isBefore(upperEnd)) || timestamp.isEqual(upperStart)) {
                                timestamp = timestamp.plusHours(12);
                            }
                            else{
                                System.out.println("Date out of bounds!!!");
                                System.out.println(timestamp);
                            }
                            hours.get(sFlow[84]).add(counter, timestamp.getHour());

                            ArrayList<String> listOfProfiles = orderedProfiles.get(timestamp);

                            if (listOfProfiles == null) {
                                orderedProfiles.put(timestamp, new ArrayList<>());
                                listOfProfiles = orderedProfiles.get(timestamp);
                            }

                            sFlow[6] = String.format("%02d", timestamp.getDayOfMonth()) + "/" + String.format("%02d", timestamp.getMonthValue()) + "/" + timestamp.getYear() + " " + String.format("%02d", timestamp.getHour()) + ":" + String.format("%02d", timestamp.getMinute()) + ":" + String.format("%02d", timestamp.getSecond());

                            StringBuilder lineString = new StringBuilder();
                            for (int i = 0; i < sFlow.length; i++) {
                                lineString.append(sFlow[i]);
                                if (i < sFlow.length - 1) {
                                    lineString.append(",");
                                }
                                if (i == 5) {
                                    String protocol = sFlow[i];

                                    if (!protocols.containsKey(protocol)) {
                                        protocols.put(protocol, new AtomicLong());
                                    }

                                    protocols.get(protocol).getAndIncrement();

                                    if (protocol.equals("6")) {
                                        sFlow[i] = "0";
                                    } else if (protocol.equals("17")) {
                                        sFlow[i] = "1";
                                    }
                                }
                            }

                            if (sFlow[5].equals("0") || sFlow[5].equals("1")) {
                                listOfProfiles.add(lineString.toString());
                            }
                        }

                    }
                    else {
                        firstLine = line;
                    }
                    counter++;
                }

                String[] fileNameSplit = currentFile.getName().split("\\.");
                PrintWriter f0 = new PrintWriter(new FileWriter("data/CIC-IDS-2017/" + fileNameSplit[0] + "Ordered." + fileNameSplit[1] + "." + fileNameSplit[2]));

                f0.print(firstLine + "\n");

                for (LocalDateTime key: orderedProfiles.keySet()) {
                    for (String line: orderedProfiles.get(key))
                    {
                        f0.print(line + "\n");
                    }
                }

                f0.close();

            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
