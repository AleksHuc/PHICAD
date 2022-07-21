package phicad;

import evaluation.CalculateResults3;
import utils.FlowMessage;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static utils.Utils.*;

/**
 * The class presents the main class of PHICAD.
 */
public class PHICAD {

    private boolean printOut = false;
    static int[] selectedValuesKeys = new int[]{1, 3};

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

    private String resultsPathName;
    private Integer[] selectedValues;
    private String[] arrayOfAnomalies;
    private ArrayList<File> arrayOfFiles;
    private ArrayList<String> selectedValuesProcessing;
    private HashMap<Integer,Integer> reverseTable;
    private HashMap<Integer,HashMap<String,Boolean>> ipSplit;
    private List<Double> parameters;
    private PrintWriter pw;
    private Set<Integer> features;
    int size;
    int i;

    static HashMap<Integer, String> selectedValuesLegend = new HashMap<>(){{
        put(1, "Source IP"); put(3, "Destination IP"); put(2, "Source Port"); put(4, "Destination Port"); put(5, "Protocol");
        put(10, "Total Length of Fwd Packets"); put(11, "Total Length of Bwd Packets"); put(8, "Total Fwd Packets"); put(9, "Total Backward Packets"); put(7, "Flow Duration");
        put(6, "Timestamp");
    }};

    /**
     *
     * @param resultsPathName String object that present the folder where PHICAD will store its results.
     * @param selectedValues Integer array that presents the indexes of network flow features to be used in analysis.
     * @param selectedValuesProcessing ArrayList&lt;String&gt; object that presents how each selected network feature should be analyzed.
     * @param reverseTable HashMap&lt;Integer, Integer&gt; object that presents index pairs for reversing the direction of the network flow.
     * @param anomalies String array that presents the labels that present anomalies in the dataset.
     * @param arrayOfFiles ArrayList&lt;File&gt; object that presents the list of files to process.
     * @param pw PrintWriter object that presents the output data stream for writing the result files.
     * @param size int value that presents whether we are running the analysis through entire dataset or per each file separately.
     * @param i int value that presents the consecutive number of the analysis.
     * @param parameters List&lt;Double&gt; object that presents the current configuration of the parameters used for analysis.
     * @param features Set&lt;Integer&gt; object that presents the list of indexes that present which features to use for analysis.
     */
    public PHICAD(String resultsPathName, Integer[] selectedValues, ArrayList<String> selectedValuesProcessing, HashMap<Integer, Integer> reverseTable, String[] anomalies, ArrayList<File> arrayOfFiles, PrintWriter pw, int size, int i, List<Double> parameters, Set<Integer> features) {
        this.resultsPathName = resultsPathName;
        this.selectedValues = selectedValues;
        this.selectedValuesProcessing = selectedValuesProcessing;
        this.reverseTable = reverseTable;
        this.arrayOfAnomalies = anomalies;
        this.arrayOfFiles = arrayOfFiles;
        this.i = i;
        this.parameters = parameters;
        this.pw = pw;
        this.features = features;
        this.size = size;
        this.ipSplit = new HashMap<>();
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

    /**
     * The method presents the main part of PHICAD analysis.
     */
    public void runAnalysis() {

        int[] currentSelectedValues = new int[features.size() + 3];
        currentSelectedValues[0] = selectedValues[0];
        currentSelectedValues[1] = selectedValues[1];
        currentSelectedValues[currentSelectedValues.length - 1] = selectedValues[selectedValues.length - 1];

        int ic = 2;
        for (Integer v : features) {
            currentSelectedValues[ic] = v;
            ic++;
        }

        String[] currentSelectedValuesLegend = new String[currentSelectedValues.length];
        for (int v = 0; v < currentSelectedValues.length; v++) {
            currentSelectedValuesLegend[v] = selectedValuesLegend.get(currentSelectedValues[v]);
        }

        List<Integer> selectedValuesList = Arrays.asList(selectedValues);
        ArrayList<String> currentSelectedValuesProcessing = new ArrayList<>();
        for (int v : currentSelectedValues) {
            currentSelectedValuesProcessing.add(selectedValuesProcessing.get(selectedValuesList.indexOf(v)));
        }

        long innerStartTime = System.nanoTime();

        String fileName = arrayOfFiles.get(i).getName().split("\\.")[0];

        System.out.println(fileName + " " + Arrays.toString(parameters.toArray()) + " " + Arrays.toString(currentSelectedValues));
        pw.print("File: " + arrayOfFiles.get(i).getName() + ", ");
        pw.print("Max Children: " + parameters.get(0) + ", ");
        pw.print("Max Nodes: " + parameters.get(1) + ", ");
        pw.print("Lambda: " + parameters.get(2) + ", ");
        pw.print("Threshold: " + parameters.get(3) + ", ");
        pw.print("Check Step: " + parameters.get(4) + ", ");
        pw.print("Max Bins: " + parameters.get(5) + ", ");
        pw.print("Size of Bin: " + parameters.get(6) + ", ");
        pw.print("Delta: " + parameters.get(7) + ", ");
        pw.print("Large Window Size: " + parameters.get(8) + ", ");
        pw.print("Large Window Probability: " + parameters.get(9) + ", ");
        pw.print("Small Window Size: " + parameters.get(10) + ", ");
        pw.print("Small Window Probability: " + parameters.get(11) + ", ");
        pw.print("Cluster Size Threshold: " + parameters.get(12) + ", ");
        pw.print("Intra Cluster Threshold: " + parameters.get(13) + ", ");
        pw.print("Features: " + Arrays.toString(currentSelectedValuesLegend) + ", ");

        pw.flush();

        ArrayList<File> currentFileArray = new ArrayList<>();
        if (size == 1) {
            currentFileArray = arrayOfFiles;
        }
        else {
            currentFileArray.add(arrayOfFiles.get(i));
        }

        long counter = 0L;

        int nThreads = Runtime.getRuntime().availableProcessors();

        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        String resultsPathName2 = resultsPathName + fileName + "/";

        File folder = new File(resultsPathName2);
        if (folder.exists() && folder.isDirectory()) {
            purgeDirectory(folder);
        } else {
            boolean newFolder = folder.mkdir();
            if (!newFolder) {
                System.out.println("New folder creation failed!");
            }
        }

        ArrayList<BlockingQueue<FlowMessage>> queues = new ArrayList<>();
        ArrayList<Future> futures = new ArrayList<>();

        for (int i = 0; i < nThreads; i++) {

            LinkedBlockingQueue<FlowMessage> bQueue = new LinkedBlockingQueue<>();
            queues.add(bQueue);

            Worker worker = new Worker(fileName + " " + i, bQueue, this.minMaxValues,
                    currentSelectedValues, currentSelectedValuesProcessing, this.parameters, arrayOfAnomalies, resultsPathName2, this.printOut);

            HashMap<String,Boolean> ips = new HashMap<>();
            this.ipSplit.put(i, ips);

            futures.add(executorService.submit(worker));
        }

        for (File currentFile : currentFileArray) {

            System.out.println(currentFile.getName());

            try {
                FileReader fr = new FileReader(currentFile);
                BufferedReader br = new BufferedReader(fr);

                boolean firstLine = false;
                for (String line; (line = br.readLine()) != null; ) {

                    if (firstLine) {

                        String[] sFlow = line.split(",");

                        if (sFlow.length > 0) {

                            for (int index_key = 0; index_key < selectedValuesKeys.length; index_key++) {
                                String ip = sFlow[selectedValuesKeys[index_key]];
                                String id = String.valueOf(counter);
                                String direction = "fwd";

                                if (index_key == 1) {
                                    sFlow = reverseFlow(sFlow, reverseTable);
                                    direction = "bwd";
                                }
                                String[] reducedFlow = new String[currentSelectedValues.length];
                                for (int j = 0; j < currentSelectedValues.length; j++) {
                                    reducedFlow[j] = sFlow[currentSelectedValues[j]];
                                }

                                int workerIndex = findAppropriateWorker(ip);
                                FlowMessage fm = new FlowMessage(reducedFlow, direction);
                                queues.get(workerIndex).put(fm);
                            }
                        }

                    } else {
                        firstLine = true;
                    }
                    counter++;
                }
                for (BlockingQueue<FlowMessage> bQ : queues) {
                    bQ.put(new FlowMessage(new String[]{"END"}, "fwd"));
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();

        for (Future f : futures) {
            try {
                System.out.println(f.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println(counter);

        long innerEstimatedTime = System.nanoTime() - innerStartTime;
        long seconds = TimeUnit.NANOSECONDS.toSeconds(innerEstimatedTime) % 60;
        long minutes = TimeUnit.NANOSECONDS.toMinutes(innerEstimatedTime) % 60;
        long hours = TimeUnit.NANOSECONDS.toHours(innerEstimatedTime) % 24;
        long days = TimeUnit.NANOSECONDS.toDays(innerEstimatedTime);

        pw.printf("Elapsed time: %02d d %02d h %02d m %02d s", days, hours, minutes, seconds);

        CalculateResults3 cr3 = new CalculateResults3();
        cr3.calculate(resultsPathName2, fileName, printOut, pw);
    }
}
