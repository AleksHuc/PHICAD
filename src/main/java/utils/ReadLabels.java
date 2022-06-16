package utils;

import mst.In;
import phicad.Profile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.temporal.ChronoUnit.SECONDS;

public class ReadLabels {

    public static void main(String[] args) {

        File folder = new File("data/ISCX-IDS-2012");
        String startsWith = "Testbed";
//        File folder = new File("data/CIC-IDS-2017");
//        String startsWith = "";
//        File folder = new File("data/SensorDataset");
//        String startsWith = "";

        DateTimeFormatter[] formats = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("dd/MM/yyyy H:m"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy H:m:s")
        };

        LocalDateTime previousTS = null;

        HashMap<String, AtomicLong> countsAll = new HashMap<>();

        try {
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (fileEntry.getName().startsWith(startsWith) && !fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
                    HashMap<String, AtomicLong> counts = new HashMap<>();

                    FileReader fr = new FileReader(fileEntry);
                    BufferedReader br = new BufferedReader(fr);
                    int counter = 0;
                    for(String line; (line = br.readLine()) != null; ) {

                        if (counter > 0) {
                            String[] sLine = line.split(",");
                            String label = sLine[sLine.length - 1];
                            if(counts.containsKey(label)) {
                                counts.get(label).getAndIncrement();
                            }
                            else {
                                AtomicLong al = new AtomicLong();
                                al.getAndIncrement();
                                counts.put(label, al);
                            }
                            if(countsAll.containsKey(label)) {
                                countsAll.get(label).getAndIncrement();
                            }
                            else {
                                AtomicLong al = new AtomicLong();
                                al.getAndIncrement();
                                countsAll.put(label, al);
                            }

//                            String timestamp = sLine[6];
//                            LocalDateTime ts = Profile.parseTimestamp(timestamp, formats);
//                            if (previousTS != null) {
//                                Long dif = SECONDS.between(previousTS, ts);
//                                if (dif < 0) {
//                                    System.nanoTime();
//                                }
//
//                            }
//
//                            previousTS = ts;

                        }
                        counter++;
                    }

                    PrintWriter pw = new PrintWriter(new FileWriter(folder.toString() + "/Labels/Labels_" + fileEntry.getName().split("\\.")[0] + ".txt"));
                    for (Map.Entry<String, AtomicLong> entry : counts.entrySet()) {
                        pw.print(entry.getKey() + ": " + entry.getValue() + "\n");
                    }
                    pw.flush();
                    pw.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        long anomalies = 0L;
        long benign = 0L;
        for (Map.Entry<String, AtomicLong> entry : countsAll.entrySet()) {
            System.out.print(entry.getKey() + ": " + entry.getValue() + "\n");
            if (!entry.getKey().equals("BENIGN")) {
                anomalies += entry.getValue().get();
            }
            else {
                benign = entry.getValue().get();
            }
        }
        System.out.println("Anomalies: " + anomalies);
        System.out.println("All flows: " + (anomalies + benign));
    }
}
