package evaluation;

import phicad.MutableLocalDateTime;
import tbirchi.CFTree;
import tbirchi.SplitChangeDifference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static utils.Utils.normalize;
import static utils.Utils.trigonometricTransform;
import static java.time.temporal.ChronoUnit.SECONDS;
import static phicad.Profile.listToArray;
import static phicad.Profile.parseTimestamp;

public class DrawTreeForGiveIP {

    public static void main(String[] args) {
        ArrayList<File> arrayOfFiles = new ArrayList<>();

//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/0_192.168.2.106_bwd_Ordered_Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/1_192.168.2.112_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/2_192.168.5.122_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/3_192.168.3.114_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/4_192.168.1.104_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/5_192.168.3.117_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/6_192.168.2.108_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/7_192.168.1.101_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/8_192.168.1.102_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/9_192.168.2.110_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/10_192.168.1.101_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/11_192.168.2.112_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/12_192.168.1.105_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/13_192.168.1.105_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/14_192.168.1.103_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/15_192.168.3.115_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/16_192.168.2.109_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/17_192.168.3.117_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/18_192.168.2.113_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/19_192.168.2.106_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/20_192.168.3.115_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/21_192.168.2.111_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/22_192.168.2.113_bwd_Ordered_Flows.csv"));

//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/0_192.168.2.106_bwd_Ordered_Flows.csv"));

        int[] selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 12};
        ArrayList<String> selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}};

        String[] anomalies = new String[] {
                "Attack"
        };
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/0_172.16.0.1_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/1_192.168.10.5_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/2_192.168.10.51_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/3_192.168.10.9_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/4_52.7.235.158_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/5_192.168.10.9_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/6_192.168.10.5_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/7_192.168.10.17_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/8_192.168.10.50_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/9_192.168.10.12_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/10_205.174.165.73_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/11_192.168.10.14_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/12_192.168.10.50_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/13_205.174.165.73_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/14_192.168.10.15_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/15_192.168.10.14_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/16_192.168.10.15_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/17_192.168.10.8_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/18_52.6.13.28_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/19_172.16.0.1_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/20_192.168.10.8_fwd_Ordered_Flows.csv"));
//
//        String[] anomalies = new String[] {
//                "SSH-Patator", "FTP-Patator",
//                "DoS Slowhttptest", "DoS GoldenEye", "Heartbleed", "DoS slowloris", "DoS Hulk",
//                "Web Attack � Sql Injection", "Web Attack � XSS", "Web Attack � Brute Force",
//                "Infiltration",
//                "Bot",
//                "PortScan",
//                "DDoS"
//        };
//
//        int[] selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 84};
//        ArrayList<String> selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}};
        DateTimeFormatter[] formats = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("dd/MM/yyyy H:m"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy H:m:s")
        };

        HashMap<Integer,long[]> minMaxValues = new HashMap<>(){{
            put(1, new long[]{0L, 255L});
            put(3, new long[]{0L, 255L});
            put(2, new long[]{0L, 65535L});
            put(4, new long[]{0L, 65535L});
            put(5, new long[]{6L, 17L});
            put(10, new long[]{0L, 34336L});
            put(11, new long[]{0L, 689853L});
            put(8, new long[]{1L, 273L});
            put(9, new long[]{0L, 408L});
            put(7, new long[]{0L, 120000000L});
            put(6, new long[]{0L, 3600L});
        }};

        String resultsPathName = "results/";
//        File dir = new File(resultsPathName);
//        purgeDirectory(dir);

//        ArrayList<double[]> normalPoints = new ArrayList<>();
//        ArrayList<double[]> anomalousPoints = new ArrayList<>();

//        for (int j = 0; j < arrayOfFiles.size(); j++) {
//
//            CFTree clustering = new CFTree(8, 0, 0.7, 100, 0.0001, 0.01, 1, 5, 5, 0.1, "Test_tree");
//
//            File currentFile = arrayOfFiles.get(j);
//
//            long counter = 0L;
//
//            String flowDirection = currentFile.getName().split("_")[2];
//            int ipIndex = 1;
//            if (flowDirection.equals("bwd")) {
//                ipIndex = 3;
//            }
//            String ip = currentFile.getName().split("_")[1];
//
//            try {
//
//                PrintWriter writer = new PrintWriter(resultsPathName + "flows_" + ip + "_" + flowDirection + ".txt");
//
//                FileReader fr = new FileReader(currentFile);
//                BufferedReader br = new BufferedReader(fr);
//
//                MutableLocalDateTime previousTimestamp = null;
//
//                boolean firstLine = false;
//                for (String line; (line = br.readLine()) != null; ) {
//
//                    if (firstLine) {
//                        String[] sFlow = line.split(",");
//                        if (sFlow[ipIndex].equals(ip)) {
//
//                            double groundTruth = 0.0;
//                            String groundTruthString = "N";
//                            String currentLabel = sFlow[sFlow.length - 1].trim();
//                            for (String label : anomalies){
//                                if (currentLabel.equals(label)) {
//                                    groundTruth = 1.0;
//                                    groundTruthString = "A-" + label;
//                                }
//                            }
//
//                            String[] flowData = new String[selectedValues.length];
//                            for (int k = 0; k < selectedValues.length; k++) {
//                                flowData[k] = sFlow[selectedValues[k]];
//                            }
//
//                            int timestampIndex = selectedValuesProcessing.indexOf("t");
//                            LocalDateTime timestamp = null;
//                            if (timestampIndex >= 0) {
//                                timestamp = parseTimestamp(flowData[selectedValuesProcessing.indexOf("t")], formats);
//                            }
//
//                            ArrayList<Double> clusteringVectorNormalized = new ArrayList<>();
//                            for(int i = 0; i < selectedValues.length; i++) {
//                                if (i != 0) {
//                                    String currentValue = flowData[i];
//                                    int currentKey = selectedValues[i];
//                                    long[] currentMinMax = minMaxValues.get(currentKey);
//                                    String currentProcessing = selectedValuesProcessing.get(i);
//
//                                    if (currentProcessing.equals("n")) {
//                                        clusteringVectorNormalized.add(normalize(Double.parseDouble(currentValue), currentMinMax[0], currentMinMax[1]));
//                                    }
//                                    else if (currentProcessing.equals("t")) {
//                                        double[] ttWeekday = trigonometricTransform(1, 7, timestamp.getDayOfWeek().getValue());
//                                        double[] ttHour = trigonometricTransform(0, 23, timestamp.getHour());
//                                        double[] ttMinute = trigonometricTransform(0, 59, timestamp.getMinute());
//                                        double[] ttSecond = trigonometricTransform(0, 59, timestamp.getSecond());
//
//                                        clusteringVectorNormalized.add(normalize(ttWeekday[0], -1 , 1));
//                                        clusteringVectorNormalized.add(normalize(ttWeekday[1], -1 , 1));
//                                        clusteringVectorNormalized.add(normalize(ttHour[0], -1 , 1));
//                                        clusteringVectorNormalized.add(normalize(ttHour[1], -1 , 1));
//                                        clusteringVectorNormalized.add(normalize(ttMinute[0], -1 , 1));
//                                        clusteringVectorNormalized.add(normalize(ttMinute[1], -1 , 1));
//                                        clusteringVectorNormalized.add(normalize(ttSecond[0], -1 , 1));
//                                        clusteringVectorNormalized.add(normalize(ttSecond[1], -1 , 1));
//
//                                        long previousDifference;
//                                        if (previousTimestamp != null) {
//                                            MutableLocalDateTime currentPreviousTimestamp = previousTimestamp;
//                                            previousDifference = SECONDS.between(currentPreviousTimestamp.getLocalDateTime(), timestamp);
//                                            currentPreviousTimestamp.setLocalDateTime(timestamp);
//                                        }
//                                        else {
//                                            previousDifference = 0L;
//                                            previousTimestamp = new MutableLocalDateTime(timestamp);
//                                        }
//
//                                        double normalizedPreviousDifference = normalize(previousDifference, currentMinMax[0], currentMinMax[1]);
//
//                                        clusteringVectorNormalized.add(normalizedPreviousDifference);
//                                    }
//                                    else {
//                                        if (!currentProcessing.equals("l")) {
//                                            String[] ipAddressInArray = currentValue.split(currentProcessing);
//                                            for (String s : ipAddressInArray) {
//                                                clusteringVectorNormalized.add(normalize(Integer.parseInt(s), currentMinMax[0], currentMinMax[1]));
//                                            }
//                                        }
//                                    }
//
//                                }
//                            }
////                            System.out.println(Arrays.toString(listToArray(clusteringVectorNormalized)));
//                            SplitChangeDifference splitClusteringDifferences = clustering.insertPoint(listToArray(clusteringVectorNormalized), timestamp, "0.0");
////                            if (groundTruth > 0.0) {
////                                normalPoints.add(listToArray(clusteringVectorNormalized));
////                            }
////                            else {
////                                anomalousPoints.add(listToArray(clusteringVectorNormalized));
////                            }
//                            writer.println(groundTruthString + "," + counter + "," + Arrays.toString(listToArray(clusteringVectorNormalized)));
//                            ArrayList<double[]> centroids = clustering.getCentroidsAndRadius();
//                            for (double[] c : centroids) {
//                                writer.println("C," + Arrays.toString(c));
//                            }
//                        }
//                    }
//                    else {
//                        firstLine = true;
//                    }
//                    counter++;
//
//                }
//
//                writer.flush();
//                writer.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    private static void purgeDirectory(File dir) {
        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory())
                purgeDirectory(file);
            boolean result = file.delete();
            if (!result) {
                System.out.println("Delete failed: " + file.getName());
            }
        }
    }
}
