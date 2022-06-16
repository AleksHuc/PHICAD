package phicad;

import tbirchi.CFTree;
import tbirchi.SplitChangeDifference;
import utils.MutableDouble;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * The class implements object that present the profile for each network IP address.
 */
public class Profile {

    private boolean printOut;

    private int pointLength;
    private int selectedKey;
    private int maxNumberOfNodes;
    private int largeWindowSize;
    private int smallWindowSize;

    private double lambda;
    private double clusterSizeThreshold;
    private double largeWindowProbability;
    private double smallWindowProbability;
    private double intraClusterThreshold;
    private double interClusterThreshold;

    private int[] selectedValues;

    String name;

    private DateTimeFormatter[] formats;

    private ArrayList<String> selectedValuesProcessing;

    private HashMap<String,MutableLocalDateTime> previousTimestamp;
    private HashMap<String,MutableLocalDateTime> lastCheckTimestamp;
    private HashMap<String, MutableDouble> average;
    private HashMap<String,MutableDouble> variance;
    private HashMap<String,AtomicInteger> count;
    private HashMap<Integer,long[]> minMaxValues;
    private HashMap<String,CFTree> clustering;
    private HashMap<String, AtomicBoolean> previousAnomaly;

    /**
     * The constructor creates new Profile object from given parameters.
     * @param minMaxValues HashMap&lt;String,int[]&gt; that presents minimum and maximum values for the given features that are used for normalization.
     * @param selectedValues int[] array that presents the indexes of selected features to be used for profile building.
     * @param maxChildren int value that presents maximum number of child nodes inside a single CFTreeNode.
     * @param threshold double value that presents the threshold that defines the maximum allowed radius of a single leaf CFTreeNode.
     * @param pointLength int value value that presents the length of the input data points.
     * @param delta double value that presents the maximum delta value for the ADWIN windows.
     * @param checkStep int value that presents the number of iterations before we check for anomaly.
     * @param maxBins int value that presents the maximum number of bins the exponential histogram can have.
     * @param sizeOfBin int value that presents the maximum number of values inside of a single bin.
     * @param formats DateTimeFormatter[] array that presents the possible formats for parsing DateTime.
     * @param printOut boolean value that enables printing of additional data to results.
     */
    public Profile(HashMap<Integer,long[]> minMaxValues,
                   int[] selectedValues,
                   ArrayList<String> selectedValuesProcessing,
                   int selectedKey,
                   int pointLength,
                   DateTimeFormatter[] formats,
                   boolean printOut,
                   int distanceFunction,
                   String name,
                   int maxChildren,
                   int maxNumberOfNodes,
                   double lambda,
                   double threshold,
                   int checkStep,
                   int maxBins,
                   int sizeOfBin,
                   double delta,
                   int largeWindowSize,
                   double largeWindowProbability,
                   int smallWindowSize,
                   double smallWindowProbability,
                   double clusterSizeThreshold,
                   double intraClusterThreshold
                   ) {
        this.minMaxValues = minMaxValues;
        this.selectedValues = selectedValues;
        this.selectedKey = selectedKey;
        this.selectedValuesProcessing = selectedValuesProcessing;
        this.previousTimestamp = new HashMap<>();
        this.lastCheckTimestamp = new HashMap<>();
        this.average = new HashMap<>();
        this.variance = new HashMap<>();
        this.count = new HashMap<>();
        this.previousAnomaly = new HashMap<>();
        this.maxNumberOfNodes = maxNumberOfNodes;
        this.formats = formats;
        this.lambda = lambda;
        this.printOut = printOut;
        this.pointLength = pointLength;
        this.clusterSizeThreshold = clusterSizeThreshold;
        this.largeWindowProbability = largeWindowProbability;
        this.smallWindowProbability = smallWindowProbability;
        this.intraClusterThreshold = intraClusterThreshold;
        this.name = name;
        this.largeWindowSize = largeWindowSize;
        this.smallWindowSize = smallWindowSize;

        this.clustering = new HashMap<>() {{
//            put("fwd", new BIRCH(maxChildren, maxChildren, threshold, pointLength, delta, checkStep, maxBins, sizeOfBin, clearThreshold, thresholdT, timestampRateMax));
//            put("bwd", new BIRCH(maxChildren, maxChildren, threshold, pointLength, delta, checkStep, maxBins, sizeOfBin, clearThreshold, thresholdT, timestampRateMax));
//            put("fwd", new BIRCH_F(maxChildren, maxChildren, threshold, pointLength, delta, checkStep, maxBins, sizeOfBin, clearThreshold, thresholdT, timestampRateMax, lambda));
//            put("bwd", new BIRCH_F(maxChildren, maxChildren, threshold, pointLength, delta, checkStep, maxBins, sizeOfBin, clearThreshold, thresholdT, timestampRateMax, lambda));
            put("fwd", new CFTree(maxChildren, maxNumberOfNodes, lambda, threshold, checkStep, maxBins, sizeOfBin, delta, largeWindowSize, largeWindowProbability, smallWindowSize, smallWindowProbability, clusterSizeThreshold, intraClusterThreshold, distanceFunction, name + "_fwd"));
            put("bwd", new CFTree(maxChildren, maxNumberOfNodes, lambda, threshold, checkStep, maxBins, sizeOfBin, delta, largeWindowSize, largeWindowProbability, smallWindowSize, smallWindowProbability, clusterSizeThreshold, intraClusterThreshold, distanceFunction, name + "_bwd"));
        }};
    }

    /**
     * The method updates the internal state of the profile with the current network flow.
     * @param flowMessage FlowMessage object that presents the direction and network flow attributes.
     * @return double[] array that presents if the change has been detected and the distance of the current difference vector to the mean difference vector.
     */
    double[] updateProfile(FlowMessage flowMessage) {

        ArrayList<Double> clusteringVectorNormalized = new ArrayList<>();

        String flowDirection = flowMessage.getDirection();
        String[] flowData = flowMessage.getFlow();
        LocalDateTime timestamp = flowMessage.getTimestamp();

//        LocalDateTime timestamp = parseTimestamp(flowData[10], this.formats);
//
////        if (!this.lastCheckTimestamp.containsKey(flowDirection)) {
////            this.lastCheckTimestamp.put(flowDirection, new MutableLocalDateTime(timestamp));
////        }
////        else {
////            MutableLocalDateTime currentLastCheckTimestamp = this.lastCheckTimestamp.get(flowDirection);
////            if (SECONDS.between(currentLastCheckTimestamp.getLocalDateTime(), timestamp) > (2 * this.thresholdT)) {
////                this.clustering.get(flowDirection).getTree().clearClusters(timestamp);
////                currentLastCheckTimestamp.setLocalDateTime(timestamp);
////            }
////        }
//
//        for(int i = 1; i < this.selectedValues.length - 1; i++) {
//            String currentValue = flowData[i];
//            int currentKey = this.selectedValues[i];
//            long[] currentMinMax = this.minMaxValues.get(currentKey);
//
//            if (currentKey == 3){
//                String[] ipAddress = currentValue.split("\\.");
//                for (String number: ipAddress) {
//                    clusteringVectorNormalized.add(normalize(Double.parseDouble(number), currentMinMax[0], currentMinMax[1]));
//                }
//            }
//            else if (currentKey == 2 || currentKey == 4 || currentKey == 5) {
//                clusteringVectorNormalized.add(normalize(Double.parseDouble(currentValue), currentMinMax[0], currentMinMax[1]));
//            }
//            else if (currentKey == 10 || currentKey == 11 || currentKey == 8 || currentKey == 9 || currentKey == 7) {
//                Double currentValueDoubleStr = Double.parseDouble(currentValue);
//                int currentValueInt = currentValueDoubleStr.intValue();
//                if (currentValueInt < 0) {
//                    currentValueInt = 0;
//                }
//
//                double currentValueDouble = normalize(currentValueInt, currentMinMax[0], currentMinMax[1]);
//
//                clusteringVectorNormalized.add(currentValueDouble);
//            }
//            else if (currentKey == 6) {
//
//                double[] ttWeekday = trigonometricTransform(1, 7, timestamp.getDayOfWeek().getValue());
//                double[] ttHour = trigonometricTransform(0, 23, timestamp.getHour());
//                double[] ttMinute = trigonometricTransform(0, 59, timestamp.getMinute());
//                double[] ttSecond = trigonometricTransform(0, 59, timestamp.getSecond());
//
//                clusteringVectorNormalized.add(normalize(ttWeekday[0], -1 , 1));
//                clusteringVectorNormalized.add(normalize(ttWeekday[1], -1 , 1));
//                clusteringVectorNormalized.add(normalize(ttHour[0], -1 , 1));
//                clusteringVectorNormalized.add(normalize(ttHour[1], -1 , 1));
//                clusteringVectorNormalized.add(normalize(ttMinute[0], -1 , 1));
//                clusteringVectorNormalized.add(normalize(ttMinute[1], -1 , 1));
//                clusteringVectorNormalized.add(normalize(ttSecond[0], -1 , 1));
//                clusteringVectorNormalized.add(normalize(ttSecond[1], -1 , 1));
//
//                long previousDifference;
//                if (this.previousTimestamp.containsKey(flowDirection)) {
//                    MutableLocalDateTime currentPreviousTimestamp = this.previousTimestamp.get(flowDirection);
//                    previousDifference = SECONDS.between(currentPreviousTimestamp.getLocalDateTime(), timestamp);
//                    currentPreviousTimestamp.setLocalDateTime(timestamp);
//                }
//                else {
//                    previousDifference = 0L;
//                    this.previousTimestamp.put(flowDirection, new MutableLocalDateTime(timestamp));
//                }
//
//                double normalizedPreviousDifference = normalize(previousDifference, currentMinMax[0], currentMinMax[1]);
//
//                clusteringVectorNormalized.add(normalizedPreviousDifference);
//            }
//        }

//        int timestampIndex = selectedValuesProcessing.indexOf("t");
//        LocalDateTime timestamp = null;
//        if (timestampIndex >= 0) {
//            timestamp = parseTimestamp(flowData[selectedValuesProcessing.indexOf("t")], this.formats);
//        }

        for(int i = 0; i < this.selectedValues.length; i++) {
            if (i != selectedKey) {
                String currentValue = flowData[i];
                int currentKey = this.selectedValues[i];
                long[] currentMinMax = this.minMaxValues.get(currentKey);
                String currentProcessing = this.selectedValuesProcessing.get(i);

                if (currentProcessing.equals("n")) {
//                    if (Double.parseDouble(currentValue) == 79.0) {
//                        System.out.println();
//                    }
                    clusteringVectorNormalized.add(normalize(Double.parseDouble(currentValue), currentMinMax[0], currentMinMax[1]));
                }
                else if (currentProcessing.equals("t")) {
                    double[] ttWeekday = trigonometricTransform(1, 7, timestamp.getDayOfWeek().getValue());
                    double[] ttHour = trigonometricTransform(0, 23, timestamp.getHour());
                    double[] ttMinute = trigonometricTransform(0, 59, timestamp.getMinute());
                    double[] ttSecond = trigonometricTransform(0, 59, timestamp.getSecond());

                    clusteringVectorNormalized.add(normalize(ttWeekday[0], -1 , 1));
                    clusteringVectorNormalized.add(normalize(ttWeekday[1], -1 , 1));
                    clusteringVectorNormalized.add(normalize(ttHour[0], -1 , 1));
                    clusteringVectorNormalized.add(normalize(ttHour[1], -1 , 1));
                    clusteringVectorNormalized.add(normalize(ttMinute[0], -1 , 1));
                    clusteringVectorNormalized.add(normalize(ttMinute[1], -1 , 1));
                    clusteringVectorNormalized.add(normalize(ttSecond[0], -1 , 1));
                    clusteringVectorNormalized.add(normalize(ttSecond[1], -1 , 1));

                    long previousDifference;
                    if (this.previousTimestamp.containsKey(flowDirection)) {
                        MutableLocalDateTime currentPreviousTimestamp = this.previousTimestamp.get(flowDirection);
                        previousDifference = SECONDS.between(currentPreviousTimestamp.getLocalDateTime(), timestamp);
                        currentPreviousTimestamp.setLocalDateTime(timestamp);
                    }
                    else {
                        previousDifference = 0L;
                        this.previousTimestamp.put(flowDirection, new MutableLocalDateTime(timestamp));
                    }

                    double normalizedPreviousDifference = normalize(previousDifference, currentMinMax[0], currentMinMax[1]);

                    clusteringVectorNormalized.add(normalizedPreviousDifference);
                }
                else {
                    if (!currentProcessing.equals("l")) {
                        String[] ipAddressInArray = currentValue.split(currentProcessing);
                        for (String s : ipAddressInArray) {
                            clusteringVectorNormalized.add(normalize(Integer.parseInt(s), currentMinMax[0], currentMinMax[1]));
                        }
                    }
                }

            }
        }

//        double[] clusteringDifferences = this.clustering.get(flowDirection).clusterPoint(listToArray(clusteringVectorNormalized), timestamp);

        SplitChangeDifference splitClusteringDifferences = this.clustering.get(flowDirection).insertPoint(listToArray(clusteringVectorNormalized), timestamp, flowMessage.getAnomaly());

//        if (printOut) {
//            double normalizedDistance = 0.0;
//            ChangeDifference[] cdList = splitClusteringDifferences.getChangeDifferences();
//
//            if (cdList != null) {
//                for (ChangeDifference cd : cdList) {
//                    normalizedDistance += cd.getDifference();
//                }
////                clusteringVectorNormalized.add(normalizedDistance / cdList.length);
//            }
//            else {
////                clusteringVectorNormalized.add(0.0);
//            }
//
//
////            clusteringVectorNormalized.add(0.0);
//        }
//
//        ChangeDifference[] changeDifferencesArray = splitClusteringDifferences.getChangeDifferences();
//
//
//
//        double distance = 0;
//        double normalizedDistance = 0;
//        if (changeDifferencesArray != null) {
//            System.out.println(changeDifferencesArray.length);
////            System.out.println((this.pointLength + 1) + " " + changeDifferencesArray.length);
//            double[] clusteringDifferences = new double[changeDifferencesArray.length + 1];
//            if (splitClusteringDifferences.isClusterAnomaly()) {
//                clusteringDifferences[0] = 1.0;
////                System.out.println();
//            }
//            for (int i = 1; i < changeDifferencesArray.length + 1; i++) {
//                double value = 0.0;
//                if (changeDifferencesArray[i-1].isChange()) {
//                    value = 1.0;
////                    System.out.println("Anomaly");
//                }
//                clusteringDifferences[i] = value;
//            }
//
//            distance = sumArray(clusteringDifferences);
//            normalizedDistance = distance / (clusteringDifferences.length + 1);
//        }

//        if (distance > 5.0 || splitClusteringDifferences.isInterClusterAnomaly() || splitClusteringDifferences.isIntraClusterAnomaly() || splitClusteringDifferences.isClusterAnomaly()) {
//            distance = 1.0;
//        }

//        if (distance > 0.0) {
//
//            if (!this.count.containsKey(flowDirection)) {
//                AtomicInteger mi = new AtomicInteger();
//                mi.getAndIncrement();
//                this.count.put(flowDirection, mi);
//            }
//            else {
//                this.count.get(flowDirection).getAndIncrement();
//            }
//
//            double thresholdNormalized = 0.0;
//
//            if (!this.average.containsKey(flowDirection)) {
//                this.average.put(flowDirection, new MutableDouble(distance));
//                this.variance.put(flowDirection, new MutableDouble(0.0));
//            } else {
//                double currentCount = (double)this.count.get(flowDirection).get();
//                MutableDouble average = this.average.get(flowDirection);
//                double previousAverage = average.getValue();
//                double newAverage = previousAverage + ((distance - previousAverage) / currentCount);
//                average.setValue(newAverage);
//
//                MutableDouble variance = this.variance.get(flowDirection);
//                double newVariance = variance.getValue() + (distance - previousAverage) * (distance - newAverage);
//                variance.setValue(newVariance);
//
//                double standardDeviation = Math.sqrt(newVariance / currentCount);
//
//                double threshold = (3 * standardDeviation) + newAverage;
//                thresholdNormalized = ((3 * standardDeviation) + newAverage) / (double) (this.pointLength + 1);
//
//                if (distance > threshold) {
//
//                    this.average.get(flowDirection).setValue(distance);
//                    this.variance.get(flowDirection).setValue(0.0);
//                    this.count.get(flowDirection).getAndSet(1);
//
//                    if (this.previousAnomaly.containsKey(flowDirection)) {
//                        boolean anomaly = this.previousAnomaly.get(flowDirection).get();
//                        this.previousAnomaly.get(flowDirection).getAndSet(!anomaly);
//                    }
//                    else {
//                        this.previousAnomaly.put(flowDirection, new AtomicBoolean(true));
//                    }
//                }
//            }
//
//            if (printOut) {
////                clusteringVectorNormalized.add(thresholdNormalized);
////                clusteringVectorNormalized.add(0.0);
//            }
//
//        }
//        else {
//            if (printOut) {
////                clusteringVectorNormalized.add(0.0);
//            }
//        }
//
//        if (this.previousAnomaly.containsKey(flowDirection)) {
//            if (this.previousAnomaly.get(flowDirection).get() || splitClusteringDifferences.isClusterAnomaly()) {
//                distance = 1.0;
////                normalizedDistance = 1.0;
//            } else {
//                distance = 0.0;
//            }
//        } else {
//            distance = 0.0;
//        }


        // PASS THROUGH FOR ANOMALY DETECTION

//        double normalizedDistance = 0.0;
//        double distance = 0.0;
//
//        if (splitClusteringDifferences.isAdditionalDetection()) {
//            distance = 1.0;
//            normalizedDistance = 1.0;
//        }


//        if (distance > 5.0 || splitClusteringDifferences.isInterClusterAnomaly() || splitClusteringDifferences.isIntraClusterAnomaly() || splitClusteringDifferences.isClusterAnomaly()) {

        byte[] adwin = splitClusteringDifferences.getChangeDifferences();
        double adwinChange = 0.0;
        double adwinNormalized = 0.0;
        double intraNormalized = 0.0;
        if (adwin != null) {
            adwinChange = adwin[0];

            adwinNormalized = adwin[0];
            intraNormalized = adwin[1];
//            for (int i = 0; i < adwin.length; i++) {
//                adwinNormalized += adwin[i];
//            }
//            if (adwinNormalized > 1.0) {
//                adwinChange = 1.0;
//            }
//            adwinNormalized /= adwin.length;
        }
////        if (adwinNormalized > 0.0) {
////            System.out.println(adwinNormalized);
////        }

        byte[] inter = splitClusteringDifferences.isInterClusterAnomaly();
        double interChange = 0.0;
        double interNormalized = 0.0;
        if (inter != null) {
            for (int i = 0; i < inter.length; i++) {
                interNormalized += inter[i];
            }
            if (interNormalized > 0.0) {
                interChange = 1.0;
            }
            interNormalized /= inter.length;
        }
//
//        byte[] intra = splitClusteringDifferences.isIntraClusterAnomaly();
//        double intraChange = 0.0;
//        double intraNormalized = 0.0;
//        if (intra != null) {
//            for (int i = 0; i < intra.length; i++) {
//                intraNormalized += intra[i];
//            }
//            if (intraNormalized > 0.0) {
//                intraChange = 1.0;
//            }
//            intraNormalized /= intra.length;
//        }
//
        double cluster = 0.0;
        if (splitClusteringDifferences.isClusterAnomaly()) {
            cluster = 1.0;
        }

//        if (adwinChange > 0 || interChange > 0) {
//            adwinChange = 1.0;
//        }

        adwinNormalized = (adwinChange + interNormalized + intraNormalized + cluster)/4.0;

//        if (adwinNormalized > 0.25) {
//            System.out.println();
//        }

        double[] result = new double[]{adwinNormalized, adwinChange, interNormalized, intraNormalized, cluster};
        if (printOut) {
            result = concatTwo(result, listToArray(clusteringVectorNormalized));
        }
        return result;
    }

    /**
     * The methods concatenates two double arrays.
     * @param a1 double[] array that present the first input double array.
     * @param a2 double[] array that present the second input double array.
     * @return double[] array that presents concatenation of the input double arrays.
     */
    public double[] concatTwo(double[] a1, double[] a2) {
        double[] array = new double[a1.length + a2.length];

        System.arraycopy(a1, 0, array, 0, a1.length);
        System.arraycopy(a2, 0, array, a1.length, a2.length);

        return array;
    }

    /**
     * The methods uses trigonometric transformation on the given value.
     * @param minValue double value that presents the minimum value that the input value can take.
     * @param maxValue double value that presents the maximum value that the input value can take.
     * @param value double value that presents the input value to be transformed.
     * @return double[] array that presents the sine and cosine value of the input value after the transformation.
     */
    private static double[] trigonometricTransform(double minValue, double maxValue, double value) {
        double sin = Math.sin(rescale(minValue, maxValue, 0, 2 * Math.PI, value));
        double cos = Math.cos(rescale(minValue, maxValue, 0, 2 * Math.PI, value));
        return new double[]{sin, cos};
    }

    /**
     * The method rescales the input value to the new interval.
     * @param oldMinValue double value that presents the minimum value that the input value can take.
     * @param oldMaxValue double value that presents the maximum value that the input value can take.
     * @param newMinValue double value that presents the minimum value that the rescaled value can take.
     * @param newMaxValue double value that presents the maximum value that the rescaled value can take.
     * @param value double value that presents the input value to be rescaled.
     * @return double value that presents the rescaled input value.
     */
    public static double rescale(double oldMinValue, double oldMaxValue, double newMinValue, double newMaxValue, double value) {
        return (((value - oldMinValue) * (newMaxValue - newMinValue)) / (oldMaxValue - oldMinValue)) + newMinValue;
    }

    /**
     * The method normalizes the input value.
     * @param value double value that presents the input value to be normalized.
     * @param minValue double value that presents the minimum value that the input value can take.
     * @param maxValue double value that presents the maximum value that the input value can take.
     * @return double value that presents the normalized input value.
     */
    private double normalize(double value, double minValue, double maxValue) {

        if (value < minValue) {
            value = minValue;
        }
        else if (value > maxValue) {
            value = maxValue;
        }
        return (value - minValue) / (maxValue - minValue);
    }

    /**
     * The method parses time-stamp string.
     * @param timestampString String that presents the given time-stamp.
     * @param formats DateTimeFormatter[] array that presents the possible formats for parsing DateTime.
     * @return LocalDateTime that presents the given time-stamp.
     */
    public static LocalDateTime parseTimestamp(String timestampString, DateTimeFormatter[] formats) {

        int count = 0;
        for (int i = 9; i < timestampString.length(); i++) {
            final char c = timestampString.charAt(i);
            if (c == ':') {
                count++;
            }
        }

        return LocalDateTime.parse(timestampString, formats[count - 1]);
    }

    /**
     * The method transforms the given List of doubles to array of doubles.
     * @param list List&lt;Dobule&gt; that presents the given List of doubles.
     * @return double[] array of the give List of doubles.
     */
    public static double[] listToArray(List<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < array.length; i++){
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * The method returns the current clustering objects.
     * @return HashMap&lt;String,BIRCH&gt; that presents current clustering objects.
     */
    public HashMap<String, CFTree> getClustering() {
        return this.clustering;
    }
//    public HashMap<String, BIRCH_F> getClustering() {
//        return this.clustering;
//    }
//    public HashMap<String, BIRCH> getClustering() {
//        return this.clustering;
//    }
}

