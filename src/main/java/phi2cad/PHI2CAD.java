package phi2cad;

import evaluation.CalculateResults;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * The class presents the object that presents the anomaly detection algorithm.
 */
public class PHI2CAD {

    private boolean printOut;

    private int nThreads;
    private int maxChildren;
    private int pointLength;
    private int checkStep;
    private int maxBins;
    private int sizeOfBin;
    private int distanceFunction;
    private int selectedKey;

    private double delta;
    private double lambda;
    private double clearThreshold;
    private double threshold;
    private double thresholdT;
    private double timestampRateMax;
    private double normalClusterThreshold;

    private String resultsPathName;

    private int[] indexIP;
    private int[] selectedValues;
    private int[] selectedValuesKeys;
    private String[] arrayOfAnomalies;

    private ArrayList<File> arrayOfFiles;
    private ArrayList<String> selectedValuesProcessing;

    private HashMap<Integer,Integer> reverseTable;
    private HashMap<Integer,HashMap<String,Boolean>> ipSplit;
    private HashMap<Integer,long[]> minMaxValues;

    /**
     * The constructor creates new PHICAD object with the given parameters.
     * @param arrayOfFiles ArrayList&lt;File&gt; that presents the file paths of files to be analyzed.
     * @param resultsPathName String value that present the path name of the directory for storing results.
     */
    public PHI2CAD(ArrayList<File> arrayOfFiles, String resultsPathName, String[] arrayOfAnomalies) {

        this.printOut = true;

        this.nThreads = Runtime.getRuntime().availableProcessors() / 2;

//        this.nThreads = 1;
        this.maxChildren = 8;
        this.pointLength = 21;
//        this.pointLength = 12;
        this.checkStep = 32;
        this.maxBins = 5;
        this.sizeOfBin = 5;
        this.distanceFunction = 0;

        this.delta = 0.2;
//        this.delta = 0.31;
//        this.lambda = 0.008;
        this.lambda = 0.008;
        this.clearThreshold = 0.05;
        this.threshold = 0.19;
        this.thresholdT = 1800;
        this.timestampRateMax = 432000.0;
        this.normalClusterThreshold = 0.1;

        this.resultsPathName = resultsPathName;

        this.selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 6, 84};
//        this.selectedValues = new int[]{1, 3, 2, 4, 5, 10, 11, 8, 9, 7, 84};
        this.selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("t"); add("l");}};
//        this.selectedValuesProcessing = new ArrayList<>(){{add("\\."); add("\\."); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("n"); add("l");}};
        this.selectedValuesKeys = new int[]{1, 3};

        this.arrayOfFiles = arrayOfFiles;
        this.arrayOfAnomalies = arrayOfAnomalies;

        this.ipSplit = new HashMap<>();
        this.reverseTable = new HashMap<>(){{
            put(1, 3); put(2, 4); put(8, 9); put(10, 11); put(12, 16); put(13, 17); put(14, 18); put(15, 19);
            put(26, 31); put(27, 32); put(28, 33); put(29, 34); put(30, 35); put(36, 37); put(38, 39); put(40, 41);
            put(42, 43); put(59, 60); put(62, 65); put(63, 66); put(64, 67); put(68, 70); put(69, 71); put(72, 73);
            put(74, 75);
        }};

        /*
        * Source Port - Min: 0,000000  Max: 65535,000000
        * Destination Port - Min: 0,000000  Max: 65535,000000
        * Protocol - Min: 0,000000  Max: 17,000000
        * Flow Duration - Min: -13,000000  Max: 119999998,000000
        * Total Fwd Packets - Min: 1,000000  Max: 219759,000000
        * Total Backward Packets - Min: 0,000000  Max: 291922,000000
        * Total Length of Fwd Packets - Min: 0,000000  Max: 12900000,000000
        * Total Length of Bwd Packets - Min: 0,000000  Max: 655453030,000000
        */

        this.minMaxValues = new HashMap<>(){{
            put(1, new long[]{0L, 255L});
            put(3, new long[]{0L, 255L});
            put(2, new long[]{0L, 65535L});
            put(4, new long[]{0L, 65535L});
            put(5, new long[]{6L, 17L});
            put(10, new long[]{0L, 12900000L});
            put(11, new long[]{0L, 655453030L});
            put(8, new long[]{1L, 219759L});
            put(9, new long[]{0L, 291922L});
            put(7, new long[]{0L, 120000000L});
            put(6, new long[]{0L, 3600L});
        }};

//        this.minMaxValues = new HashMap<>(){{
//            put(1, new long[]{0L, 255L});
//            put(3, new long[]{0L, 255L});
//            put(2, new long[]{0L, 65535L});
//            put(4, new long[]{0L, 65535L});
//            put(5, new long[]{0L, 255L});
//            put(10, new long[]{0L, 100000L});
//            put(11, new long[]{0L, 100000L});
//            put(8, new long[]{1L, 100L});
//            put(9, new long[]{0L, 100L});
//            put(7, new long[]{0L, 120000000L});
//            put(6, new long[]{0L, 3600L});
//        }};
    }

    /**
     * The method parses traffic flows from files and sends them to the Worker threads for analysis.
     */
    public void runAnalysis(PrintWriter pw, double threshold, double lambda, String filename){

        File dir = new File(this.resultsPathName);

        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().startsWith("results_")) {
                boolean result = file.delete();
                if (!result) {
                    System.out.println("Delete failed: " + file.getName());
                }
            }
        }

        if (threshold > 0.0) {
            this.threshold = threshold;
        }
        if (lambda > 0.0) {
            this.lambda = lambda;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(this.nThreads);

        ArrayList<BlockingQueue<FlowMessage>> queues = new ArrayList<>();

        for (int i = 0; i < this.nThreads; i++) {

            LinkedBlockingQueue<FlowMessage> bQueue = new LinkedBlockingQueue<>();
            queues.add(bQueue);

            Worker worker = new Worker(Integer.toString(i), bQueue, this.minMaxValues,
                    this.selectedValues, this.selectedValuesProcessing, this.selectedKey, this.maxChildren, this.threshold, this.pointLength, this.resultsPathName, this.delta,
                    this.clearThreshold, this.checkStep, this.maxBins, this.sizeOfBin, this.thresholdT, this.timestampRateMax,
                    this.printOut, this.lambda, this.arrayOfAnomalies, this.distanceFunction, this.normalClusterThreshold);

            HashMap<String,Boolean> ips = new HashMap<>();
            this.ipSplit.put(i, ips);

            executorService.submit(worker);
        }

        try {
            for (File currentFile : this.arrayOfFiles) {

                FileReader fr = new FileReader(currentFile);
                BufferedReader br = new BufferedReader(fr);

                boolean firstLine = false;
                for(String line; (line = br.readLine()) != null;) {

                    if(firstLine) {

                        String[] sFlow = line.split(",");

                        if (sFlow.length > 0) {

                            for (int index_key = 0; index_key < selectedValuesKeys.length; index_key++) {
                                String ip = sFlow[selectedValuesKeys[index_key]];

                                int workerIndex = findAppropriateWorker(ip);
                                String direction = "fwd";

                                if (index_key == 1) {
                                    sFlow = reverseFlow(sFlow);
                                    direction = "bwd";
                                }
                                String[] reducedFlow = new String[this.selectedValues.length];
                                for (int i = 0; i < this.selectedValues.length; i++) {
                                    reducedFlow[i] = sFlow[this.selectedValues[i]];
                                }
                                queues.get(workerIndex).put(new FlowMessage(reducedFlow, direction, "0.0"));
                            }
                        }

                    } else {
                        firstLine = true;
                    }
                }
            }

            for (BlockingQueue<FlowMessage> bQ : queues) {
                bQ.put(new FlowMessage(new String[]{"END"}, "fwd", "0.0"));
            }

            executorService.shutdown();
            executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

//        CalculateResultsPHICAD cr = new CalculateResultsPHICAD();
//        cr.calculate(this.resultsPathName, this.printOut, pw);

        CalculateResults cr = new CalculateResults();
        cr.calculate(this.resultsPathName, "", this.printOut, pw);

    }

    public void runMultipleAnalysis(PrintWriter pw){

        for (double i = 0.01; i < 0.31; i += 0.01) {
//            for (double i = 0.01; i < 0.02; i += 0.01) {
            this.threshold = new BigDecimal(i).setScale(2, RoundingMode.HALF_UP).doubleValue();
            for (double j = 0.0; j < 0.001; j += 0.0001) {
//                for (double j = 0.0; j < 0.002; j += 0.001) {
                this.lambda = new BigDecimal(j).setScale(4, RoundingMode.HALF_UP).doubleValue();
//                this.lambda = 0.0001;
//                for (int k = 0; k < 6; k ++) {
//                    for (int k = 0; k < 1; k ++) {
                    this.distanceFunction = 0;
//                    this.distanceFunction = k;
                    System.out.print("  Threshold: " + String.format("%.2f", this.threshold) + " Lambda: " + String.format("%.4f", this.lambda) + " DistanceFunction: " + this.distanceFunction + " ");
                    pw.print("  Threshold: " + String.format("%.2f", this.threshold) + " Lambda: " + String.format("%.4f", this.lambda) + " DistanceFunction: " + this.distanceFunction + " ");
                    pw.flush();
                    this.runAnalysis(pw, -1.0, -1.0, "");
//                }
            }
        }
    }

    /**
     * The method reverses the traffic flows so that destination IP is now source IP.
     * @param flow String[] array that presents the given traffic flow.
     * @return String[] array that presents the reversed traffic flow.
     */
    private String[] reverseFlow(String[] flow) {
        String[] reversedFlow = new String[flow.length];

        for (int i = 0; i < flow.length; i++) {
            if (this.reverseTable.containsKey(i)) {
                reversedFlow[i] = flow[this.reverseTable.get(i)];
                reversedFlow[this.reverseTable.get(i)] = flow[i];
            }
            else if (!this.reverseTable.containsValue(i)) {
                reversedFlow[i] = flow[i];
            }
        }

        return reversedFlow;
    }

    /**
     * The method finds the appropriate Worker thread for the given IP address on the basis of already existing profile or lowest number of profiles.
     * @param ip String value that presents the given IP address.
     * @return int value that presents the Worker thread index.
     */
    private int findAppropriateWorker(String ip) {

        int workerIndex;

        int alreadySeenByWorker = ipAlreadySeenByWorker(ip);
        if (alreadySeenByWorker > -1) {
            workerIndex = alreadySeenByWorker;
        }
        else {
            workerIndex = findBestWorker();
            this.ipSplit.get(workerIndex).put(ip, Boolean.TRUE);
        }

        return workerIndex;
    }

    /**
     * The method checks if the given IP address already has a profile in any of the Worker threads.
     * @param ip String value that presents the given IP address.
     * @return int value that presents the Worker thread index.
     */
    private int ipAlreadySeenByWorker(String ip) {

        int workerIndex = -1;

        for (Map.Entry<Integer,HashMap<String,Boolean>> ipList : this.ipSplit.entrySet()) {

            if (ipList.getValue().get(ip) != null){
                workerIndex = ipList.getKey();
                break;
            }
        }
        return workerIndex;
    }

    /**
     * The method finds the Worker thread with the lowest number of profiles.
     * @return int value that presents the Worker thread index.
     */
    private int findBestWorker() {
        int minimum = Integer.MAX_VALUE;
        int workerIndex = 0;

        for (Map.Entry<Integer,HashMap<String,Boolean>> ipList : this.ipSplit.entrySet()) {
            int currentMinimum = ipList.getValue().size();
            if (currentMinimum < minimum) {
                minimum = currentMinimum;
                workerIndex = ipList.getKey();
            }
        }
        return workerIndex;
    }
}
