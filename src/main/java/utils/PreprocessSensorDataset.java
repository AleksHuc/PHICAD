package utils;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PreprocessSensorDataset {

    static public void main(String[] args) {

        File folder = new File("data/SensorDataset/Original");

        HashMap<String, HashMap<String,Integer>> data = new HashMap<>();
        HashMap<String, AtomicInteger> counts = new HashMap<>();

        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.getName().startsWith("main")) {
                System.out.println(fileEntry);
                String[] legend = null;
                try {
                    FileReader fr = new FileReader(fileEntry);
                    BufferedReader br = new BufferedReader(fr);
                    long counter = 0L;
                    for(String line; (line = br.readLine()) != null; ) {
                        String[] sLine = line.split(",");
                        if (counter > 0) {
                            for (int i = 0; i < legend.length; i++) {
                                if (!legend[i].equals("timestamp")) {
                                    if (!data.get(legend[i]).containsKey(sLine[i])) {
                                        data.get(legend[i]).put(sLine[i], counts.get(legend[i]).getAndIncrement());
                                    }
                                }
                            }
                        }
                        else {
                            legend = sLine;
                            for (String s : sLine) {
//                                System.out.println(s);
                                if (!s.equals("timestamp")) {
                                    data.put(s, new HashMap<>());
                                    counts.put(s, new AtomicInteger());
                                }
                            }
                        }
                        counter++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        double max = 0.0;
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.getName().startsWith("main")) {
                System.out.println(fileEntry);
                try {
                    FileReader fr = new FileReader(fileEntry);
                    BufferedReader br = new BufferedReader(fr);

                    TreeMap<LocalDateTime, ArrayList<String>> orderedProfiles = new TreeMap<>();
                    String[] legend = null;
                    String legendS = null;
                    long counter = 0L;
                    for(String line; (line = br.readLine()) != null; ) {
                        String[] sLine = line.split(",");
                        if (counter > 0) {

                            LocalDateTime timestamp = null;
//                            ArrayList<String> lineList = new ArrayList<>();
                            StringBuilder linelist = new StringBuilder();
                            for (int i = 0; i < legend.length; i++) {
                                if (legend[i].equals("timestamp")) {
                                    timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(sLine[i])), ZoneId.systemDefault());
                                    linelist.append(String.format("%02d", timestamp.getDayOfMonth()) + "/" + String.format("%02d", timestamp.getMonthValue()) + "/" + timestamp.getYear() + " " + String.format("%02d", timestamp.getHour()) + ":" + String.format("%02d", timestamp.getMinute()) + ":" + String.format("%02d", timestamp.getSecond()) + ",");
                                }
                                else if (legend[i].equals("value")) {
                                    if (sLine[i].equals("none") || sLine[i].equals("false") || sLine[i].equals("true") || sLine[i].equals("") || sLine[i].equals("twenty")) {
                                        linelist.append("0.0,");
                                    }
                                    else {
                                        linelist.append(sLine[i] + ",");
                                        if (Double.parseDouble(sLine[i]) > max) {
                                            if (Double.parseDouble(sLine[i]) < 10000){
                                                max = Double.parseDouble(sLine[i]);
                                            }
                                        }
                                    }
                                }
                                else if (legend[i].equals("normality")) {
//                                   System.out.println(sLine[i]);
                                    linelist.append(data.get(legend[i]).get(sLine[i]));
                                }
                                else {

                                    linelist.append(data.get(legend[i]).get(sLine[i]) + ",");
                                }
                            }
                            if (!orderedProfiles.containsKey(timestamp)) {
                                orderedProfiles.put(timestamp, new ArrayList<>());
                            }
                            orderedProfiles.get(timestamp).add(linelist.toString());
                        }
                        else {
                            legend = sLine;
                            legendS = line;
                        }
                        counter++;
                    }

                    PrintWriter f0 = new PrintWriter(new FileWriter("data/SensorDataset/" + fileEntry.getName() + "Ordered.csv"));
                    f0.println(legendS);

                    for (Map.Entry<LocalDateTime, ArrayList<String>> entry: orderedProfiles.entrySet()) {
                        for (String line: entry.getValue())
                        {
                            f0.println(line);
                        }
                    }

                    f0.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
//        System.out.println("Max: " + max);
//
//        for (Map.Entry<String, AtomicInteger> entry : counts.entrySet()) {
//            System.out.println(entry.getKey() + " " + entry.getValue().get());
//        }
    }
}
