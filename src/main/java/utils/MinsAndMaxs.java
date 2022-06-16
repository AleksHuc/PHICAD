package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class MinsAndMaxs {
    public static void main(String[] args) {

//        File folder = new File("data/ISCX-IDS-2012");
//        String startsWith = "Testbed";
        File folder = new File("data/CIC-IDS-2017");
        String startsWith = "";
        ArrayList<Integer> exclude = new ArrayList<>();
        exclude.add(0);
        exclude.add(1);
        exclude.add(3);
        exclude.add(6);
        exclude.add(84);
//        File folder = new File("data/SensorDataset");
//        String startsWith = "";




        try {

            HashMap<Integer, Double> mins = new HashMap<>();
            HashMap<Integer, Double> maxs = new HashMap<>();
            HashMap<Integer, String> labels = new HashMap<>();

            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (fileEntry.getName().startsWith(startsWith) && !fileEntry.isDirectory()) {
                    System.out.println(fileEntry);
//                    HashMap<Integer, Double> mins = new HashMap<>();
//                    HashMap<Integer, Double> maxs = new HashMap<>();
//                    HashMap<Integer, String> labels = new HashMap<>();

                    FileReader fr = new FileReader(fileEntry);
                    BufferedReader br = new BufferedReader(fr);
                    int counter = 0;
                    for(String line; (line = br.readLine()) != null; ) {

                        if (counter > 0) {
                            String[] sLine = line.split(",");

                            for (int i = 0; i<sLine.length; i++) {
                                if (!exclude.contains(i)) {
                                    double current = Double.parseDouble(sLine[i].trim());

                                    if (i == 7 && current < 0.0) {
                                        System.out.println(line);
                                    }

                                    if (!mins.containsKey(i)) {
                                        mins.put(i, current);
                                    } else {
                                        double currentMin = mins.get(i);
                                        if (current < currentMin) {
                                            mins.replace(i, current);
                                        }
                                    }
                                    if (!maxs.containsKey(i)) {
                                        maxs.put(i, current);
                                    } else {
                                        double currentMax = maxs.get(i);
                                        if (current > currentMax) {
                                            maxs.replace(i, current);
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            String[] sLine = line.split(",");
                            for (int i = 0; i<sLine.length; i++) {
                                if (!exclude.contains(i)) {
                                    labels.put(i, sLine[i].trim());
                                }
                            }
                        }
                        counter++;
                    }

//                    PrintWriter pw = new PrintWriter(new FileWriter(folder.toString() + "/Labels/MinsAndMaxs_" + fileEntry.getName().split("\\.")[0] + ".txt"));
//                    for (Map.Entry<Integer, String> entry : labels.entrySet()) {
//                        pw.print(entry.getValue() + " - Min: " + mins.get(entry.getKey()) + " Max: " + maxs.get(entry.getKey()) + "\n");
//                    }
//                    pw.flush();
//                    pw.close();
                }
            }

            PrintWriter pw = new PrintWriter(new FileWriter(folder.toString() + "/Labels/MinsAndMaxs_" + folder.getName().split("\\.")[0] + ".txt"));
            for (Map.Entry<Integer, String> entry : labels.entrySet()) {
                pw.printf(entry.getValue() + " - Min: %f "  + " Max: %f " +  "\n", mins.get(entry.getKey()), maxs.get(entry.getKey()));
            }
            pw.flush();
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

