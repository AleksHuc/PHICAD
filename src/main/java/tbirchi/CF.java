package tbirchi;

import adwin.ADWIN;
import adwin.ChangeDifference;
import utils.HitWindow;
import utils.MutableDouble;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * The class implements an object that present the incremental cluster feature.
 */
public class CF {

    private boolean normalCluster;
    private boolean anomalyCluster;
    private int n;
    private double w;
    private double normalClusterThreshold;
    private double intraClusterThreshold;
    private long changeTime;
    private long id;
    private double lambda = 0.001;
    private double[] linearSum;
    private double[] squareSum;
    private CFNode child;
    private LocalDateTime timestamp;
    private LocalDateTime creationTimestamp;
    private ArrayList<ADWIN> windows;
    private ArrayList<ADWIN> windowsD;
    private ArrayList<HitWindow> hitWindows;
    private ArrayList<HitWindow> intraHitWindows;
    public PrintWriter pw;
    private String name;

    private boolean[] flags;
    private boolean[] intraFlags;
    private int[] lastSwitch;
    private int[] intraLastSwitch;

    private double delta;
    private int checkStep;
    private int maxBins;
    private int sizeOfBin;
    private int[] windowsRate;
    private int hitWindowLength;
    private double hitWindowProbability;
    private int anomalyWindowLength;
    private double anomalyWindowProbability;
    private int hitIntraWindowLength;
    private double hitIntraWindowProbability;
    private int anomalyIntraWindowLength;
    private double anomalyIntraWindowProbability;

    /**
     * The constructor creates an empty incremental cluster features with the corresponding parameters.
     * @param lambda double value that presents the fading factor for forgetting old data.
     * @param delta double value that presents the maximum delta (confidence) value for the ADWIN windows.
     * @param checkStep int value that presents the number of iterations before we check for anomaly.
     * @param maxBins int value that presents the maximum number of bins the exponential histogram can have.
     * @param sizeOfBin int value that presents the maximum number of values inside a single bin.
     * @param normalClusterThreshold double value that presents the multiplication factor for the harmonic mean to determine normal clusters based on their size.
     * @param id long value that presents the unique identifier of the corresponding incremental cluster feature.
     * @param name String value that presents the name of the corresponding incremental cluster feature.
     * @param largeWindowSize int value that presents the size of the long-term window of short-term models.
     * @param largeWindowProbability double value that presents the maximum probability of a given detection mechanism triggering to still be considered useful.
     * @param smallWindowSize int value that presents the size of the short-term window of short-term models.
     * @param smallWindowProbability double value that presents the minimum probability of a given detection mechanism triggering to still be considered useful.
     * @param intraClusterThreshold double value that presents the multiplication factor for the standard deviation to determine distance threshold.
     */
    public CF(double lambda, double delta, int checkStep, int maxBins, int sizeOfBin, double normalClusterThreshold, long id, String name, int largeWindowSize, double largeWindowProbability, int smallWindowSize, double smallWindowProbability, double intraClusterThreshold) {
        this.n = 0;
        this.w = 0.0;
        this.linearSum = null;
        this.squareSum = null;
        this.child = null;
        this.timestamp = null;
        this.windows = null;
        this.lambda = lambda;
        this.delta = delta;
        this.checkStep = checkStep;
        this.maxBins = maxBins;
        this.sizeOfBin = sizeOfBin;
        this.normalCluster = true;
        this.anomalyCluster = false;
        this.normalClusterThreshold = normalClusterThreshold;
        this.id = id;
        this.name = name;
        this.intraClusterThreshold = intraClusterThreshold;
        this.hitWindowLength = largeWindowSize;
        this.hitIntraWindowLength = largeWindowSize;
        this.hitWindowProbability = largeWindowProbability;
        this.hitIntraWindowProbability = largeWindowProbability;
        this.anomalyWindowLength = smallWindowSize;
        this.anomalyIntraWindowLength = smallWindowSize;
        this.anomalyWindowProbability = smallWindowProbability;
        this.anomalyIntraWindowProbability = smallWindowProbability;
    }

    /**
     * The method returns the current value of changeTime variable.
     * @return long value of the current value of changeTime variable.
     */
    public long getChangeTime() {
        return changeTime;
    }

    /**
     * The method sets the changeTime variable to argument value.
     * @param changeTime long value that presents the current value of changeTime.
     */
    public void setChangeTime(long changeTime) {
        this.changeTime = changeTime;
    }

    /**
     * The method returns the incremental cluster feature creation time.
     * @return LocalDateTime object that presents the incremental cluster feature creation timestamp.
     */
    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * The method sets incremental cluster feature creation time to argument value.
     * @param creationTimestamp LocalDateTime object of the creation timestamp.
     */
    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }


    /**
     * The constructor creates a new incremental cluster feature from the current input point with the corresponding parameters.
     * @param point double array that present the vector of the input point.
     * @param timestamp LocalDateTime object that present the current time timestamp.
     * @param lambda double value that presents the fading factor for forgetting old data.
     * @param delta double value that presents the maximum delta (confidence) value for the ADWIN windows.
     * @param checkStep int value that presents the number of iterations before we check for anomaly.
     * @param maxBins int value that presents the maximum number of bins the exponential histogram can have.
     * @param sizeOfBin int value that presents the maximum number of values inside a single bin.
     * @param normalClusterThreshold double value that presents the multiplication factor for the harmonic mean to determine normal clusters based on their size.
     * @param id long value that presents the unique identifier of the corresponding incremental cluster feature.
     * @param name String value that presents the name of the corresponding incremental cluster feature.
     * @param largeWindowSize int value that presents the size of the long-term window of short-term models.
     * @param largeWindowProbability double value that presents the maximum probability of a given detection mechanism triggering to still be considered useful.
     * @param smallWindowSize int value that presents the size of the short-term window of short-term models.
     * @param smallWindowProbability double value that presents the minimum probability of a given detection mechanism triggering to still be considered useful.
     * @param intraClusterThreshold double value that presents the multiplication factor for the standard deviation to determine distance threshold.
     */
    public CF(double[] point, LocalDateTime timestamp, double lambda, double delta, int checkStep, int maxBins, int sizeOfBin, double normalClusterThreshold, long id, String name, int largeWindowSize, double largeWindowProbability, int smallWindowSize, double smallWindowProbability, double intraClusterThreshold){
        this.n = 1;
        this.w = 1.0;
        this.timestamp = timestamp;
        this.creationTimestamp = timestamp;
        this.changeTime = 0;
        this.child = null;
        this.windows = new ArrayList<>();
        this.windowsD = new ArrayList<>();
        this.lambda = lambda;
        this.delta = delta;
        this.checkStep = checkStep;
        this.maxBins = maxBins;
        this.sizeOfBin = sizeOfBin;
        this.normalCluster = true;
        this.anomalyCluster = false;
        this.normalClusterThreshold = normalClusterThreshold;
        this.id = id;
        this.name = name;
        this.intraFlags = new boolean[point.length];;
        this.flags = new boolean[2 * point.length + 1];
        this.lastSwitch = new int[2 * point.length + 1];
        this.intraLastSwitch = new int[point.length];
        this.windowsRate = new int[2 * point.length + 1];
        this.hitWindows = new ArrayList<>();
        this.intraHitWindows = new ArrayList<>();
        this.intraClusterThreshold = intraClusterThreshold;
        this.hitWindowLength = largeWindowSize;
        this.hitIntraWindowLength = largeWindowSize;
        this.hitWindowProbability = largeWindowProbability;
        this.hitIntraWindowProbability = largeWindowProbability;
        this.anomalyWindowLength = smallWindowSize;
        this.anomalyIntraWindowLength = smallWindowSize;
        this.anomalyWindowProbability = smallWindowProbability;
        this.anomalyIntraWindowProbability = smallWindowProbability;

        this.linearSum = new double[point.length];
        System.arraycopy(point, 0, this.linearSum, 0, this.linearSum.length);

        this.squareSum = new double[point.length];
        for (int i = 0; i < this.squareSum.length; i++) {
            this.squareSum[i] = point[i] * point[i];
        }

        for (int i = 0; i < this.linearSum.length + 1; i++) {
            ADWIN currentADWIN = new ADWIN(this.delta, this.checkStep, this.maxBins, this.sizeOfBin);
            if (i == this.linearSum.length) {
                currentADWIN.addNewValue(0);
            }
            else {
                currentADWIN.addNewValue(point[i]);
            }
            this.windows.add(currentADWIN);
        }

        for (int i = 0; i < this.linearSum.length; i++) {
            ADWIN currentADWIN = new ADWIN(this.delta, this.checkStep, this.maxBins, this.sizeOfBin);
            currentADWIN.addNewValue(point[i]);
            this.windowsD.add(currentADWIN);
        }

        for (int i = 0; i < 2 * this.linearSum.length + 1; i++) {
            HitWindow hw = new HitWindow(hitWindowLength);
            this.hitWindows.add(hw);
        }

        for (int i = 0; i < this.linearSum.length; i++) {
            HitWindow hw = new HitWindow(hitIntraWindowLength);
            this.intraHitWindows.add(hw);
        }
    }

    /**
     * The method updates the incremental cluster feature with another incremental cluster feature.
     * @param cf CF object that present the incremental cluster feature to be merged with the current incremental cluster feature.
     * @return byte array of results from detection mechanisms in incremental cluster feature.
     */
    byte[] update(CF cf) {

        this.checkIntraAnomalies(cf);

        long timestampDifference = this.updateValues(cf);

        byte[] differences = this.updateWindows(cf, timestampDifference);

        return this.determineAnomalies(differences);
    }

    /**
     * The method determines if any change has been detected during the incremental cluster feature update.
     * @param differences byte array that presents results from change detection mechanisms from each individual ADWIN window.
     * @return byte array of results from detection mechanisms in incremental cluster feature.
     */
    byte[] determineAnomalies(byte[] differences) {
        byte[] anomalies = new byte[2];
        if (differences != null) {
            int anomalyCount = 0;
            for (int i = 0; i < flags.length; i++) {
                if (flags[i]) {
                    anomalyCount++;
                }
            }
            if (anomalyCount > 1) {
                anomalies[0] = 1;
            }
            anomalyCount = 0;
            for (int i = 0; i < intraFlags.length; i++) {
                if (intraFlags[i]) {
                    anomalyCount++;
                }
            }
            if (anomalyCount > 1) {
                anomalies[1] = 1;
            }
        }
        return anomalies;
    }

    /**
     * The method updates the ADWIN windows inside the current incremental cluster feature with the values from new incremental cluster feature.
     * @param cf CF object that present the incremental cluster feature to be merged with the current incremental cluster feature.
     * @param timestampDifference long value that presents the timestamp difference between current and previous update to this incremental cluster feature.
     * @return byte array of results from change detection mechanisms from each individual ADWIN window.
     */
    byte[] updateWindows(CF cf, long timestampDifference) {

        byte[] differences = null;

        if (this.windows != null && cf.getN() == 1) {

            double normalizedTimestampDifference = Math.abs(timestampDifference) / 6000.0;
            if (normalizedTimestampDifference > 1.0) {
                normalizedTimestampDifference = 1.0;
            }

            double[] wi = new double[this.windows.size()];
            double[] wid = new double[this.windowsD.size()];

            try {
                differences = new byte[2*this.linearSum.length + 1];
                for (int i = 0; i < this.linearSum.length + 1; i++) {

                    double currentValue;
                    if (i == this.linearSum.length) {
                        currentValue = Math.abs(normalizedTimestampDifference);
                    } else {
                        currentValue = this.linearSum[i] / this.w;
                    }

                    ChangeDifference cd = this.windows.get(i).addNewValue(currentValue);

                    if (cd.isChange()) {
                        this.hitWindows.get(i).add(this.n, 1.0);
                        double t = hitWindowLength * hitWindowProbability;
                        if (this.n < hitWindowLength) {
                            t = this.n * hitWindowProbability;
                        }

                        if (this.hitWindows.get(i).newestSinceSize(this.n, hitWindowLength) < t) {
                            if (this.hitWindows.get(i).newestSinceSize(this.n, anomalyWindowLength) > (anomalyWindowLength * anomalyWindowProbability)) {
                                this.flags[i] = true;
                                this.lastSwitch[i] = this.n;
                            }
                        }
                    }

                    else {
                        if (this.flags[i]){
                            this.flags[i] = false;
                        }
                    }

                    if (this.flags[i]) {
                        wi[i] = 1.0;
                        differences[i] = 1;
                    }
                }

                double[] currentLinearSum = cf.getLinearSum();
                int lengthOffset = this.linearSum.length + 1;
                double[] differences2 = new double[this.linearSum.length];
                for (int i = 0; i < this.linearSum.length; i++) {
                    ChangeDifference cd = this.windowsD.get(i).addNewValue(Math.abs(this.windows.get(i).getLatestValue() - currentLinearSum[i]/cf.getW()));
                    differences2[i] = Math.abs(this.windows.get(i).getLatestValue() - currentLinearSum[i]/cf.getW());

                    if (cd.isChange()) {

                        this.hitWindows.get(lengthOffset + i).add(this.n, 1.0);

                        double t = hitWindowLength * hitWindowProbability;
                        if (this.n < hitWindowLength) {
                            t = this.n * hitWindowProbability;
                        }

                        if (this.hitWindows.get(lengthOffset + i).newestSinceSize(this.n, hitWindowLength) < t) {

                            if (this.hitWindows.get(lengthOffset + i).newestSinceSize(this.n, anomalyWindowLength) > (anomalyWindowLength * anomalyWindowProbability)) {
                                this.flags[lengthOffset + i] = true;
                                this.lastSwitch[lengthOffset + i] = this.n;
                            }

                        }
                    }
                    else {
                        if (this.flags[lengthOffset + i]){
                            this.flags[lengthOffset + i] = false;
                        }
                    }

                    if (this.flags[lengthOffset + i]) {
                        wid[i] = 1.0;
                        differences[lengthOffset + i] = 1;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return differences;
    }

    /**
     The method updates the incremental cluster feature statistics with another incremental cluster feature.
     * @param cf CF object that present the incremental cluster feature to be merged with the current incremental cluster feature.
     * @return long value that presents the timestamp difference between current and previous update to this incremental cluster feature.
     */
    long updateValues(CF cf) {
        // Calculate timestamp difference between the new cf added and the cf and calculate new weight.
        long timestampDifference = 0;
        double currentW = 1.0;

        LocalDateTime cfTimestamp = cf.getTimestamp();

        if (this.timestamp != null && cfTimestamp != null) {
            timestampDifference = SECONDS.between(this.timestamp, cfTimestamp);
            currentW = Math.pow(2, -this.lambda * Math.abs(timestampDifference));
        }

        // Update the number of flows held by the cf.
        this.n += cf.getN();

        // Depending on the timestamp difference update the cf weight, timestamp, linear and square sums.
        if (timestampDifference >= 0.0) {
            this.w = this.w * currentW + cf.getW();
            this.timestamp = cfTimestamp;

            double[] currentLinearSum = cf.getLinearSum();
            if(this.linearSum == null){
                this.linearSum = currentLinearSum.clone();
            }
            else {
                for (int i = 0; i < this.linearSum.length; i++) {
                    this.linearSum[i] = this.linearSum[i] * currentW + currentLinearSum[i];
                }
            }

            double[] currentSquareSum = cf.getSquareSum();
            if(this.squareSum == null){
                this.squareSum = currentSquareSum.clone();
            }
            else {
                for (int i = 0; i < this.squareSum.length; i++) {
                    this.squareSum[i] = this.squareSum[i] * currentW + currentSquareSum[i];
                }
            }
        }
        else {
            this.w = cf.getW() * currentW + this.w;

            double[] currentLinearSum = cf.getLinearSum();
            if(this.linearSum == null){
                this.linearSum = currentLinearSum.clone();
            }
            else {
                for (int i = 0; i < this.linearSum.length; i++) {
                    this.linearSum[i] = currentLinearSum[i] * currentW + this.linearSum[i];
                }
            }

            double[] currentSquareSum = cf.getSquareSum();
            if(this.squareSum == null){
                this.squareSum = currentSquareSum.clone();
            }
            else {
                for (int i = 0; i < this.squareSum.length; i++) {
                    this.squareSum[i] = currentSquareSum[i] * currentW + this.squareSum[i];
                }
            }
        }
        return timestampDifference;
    }

    /**
     * The method checks for the distance between the centroid of the current incremental cluster feature and a new merged incremental cluster feature and determines if a change has occurred.
     * @param cf CF object that present the incremental cluster feature to be merged with the current incremental cluster feature.
     * @return double array that represents the results from change detection mechanisms for each dimension.
     */
    double[] checkIntraAnomalies(CF cf) {
        // Check if the new cf added to the cf falls out of the standard deviation threshold.
        byte[] intraClusterAnomaly = null;
        double[] winter = null;
        if (this.windows != null) {

            winter = new double[windowsD.size()];
            intraClusterAnomaly = this.checkStandardDeviation(cf);
            for (int i = 0; i < intraClusterAnomaly.length; i++) {
                if (intraClusterAnomaly[i] > 0) {
                    this.intraHitWindows.get(i).add(this.n + 1, 1.0);

                    double t = hitIntraWindowLength * hitIntraWindowProbability;
                    if (this.n < hitIntraWindowLength) {
                        t = this.n * hitIntraWindowProbability;
                    }

                    if (this.intraHitWindows.get(i).newestSinceSize(this.n, hitIntraWindowLength) < t) {
                        if (this.intraHitWindows.get(i).newestSinceSize(this.n, anomalyIntraWindowLength) > (anomalyIntraWindowLength * anomalyIntraWindowProbability)) {
                            this.intraFlags[i] = true;
                            this.intraLastSwitch[i] = this.n;
                        }
                    }
                } else {
                    this.intraFlags[i] = false;
                }

                if (this.intraFlags[i]) {
                    winter[i] = 1.0;
                }

            }
        }
        return winter;
    }

    /**
     * The method checks if a new incremental cluster feature is inside of a threshold
     * @param cf CF object that present the incremental cluster feature to be merged with the current incremental cluster feature.
     * @param distanceThreshold double value that presents the maximum cluster radius for including new incremental clsuter features.
     * @param distanceFunction int value that determines which distance function to use.
     * @return boolean value that determines if merged incremental cluster feature is within the cluster or not.
     */
    boolean isWithinThreshold(CF cf, double distanceThreshold, int distanceFunction) {
        double distance = distance(cf, distanceFunction);
        return distance == 0 || distance <= distanceThreshold;
    }

    /**
     * The method calculates the distance between two incremental cluster features.
     * @param cf CF object that present the new incremental cluster feature.
     * @param distFunction int value that determines which distance function to use.
     * @return double value that presents the distance between two incremental cluster features.
     */
    protected double distance(CF cf, int distFunction) {
        double dist = Double.MAX_VALUE;

        switch(distFunction) {
            case CFTree.D0_DIST:
                dist = d0(this, cf);
                break;
            case CFTree.D1_DIST:
                dist = d1(this, cf);
                break;
            case CFTree.D2_DIST:
                dist = d2(this, cf);
                break;
            case CFTree.D3_DIST:
                dist = d3(this, cf);
                break;
            case CFTree.D4_DIST:
                dist = d4(this, cf);
                break;
            case CFTree.D5_DIST:
                dist = d5(this, cf);
                break;
        }

        return dist;
    }

    /**
     * The method calculates the Euclidean distance between the two incremental cluster features.
     * @param cf1 CF object that presents the first incremental cluster feature.
     * @param cf2 CF object that presents the second incremental cluster feature.
     * @return double value that presents the distance between the two incremental cluster features.
     */
    private double d0(CF cf1, CF cf2) {
        double dist = 0;
        double wCF1 = cf1.getW();
        double wCF2 = cf2.getW();
        double[] linearSumCF1 = cf1.getLinearSum();
        double[] linearSumCF2 = cf2.getLinearSum();
        for (int i = 0; i < linearSumCF1.length; i++) {
            double diff = linearSumCF1[i]/wCF1 - linearSumCF2[i]/wCF2;
            dist += diff*diff;
        }

        if (dist < 0.0){
            if(dist < -0.00000001)
                System.err.println("d0 < 0 !!!");
            dist = 0.0;
        }

        return Math.sqrt(dist);
    }

    /**
     * The method calculates the Manhattan distance between the two incremental cluster features.
     * @param cf1 CF object that presents the first incremental cluster feature.
     * @param cf2 CF object that presents the second incremental cluster feature.
     * @return double value that presents the distance between the two incremental cluster features.
     */
    private double d1(CF cf1, CF cf2) {
        double dist = 0;
        double wCF1 = cf1.getW();
        double wCF2 = cf2.getW();
        double[] linearSumCF1 = cf1.getLinearSum();
        double[] linearSumCF2 = cf2.getLinearSum();
        for (int i = 0; i < linearSumCF1.length; i++) {
            double diff = Math.abs(linearSumCF1[i]/wCF1 - linearSumCF2[i]/wCF2);
            dist += diff;
        }

        if (dist < 0.0){
            if(dist < -0.00000001 )
                System.err.println("d1 < 0 !!!");
            dist = 0.0;
        }

        return dist;
    }

    /**
     * The method calculates the average inter-cluster distance between the two incremental cluster features.
     * @param cf1 CF object that presents the first incremental cluster feature.
     * @param cf2 CF object that presents the second incremental cluster feature.
     * @return double value that presents the distance between the two incremental cluster features.
     */
    private double d2(CF cf1, CF cf2) {
        double dist = 0;

        double wCF1 = cf1.getW();
        double wCF2 = cf2.getW();
        double[] linearSumCF1 = cf1.getLinearSum();
        double[] linearSumCF2 = cf2.getLinearSum();
        double[] squareSumCF1 = cf1.getSquareSum();
        double[] squareSumCF2 = cf2.getSquareSum();
        for (int i = 0; i < linearSumCF1.length; i++) {
            double diff = (wCF2 * squareSumCF1[i] - 2 * linearSumCF1[i]*linearSumCF2[i] + wCF1 * squareSumCF2[i])/(wCF1 * wCF2);
            dist += diff;
        }

        if (dist < 0.0){
            if(dist < -0.00000001 )
                System.err.println("d2 < 0 !!!");
            dist = 0.0;
        }

        return Math.sqrt(dist);
    }

    /**
     * The method calculates the average intra-cluster distance between the two incremental cluster features.
     * @param cf1 CF object that presents the first incremental cluster feature.
     * @param cf2 CF object that presents the second incremental cluster feature.
     * @return double value that presents the distance between the two incremental cluster features.
     */
    private double d3(CF cf1, CF cf2) {
        double dist = 0;
        double wCF1 = cf1.getW();
        double wCF2 = cf2.getW();
        double[] linearSumCF1 = cf1.getLinearSum();
        double[] linearSumCF2 = cf2.getLinearSum();
        double[] squareSumCF1 = cf1.getSquareSum();
        double[] squareSumCF2 = cf2.getSquareSum();
        double[] totalLinearSum = linearSumCF1.clone();
        double[] totalSquareSum = squareSumCF1.clone();
        for(int i = 0; i < linearSumCF1.length; i++) {
            totalLinearSum[i] += linearSumCF2[i];
            totalSquareSum[i] += squareSumCF2[i];
        }

        for(int i = 0; i < totalLinearSum.length; i++) {
            double diff = ((wCF1 + wCF2) * totalSquareSum[i] - 2 * totalLinearSum[i] * totalLinearSum[i] + (wCF1 + wCF2) * totalSquareSum[i])/((wCF1 + wCF2) * (wCF1 + wCF2 - 1));
            dist += diff;
        }

        if (dist < 0.0){
            if(dist < -0.00000001 )
                System.err.println("d3 < 0 !!!");
            dist = 0.0;
        }

        return Math.sqrt(dist);
    }

    /**
     * The method calculates the variance increase distance between the two incremental cluster features.
     * @param cf1 CF object that presents the first incremental cluster feature.
     * @param cf2 CF object that presents the second incremental cluster feature.
     * @return double value that presents the distance between the two incremental cluster features.
     */
    private double d4(CF cf1, CF cf2) {
        double dist = 0;
        double wCF1 = cf1.getW();
        double wCF2 = cf2.getW();
        double[] linearSumCF1 = cf1.getLinearSum();
        double[] linearSumCF2 = cf2.getLinearSum();
        double[] squareSumCF1 = cf1.getSquareSum();
        double[] squareSumCF2 = cf2.getSquareSum();

        double[] totalLinearSum = linearSumCF1.clone();
        double[] totalSquareSum = squareSumCF1.clone();
        for(int i = 0; i < linearSumCF1.length; i++) {
            totalLinearSum[i] += linearSumCF2[i];
            totalSquareSum[i] += squareSumCF2[i];
        }

        for(int i = 0; i < totalLinearSum.length; i++) {
            double diff1 = totalSquareSum[i] - 2 * totalLinearSum[i] * totalLinearSum[i]/(wCF1 + wCF2) + (wCF1 + wCF2) * (totalLinearSum[i]/(wCF1 + wCF2))*(totalLinearSum[i]/(wCF1 + wCF2));
            double diff2 = squareSumCF1[i] - 2 * linearSumCF1[i] * linearSumCF1[i]/wCF1 + wCF1 * (linearSumCF1[i]/wCF1)*(linearSumCF1[i]/wCF1);
            double diff3 = squareSumCF2[i] - 2 * linearSumCF2[i] * linearSumCF2[i]/wCF2 + wCF2 * (linearSumCF2[i]/wCF2)*(linearSumCF2[i]/wCF2);
            dist += diff1 - diff2 - diff3;
        }

        if (dist < 0.0){
            if(dist < -0.00000001 )
                System.err.println("d4 < 0 !!!");
            dist = 0.0;
        }

        return Math.sqrt(dist);
    }

    /**
     * The method calculates the radius distance between the two incremental cluster features.
     * @param cf1 CF object that presents the first incremental cluster feature.
     * @param cf2 CF object that presents the second incremental cluster feature.
     * @return double value that presents the distance between the two incremental cluster features.
     */
    private double d5(CF cf1, CF cf2) {
        double dist = 0;

        double wCF1 = cf1.getW();
        double wCF2 = cf2.getW();
        double[] linearSumCF1 = cf1.getLinearSum();
        double[] linearSumCF2 = cf2.getLinearSum();
        double[] squareSumCF1 = cf1.getSquareSum();
        double[] squareSumCF2 = cf2.getSquareSum();

        double[] totalLinearSum = linearSumCF1.clone();
        double[] totalSquareSum = squareSumCF1.clone();
        for(int i = 0; i < linearSumCF1.length; i++) {
            totalLinearSum[i] += linearSumCF2[i];
            totalSquareSum[i] += squareSumCF2[i];
        }

        for(int i = 0; i < totalLinearSum.length; i++) {
            double centroid = totalLinearSum[i] / (wCF1 + wCF2);
            dist += totalSquareSum[i] / (wCF1 + wCF2) - centroid * centroid;

        }

        if (dist < 0.0){
            if(dist < -0.00000001)
                System.err.println("d5 < 0 !!!");
            dist = 0.0;
        }

        return Math.sqrt(dist);
    }

    /**
     * The method checks for changes for each dimension in the standard deviation and distance between the current incremental feature and new incremental feature.
     * @param cf CF object that present the new incremental cluster feature.
     * @return byte array that presents detected changes in each dimension when comparing the distance and standard deviation.
     */
    byte[] checkStandardDeviation(CF cf) {

        byte[] anomalyCount = new byte[this.linearSum.length];

        if (this.n > 2) {

            double[] standardDeviation = this.getStandardDeviation();
            double[] cfLinearSum = cf.getLinearSum();
            double cfW = cf.getW();
            for (int i = 0; i < this.linearSum.length; i++) {
                if (standardDeviation[i] != 0.0 && !Double.isNaN(standardDeviation[i])) {
                    double distance = Math.abs(this.linearSum[i] / this.w - cfLinearSum[i] / cfW);
                    if (distance > (this.intraClusterThreshold * standardDeviation[i])) {
                        anomalyCount[i] = 1;
                    }
                }
            }
        }

        return anomalyCount;
    }

    /**
     * The method calculates the centroid from the incremental cluster feature statistics.
     * @return double array that presents the centroid of incremental cluster feature.
     */
    double[] getCentroid() {
        double[] centroid = new double[this.linearSum.length];

        for (int i = 0; i < this.linearSum.length; i++) {
            centroid[i] = this.linearSum[i] / this.w;
        }

        return centroid;
    }

    /**
     * The method calculates the standard deviation from the incremental cluster feature statistics.
     * @return double array that presents the standard deviation of incremental cluster feature.
     */
    double[] getStandardDeviation() {
        double[] standardDeviation = new double[this.linearSum.length];

        for (int i = 0; i < this.linearSum.length; i++) {
            standardDeviation[i] = Math.sqrt((this.squareSum[i] - (2 * this.linearSum[i] * (this.linearSum[i] / this.w)) + this.w * ((this.linearSum[i] / this.w) * (this.linearSum[i] / this.w))) / this.w);
            if (Double.isNaN(standardDeviation[i])) {
                standardDeviation[i] = 0.0;
            }
        }

        return standardDeviation;
    }

    /**
     * The method for determining if two incremental cluster features are equal.
     * @param o object presents an incremental cluster feature to be compared with the current incremental cluster feature.
     * @return boolean value that present whether the two incremental cluster feature are equal or not.
     */
    public boolean equals(Object o) {
        CF cf = (CF)o;

        if (this.n != cf.getN()) {
            return false;
        }

        if (this.w != cf.getW()) {
            return false;
        }

        if (!this.timestamp.equals(cf.getTimestamp())) {
            return false;
        }

        if (this.child != null && cf.getChild() == null) {
            return false;
        }

        if (this.child == null && cf.getChild() != null) {
            return false;
        }

        if (this.child != null && !this.child.equals(cf.getChild())) {
            return false;
        }

        if(!Arrays.equals(this.linearSum, cf.getLinearSum())) {
            return false;
        }

        if(!Arrays.equals(this.squareSum, cf.getSquareSum())) {
            return false;
        }

        if (this.windows != null && cf.getWindows() == null) {
            return false;
        }

        if (this.windows == null && cf.getWindows() != null) {
            return false;
        }

        if(this.windows != null && !this.windows.equals(cf.getWindows())) {
            return false;
        }

        return true;

    }

    /**
     * The method returns if the incremental cluster feature is anomalous or not.
     * @return boolean value that presents whether the incremental cluster feature has been flagged as anomalous or not.
     */
    public boolean isAnomalyCluster() {
        return anomalyCluster;
    }

    /**
     * The method sets the incremental cluster feature to anomaly on the basis of input parameters.
     * @param anomalyCluster boolean array that presents detected changes.
     */
    public void setAnomalyCluster(boolean[] anomalyCluster) {
        int count = 0;
        for (boolean b : anomalyCluster) {
            if (b) {
                count++;
            }
        }
        if (count > 0) {
            this.anomalyCluster = true;
        } else {
            this.anomalyCluster = false;
        }
    }

    /**
     * The method returns if the incremental cluster feature is normal or not.
     * @return boolean value that presents whether the incremental cluster feature has been flagged as normal or not.
     */
    public boolean isNormalCluster() {
        return this.normalCluster;
    }

    /**
     * The method determines whether the incremental cluster feature is a normal cluster on the basis of its size.
     * @param nLeafCFs AtomicInteger object that presents atomic int value for counting the number of leaf incremental cluster features in the tree.
     * @param nLeafCFsSum AtomicInteger object that presents atomic int value for counting the linear sum of number of leaf incremental cluster features in the tree.
     * @param nLeafCFsSquared AtomicInteger object that presents atomic int value for counting the square sum of number of leaf incremental cluster features in the tree.
     * @param nLeafCFsHSum MutableDouble object that presents atomic double value for counting the linear sum of one divided with number of leaf incremental cluster features in the tree.
     * @return boolean value that presents whether the incremental cluster feature is anomalous or not.
     */
    public boolean isNormalCluster2(AtomicInteger nLeafCFs, AtomicInteger nLeafCFsSum, AtomicInteger nLeafCFsSquared, MutableDouble nLeafCFsHSum) {
        double hmeanN = (double)nLeafCFs.get() / nLeafCFsHSum.getValue();

        if (this.n < hmeanN * this.normalClusterThreshold) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * The method determines the difference in seconds between the current timestamp and new timestamp.
     * @param ts LocalDateTime object that present a new timestamp.
     * @return long value that presents the difference in seconds between the current timestamp and new timestamp.
     */
    public long getTsDifference(LocalDateTime ts) {
        return SECONDS.between(this.timestamp, ts);
    }

    /**
     * The method sets the incremental cluster feature to normal on the basis of input parameters.
     * @param normalCluster boolean value that presents normal cluster flag.
     */
    public void setNormalCluster(boolean normalCluster) {
        this.normalCluster = normalCluster;
    }

    /**
     * The method adds a new incremental cluster feature to the child incremental cluster feature.
     * @param cf CF object to be added to the child incremental cluster feature.
     */
    void addToChild(CF cf) {
        this.child.getCfList().add(cf);
    }

    /**
     * The method returns the number of data points aggregated in the incremental cluster feature.
     * @return int value that presents the number of data points aggregated in the incremental cluster feature.
     */
    public int getN() {
        return n;
    }

    /**
     * The method returns the time fading weight of data points aggregated in the incremental cluster feature.
     * @return int double that presents the time fading weight of data points aggregated in the incremental cluster feature.
     */
    public double getW() {
        return w;
    }

    /**
     * The method returns the linear sum of data points aggregated in the incremental cluster feature.
     * @return double array that presents the linear sum of data points aggregated in the incremental cluster feature.
     */
    double[] getLinearSum() {
        return linearSum;
    }

    /**
     * The method returns the square sum of data points aggregated in the incremental cluster feature.
     * @return double array that presents the square sum of data points aggregated in the incremental cluster feature.
     */
    double[] getSquareSum() {
        return squareSum;
    }

    /**
     * The method returns the timestamp of the incremental cluster feature.
     * @return LocalDateTime object that presents the timestamp of the incremental cluster feature.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * The method returns the child incremental cluster feature node.
     * @return CFNode object that presents the child incremental cluster feature node.
     */
    CFNode getChild() {
        return child;
    }

    /**
     * The method returns the list of ADWIN windows of the incremental cluster feature node.
     * @return ArrayList&lt;ADWIN&gt; object that present the list of ADWIN windows of the incremental cluster feature node.
     */
    public ArrayList<ADWIN> getWindows() {
        return windows;
    }

    /**
     * The method sets the list of ADWIN windows of the incremental cluster feature node to the input argument.
     * @param windows ArrayList&lt;ADWIN&gt; object that present the list of ADWIN windows of the incremental cluster feature node.
     */
    public void setWindows(ArrayList<ADWIN> windows) {
        this.windows = windows;
    }

    /**
     * The methods set the child incremental cluster feature node of the incremental cluster feature to the input argument.
     * @param child
     */
    void setChild(CFNode child) {
        this.child = child;
    }

    /**
     * The method checks whether the incremental cluster feature has a child incremental cluster feature node.
     * @return boolean value that presents whether the incremental cluster feature has a child incremental cluster feature node.
     */
    boolean hasChild() {
        return this.child != null;
    }
}
