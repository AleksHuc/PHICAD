package evaluation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * The class implements object for drawing prediction vs ground truth values for each data point.
 */
public class CalculateResults3 {

    /**
     * The constructor creates new PredictionVsGroundTruth object with the given parameters.
     * @param filePath String value that presents the path to where the file will be written to.
     * @param printOut boolean value that presents if raw values should also be written out.
     */
    public void calculate(String filePath, String filename, boolean printOut, PrintWriter pw) {

        double bestScore = 0.0;
        LinkedHashMap<String,Double> bestScoreResults = null;
        String bestScoreKey = "F1 score (F1)";

        LinkedList<Double> predictionsList = new LinkedList<>();
        LinkedList<Double> groundtruthList = new LinkedList<>();

        TreeMap<Double, LinkedHashMap<String, Double>> rocData = new TreeMap<>();
//        for (double i = 0.00; i < 1.0; i += 0.01) {
        double i = 0.00;
        double threshold = new BigDecimal(i).setScale(2, RoundingMode.HALF_UP).doubleValue();

//        int counter = 0;
        int tp = 0;
        int fn = 0;
        int fp = 0;
        int tn = 0;
        int n = 0;
        int p = 0;

        HashSet<String> detectedAnomalies = new HashSet<>();
        HashSet<String> allAnomalies = new HashSet<>();

        int noAnomalies = 0;

        for (final File fileEntry : Objects.requireNonNull(new File(filePath).listFiles((dir, name) -> name.startsWith("results_" + filename)))) {
//            System.out.println(fileEntry.getName());
            try {

                double startingValue = 0.0;

                TreeMap<Integer, Integer> anomalies = new TreeMap<>();
                ArrayList<String[]> locAno = new ArrayList<>();
//                TreeMap<Integer, Integer> anomalies2 = new TreeMap<>();
                HashMap<Integer, Integer> predictions = new HashMap<>();

                XYSeries groundTruthS = new XYSeries("Ground truth");
                XYSeries predictionsS = new XYSeries("Predictions");

                int counter = 0;

                FileReader fr = new FileReader(fileEntry);
                BufferedReader br = new BufferedReader(fr);

                int startIndex = 0;
                boolean anomaly = false;
                int endIndex = 0;

                for (String line; (line = br.readLine()) != null; ) {
                    String[] data = line.split(" ");

                    String id = data[0].split("_")[2];

//                        double predictionValue = Double.parseDouble(data[1]);
                    double groundTruth = Double.parseDouble(data[2]);
                    double prediction = Double.parseDouble(data[3]);
                    locAno.add(new String[]{id, Integer.toString((int)groundTruth)});

                    groundTruthS.add(counter, startingValue + groundTruth);
                    predictionsS.add(counter, startingValue + prediction + 2.0);

                    if (prediction > i) {
                        predictions.put(counter, 1);
                    }

                    if (groundTruth > 0.0) {
                        allAnomalies.add(id);
                        noAnomalies++;
//                        p += 1;
                        if (!anomaly) {
                            if (counter - endIndex > 10) {
                                if (startIndex != 0 && endIndex != 0) {
                                    anomalies.put(startIndex, endIndex);
//                                    anomalies2.put(endIndex, startIndex);
                                }
                                startIndex = counter;
                            }
                            anomaly = true;
//                                if (counter - endIndex > 100) {
//                                    startIndex = counter;
//                                }
                        }
                    } else {
//                        n += 1;
                        if (anomaly) {
                            anomaly = false;
                            endIndex = counter - 1;
//                                anomalies.put(startIndex, endIndex);
//                                for (int m = startIndex; m <= endIndex; m++) {
//                                    anomalies2.put(m, endIndex);
//                                }
                        }
                    }


//                        predictionsList.add(prediction);
//                        groundtruthList.add(groundTruth);

//                        if (predictionsList.size() > 100) {
//                            predictionsList.removeFirst();
//                        }
//                        if (groundtruthList.size() > 100) {
//                            groundtruthList.removeFirst();
//                        }

//                        if (counter > 100) {
//
//                            double currentPrediction = predictionsList.get(100/2);
//
//                            if (groundtruthList.contains(1.0)) {
//                                groundTruth = 1.0;
//                            }
//                            else {
//                                groundTruth = 0.0;
//                            }
//
//                            if (groundTruth == 0.0) {
//                                n += 1;
//                            }
//                            else {
//                                p += 1;
//                            }
//                            if (currentPrediction > threshold) {
//                                if (groundTruth == 0.0) {
//                                    fp += 1;
//                                }
//                                else {
//                                    tp += 1;
//                                }
//                            }
//                            else {
//                                if (groundTruth == 0.0) {
//                                    tn += 1;
//                                }
//                                else {
//                                    fn += 1;
//                                }
//                            }
//                        }
                    counter++;
                }

                if (startIndex != 0 && !anomalies.containsKey(startIndex)) {
                    anomalies.put(startIndex, endIndex);
//                    anomalies2.put(endIndex, startIndex);
                }

                startingValue += 4.0;


//                if (counter > 10 && anomalies.size() > 0) {

                int[] groundTruthArray = new int[counter];

//                noAnomalies += anomalies.size();

                for (Map.Entry<Integer, Integer> e : anomalies.entrySet()) {

                    for (int k = e.getKey(); k <= e.getValue(); k++) {
                        groundTruthArray[k] = 1;
                    }
                }

                int[] detections = new int[counter];

                XYSeries correctedGroundTruthS = new XYSeries("Corrected ground truth");
                for (int j = 0; j < groundTruthArray.length; j++) {
                    correctedGroundTruthS.add(j, startingValue + groundTruthArray[j]);
                    if (groundTruthArray[j] > 0) {
                        p += 1;
                    } else {
                        n += 1;
                    }

                    if (predictions.containsKey(j)) {
                        int lower = j - 10;
                        if (lower < 0) {
                            lower = 0;
                        }
                        int upper = j + 10;
                        if (upper > counter - 1) {
                            upper = counter - 1;
                        }

                        int aCounter = 0;
                        for (Map.Entry<Integer, Integer> entry : anomalies.entrySet()) {
                            if ((lower < entry.getKey() && entry.getKey() < upper) || (lower < entry.getValue() && entry.getValue() < upper) || (entry.getKey() < lower && upper < entry.getValue())) {
                                for (int l = entry.getKey(); l <= entry.getValue(); l++) {
                                    detections[l] = 1;
                                    if (locAno.get(l)[1].equals("1")) {
                                        detectedAnomalies.add(locAno.get(l)[0]);
                                    }

                                }
                                aCounter++;
                            }

                        }
                        if (aCounter == 0) {
                            detections[j] = 1;
                            if (locAno.get(j)[1].equals("1")) {
                                detectedAnomalies.add(locAno.get(j)[0]);
                            }
                        }

//                                TreeMap<Integer, Integer> currentAnomalies = new TreeMap<>();
//                                if (anomalies2.subMap(lower, upper).size() > 0) {
//                                    for (Map.Entry<Integer,Integer> entry : anomalies2.subMap(lower, upper).entrySet()) {
//                                        currentAnomalies.put(entry.getValue(), entry.getKey());
//                                    }
//                                }
//                                if (anomalies.subMap(lower, upper).size() > 0) {
//                                    for (Map.Entry<Integer,Integer> entry : anomalies.subMap(lower, upper).entrySet()) {
//                                        if (!currentAnomalies.containsKey(entry.getKey())) {
//                                            currentAnomalies.put(entry.getKey(), entry.getValue());
//                                        }
//                                    }
//                                }

//                                if (currentAnomalies.size() > 0) {
//                                    for (Map.Entry<Integer, Integer> entry : currentAnomalies.subMap(lower, upper).entrySet()) {
//                                        for (int l = entry.getKey(); l <= entry.getValue(); l++) {
//                                            detections[l] = 1;
//                                        }
//                                    }
//                                }
//                                else {
//                                    detections[j] = 1;
//                                }
                    }
                }

                XYSeries correctedPredictionsS = new XYSeries("Corrected predictions");
                for (int k = 0; k < detections.length; k++) {
                    correctedPredictionsS.add(k, startingValue + 2.0 + detections[k]);

                    if (detections[k] == 1) {
                        if (groundTruthArray[k] == 0) {
                            fp += 1;
                        } else {
                            tp += 1;
                        }
                    } else {
                        if (groundTruthArray[k] == 0) {
                            tn += 1;
                        } else {
                            fn += 1;
                        }
                    }

//                    }

//                        for (int j = 0; j < counter; j++) {
//                            if (predictions.containsKey(j)) {
//                                int lower = j - 100;
//                                if (lower < 0) {
//                                    lower = 0;
//                                }
//                                int upper = j + 100;
//                                if (upper > counter - 1) {
//                                    upper = counter - 1;
//                                }
//                                if (anomalies2.subMap(lower, upper).size() > 0) {
//                                    for (Map.Entry<Integer,Integer> entry : anomalies2.subMap(lower, upper).entrySet()) {
//                                        for (int l = entry.getKey(); l <= entry.getValue(); l++) {
//                                            detections[l] = 1;
//                                        }
//                                    }
//                                }
//                                else {
//                                    detections[j] = 1;
//                                }
//                            }
//                        }


//                        for (int j = 0; j < detections.length; j++) {
//                            if (anomalies.containsKey(j)) {
//                                int length = j - anomalies.get(j);
//                                for (int k = 0; k <= length; k++) {
//                                    extractedGroundTruthS.add(j + k, startingValue + 1.0);
//                                }
//                                j += length;
//                            }
//                            else {
//                                extractedGroundTruthS.add(j, startingValue);
//                            }
//                        }

//
//                        for (int j = 0; j < detections.length; j++) {
//                            if (anomalies2.containsKey(j)) {
//                                if (detections[j] == 0) {
//                                    fp += 1;
//                                }
//                                else {
//                                    tp += 1;
//                                }
//                            }
//                            else {
//                                if (detections[j] == 0) {
//                                    tn += 1;
//                                }
//                                else {
//                                    fn += 1;
//                                }
//                            }
//                            extractedGroundTruthS.add(j, startingValue + detections[j]);
//                        }

//                        XYSeries extractedGroundTruthS = new XYSeries("Extracted Ground truth");
//
//                        int anomalyLength = -1;
//
//                        for (int j = 0; j < counter; j++) {
//                            if (anomalies.containsKey(j)) {
//                                anomalyLength = j - anomalies.get(j);
//                                extractedGroundTruthS.add(j, startingValue + 1.0);
//                                anomalyLength--;
//                            } else if (anomalyLength > -1) {
//                                extractedGroundTruthS.add(j, startingValue + 1.0);
//                                anomalyLength--;
//                            } else {
//                                extractedGroundTruthS.add(j, startingValue + 0.0);
//                            }
//
//                        }
//                    if (counter > 10 && anomalies.size() > 0) {
//                        XYSeriesCollection dataset = new XYSeriesCollection();
//                        dataset.addSeries(groundTruthS);
//                        dataset.addSeries(predictionsS);
//                        dataset.addSeries(correctedGroundTruthS);
//                        dataset.addSeries(correctedPredictionsS);
//
//                        JFreeChart chart = ChartFactory.createXYLineChart(
//                                "Profile features of " + i, "Consecutive flows",
//                                "Normalized Values", dataset,
//                                PlotOrientation.VERTICAL, true, true, false
//                        );
//
//                        chart.getPlot().setBackgroundPaint(Color.WHITE);
////
////                String d = "0";
////                if (detections.get(entry.getKey())) {
////                    d = "1";
////                }
//
//                        try {
//                            ChartUtilities.saveChartAsJPEG(new File(filePath + "image_" + fileEntry.getName() + "_" + i + ".png"), chart, 1024, 1440);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }
//                }

            } catch (IOException e) {
                e.printStackTrace();
            }
//                counter ++;
//        }
//    }
//            if (p > 0) {
//                System.out.println();
//            }

        }
        LinkedHashMap<String,Double> results = new LinkedHashMap<>();
        results.put("Threshold", threshold);
        results.put("Examples (E)", (double)n + (double)p);
        results.put("Condition positive (P)", (double)p);
        results.put("Condition positive percentage (P%)", (double)p / ((double)p + (double)n));
        results.put("Condition negative (N)", (double)n);
        results.put("Condition negative percentage (N%)", (double)n / ((double)p + (double)n));
        results.put("True positive (TP)", (double)tp);
        results.put("True positive percentage (TP%)", (double)tp / ((double)p + (double)n));
        results.put("True negative (TN)", (double)tn);
        results.put("True negative percentage (TN%)", (double)tn / ((double)p + (double)n));
        results.put("False positive (FP)", (double)fp);
        results.put("False positive percentage (FP%)", (double)fp / ((double)p + (double)n));
        results.put("False negative (FN)", (double)fn);
        results.put("False negative percentage (FN%)", (double)fn / ((double)p + (double)n));
        results.put("Sensitivity, Recall, Hit rate or True positive rate (TPR)", (double)tp / (double)p);
        results.put("Specificity, Selectivity or True negative rate (TNR)", (double)tn / (double)n);
        results.put("Precision or Positive predictive value (PPV)", (double)tp / ((double)tp + (double)fp));
        results.put("Negative predictive value (NPV)", (double)tn / ((double)tn + (double)fn));
        results.put("Miss rate or False negative rate (FNR)", (double)fn / (double)p);
        results.put("Fall-out or False positive rate (FPR)", (double)fp / (double)n);
        results.put("False discovery rate (FDR)", (double)fp / ((double)fp + (double)tp));
        results.put("False omission rate (FOR)", (double)fn / ((double)fn + (double)tn));
        results.put("Threat score (TS) or Critical Success Index (CSI)", (double)tp / ((double)tp + (double)fn + (double)fp));
        results.put("Accuracy (ACC)", ((double)tp + (double)tn) / ((double)p + (double)n));
        results.put("Balanced accuracy (BA)", (results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)") + results.get("Specificity, Selectivity or True negative rate (TNR)"))/ 2.0);
        results.put("F1 score (F1)", 2.0 * (results.get("Precision or Positive predictive value (PPV)") * results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)")) / (results.get("Precision or Positive predictive value (PPV)") + results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)")));
        results.put("F0.5 score (F0.5)", 1.25 * (results.get("Precision or Positive predictive value (PPV)") * results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)")) / (0.25 * results.get("Precision or Positive predictive value (PPV)") + results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)")));
        results.put("F2 score (F2)", 5.0 * (results.get("Precision or Positive predictive value (PPV)") * results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)")) / (4.0 * results.get("Precision or Positive predictive value (PPV)") + results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)")));
        results.put("Geometric mean (GMEAN)", Math.sqrt(results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)") * results.get("Specificity, Selectivity or True negative rate (TNR)")));
        results.put("Matthews correlation coefficient (MCC)", (double)((tp * tn) - (fp * fn)) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn)));
        results.put("Fowlkes???Mallows index (FM)", Math.sqrt(results.get("Precision or Positive predictive value (PPV)") * results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)")));
        results.put("Informedness or Bookmaker informedness (BM)", results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)") + results.get("Specificity, Selectivity or True negative rate (TNR)") - 1.0);
        results.put("Markedness or DeltaP (MK)", results.get("Precision or Positive predictive value (PPV)") + results.get("Negative predictive value (NPV)") - 1.0);

        if (threshold == 0.0) {
//            System.out.print("noAnomalies: " + noAnomalies + "\n");
            System.out.print("All anomalies: " + allAnomalies.size() + "\n");
            System.out.print("Detected anomalies: " + detectedAnomalies.size() + "\n");
            System.out.print("Sensitivity, Recall, Hit rate or True positive rate (TPR): " + results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)") + "\n");
            System.out.print("Specificity, Selectivity or True negative rate (TNR): " + results.get("Specificity, Selectivity or True negative rate (TNR)") + "\n");
            System.out.print("Miss rate or False negative rate (FNR): " + results.get("Miss rate or False negative rate (FNR)") + "\n");
            System.out.print("Negative predictive value (NPV): " + results.get("Negative predictive value (NPV)") + "\n");
            System.out.print("Fall-out or False positive rate (FPR): " + results.get("Fall-out or False positive rate (FPR)") + "\n");
            System.out.print("Precision or Positive predictive value (PPV): " + results.get("Precision or Positive predictive value (PPV)") + "\n");
            System.out.print(" F1 score (F1): " + results.get("F1 score (F1)") + "\n");
        }

//        pw.print(" Threshold: " + String.format("%.2f", threshold));
//        pw.print(" Examples (E): " + String.format("%.2f", results.get("Examples (E)")));
//        pw.print(" Condition positive (P): " + String.format("%.2f", results.get("Condition positive (P)")) + " = " + String.format("%.2f", results.get("Condition positive percentage (P%)")));
//        pw.print(" Condition negative (N): " + String.format("%.2f", results.get("Condition negative (N)")) + " = " + String.format("%.2f", results.get("Condition negative percentage (N%)")));
//        pw.print(" True positive (TP): " + String.format("%.2f", results.get("True positive (TP)")) + " = " + String.format("%.2f", results.get("True positive percentage (TP%)")));
//        pw.print(" True negative (TN): " + String.format("%.2f", results.get("True negative (TN)")) + " = " + String.format("%.2f", results.get("True negative percentage (TN%)")));
//        pw.print(" False positive (FP): " + String.format("%.2f", results.get("False positive (FP)")) + " = " + String.format("%.2f", results.get("False positive percentage (FP%)")));
//        pw.print(" False negative (FN): " + String.format("%.2f", results.get("False negative (FN)")) + " = " + String.format("%.2f", results.get("False negative percentage (FN%)")));
//        pw.println(" F1 score (F1): " + String.format("%.2f", results.get("F1 score (F1)")));
        pw.print("Examples (E): " + String.format("%d", results.get("Examples (E)").intValue()) + ", ");
        pw.print("Positive (P): " + String.format("%d", results.get("Condition positive (P)").intValue()) + ", ");
        pw.print("Negative (N): " + String.format("%d", results.get("Condition negative (N)").intValue()) + ", ");
        pw.print("True Positive (TP): " + String.format("%d", results.get("True positive (TP)").intValue()) + ", ");
        pw.print("True Negative (TN): " + String.format("%d", results.get("True negative (TN)").intValue()) + ", ");
        pw.print("False Positive (FP): " + String.format("%d", results.get("False positive (FP)").intValue()) + ", ");
        pw.print("False Negative (FN): " + String.format("%d", results.get("False negative (FN)").intValue()) + ", ");
        pw.print("Precision: " + String.format(Locale.US, "%.2f", results.get("Precision or Positive predictive value (PPV)")) + ", ");
        pw.print("Recall: " + String.format(Locale.US, "%.2f", results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)")) + ", ");
        pw.print("Specificity: " + String.format(Locale.US, "%.2f", results.get("Specificity, Selectivity or True negative rate (TNR)")) + ", ");
        pw.print("Miss rate: " + String.format(Locale.US, "%.2f", results.get("Miss rate or False negative rate (FNR)")) + ", ");
        pw.print("Fall-out: " + String.format(Locale.US, "%.2f", results.get("Fall-out or False positive rate (FPR)")) + ", ");
        pw.print("F1 score (F1): " + String.format(Locale.US, "%.2f", results.get("F1 score (F1)")) + System.lineSeparator());
        pw.flush();

        rocData.put(threshold, results);

        if (results.get(bestScoreKey) > bestScore) {
            bestScore = results.get(bestScoreKey);
            bestScoreResults = results;
        }
    }

//        if (bestScoreResults != null) {
//            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath + filename + "_best_metric_results.txt"), StandardCharsets.UTF_8))) {
//                for (Map.Entry<String, Double> entry : bestScoreResults.entrySet()){
//                    writer.write(entry.getKey() + ": " + String.format("%.2f", entry.getValue()) + System.lineSeparator());
//                }
//                writer.flush();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        else {
//            System.out.println("bestScoreResults == null");
//        }
//
//        LinkedHashSet<double[]> setRoc = new LinkedHashSet<>();
//        for (double key: rocData.keySet()) {
//            LinkedHashMap<String,Double> results = rocData.get(key);
//            double fpr = results.get("Fall-out or False positive rate (FPR)");
//            double tpr = results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)");
//            setRoc.add(new double[]{fpr, tpr});
//        }
//
//        TreeMap<Double, Double> orderedRoc = new TreeMap<>();
//        for (double[] point : setRoc) {
//            orderedRoc.put(point[0], point[1]);
//        }
//
//        XYSeries series = new XYSeries("Classificator");
//        series.add(0.0, 0.0);
//
//        double auc = 0.0;
//        double previousX = 0.0;
//        double previousY = 0.0;
//        for (Map.Entry<Double, Double> entry : orderedRoc.entrySet()) {
//            double fpr = entry.getKey();
//            double tpr = entry.getValue();
//            series.add(fpr, tpr);
//
//            double dX = fpr - previousX;
//            double dY = tpr - previousY;
//
//            auc += previousY * dX;
//            auc += (dX * dY) / 2.0;
//
//            previousX = fpr;
//            previousY = tpr;
//        }
//
//        double dX = 1.0 - previousX;
//        double dY = 1.0 - previousY;
//
//        auc += previousY * dX;
//        auc += (dX * dY) / 2.0;
//        series.add(1.0, 1.0);
//
//        XYSeries randomClassifier = new XYSeries("Random Classificator");
//        randomClassifier.add(0.0, 0.0);
//        randomClassifier.add(1.0, 1.0);
//
//        XYSeriesCollection dataset = new XYSeriesCollection();
//        dataset.addSeries(randomClassifier);
//        dataset.addSeries(series);
//
//        JFreeChart chart = ChartFactory.createXYLineChart(
//                "Roc curve (AUC=" + auc + ")", "False Positive Rate", "True Positive Rate", dataset,
//                PlotOrientation.VERTICAL, true, true, false
//        );
//
//        chart.getPlot().setBackgroundPaint(Color.WHITE);
//
//        try {
//            ChartUtilities.saveChartAsJPEG(new File(filePath + filename + "_roc.png"), chart, 1280, 1024);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        LinkedHashSet<double[]> setPr = new LinkedHashSet<>();
//        for (double key: rocData.keySet()) {
//            LinkedHashMap<String,Double> results = rocData.get(key);
//
//            double tpr = results.get("Sensitivity, Recall, Hit rate or True positive rate (TPR)");
//            double ppv = results.get("Precision or Positive predictive value (PPV)");
//            setPr.add(new double[]{tpr, ppv});
//        }
//
//        TreeMap<Double, Double> orderedPr = new TreeMap<>(Collections.reverseOrder());
//        for (double[] point : setPr) {
//            orderedPr.put(point[0], point[1]);
//        }
//
//        XYSeries series2 = new XYSeries("Classificator");
//        series2.add(0.0, 1.0);
//
//        double endY = rocData.get(0.01).get("Condition positive (P)") / rocData.get(0.01).get("Examples (E)");
//
//        double auc2 = 0.0;
//        double previousX2 = 0.0;
//        double previousY2 = 1.0;
//        for (Map.Entry<Double, Double> entry : orderedPr.entrySet()) {
//            double tpr = entry.getKey();
//            double ppv = entry.getValue();
//            series2.add(tpr, ppv);
//
//            double dX2 = tpr - previousX2;
//            double dY2 = ppv - previousY2;
//
//            auc2 += previousY2 * dX2;
//            auc2 += (dX2 * dY2) / 2.0;
//
//            previousX2 = tpr;
//            previousY2 = ppv;
//        }
//
//        double dX2 = 1.0 - previousX2;
//        double dY2 = endY - previousY2;
//
//        auc2 += previousY2 * dX2;
//        auc2 += (dX2 * dY2) / 2.0;
//        series2.add(1.0, endY);
//
//        XYSeries randomClassifier2 = new XYSeries("Random Classificator");
//        randomClassifier2.add(0.0, endY);
//        randomClassifier2.add(1.0, endY);
//
//        XYSeriesCollection dataset2 = new XYSeriesCollection();
//        dataset2.addSeries(randomClassifier2);
//        dataset2.addSeries(series2);
//
//        JFreeChart chart2 = ChartFactory.createXYLineChart(
//                "PR curve (AUC=" + auc2 + ")", "Recall", "Precision", dataset2,
//                PlotOrientation.VERTICAL, true, true, false
//        );
//
//        chart2.getPlot().setBackgroundPaint(Color.WHITE);
//
//        try {
//            ChartUtilities.saveChartAsJPEG(new File(filePath + filename + "_pr.png"), chart2, 1280, 1024);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void main(String[] args) {
//        int[] a = new int[]{1, 1, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0};
//        double[] b = new double[]{0.03, 0.08, 0.10, 0.11, 0.22, 0.32, 0.35, 0.42, 0.44, 0.48, 0.56, 0.65, 0.71, 0.72, 0.73, 0.80, 0.82, 0.99};
//
//        TreeMap<Double, double[]> sorted = new TreeMap<>();
//
//        for (double i = 0.00; i < 1.00; i += 0.01) {
//            double threshold = new BigDecimal(i).setScale(2, RoundingMode.HALF_UP).doubleValue();
//
//            int tp = 0;
//            int fn = 0;
//            int fp = 0;
//            int tn = 0;
//            int n = 0;
//            int p = 0;
//
//            for (int j = 0; j < b.length; j++) {
//
//                double predictionValue = b[j];
//                double groundTruth = a[j];
//                if (groundTruth == 0.0) {
//                    n += 1;
//                }
//                else {
//                    p += 1;
//                }
//                if (predictionValue < threshold) {
//                    if (groundTruth == 0.0) {
//                        fp += 1;
//                    }
//                    else {
//                        tp += 1;
//                    }
//                }
//                else {
//                    if (groundTruth == 0.0) {
//                        tn += 1;
//                    }
//                    else {
//                        fn += 1;
//                    }
//                }
//            }
//
//            double[] results = new double[]{n, p, tp, fp, tn, fn};
//
//            sorted.put(i, results);
//        }
//
//        XYSeries series = new XYSeries("Classificator");
//
//        double auc = 0.0;
//
//        double previousX = 0.0;
//        double previousY = 0.0;
//        for (double key: sorted.keySet()) {
//            System.out.println(key);
//
//            double[] counts = sorted.get(key);
//            double fpr = counts[3] / (counts[3] + counts[4]);
//            double tpr = counts[2] / (counts[2] + counts[5]);
//            series.add(fpr, tpr);
//
//            double dX = fpr - previousX;
//            double dY = tpr - previousY;
//
//            auc += previousY * dX;
//            auc += (dX * dY) / 2.0;
//
//            previousX = fpr;
//            previousY = tpr;
//        }
//
//        double dX = 1.0 - previousX;
//        double dY = 1.0 - previousY;
//
//        auc += previousY * dX;
//        auc += (dX * dY) / 2.0;
//        series.add(1.0, 1.0);
//
//        XYSeries randomClassifier = new XYSeries("Random Classificator");
//        randomClassifier.add(0.0, 0.0);
//        randomClassifier.add(1.0, 1.0);
//
//        XYSeriesCollection dataset = new XYSeriesCollection();
//        dataset.addSeries(randomClassifier);
//        dataset.addSeries(series);
//
//        JFreeChart chart = ChartFactory.createXYLineChart(
//                "Roc curve (AUC=" + auc + ")", "False Positive Rate", "True Positive Rate", dataset,
//                PlotOrientation.VERTICAL, true, true, false
//        );
//
//        chart.getPlot().setBackgroundPaint(Color.WHITE);
//
//        try {
//            ChartUtilities.saveChartAsJPEG(new File("results/test_roc.png"), chart, 1280, 1024);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
