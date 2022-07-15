package phi2cad;

import evaluation.CalculateResults3;
import lctbirchi.CF;
import lctbirchi.CFTree;
import lctbirchi.SplitChangeDifference;
import utils.FlowMessage;
import utils.Utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;

public class SingleThreadMainSingleFile {

    static boolean printOut = true;

    static int maxChildren = 8;
    static int pointLength = 21;
//    static int pointLength = 12;
    static int checkStep = 5;
    static int maxBins = 5;
    static int sizeOfBin = 5;
    static int distanceFunction = 0;

    static double delta = 0.01;
    static double clearThreshold = 0.05;
    static double thresholdT = 1800;
    static double timestampRateMax = 432000.0;
    static double normalClusterThreshold = 0.5;

    static int[] selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 84};
    //        static int[] selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 84};
    static ArrayList<String> selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}};
    //        static ArrayList<String>  selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("l");}};

//        static int[] selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 12};
//    static ArrayList<String> selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}};

    static int[] selectedValuesKeys = new int[]{1, 3};

    static HashMap<Integer,Integer> reverseTable = new HashMap<>(){{
        put(1, 3); put(2, 4); put(8, 9); put(10, 11); put(12, 16); put(13, 17); put(14, 18); put(15, 19);
        put(26, 31); put(27, 32); put(28, 33); put(29, 34); put(30, 35); put(36, 37); put(38, 39); put(40, 41);
        put(42, 43); put(59, 60); put(62, 65); put(63, 66); put(64, 67); put(68, 70); put(69, 71); put(72, 73);
        put(74, 75);
    }};

//        static  HashMap<Integer, Integer> reverseTable = new HashMap<>(){{
//        put(1, 3); put(2, 4); put(8, 9); put(10, 11);
//    }};

//    static HashMap<Integer,long[]> minMaxValues = new HashMap<>(){{
//        put(1, new long[]{0L, 255L});
//        put(3, new long[]{0L, 255L});
//        put(2, new long[]{0L, 65535L});
//        put(4, new long[]{0L, 65535L});
//        put(5, new long[]{0L, 255L});
//        put(10, new long[]{0L, 100000L});
//        put(11, new long[]{0L, 100000L});
//        put(8, new long[]{1L, 100L});
//        put(9, new long[]{0L, 100L});
//        put(7, new long[]{0L, 120000000L});
//        put(6, new long[]{0L, 3600L});
//    }};
//    static HashMap<Integer,long[]> minMaxValues = new HashMap<>(){{
//        put(1, new long[]{0L, 255L});
//        put(3, new long[]{0L, 255L});
//        put(2, new long[]{0L, 65535L});
//        put(4, new long[]{0L, 65535L});
//        put(5, new long[]{0L, 255L});
//        put(10, new long[]{0L, 12900000L});
//        put(11, new long[]{0L, 655453030L});
//        put(8, new long[]{1L, 219759L});
//        put(9, new long[]{0L, 291922L});
//        put(7, new long[]{0L, 120000000L});
//        put(6, new long[]{0L, 3600L});
//    }};

    /*
    Threshold: 0.001 for Total Fwd Packets X: 273.00.0 Y: 0,000994
    Threshold: 0.001 for Total Backward Packets X: 408.00.0 Y: 0,000998
    Threshold: 0.001 for Total Length of Fwd Packets X: 34336.00.0 Y: 0,001000
    Threshold: 0.001 for Total Length of Bwd Packets X: 689853.00.0 Y: 0,001000
     */

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

    /* SensorDataset */

//    static HashMap<Integer, long[]> minMaxValues = new HashMap<>(){{
////        put(1, new long[]{0, 4294967295L});
////        put(3, new long[]{0, 4294967295L});
//        put(0, new long[]{0, 83L});
//        put(1, new long[]{0, 88L});
//        put(2, new long[]{0, 7L});
//        put(3, new long[]{0, 20L});
//        put(4, new long[]{0, 84L});
//        put(5, new long[]{0, 7L});
//        put(6, new long[]{0, 20L});
//        put(7, new long[]{1, 169L});
//        put(8, new long[]{0, 12L});
//        put(9, new long[]{0, 3L});
//        put(10, new long[]{0, 39L});
//        put(11, new long[]{0, 3600L});
//    }};
//
//    static int[] selectedValues = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
//    static ArrayList<String> selectedValuesProcessing = new ArrayList<>(){{add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}};
//    static int[] selectedValuesKeys = new int[]{0};
//
//    static HashMap<Integer, Integer> reverseTable = new HashMap<>();

    static DateTimeFormatter[] formats = new DateTimeFormatter[]{
        DateTimeFormatter.ofPattern("dd/MM/yyyy H:m"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy H:m:s")
    };

    public static void main(String[] args) {

        long startTime = System.nanoTime();

        ArrayList<File> arrayOfFiles = new ArrayList<>();

//        arrayOfFiles.add(new File("data/SensorDataset/mainSimulationAccessTraces.csvOrdered.csv"));

        arrayOfFiles.add(new File("data/CIC-IDS-2017/Monday-WorkingHoursOrdered.pcap_ISCX.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Tuesday-WorkingHoursOrdered.pcap_ISCX.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Wednesday-workingHoursOrdered.pcap_ISCX.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Morning-WebAttacksOrdered.pcap_ISCX.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Afternoon-InfilterationOrdered.pcap_ISCX.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-MorningOrdered.pcap_ISCX.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-PortScanOrdered.pcap_ISCX.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-DDosOrdered.pcap_ISCX.csv"));
//
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/1_192.168.10.8_fwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/2_192.168.10.9_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/3_192.168.10.50_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/4_192.168.10.51_bwd_Ordered_Flows.csv"));
////
        ArrayList<String[]> arrayOfAnomalies = new ArrayList<>();

//        arrayOfAnomalies.add(new String[]{"1", "2", "3", "4", "5", "6", "7"});

        arrayOfAnomalies.add(new String[]{});
        arrayOfAnomalies.add(new String[]{"SSH-Patator", "FTP-Patator"});
        arrayOfAnomalies.add(new String[]{"DoS Slowhttptest", "DoS GoldenEye", "Heartbleed", "DoS slowloris", "DoS Hulk"});
        arrayOfAnomalies.add(new String[]{"Web Attack � Sql Injection", "Web Attack � XSS", "Web Attack � Brute Force"});
        arrayOfAnomalies.add(new String[]{"Infiltration"});
        arrayOfAnomalies.add(new String[]{"Bot"});
        arrayOfAnomalies.add(new String[]{"PortScan"});
        arrayOfAnomalies.add(new String[]{"DDoS"});

        String[] anomalies = new String[] {
                "SSH-Patator", "FTP-Patator",
                "DoS Slowhttptest", "DoS GoldenEye", "Heartbleed", "DoS slowloris", "DoS Hulk",
                "Web Attack � Sql Injection", "Web Attack � XSS", "Web Attack � Brute Force",
                "Infiltration",
                "Bot",
                "PortScan",
                "DDoS"
        };

//        String[] anomalies = new String[] {
//                "Attack"
//        };

        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedSatJun12Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedSunJun13Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedMonJun14Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedTueJun15-1Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedTueJun15-2Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedTueJun15-3Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedWedJun16-1Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedWedJun16-2Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedWedJun16-3Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedThuJun17-1Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedThuJun17-2Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedThuJun17-3Flows.csv"));

//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/0_192.168.2.106_bwd_Ordered_Flows.csv"));
//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/1_192.168.2.112_fwd_Ordered_Flows.csv"));
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


        String resultsPathName = "results/";
        File dir = new File(resultsPathName);
        Utils.purgeDirectory(dir);

        int anomalousFlows = 0;

        try {
            PrintWriter pw = new PrintWriter("results/result_strings5.txt");

            //             Test the best per file results

//            double[] thresholds = new double[]{
////                    0.01,
//                    0.11, 0.24, 0.03, 0.01, 0.12, 0.17, 0.20};
//            double[] lambdas = new double[]{
////                    0.009,
//                    0.001, 0.006, 0.008, 0.001, 0.008, 0.009, 0.008};
//
//            double[] thresholds = new double[]{
////                    0.01,
//                    0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8};
//            double[] lambdas = new double[]{
////                    0.009,
//                    0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001};

//            double[] thresholds = new double[]{
//                    0.1, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8};
//            double[] thresholds = new double[]{
//                    0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
            double[] thresholds = new double[]{
                    0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
//                    0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3};
            double[] lambdas = new double[]{
                    0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01};

//            double[] thresholds = new double[] {0.17};
//
//            double[] lambdas = new double[] {0.009};

            for (int i = 0; i < arrayOfFiles.size(); i++) {
                long innerStartTime = System.nanoTime();

                HashMap<String, Profile> profiles = new HashMap<>();

                String fileName = arrayOfFiles.get(i).getName().split("\\.")[0];

//                String fileName = arrayOfFiles.get(i).getName().split("_")[1] + "_" + arrayOfFiles.get(i).getName().split("_")[2];

                System.out.println(fileName + " " + thresholds[i]);
                pw.println(arrayOfFiles.get(i).getName());
                pw.flush();

                String resultsPathName2 = "results/" + fileName + "/";

                File folder = new File(resultsPathName2);
                if (folder.exists() && folder.isDirectory()) {
                    Utils.purgeDirectory(folder);
                }
                else {
                    boolean newFolder = folder.mkdir();
                    if (!newFolder) {
                        System.out.println("New folder creation failed!");
                    }
                }

                final HashMap<String, PrintWriter> pw2 = new HashMap<>();
//                for (String direction : new String[]{"fwd", "bwd"}) {
//                    pw2.put(direction, new PrintWriter(resultsPathName2 + "results_" + fileName + "_" + direction + ".txt"));
//                }

                ArrayList<File> currentFileArray = new ArrayList<>();
//                currentFileArray.add(new File("data/CIC-IDS-2017/Monday-WorkingHoursOrdered.pcap_ISCX.csv"));
                currentFileArray.add(arrayOfFiles.get(i));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedSunJun13Flows.csv"));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedMonJun14Flows.csv"));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedTueJun15-1Flows.csv"));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedTueJun15-2Flows.csv"));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedTueJun15-3Flows.csv"));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedWedJun16-1Flows.csv"));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedWedJun16-2Flows.csv"));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedWedJun16-3Flows.csv"));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedThuJun17-1Flows.csv"));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedThuJun17-2Flows.csv"));
//                currentFileArray.add(new File("data/ISCX-IDS-2012/TestbedThuJun17-3Flows.csv"));
                currentFileArray.add(new File("data/CIC-IDS-2017/Tuesday-WorkingHoursOrdered.pcap_ISCX.csv"));
                currentFileArray.add(new File("data/CIC-IDS-2017/Wednesday-workingHoursOrdered.pcap_ISCX.csv"));
                currentFileArray.add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Morning-WebAttacksOrdered.pcap_ISCX.csv"));
                currentFileArray.add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Afternoon-InfilterationOrdered.pcap_ISCX.csv"));
                currentFileArray.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-MorningOrdered.pcap_ISCX.csv"));
                currentFileArray.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-PortScanOrdered.pcap_ISCX.csv"));
                currentFileArray.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-DDosOrdered.pcap_ISCX.csv"));

                long counter = 0L;

                LocalDateTime previousTS = null;

                pw2.put("2level", new PrintWriter(resultsPathName2 + "results_2_level.txt"));
                lctbirchi.CFTree secondLevelCFTree = new CFTree(maxChildren, 0, 200, 10000, 0.2, 0.2, delta, checkStep, maxBins, sizeOfBin, normalClusterThreshold, "2level");

                for (File currentFile : currentFileArray) {

                    System.out.println(currentFile.getName());

                    FileReader fr = new FileReader(currentFile);
                    BufferedReader br = new BufferedReader(fr);

                    boolean firstLine = false;
                    for(String line; (line = br.readLine()) != null;) {


//                        System.out.println(counter);
//                        if (counter == 382433) {
//                            System.out.println(counter);
//                        }

                        if(firstLine) {



                            String[] sFlow = line.split(",");

                            String timestampString = sFlow[6];
                            LocalDateTime timestamp = Profile.parseTimestamp(timestampString, formats);
                            if (previousTS != null) {
                                Long dif = SECONDS.between(previousTS, timestamp);
                                if (dif < 0) {
                                    System.nanoTime();
                                }

                            }

                            previousTS = timestamp;

                            if (sFlow.length > 0) {

                                for (int index_key = 0; index_key < selectedValuesKeys.length; index_key++) {
                                    String ip = sFlow[selectedValuesKeys[index_key]];
                                    String id = String.valueOf(counter);
                                    String direction = "fwd";

                                    if (index_key == 1) {
                                        sFlow = reverseFlow(sFlow);
                                        direction = "bwd";
                                    }
                                    String[] reducedFlow = new String[selectedValues.length];
                                    for (int j = 0; j < selectedValues.length; j++) {
                                        reducedFlow[j] = sFlow[selectedValues[j]];
                                    }

                                    double groundTruth = 0.0;
                                    String currentLabel = reducedFlow[reducedFlow.length - 1].trim();
//                                    for (String label : arrayOfAnomalies.get(i)){
                                    for (String label : anomalies){
                                        if (currentLabel.equals(label)) {
                                            groundTruth = 1.0;
                                            anomalousFlows++;
                                        }
                                    }
                                    FlowMessage fm = new FlowMessage(reducedFlow, direction);

                                    String currentIP = fm.getFlow()[0];
//                System.out.println(currentIP);
                                    Profile profile = profiles.get(currentIP);
                                    if (profile == null) {
                                        profile = new Profile(minMaxValues, selectedValues, selectedValuesProcessing, 0,
                                                maxChildren, thresholds[i], pointLength, delta, clearThreshold, checkStep,
                                                maxBins, sizeOfBin, thresholdT, timestampRateMax, formats, printOut, lambdas[i], distanceFunction, normalClusterThreshold, currentIP);
                                        profiles.put(currentIP, profile);
                                        pw2.put(currentIP + "_fwd", new PrintWriter(resultsPathName2 + "results_" + fileName + "_" + currentIP + "_bwd.txt"));
                                        pw2.put(currentIP + "_bwd", new PrintWriter(resultsPathName2 + "results_" + fileName + "_" + currentIP + "_fwd.txt"));
                                    }



//                                    if (ip.equals(fileName.split("_")[0]) && direction.equals(fileName.split("_")[1]) && groundTruth > 0.0) {
//                                        profile.drawTree(direction);
//                                    }

//                                    int counterll = 0;
//                                    CFNode currentNode = profile.getClustering().get(direction).getLeafList();
//                                    while (currentNode != null) {
//                                        if (!currentNode.isDummy()) {
//                                            for (CF cf : currentNode.getCfList()) {
//                                                counterll++;
//                                            }
//                                        }
//                                        currentNode = currentNode.getNextCFLeaf();
//                                    }



//                                    if (counter == 515 && direction.equals("fwd")) {
//                                        System.out.println(counter + " " + direction);
//                                    }

                                    ProfileUpdate profileUpdate = profile.updateProfile(fm);

                                    double[] anomaly = profileUpdate.getDetections();
                                    double[] response = new double[]{anomaly[1], groundTruth, anomaly[0], anomaly[2], anomaly[3], anomaly[4]};
//                                    double[] response = new double[]{anomaly[1], groundTruth, anomaly[0]};

//                                    if (anomaly[0] > 0 && groundTruth > 0) {
//                                        System.out.println("BOTH: " + counter + " " + direction);
//                                    } else if (anomaly[0] > 0 ) {
//                                        System.out.println(counter + " " + direction);
//                                    }
//                                    if (anomaly[0] > 0 && counterll > 5) {
//                                        System.out.println(counter + " " + direction);
//                                    }


                                    if (printOut) {
                                        response = Utils.concatTwo(response, Arrays.copyOfRange(anomaly, 5, anomaly.length));
                                    }

                                    StringBuilder builder = new StringBuilder();
                                    builder.append(currentIP);
                                    builder.append("_");
                                    builder.append(fm.getDirection());
                                    builder.append("_");
                                    builder.append(id);
                                    builder.append(" ");
                                    for(double s : response) {
                                        builder.append(s);
                                        builder.append(" ");
                                    }
                                    String writableResponse = builder.toString();

                                    PrintWriter current = pw2.get(currentIP + "_" + fm.getDirection());

//                                    if (current == null) {
//                                        System.out.println();
//                                    }

                                    current.println(writableResponse);
                                    current.flush();

//                                    ProfileTreeMessage profileTreeMessage = new ProfileTreeMessage(groundTruth == 1.0, currentIP, direction, profileUpdate.getCFList(), profileUpdate.getTimeStamp(), profileUpdate.getDetections());

//                                    System.out.println(currentIP + "_" + direction + "_" + profileUpdate.getCFList().size());

                                    boolean anomalyB = false;
                                    if (anomaly[0] > 0.0) {
                                        anomalyB = true;
                                    }

                                    SplitChangeDifference secondLevelSCD = secondLevelCFTree.insertPoint(currentIP + "_" + direction, profileUpdate.getCFList(), profileUpdate.getTimeStamp(), anomalyB);

//                                    if (("192.168.5.122_fwd").equals(currentIP + "_" + direction)) {
//                                        CF currentCF = secondLevelCFTree.getStoredProfiles().get("192.168.5.122_fwd");
//                                        currentCF.getTree().print(resultsPathName2 + "tree_192.168.5.122_fwd_" + counter + ".txt");
//                                        currentCF.getTree().checkZeroCFs();
//                                        currentCF.getTree().checkZeroCFs2(currentCF.getTree().getRoot());
//                                    }
//                                    secondLevelCFTree.print(resultsPathName2 + "tree_2_" + counter + "_" + direction + ".txt");

                                    byte[] adwin = secondLevelSCD.getChangeDifferences();
                                    double adwinChange = 0.0;
                                    double adwinNormalized = 0.0;
                                    double intraNormalized = 0.0;
                                    double level1 = 0.0;
                                    if (adwin != null) {
                                        adwinChange = adwin[0];
                                        intraNormalized = adwin[1];
                                        level1 = adwin[2];
                                    }

                                    byte[] inter = secondLevelSCD.isInterClusterAnomaly();
                                    double interChange = 0.0;
                                    double interNormalized = 0.0;
                                    if (inter != null) {
                                        for (int j = 0; j < inter.length; j++) {
                                            interNormalized += inter[j];
                                        }
                                        if (interNormalized > 0.0) {
                                            interChange = 1.0;
                                        }
                                        interNormalized /= inter.length;
                                    }

                                    double cluster = 0.0;
                                    if (secondLevelSCD.isClusterAnomaly()) {
                                        cluster = 1.0;
                                    }

//                                    adwinNormalized = (anomaly[0] + adwinChange + interNormalized + intraNormalized + cluster)/5.0;
                                    adwinNormalized = (level1 + adwinChange + interNormalized + intraNormalized + cluster)/5.0;
//                                    adwinNormalized = (adwinChange + interNormalized + intraNormalized + cluster)/4.0;

//                                    if (anomaly[0] > 0.0 && adwinNormalized < 0.1) {
//                                        System.out.println();
//                                    }
//                                    adwinNormalized = (adwinChange + interNormalized + intraNormalized + cluster)/4.0;

                                    anomaly = new double[]{adwinNormalized, adwinChange, interNormalized, intraNormalized, cluster};
                                    response = new double[]{anomaly[1], groundTruth, anomaly[0], anomaly[2], anomaly[3], anomaly[4]};

                                    builder = new StringBuilder();
                                    builder.append(currentIP);
                                    builder.append("_");
                                    builder.append(fm.getDirection());
                                    builder.append("_");
                                    builder.append(id);
                                    builder.append(" ");
                                    for(double s : response) {
                                        builder.append(s);
                                        builder.append(" ");
                                    }
                                    writableResponse = builder.toString();

                                    current = pw2.get("2level");
                                    current.println(writableResponse);
                                    current.flush();

                                }
                            }

                        } else {
                            firstLine = true;
                        }
                        counter++;
//                        System.gc();

                        if (counter % 10000 == 0 && counter > 0) {
                            System.out.println(counter);
                        }

//                        if (counter % 100000 == 0 && counter > 0) {
//                            for (Map.Entry<String, Profile> p : profiles.entrySet()) {
//                                p.getValue().getClustering().get("fwd").print(resultsPathName2 + "tree_test_" + p.getKey() + "_fwd_" + counter + ".txt");
//                                p.getValue().getClustering().get("bwd").print(resultsPathName2 + "tree_test_" + p.getKey() + "_bwd_" + counter + ".txt");
//                            }
//                        }
                    }
                }

                secondLevelCFTree.print(resultsPathName2 + "tree_2_final.txt");

                System.out.println(counter);

//                for (Map.Entry<String, Profile> p : profiles.entrySet()) {
//                    p.getValue().getClustering().get("fwd").print(resultsPathName2 + "tree_test_" + p.getKey() + "_fwd_end.txt");
//
//                    CFNode currentNode = p.getValue().getClustering().get("fwd").getLeafList();
//                    while (currentNode != null) {
//                        if (!currentNode.isDummy()) {
//                            for (CF c : currentNode.getCfList()) {
//                                if (c.pw != null) {
//                                    c.pw.close();
//                                }
//                            }
//                        }
//                        currentNode = currentNode.getNextCFLeaf();
//                    }
//
//                    p.getValue().getClustering().get("bwd").print(resultsPathName2 + "tree_test_" + p.getKey() + "_bwd_end.txt");
//
//                    currentNode = p.getValue().getClustering().get("bwd").getLeafList();
//                    while (currentNode != null) {
//                        if (!currentNode.isDummy()) {
//                            for (CF c : currentNode.getCfList()) {
//                                if (c.pw != null) {
//                                    c.pw.close();
//                                }
//                            }
//                        }
//                        currentNode = currentNode.getNextCFLeaf();
//                    }
//                }

                for (Map.Entry<String, PrintWriter> entry : pw2.entrySet()) {
                    entry.getValue().flush();
                    entry.getValue().close();
                }
//                pw2.get("fwd").flush();
//                pw2.get("bwd").flush();
//                pw2.get("fwd").close();
//                pw2.get("bwd").close();

//                CalculateResults2 cr = new CalculateResults2();
//                cr.calculate(resultsPathName2, fileName, printOut, pw);

//                System.out.println("anomalousFlows: " + anomalousFlows);

                CalculateResults3 cr3 = new CalculateResults3();
                cr3.calculate(resultsPathName2, "2_level.txt", printOut, pw);

//                DrawTimeSeries2 ts = new DrawTimeSeries2();
//                ts.draw(resultsPathName + fileName + "/", fileName);

                long innerEstimatedTime = System.nanoTime() - innerStartTime;
                long seconds = TimeUnit.NANOSECONDS.toSeconds(innerEstimatedTime) % 60;
                long minutes = TimeUnit.NANOSECONDS.toMinutes(innerEstimatedTime) % 60;
                long hours = TimeUnit.NANOSECONDS.toHours(innerEstimatedTime) % 24;

                pw.printf("Elapsed time: %02d h, %02d m, %02d s %n", hours, minutes, seconds);
            }

            long estimatedTime = System.nanoTime() - startTime;
            long seconds = TimeUnit.NANOSECONDS.toSeconds(estimatedTime) % 60;
            long minutes = TimeUnit.NANOSECONDS.toMinutes(estimatedTime) % 60;
            long hours = TimeUnit.NANOSECONDS.toHours(estimatedTime) % 24;

            pw.printf("Elapsed time: %02d h, %02d m, %02d s %n", hours, minutes, seconds);

            pw.flush();
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] reverseFlow(String[] flow) {
        String[] reversedFlow = new String[flow.length];

        for (int i = 0; i < flow.length; i++) {
            if (reverseTable.containsKey(i)) {
                reversedFlow[i] = flow[reverseTable.get(i)];
                reversedFlow[reverseTable.get(i)] = flow[i];
            }
            else if (!reverseTable.containsValue(i)) {
                reversedFlow[i] = flow[i];
            }
        }

        return reversedFlow;
    }
}
