package phicad;

import evaluation.*;
import tbirchi.CF;
import tbirchi.CFNode;
import utils.Utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static utils.Utils.concatTwo;
import static java.time.temporal.ChronoUnit.SECONDS;

public class SingleThreadMainSingleFile {

    /* Common parameters */
    static boolean printOut = true;
    static int distanceFunction = 0;
    static int pointLength = 21;
    static String resultsPathName = "results/";
    static int[] selectedValuesKeys = new int[]{1, 3};
    static DateTimeFormatter[] formats = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("dd/MM/yyyy H:m"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy H:m:s")
    };

    static Set<Integer> mySet = new HashSet<Integer>(){{add(2); add(4); add(5); add(10); add(11); add(8); add(9); add(7); add(6);}};

    // Source IP, Source Port, Destination IP, Destination Port, Protocol, Timestamp, Flow Duration, Total Fwd Packets, Total Backward Packets,Total Length of Fwd Packets, Total Length of Bwd Packets,
    static HashMap<Integer, String> selectedValuesLegend = new HashMap<>(){{
        put(1, "Source IP"); put(3, "Destination IP"); put(2, "Source Port"); put(4, "Destination Port"); put(5, "Protocol");
        put(10, "Total Length of Fwd Packets"); put(11, "Total Length of Bwd Packets"); put(8, "Total Fwd Packets"); put(9, "Total Backward Packets"); put(7, "Flow Duration");
        put(6, "Timestamp");
    }};

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


    public static void main(String[] args) {

        long startTime = System.nanoTime();

        // Run with all parameters
        Set<Set<Integer>> ps = new HashSet<>(){{add(mySet);}};

        // Run with all possible combinations of parameters
//        Set<Set<Integer>> ps = powerSet(mySet);

        File dir = new File(resultsPathName);
        purgeDirectory(dir);

        try {
            PrintWriter pw = new PrintWriter(resultsPathName + "PHICAD_SingleThreadMainSingleFile_results.txt");

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

                        HashMap<String, Profile> profiles = new HashMap<>();

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

                        final HashMap<String, PrintWriter> pw2 = new HashMap<>();

                        ArrayList<File> currentFileArray = new ArrayList<>();
                        if (size == 1) {
                            currentFileArray = arrayOfFiles;
                        }
                        else {
                            currentFileArray.add(arrayOfFiles.get(i));
                        }

                        long counter = 0L;

                        for (File currentFile : currentFileArray) {

                            System.out.println(currentFile.getName());

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
                                                sFlow = reverseFlow(sFlow);
                                                direction = "bwd";
                                            }
                                            String[] reducedFlow = new String[currentSelectedValues.length];
                                            for (int j = 0; j < currentSelectedValues.length; j++) {
                                                reducedFlow[j] = sFlow[currentSelectedValues[j]];
                                            }

                                            double groundTruth = 0.0;
                                            String currentLabel = reducedFlow[reducedFlow.length - 1].trim();
                                            for (String label : anomalies) {
                                                if (currentLabel.equals(label)) {
                                                    groundTruth = 1.0;
                                                    anomalousFlows++;
                                                }
                                            }

                                            int timestampIndex = selectedValuesProcessing.indexOf("t");
                                            LocalDateTime timestamp = null;
                                            if (timestampIndex >= 0) {
                                                timestamp = Utils.parseTimestamp(sFlow[selectedValues[timestampIndex]], formats);
                                            }

                                            FlowMessage fm = new FlowMessage(reducedFlow, direction, timestamp);

                                            String currentIP = fm.getFlow()[0];
                                            //                System.out.println(currentIP);
                                            Profile profile = profiles.get(currentIP);
                                            if (profile == null) {
                                                profile = new Profile(minMaxValues, currentSelectedValues, currentSelectedValuesProcessing, 0, pointLength, formats, printOut, distanceFunction, currentIP,
                                                        parameters.get(0).intValue(), parameters.get(1).intValue(), parameters.get(2), parameters.get(3), parameters.get(4).intValue(), parameters.get(5).intValue(), parameters.get(6).intValue(), parameters.get(7), parameters.get(8).intValue(), parameters.get(9), parameters.get(10).intValue(), parameters.get(11), parameters.get(12), parameters.get(13));
                                                profiles.put(currentIP, profile);
                                                pw2.put(currentIP + "_fwd", new PrintWriter(resultsPathName2 + "results_" + fileName + "_" + currentIP + "_bwd.txt"));
                                                pw2.put(currentIP + "_bwd", new PrintWriter(resultsPathName2 + "results_" + fileName + "_" + currentIP + "_fwd.txt"));
                                            }


                                            //                                    if (ip.equals(fileName.split("_")[0]) && direction.equals(fileName.split("_")[1]) && groundTruth > 0.0) {
                                            //                                        profile.drawTree(direction);
                                            //                                    }

                                            int counterll = 0;
                                            CFNode currentNode = profile.getClustering().get(direction).getLeafList();
                                            while (currentNode != null) {
                                                if (!currentNode.isDummy()) {
                                                    for (CF cf : currentNode.getCfList()) {
                                                        counterll++;
                                                    }
                                                }
                                                currentNode = currentNode.getNextCFLeaf();
                                            }


                                            //                                    if (counter == 515 && direction.equals("fwd")) {
                                            //                                        System.out.println(counter + " " + direction);
                                            //                                    }

                                            double[] anomaly = profile.updateProfile(fm);

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
                                                response = concatTwo(response, Arrays.copyOfRange(anomaly, 5, anomaly.length));
                                            }

                                            StringBuilder builder = new StringBuilder();
                                            builder.append(currentIP);
                                            builder.append("_");
                                            builder.append(fm.getDirection());
                                            builder.append("_");
                                            builder.append(id);
                                            builder.append(" ");
                                            for (double s : response) {
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

                                        }
                                    }

                                } else {
                                    firstLine = true;
                                }
                                counter++;

                                //                        if (counter % 100000 == 0 && counter > 0) {
                                //                            for (Map.Entry<String, Profile> p : profiles.entrySet()) {
                                //                                p.getValue().getClustering().get("fwd").print(resultsPathName2 + "tree_test_" + p.getKey() + "_fwd_" + counter + ".txt");
                                //                                p.getValue().getClustering().get("bwd").print(resultsPathName2 + "tree_test_" + p.getKey() + "_bwd_" + counter + ".txt");
                                //                            }
                                //                        }
                            }
                        }
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

                        long innerEstimatedTime = System.nanoTime() - innerStartTime;
                        long seconds = TimeUnit.NANOSECONDS.toSeconds(innerEstimatedTime) % 60;
                        long minutes = TimeUnit.NANOSECONDS.toMinutes(innerEstimatedTime) % 60;
                        long hours = TimeUnit.NANOSECONDS.toHours(innerEstimatedTime) % 24;
                        long days = TimeUnit.NANOSECONDS.toDays(innerEstimatedTime);

                        pw.printf("Elapsed time: %02d d %02d h %02d m %02d s", days, hours, minutes, seconds);

                        CalculateResults3 cr3 = new CalculateResults3();
                        cr3.calculate(resultsPathName2, fileName, printOut, pw);

                        //                DrawTimeSeries2 ts = new DrawTimeSeries2();
                        //                ts.draw(resultsPathName + fileName + "/", fileName);

                    }
                }
            }

            long estimatedTime = System.nanoTime() - startTime;
            long seconds = TimeUnit.NANOSECONDS.toSeconds(estimatedTime) % 60;
            long minutes = TimeUnit.NANOSECONDS.toMinutes(estimatedTime) % 60;
            long hours = TimeUnit.NANOSECONDS.toHours(estimatedTime) % 24;
            long days = TimeUnit.NANOSECONDS.toDays(estimatedTime);

            pw.printf("Elapsed time: %02d d %02d h %02d m %02d s %n", days, hours, minutes, seconds);

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

    public static List<List<Double>> product(List<List<Double>> lists) {
        List<List<Double>> product = new ArrayList<>();

        // We first create a list for each value of the first list
        product(product, new ArrayList<>(), lists);

        return product;
    }

    private static void product(List<List<Double>> result, List<Double> existingTupleToComplete, List<List<Double>> valuesToUse) {
        for (Double value : valuesToUse.get(0)) {
            List<Double> newExisting = new ArrayList<>(existingTupleToComplete);
            newExisting.add(value);

            // If only one column is left
            if (valuesToUse.size() == 1) {
                // We create a new list with the exiting tuple for each value with the value
                // added
                result.add(newExisting);
            } else {
                // If there are still several columns, we go into recursion for each value
                List<List<Double>> newValues = new ArrayList<>();
                // We build the next level of values
                for (int i = 1; i < valuesToUse.size(); i++) {
                    newValues.add(valuesToUse.get(i));
                }

                product(result, newExisting, newValues);
            }
        }
    }

    public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }
}
