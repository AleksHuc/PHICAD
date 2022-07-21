package phicad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static utils.Utils.product;
import static utils.Utils.purgeDirectory;

/**
 * The class presents the class for running the PHICAD anomaly detection algorithm.
 */
public class Main {

    static String resultsPathName = "results/";
    static Set<Integer> mySet = new HashSet<Integer>(){{add(2); add(4); add(5); add(10); add(11); add(8); add(9); add(7); add(6);}};

    /* CICIDS2017 specific parameters */
    static Integer[] selectedValues = new Integer[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 84};
    static ArrayList<String> selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}};
    static HashMap<Integer,Integer> reverseTable = new HashMap<>(){{
        put(1, 3); put(2, 4); put(8, 9); put(10, 11); put(12, 16); put(13, 17); put(14, 18); put(15, 19);
        put(26, 31); put(27, 32); put(28, 33); put(29, 34); put(30, 35); put(36, 37); put(38, 39); put(40, 41);
        put(42, 43); put(59, 60); put(62, 65); put(63, 66); put(64, 67); put(68, 70); put(69, 71); put(72, 73);
        put(74, 75);
    }};

    static String[] anomalies = new String[] {
            "SSH-Patator", "FTP-Patator",
            "DoS Slowhttptest", "DoS GoldenEye", "Heartbleed", "DoS slowloris", "DoS Hulk",
            "Web Attack � Sql Injection", "Web Attack � XSS", "Web Attack � Brute Force",
            "Infiltration",
            "Bot",
            "PortScan",
            "DDoS"
    };

    static ArrayList<File> arrayOfFiles = new ArrayList<>(){{
        add(new File("data/CIC-IDS-2017/Monday-WorkingHoursOrdered.pcap_ISCX.csv"));
        add(new File("data/CIC-IDS-2017/Tuesday-WorkingHoursOrdered.pcap_ISCX.csv"));
        add(new File("data/CIC-IDS-2017/Wednesday-workingHoursOrdered.pcap_ISCX.csv"));
        add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Morning-WebAttacksOrdered.pcap_ISCX.csv"));
        add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Afternoon-InfilterationOrdered.pcap_ISCX.csv"));
        add(new File("data/CIC-IDS-2017/Friday-WorkingHours-MorningOrdered.pcap_ISCX.csv"));
        add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-PortScanOrdered.pcap_ISCX.csv"));
        add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-DDosOrdered.pcap_ISCX.csv"));
    }};


    /* ISCXIDS2012 specific parameters */
//    static int[] selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 12};
//    static ArrayList<String> selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}};
//    static  HashMap<Integer, Integer> reverseTable = new HashMap<>(){{
//        put(1, 3); put(2, 4); put(8, 9); put(10, 11);
//    }};

//    static String[] anomalies = new String[] {
//            "Attack"
//    };

//    static ArrayList<File> arrayOfFiles = new ArrayList<>(){{
//        add(new File("data/ISCX-IDS-2012/TestbedSatJun12Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedSunJun13Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedMonJun14Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedTueJun15-1Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedTueJun15-2Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedTueJun15-3Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedWedJun16-1Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedWedJun16-2Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedWedJun16-3Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedThuJun17-1Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedThuJun17-2Flows.csv"));
//        add(new File("data/ISCX-IDS-2012/TestbedThuJun17-3Flows.csv"));
//    }};

    /**
     * The method sets necessary parameters and runs the PHICAD algorithm.
     * @param args String[] array that presents general parameters.
     */
    public static void main(String[] args) {
        long startTime = System.nanoTime();

        // Run with all parameters
        Set<Set<Integer>> ps = new HashSet<>(){{add(mySet);}};

        // Run with all possible combinations of parameters
//        Set<Set<Integer>> ps = powerSet(mySet);

        File dir = new File(resultsPathName);
        purgeDirectory(dir);

        try {
            PrintWriter pw = new PrintWriter(resultsPathName + "PHICAD_Main_results.txt");

            // Run with the selected parameters
            List<Double> maxChildren = new ArrayList<>(Arrays.asList(8.0));
            List<Double> maxNodes = new ArrayList<>(Arrays.asList(100.0));
            List<Double> lambda = new ArrayList<>(Arrays.asList(0.01));
            List<Double> threshold = new ArrayList<>(Arrays.asList(0.1));
            List<Double> checkStep = new ArrayList<>(Arrays.asList(5.0));
            List<Double> maxBins = new ArrayList<>(Arrays.asList(5.0));
            List<Double> sizeOfBin = new ArrayList<>(Arrays.asList(5.0));
            List<Double> delta = new ArrayList<>(Arrays.asList(0.01));
            List<Double> largeWindowSize = new ArrayList<>(Arrays.asList(1000.0));
            List<Double> largeWindowProbability = new ArrayList<>(Arrays.asList(0.05));
            List<Double> smallWindowSize = new ArrayList<>(Arrays.asList(20.0));
            List<Double> smallWindowProbability = new ArrayList<>(Arrays.asList(0.2));
            List<Double> clusterSizeThreshold = new ArrayList<>(Arrays.asList(0.5));
            List<Double> intraClusterThreshold = new ArrayList<>(Arrays.asList(3.0));

            // Run with the selected parameters with multiple values
//            List<Double> maxChildren = new ArrayList<>(Arrays.asList(4.0, 8.0, 12.0, 16.0, 20.0));
//            List<Double> maxNodes = new ArrayList<>(Arrays.asList(100.0, 200.0, 300.0, 400., 500.0));
//            List<Double> lambda = new ArrayList<>(Arrays.asList(0.0, 0.01, 0.05, 0.1, 0.15));
//            List<Double> threshold = new ArrayList<>(Arrays.asList(0.05, 0.1, 0.15, 0.2, 0.25));
//            List<Double> checkStep = new ArrayList<>(Arrays.asList(5.0, 10.0, 20.0));
//            List<Double> maxBins = new ArrayList<>(Arrays.asList(5.0, 10.0, 20.0));
//            List<Double> sizeOfBin = new ArrayList<>(Arrays.asList(5.0, 10.0, 20.0));
//            List<Double> delta = new ArrayList<>(Arrays.asList(0.005, 0.01, 0.015));
//            List<Double> largeWindowSize = new ArrayList<>(Arrays.asList(1000.0, 2000.0, 5000.0));
//            List<Double> largeWindowProbability = new ArrayList<>(Arrays.asList(0.01, 0.05, 0.1));
//            List<Double> smallWindowSize = new ArrayList<>(Arrays.asList(10.0, 30.0, 50.0));
//            List<Double> smallWindowProbability = new ArrayList<>(Arrays.asList(0.1, 0.2, 0.3));
//            List<Double> clusterSizeThreshold = new ArrayList<>(Arrays.asList(0.2, 0.5, 1.0));
//            List<Double> intraClusterThreshold = new ArrayList<>(Arrays.asList(2.0, 2.5, 3.0));

            List<List<Double>> input = new ArrayList<>(){{add(maxChildren); add(maxNodes); add(lambda); add(threshold); add(checkStep); add(maxBins); add(sizeOfBin); add(delta); add(largeWindowSize); add(largeWindowProbability); add(smallWindowSize); add(smallWindowProbability); add(clusterSizeThreshold); add(intraClusterThreshold);}};
            List<List<Double>> product = product(input);

            System.out.println("Number of parameter combinations: " + product.size());

            int anomalousFlows = 0;

            // Run through entire dataset
            int size = 1;

            // Run through each file separately
//            int size = arrayOfFiles.size();

            for (int i = 0; i < size; i++) {

                for (List<Double> parameters : product) {

                    for (Set<Integer> features : ps) {

                        PHICAD ad = new PHICAD(resultsPathName, selectedValues, selectedValuesProcessing, reverseTable, anomalies, arrayOfFiles, pw, size, i, parameters, features);

                        ad.runAnalysis();

                    }
                }
            }

            long estimatedTime = System.nanoTime() - startTime;
            long seconds = TimeUnit.NANOSECONDS.toSeconds(estimatedTime) % 60;
            long minutes = TimeUnit.NANOSECONDS.toMinutes(estimatedTime) % 60;
            long hours = TimeUnit.NANOSECONDS.toHours(estimatedTime) % 24;

            pw.printf("Elapsed time: %02d h, %02d m, %02d s %n", hours, minutes, seconds);

            pw.flush();
            pw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
