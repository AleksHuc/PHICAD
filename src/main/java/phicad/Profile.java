package phicad;

import tbirchi.CFTree;
import tbirchi.SplitChangeDifference;
import utils.FlowMessage;
import utils.MutableDouble;
import utils.MutableLocalDateTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.temporal.ChronoUnit.SECONDS;
import static utils.Utils.*;

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

    private HashMap<String, MutableLocalDateTime> previousTimestamp;
    private HashMap<String,MutableLocalDateTime> lastCheckTimestamp;
    private HashMap<String, MutableDouble> average;
    private HashMap<String,MutableDouble> variance;
    private HashMap<String,AtomicInteger> count;
    private HashMap<Integer,long[]> minMaxValues;
    private HashMap<String,CFTree> clustering;
    private HashMap<String, AtomicBoolean> previousAnomaly;

    /**
     * The constructor creates new Profile object from given parameters.
     * @param minMaxValues HashMap&lt;Integer,long[]&gt; object that presents the minimum and maximum values for each network flow feature we analyze.
     * @param selectedValues Integer array that presents the indexes of network flow features to be used in analysis.
     * @param selectedValuesProcessing ArrayList&lt;String&gt; object that presents how each selected network feature should be analyzed.
     * @param selectedKey int array that presents indexes of parameters that present source and destination IP addresses.
     * @param pointLength int value that presents the length of the feature vector.
     * @param formats DateTimeFormatter array that presents the two datetime formats the network flows use.
     * @param printOut boolean value that present if verbose print in enabled.
     * @param distanceFunction int value that present the selected distance function.
     * @param name String object that presents the name of the current Profile.
     * @param maxChildren int value that presents the maximum number of incremental cluster features that incremental cluster feature node can store.
     * @param maxNumberOfNodes int value that present the maximum number of incremental cluster feature nodes that incremental cluster feature tree can store.
     * @param lambda double value that presents the fading factor for forgetting old data.
     * @param threshold double value that presents the initial incremental cluster feature radius threshold.
     * @param checkStep int value that presents the number of iterations before we check for anomaly.
     * @param maxBins int value that presents the maximum number of bins the exponential histogram can have.
     * @param sizeOfBin int value that presents the maximum number of values inside a single bin.
     * @param delta double value that presents the maximum delta (confidence) value for the ADWIN windows.
     * @param largeWindowSize int value that presents the size of the long-term window of short-term models.
     * @param largeWindowProbability double value that presents the maximum probability of a given detection mechanism triggering to still be considered useful.
     * @param smallWindowSize int value that presents the size of the short-term window of short-term models.
     * @param smallWindowProbability double value that presents the minimum probability of a given detection mechanism triggering to still be considered useful.
     * @param clusterSizeThreshold double value that presents the multiplication factor for the harmonic mean to determine normal clusters based on their size.
     * @param intraClusterThreshold double value that presents the multiplication factor for the standard deviation to determine distance threshold.
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
            put("fwd", new CFTree(maxChildren, maxNumberOfNodes, lambda, threshold, checkStep, maxBins, sizeOfBin, delta, largeWindowSize, largeWindowProbability, smallWindowSize, smallWindowProbability, clusterSizeThreshold, intraClusterThreshold, distanceFunction, name + "_fwd"));
            put("bwd", new CFTree(maxChildren, maxNumberOfNodes, lambda, threshold, checkStep, maxBins, sizeOfBin, delta, largeWindowSize, largeWindowProbability, smallWindowSize, smallWindowProbability, clusterSizeThreshold, intraClusterThreshold, distanceFunction, name + "_bwd"));
        }};
    }

    /**
     * The method updates the internal state of the profile with the current network flow.
     * @param flowMessage FlowMessage object that presents the direction and network flow attributes.
     * @return double[] array that presents if the change has been detected by any of the detection mechanisms and the feature vector of the current flow.
     */
    double[] updateProfile(FlowMessage flowMessage) {

        ArrayList<Double> clusteringVectorNormalized = new ArrayList<>();

        String flowDirection = flowMessage.getDirection();
        String[] flowData = flowMessage.getFlow();

        int timestampIndex = selectedValuesProcessing.indexOf("t");
        LocalDateTime timestamp = null;
        if (timestampIndex >= 0) {
            timestamp = parseTimestamp(flowData[selectedValuesProcessing.indexOf("t")], this.formats);
        }

        for(int i = 0; i < this.selectedValues.length; i++) {
            if (i != selectedKey) {
                String currentValue = flowData[i];
                int currentKey = this.selectedValues[i];
                long[] currentMinMax = this.minMaxValues.get(currentKey);
                String currentProcessing = this.selectedValuesProcessing.get(i);

                if (currentProcessing.equals("n")) {
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

        SplitChangeDifference splitClusteringDifferences = this.clustering.get(flowDirection).insertPoint(listToArray(clusteringVectorNormalized), timestamp);

        byte[] adwin = splitClusteringDifferences.getChangeDifferences();
        double adwinChange = 0.0;
        double adwinNormalized = 0.0;
        double intraNormalized = 0.0;
        if (adwin != null) {
            adwinChange = adwin[0];

            adwinNormalized = adwin[0];
            intraNormalized = adwin[1];
        }

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

        double cluster = 0.0;
        if (splitClusteringDifferences.isClusterAnomaly()) {
            cluster = 1.0;
        }

        adwinNormalized = (adwinChange + interNormalized + intraNormalized + cluster)/4.0;

        double[] result = new double[]{adwinNormalized, adwinChange, interNormalized, intraNormalized, cluster};
        if (printOut) {
            result = concatTwo(result, listToArray(clusteringVectorNormalized));
        }
        return result;
    }
}

