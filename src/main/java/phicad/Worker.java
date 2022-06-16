package phicad;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * The class implements object that presents a worker that can analyze traffic flows in parallel.
 */
public class Worker implements Runnable{
    private boolean printOut;

    private int maxChildren;
    private int pointLength;
    private int checkStep;
    private int maxBins;
    private int sizeOfBin;
    private int distanceFunction;
    private int selectedKey;

    private int[] selectedValues;
    private String[] arrayOfAnomalies;

    private double threshold;
    private double delta;
    private double clearThreshold;
    private double thresholdT;
    private double timestampRateMax;
    private double lambda;
    private double normalClusterThreshold;

    private DateTimeFormatter[] formats;

    private ArrayList<String> selectedValuesProcessing;

    private String threadName;
    private String resultsPathName;
    private BlockingQueue linkedBlockingQueue;
    private HashMap<Integer,long[]> minMaxValues;

    /**
     * The constructor creates new Worker object with the given parameters.
     * @param threadName String value that presents the name of the worker.
     * @param linkedBlockingQueue LinkedBlockingQueue object presents the queue through which the Worker receives traffic flows for analysis.
     * @param minMaxValues HashMap<String,int[]> that presents minimum and maximum values for the given features that are used for normalization.
     * @param selectedValues int[] array that presents the indexes of selected features to be used for profile building.
     * @param maxChildren int that presents maximum number of child nodes inside a single CFTreeNode.
     * @param threshold double that presents the threshold that defines the maximum allowed radius of a single leaf CFTreeNode.
     * @param pointLength int value that presents the length of the input data points.
     * @param resultsPathName String value that present the path name of the directory for storing results.
     * @param delta double that presents the maximum delta value for the ADWIN windows.
     * @param clearThreshold double value that presents the percentage for the calculation of the minimum allowed size of the cluster.
     * @param checkStep int that presents the number of iterations before we check for anomaly.
     * @param maxBins int that presents the maximum number of bins the exponential histogram can have.
     * @param sizeOfBin int that presents the maximum number of values inside of a single bin.
     * @param thresholdT double value that presents the maximum allowed time difference.
     * @param timestampRateMax double value that presents the maximum difference between two updates to the cluster.
     * @param printOut boolean value that enables printing of additional data to results.
     */
    Worker(String threadName, LinkedBlockingQueue linkedBlockingQueue, HashMap<Integer,long[]> minMaxValues, int[] selectedValues,
           ArrayList<String> selectedValuesProcessing, int selectedKey, int maxChildren, double threshold, int pointLength, String resultsPathName, double delta, double clearThreshold,
           int checkStep, int maxBins, int sizeOfBin, double thresholdT, double timestampRateMax, boolean printOut, double lambda,
           String[] arrayOfAnomalies, int distanceFunction, double normalClusterThreshold
    ) {

        this.selectedValuesProcessing = selectedValuesProcessing;
        this.selectedKey = selectedKey;
        this.threadName = threadName;
        this.linkedBlockingQueue = linkedBlockingQueue;
        this.minMaxValues = minMaxValues;
        this.selectedValues = selectedValues;
        this.maxChildren = maxChildren;
        this.threshold = threshold;
        this.pointLength = pointLength;
        this.resultsPathName = resultsPathName;
        this.delta = delta;
        this.lambda = lambda;
        this.clearThreshold = clearThreshold;
        this.checkStep = checkStep;
        this.maxBins = maxBins;
        this.sizeOfBin = sizeOfBin;
        this.thresholdT = thresholdT;
        this.timestampRateMax = timestampRateMax;
        this.printOut = printOut;
        this.arrayOfAnomalies = arrayOfAnomalies;
        this.formats = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("dd/MM/yyyy H:m"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy H:m:s")
        };
        this.distanceFunction = distanceFunction;
        this.normalClusterThreshold = normalClusterThreshold;

    }

    /**
     * The method presents the run method for starting the Worker thread.
     */
    @Override
    public void run() {
        HashMap<String, Profile> profiles = new HashMap<>();

//        try {
//            final HashMap<String, PrintWriter> pw = new HashMap<>();
//            for (String direction : new String[]{"fwd", "bwd"}) {
//                pw.put(direction, new PrintWriter(this.resultsPathName + "results_" + direction + "_" + this.threadName + ".txt"));
//            }
//
//            ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//            while (true) {
//                FlowMessage data = (FlowMessage) this.linkedBlockingQueue.take();
//
//                if (data.getFlow()[0].equals("END")) {
////                    if (printOut) {
////                        for (String key : profiles.keySet()) {
//////                            HashMap<String, BIRCH_F> clustering = profiles.get(key).getClustering();
////                            HashMap<String, CFTree> clustering = profiles.get(key).getClustering();
////                            for (String clusteringKey : clustering.keySet()) {
////                                CFTree t = clustering.get(clusteringKey);
////                                t.print(this.resultsPathName + "tree_" + clusteringKey + "_" + key);
////                            }
////                        }
////                    }
//                    break;
//                }
//
//                String currentIP = data.getFlow()[0];
////                System.out.println(currentIP);
//                Profile profile = profiles.get(currentIP);
//                if (profile == null) {
//                    profile = new Profile(this.minMaxValues, this.selectedValues, this.selectedValuesProcessing, this.selectedKey,
//                            this.maxChildren, this.threshold, this.pointLength, this.delta, this.clearThreshold, this.checkStep,
//                            this.maxBins, this.sizeOfBin, this.thresholdT, this.timestampRateMax, this.formats, this.printOut, this.lambda, this.distanceFunction, this.normalClusterThreshold, currentIP);
//                    profiles.put(currentIP, profile);
//                }
//
//                double[] anomaly = profile.updateProfile(data);
//
////                double groundTruth = 1.0;
////                if (data.getFlow()[11].trim().equals("BENIGN")){
////                    groundTruth = 0.0;
////                }
//                double groundTruth = 0.0;
//                String currentLabel = data.getFlow()[11].trim();
//                for (String label : this.arrayOfAnomalies){
//                    if (currentLabel.equals(label)) {
//                        groundTruth = 1.0;
//                    }
//                }
//
//                double[] response = new double[]{anomaly[0], groundTruth, anomaly[1]};
//
//                if (printOut) {
//                    response = concatTwo(response, Arrays.copyOfRange(anomaly, 2, anomaly.length));
//                }
//
//                StringBuilder builder = new StringBuilder();
//                builder.append(currentIP);
//                builder.append("_");
//                builder.append(data.getDirection());
//                builder.append(" ");
//                for(double s : response) {
//                    builder.append(s);
//                    builder.append(" ");
//                }
//                final String writableResponse = builder.toString();
//
//                final PrintWriter current = pw.get(data.getDirection());
//
//                final Runnable runnable = () -> {
//                    current.println(writableResponse);
//                    current.flush();
//                };
//
//                executorService.submit(runnable);
//            }
//
//            executorService.shutdown();
//            executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);
//
//            pw.get("fwd").flush();
//            pw.get("bwd").flush();
//            pw.get("fwd").close();
//            pw.get("bwd").close();
//
//        } catch (FileNotFoundException | InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * The methods concatenates two double arrays.
     * @param a1 double[] array that present the first input double array.
     * @param a2 double[] array that present the second input double array.
     * @return double[] array that presents concatenation of the input double arrays.
     */
    private double[] concatTwo(double[] a1, double[] a2) {
        double[] array = new double[a1.length + a2.length];

        System.arraycopy(a1, 0, array, 0, a1.length);
        System.arraycopy(a2, 0, array, a1.length, a2.length);

        return array;
    }
}
