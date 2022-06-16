package phi2cad;

import evaluation.CalculateResults;
import evaluation.DrawTimeSeries;
import javafx.application.Application;
import javafx.stage.Stage;
import utils.JavaFXRealTimeGraph;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static utils.Utils.concatTwo;
import static java.time.temporal.ChronoUnit.SECONDS;

public class SingleThreadMainSingleFileJavaFX extends Application {

    static boolean printOut = true;

    static int maxChildren = 8;
    static int pointLength = 21;
//    static int pointLength = 12;
    static int checkStep = 1;
    static int maxBins = 5;
    static int sizeOfBin = 5;
    static int distanceFunction = 0;

    static double delta = 0.01;
    static double clearThreshold = 0.05;
    static double thresholdT = 1800;
    static double timestampRateMax = 432000.0;
    static double normalClusterThreshold = 0.1;

//    static int[] selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 84};
//    //        static int[] selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 84};
//    static ArrayList<String> selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}};
//    //        static ArrayList<String>  selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("l");}};

        static int[] selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 12};
    static ArrayList<String> selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}};

    static int[] selectedValuesKeys = new int[]{1, 3};

//    static HashMap<Integer,Integer> reverseTable = new HashMap<>(){{
//        put(1, 3); put(2, 4); put(8, 9); put(10, 11); put(12, 16); put(13, 17); put(14, 18); put(15, 19);
//        put(26, 31); put(27, 32); put(28, 33); put(29, 34); put(30, 35); put(36, 37); put(38, 39); put(40, 41);
//        put(42, 43); put(59, 60); put(62, 65); put(63, 66); put(64, 67); put(68, 70); put(69, 71); put(72, 73);
//        put(74, 75);
//    }};

        static  HashMap<Integer, Integer> reverseTable = new HashMap<>(){{
        put(1, 3); put(2, 4); put(8, 9); put(10, 11);
    }};

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
        put(5, new long[]{6L, 17L});
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

    @Override
    public void start(Stage primaryStage) throws Exception {

        long startTime = System.nanoTime();

        ArrayList<File> arrayOfFiles = new ArrayList<>();

//        arrayOfFiles.add(new File("data/SensorDataset/mainSimulationAccessTraces.csvOrdered.csv"));

//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Monday-WorkingHoursOrdered.pcap_ISCX.csv"));
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

//        String[] anomalies = new String[] {
//                "SSH-Patator", "FTP-Patator",
//                "DoS Slowhttptest", "DoS GoldenEye", "Heartbleed", "DoS slowloris", "DoS Hulk",
//                "Web Attack � Sql Injection", "Web Attack � XSS", "Web Attack � Brute Force",
//                "Infiltration",
//                "Bot",
//                "PortScan",
//                "DDoS"
//        };

        String[] anomalies = new String[] {
                "Attack"
        };

//        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedSatJun12Flows.csv"));
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

        arrayOfFiles.add(new File("data/ISCX-IDS-2012/0_192.168.2.106_bwd_Ordered_Flows.csv"));

        String resultsPathName = "results/";
        File dir = new File(resultsPathName);
        purgeDirectory(dir);

        LinkedBlockingQueue<double[]> bQueue = new LinkedBlockingQueue<>();
        JavaFXRealTimeGraph graph = new JavaFXRealTimeGraph(primaryStage, bQueue);
        graph.run();

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

            double[] thresholds = new double[]{
                    0.7, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8, 0.8};
            double[] lambdas = new double[]{
                    0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001};

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
                    purgeDirectory(folder);
                }
                else {
                    boolean newFolder = folder.mkdir();
                    if (!newFolder) {
                        System.out.println("New folder creation failed!");
                    }
                }

                final HashMap<String, PrintWriter> pw2 = new HashMap<>();
                for (String direction : new String[]{"fwd", "bwd"}) {
                    pw2.put(direction, new PrintWriter(resultsPathName2 + "results_" + fileName + "_" + direction + ".txt"));
                }

                ArrayList<File> currentFileArray = new ArrayList<>();
//                currentFileArray.add(new File("data/CIC-IDS-2017/Monday-WorkingHoursOrdered.pcap_ISCX.csv"));
                currentFileArray.add(arrayOfFiles.get(i));
//                currentFileArray.add(new File("data/CIC-IDS-2017/Tuesday-WorkingHoursOrdered.pcap_ISCX.csv"));
//                currentFileArray.add(new File("data/CIC-IDS-2017/Wednesday-workingHoursOrdered.pcap_ISCX.csv"));
//                currentFileArray.add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Morning-WebAttacksOrdered.pcap_ISCX.csv"));
//                currentFileArray.add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Afternoon-InfilterationOrdered.pcap_ISCX.csv"));
//                currentFileArray.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-MorningOrdered.pcap_ISCX.csv"));
//                currentFileArray.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-PortScanOrdered.pcap_ISCX.csv"));
//                currentFileArray.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-DDosOrdered.pcap_ISCX.csv"));

                long counter = 0L;

                LocalDateTime previousTS = null;

                for (File currentFile : currentFileArray) {

                    FileReader fr = new FileReader(currentFile);
                    BufferedReader br = new BufferedReader(fr);

                    boolean firstLine = false;
                    for(String line; (line = br.readLine()) != null;) {

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
                                        }
                                    }

                                    FlowMessage fm = new FlowMessage(reducedFlow, direction, Double.toString(groundTruth));

                                    String currentIP = fm.getFlow()[0];
//                System.out.println(currentIP);
                                    Profile profile = profiles.get(currentIP);
                                    if (profile == null) {
                                        profile = new Profile(minMaxValues, selectedValues, selectedValuesProcessing, 0,
                                                maxChildren, thresholds[i], pointLength, delta, clearThreshold, checkStep,
                                                maxBins, sizeOfBin, thresholdT, timestampRateMax, formats, printOut, lambdas[i], distanceFunction, normalClusterThreshold, currentIP);
                                        profiles.put(currentIP, profile);
                                    }



//                                    if (ip.equals(fileName.split("_")[0]) && direction.equals(fileName.split("_")[1]) && groundTruth > 0.0) {
//                                        profile.drawTree(direction);
//                                    }

//                                    double[] anomaly = profile.updateProfile(fm);

//                                    double[] response = new double[]{anomaly[1], groundTruth, anomaly[0]};
//
//                                    if (ip.equals("192.168.2.106") && direction.equals("bwd")) {
//
//                                        bQueue.put(concatTwo(response, Arrays.copyOfRange(anomaly, 2, anomaly.length)));
//
//                                    }
//                                    if (printOut) {
//                                        response = concatTwo(response, Arrays.copyOfRange(anomaly, 2, anomaly.length));
//                                    }
//
//                                    StringBuilder builder = new StringBuilder();
//                                    builder.append(currentIP);
//                                    builder.append("_");
//                                    builder.append(fm.getDirection());
//                                    builder.append(" ");
//                                    for(double s : response) {
//                                        builder.append(s);
//                                        builder.append(" ");
//                                    }
//                                    String writableResponse = builder.toString();
//
//                                    PrintWriter current = pw2.get(fm.getDirection());
//
//                                    current.println(writableResponse);
//                                    current.flush();

                                }
                            }

                        } else {
                            firstLine = true;
                        }
                        counter++;

                        if (counter % 100000 == 0 && counter > 0) {
                            for (Map.Entry<String, Profile> p : profiles.entrySet()) {
                                p.getValue().getClustering().get("fwd").print(resultsPathName2 + "tree_test_" + p.getKey() + "_fwd_" + counter + ".txt");
                                p.getValue().getClustering().get("bwd").print(resultsPathName2 + "tree_test_" + p.getKey() + "_bwd_" + counter + ".txt");
                            }
                        }
                    }
                }

                for (Map.Entry<String, Profile> p : profiles.entrySet()) {
                    p.getValue().getClustering().get("fwd").print(resultsPathName2 + "tree_test_" + p.getKey() + "_fwd_end.txt");
                    p.getValue().getClustering().get("bwd").print(resultsPathName2 + "tree_test_" + p.getKey() + "_bwd_end.txt");
                }
                pw2.get("fwd").flush();
                pw2.get("bwd").flush();
                pw2.get("fwd").close();
                pw2.get("bwd").close();

                bQueue.put(new double[0]);

                CalculateResults cr = new CalculateResults();
                cr.calculate(resultsPathName2, fileName, printOut, pw);

                DrawTimeSeries ts = new DrawTimeSeries();
                ts.draw(resultsPathName + fileName + "/", fileName);

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

    @Override
    public void stop() throws Exception {
        super.stop();
    }


    public static void main (String[] args) {
        Application.launch(args);
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
