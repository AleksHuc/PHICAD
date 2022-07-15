package utils;


import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.SECONDS;

public class ExtractVectors {

    static String[] legend = new String[]{"DIP1", "DIP2", "DIP3", "DIP4", "SP", "DP", "PROT", "FBYT", "BBYT",
                                            "FPCK", "BPCK", "DUR", "WSIN", "WCOS", "HSIN", "HCOS", "MSIN", "MCOS", "SSIN", "SCOS", "TDIF", "LAB"};

    static String[] directories = new String[]{"data/CIC-IDS-2017/", "data/ISCX-IDS-2012/"};
    static int[][] selectedValues = new int[][]{new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 84}, new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 12}};
    static ArrayList<ArrayList<String>> selectedValuesProcessing = new ArrayList<>(){{
        add(new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}});
        add(new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}});}};
    static ArrayList<HashMap<Integer, Integer>> reverseTable = new ArrayList<>(){{
        add(new HashMap<>(){{put(1, 3); put(2, 4); put(8, 9); put(10, 11); put(12, 16); put(13, 17); put(14, 18); put(15, 19); put(26, 31); put(27, 32); put(28, 33); put(29, 34); put(30, 35); put(36, 37); put(38, 39); put(40, 41); put(42, 43); put(59, 60); put(62, 65); put(63, 66); put(64, 67); put(68, 70); put(69, 71); put(72, 73); put(74, 75);}});
        add(new HashMap<>(){{put(1, 3); put(2, 4); put(8, 9); put(10, 11);}});
    }};
    static int[] selectedValuesKeys = new int[]{1, 3};
    static HashMap<Integer,long[]> minMaxValues = new HashMap<>(){{
        put(1, new long[]{0L, 255L});
        put(3, new long[]{0L, 255L});
        put(2, new long[]{0L, 65535L});
        put(4, new long[]{0L, 65535L});
        put(5, new long[]{0L, 1L});
        put(10, new long[]{0L, 34336L});
        put(11, new long[]{0L, 689853L});
        put(8, new long[]{1L, 273L});
        put(9, new long[]{0L, 408L});
        put(7, new long[]{0L, 120000000L});
        put(6, new long[]{0L, 3600L});
    }};
    static String[][] anomalies = new String[][]{new String[] {"SSH-Patator", "FTP-Patator", "DoS Slowhttptest", "DoS GoldenEye", "Heartbleed", "DoS slowloris", "DoS Hulk", "Web Attack � Sql Injection", "Web Attack � XSS", "Web Attack � Brute Force", "Infiltration", "Bot", "PortScan", "DDoS"}, new String[] {"Attack"}};

    static int selectedKey = 0;

    public static void main (String[] args) {

        try {
            int c = 0;
            for (String directory : directories) {
                File directoryFile = new File(directory + "Vectors/");
                Utils.purgeDirectory(directoryFile);

                for (final File fileEntry : Objects.requireNonNull(new File(directory).listFiles())) {
                    if (fileEntry.isFile()) {
                        System.out.println(fileEntry.getName());

                        FileReader fr = new FileReader(fileEntry);
                        BufferedReader br = new BufferedReader(fr);

                        PrintWriter pw = new PrintWriter(directory + "Vectors/" + fileEntry.getName());

                        HashMap<String, MutableLocalDateTime> previousTimestamp = new HashMap<>();

                        boolean firstLine = false;
                        for (String line; (line = br.readLine()) != null; ) {
                            String[] sFlow = line.split(",");
                            if(firstLine) {

                                if (sFlow.length > 0) {
                                    for (int index_key = 0; index_key < selectedValuesKeys.length; index_key++) {

                                        ArrayList<Double> clusteringVectorNormalized = new ArrayList<>();

                                        String ip = sFlow[selectedValuesKeys[index_key]];
                                        String direction = "fwd";

                                        if (index_key == 1) {
                                            sFlow = Utils.reverseFlow(sFlow, reverseTable.get(c));
                                            direction = "bwd";
                                        }
                                        String[] reducedFlow = new String[selectedValues[c].length];
                                        for (int j = 0; j < selectedValues[c].length; j++) {
                                            reducedFlow[j] = sFlow[selectedValues[c][j]];
                                        }

                                        double groundTruth = 0.0;
                                        String currentLabel = reducedFlow[reducedFlow.length - 1].trim();

                                        int index = 0;
                                        for (String label : anomalies[c]){
                                            if (currentLabel.equals(label)) {
                                                groundTruth = 1.0;
                                            }
                                            index++;
                                        }
                                        FlowMessage fm = new FlowMessage(reducedFlow, direction);

                                        String flowDirection = fm.getDirection();
                                        String[] flowData = fm.getFlow();

                                        int timestampIndex = selectedValuesProcessing.get(c).indexOf("t");
                                        LocalDateTime timestamp = null;
                                        if (timestampIndex >= 0) {
                                            timestamp = Utils.parseTimestamp(flowData[selectedValuesProcessing.get(c).indexOf("t")], Utils.TIME_FORMATS);
                                        }

                                        for (int i = 0; i < selectedValues[c].length; i++) {
                                            if (i != selectedKey) {
                                                String currentValue = flowData[i];
                                                int currentKey = selectedValues[c][i];
                                                long[] currentMinMax = minMaxValues.get(currentKey);
                                                String currentProcessing = selectedValuesProcessing.get(c).get(i);

                                                if (currentProcessing.equals("n")) {

                                                    clusteringVectorNormalized.add(Utils.normalize(Double.parseDouble(currentValue), currentMinMax[0], currentMinMax[1]));
                                                } else if (currentProcessing.equals("t")) {
                                                    double[] ttWeekday = Utils.trigonometricTransform(1, 7, timestamp.getDayOfWeek().getValue());
                                                    double[] ttHour = Utils.trigonometricTransform(0, 23, timestamp.getHour());
                                                    double[] ttMinute = Utils.trigonometricTransform(0, 59, timestamp.getMinute());
                                                    double[] ttSecond = Utils.trigonometricTransform(0, 59, timestamp.getSecond());

                                                    clusteringVectorNormalized.add(Utils.normalize(ttWeekday[0], -1, 1));
                                                    clusteringVectorNormalized.add(Utils.normalize(ttWeekday[1], -1, 1));
                                                    clusteringVectorNormalized.add(Utils.normalize(ttHour[0], -1, 1));
                                                    clusteringVectorNormalized.add(Utils.normalize(ttHour[1], -1, 1));
                                                    clusteringVectorNormalized.add(Utils.normalize(ttMinute[0], -1, 1));
                                                    clusteringVectorNormalized.add(Utils.normalize(ttMinute[1], -1, 1));
                                                    clusteringVectorNormalized.add(Utils.normalize(ttSecond[0], -1, 1));
                                                    clusteringVectorNormalized.add(Utils.normalize(ttSecond[1], -1, 1));

                                                    long previousDifference;
                                                    if (previousTimestamp.containsKey(ip + "_" +flowDirection)) {
                                                        MutableLocalDateTime currentPreviousTimestamp = previousTimestamp.get(ip + "_" +flowDirection);
                                                        previousDifference = SECONDS.between(currentPreviousTimestamp.getLocalDateTime(), timestamp);
                                                        currentPreviousTimestamp.setLocalDateTime(timestamp);
                                                    } else {
                                                        previousDifference = 0L;
                                                        previousTimestamp.put(ip + "_" +flowDirection, new MutableLocalDateTime(timestamp));
                                                    }

                                                    double normalizedPreviousDifference = Utils.normalize(previousDifference, currentMinMax[0], currentMinMax[1]);

                                                    clusteringVectorNormalized.add(normalizedPreviousDifference);
                                                } else {
                                                    if (!currentProcessing.equals("l")) {
                                                        String[] ipAddressInArray = currentValue.split(currentProcessing);
                                                        for (String s : ipAddressInArray) {
                                                            clusteringVectorNormalized.add(Utils.normalize(Integer.parseInt(s), currentMinMax[0], currentMinMax[1]));
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        for (int j = 0; j < clusteringVectorNormalized.size(); j++) {
                                            pw.print(clusteringVectorNormalized.get(j) + ", ");
                                        }
                                        pw.print(groundTruth + System.getProperty("line.separator"));
                                    }
                                }
                            }
                            else {
                                for (int j = 0; j < legend.length; j++) {
                                    if (j < legend.length - 1) {
                                        pw.print(legend[j] + ", ");
                                    }
                                    else {
                                        pw.print(legend[j] + System.getProperty("line.separator"));
                                    }
                                }
                                firstLine = true;
                            }

                        }

                        pw.flush();
                        pw.close();

                    }

                }
                c++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
