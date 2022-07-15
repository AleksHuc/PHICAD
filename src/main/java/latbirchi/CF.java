package latbirchi;

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

public class CF {

    private boolean normalCluster;
    private boolean anomalyCluster;
    private int n;
    private double w;
    private double normalClusterThreshold;
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

    private double delta = 0.01;
    private int checkStep = 32;
    private int maxBins = 5;
    private int sizeOfBin = 5;
    private int[] windowsRate;
    private int hitWindowLength = 1000;
    private double hitWindowProbability = 0.05;
    private int anomalyWindowLength = 20;
    private double anomalyWindowProbability = 0.1;
    private int hitIntraWindowLength = 1000;
    private double hitIntraWindowProbability = 0.05;
    private int anomalyIntraWindowLength = 40;
    private double anomalyIntraWindowProbability = 0.2;

    public CF(double lambda, double delta, int checkStep, int maxBins, int sizeOfBin, double normalClusterThreshold, long id, String name) {
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
    }

    public CF() {

    }

    public long getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(long changeTime) {
        this.changeTime = changeTime;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public CF(double[] point, LocalDateTime timestamp, double lambda, double delta, int checkStep, int maxBins, int sizeOfBin, double normalClusterThreshold, long id, String name){
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

//        if (id == 2491) {
//            System.out.println();
//        }

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

    public CF(double[] point){
        this.n = 1;
        this.w = 1.0;

        this.linearSum = new double[point.length];
        System.arraycopy(point, 0, this.linearSum, 0, this.linearSum.length);

        this.squareSum = new double[point.length];
        for (int i = 0; i < this.squareSum.length; i++) {
            this.squareSum[i] = point[i] * point[i];
        }
    }

    byte[] update(CF cf) {

//        if (this.windows != null && cf.getN() == 1) {
//
//            if (this.pw == null) {
//                try {
//                    this.pw = new PrintWriter("results/" + name + "_" + this.id + "_CFTimeSeries.txt");
//                    double[] centroid = this.getCentroid();
//                    double[] wi = new double[this.windows.size()];
//                    double[] wid = new double[this.windowsD.size()];
//                    double[] winter = new double[this.windowsD.size()];
//                    pw.println(Arrays.toString(centroid)+ ", 0.0, " + Arrays.toString(new double[12]) + ", 0.0" + " / " + Arrays.toString(wi) + ", " + Arrays.toString(wid) + ", 0.0" + " / " + Arrays.toString(winter) + ", 0.0");
//                    pw.flush();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        double[] winter = this.checkIntraAnomalies(cf);

        long timestampDifference = this.updateValues(cf);

        byte[] differences = this.updateWindows(cf, timestampDifference, winter);

        return this.determineAnomalies(differences);
    }

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

    byte[] updateWindows(CF cf, long timestampDifference, double[] winter) {

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

//                            int newSize = this.n - this.lastSwitch[i];
//                            if (newSize >= 40) {
//                                newSize = 40;
                            if (this.hitWindows.get(i).newestSinceSize(this.n, anomalyWindowLength) > (anomalyWindowLength * anomalyWindowProbability)) {
//                                    this.flags[i] = !this.flags[i];
                                this.flags[i] = true;
                                this.lastSwitch[i] = this.n;
//                                                            wi[i] = 1.0;
                            }
//                            }
//                            this.flags[i] = !this.flags[i];

                        }
//                        else {
//                            if (this.flags[i]){
//                                this.flags[i] = false;
//                                this.flags[i] = !this.flags[i];
//                                this.lastSwitch[i] = this.n;
//                            }
//                        }
                    }

                    else {
                        if (this.flags[i]){
                            this.flags[i] = false;
//                                this.flags[i] = !this.flags[i];
//                                this.lastSwitch[i] = this.n;
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

//                        if (2 < this.hitWindows.get(i).newestSinceSize(this.n, 20)) {

//                            if (2 < this.hitWindows.get(i).newestSinceSize(this.n, 20) && this.hitWindows.get(i).newestSinceSize(this.n, 1000) < 10) {
                        double t = hitWindowLength * hitWindowProbability;
                        if (this.n < hitWindowLength) {
                            t = this.n * hitWindowProbability;
                        }

                        if (this.hitWindows.get(lengthOffset + i).newestSinceSize(this.n, hitWindowLength) < t) {

//                            int newSize = this.n - this.lastSwitch[lengthOffset + i];
//                            if (newSize >= this.anomalyWindowLength) {

                            if (this.hitWindows.get(lengthOffset + i).newestSinceSize(this.n, anomalyWindowLength) > (anomalyWindowLength * anomalyWindowProbability)) {
//                                    this.flags[lengthOffset + i] = !this.flags[lengthOffset + i];
                                this.flags[lengthOffset + i] = true;
                                this.lastSwitch[lengthOffset + i] = this.n;
//                                                            wi[i] = 1.0;
                            }
//                            }
//                            this.flags[i] = !this.flags[i];

                        }
//                        else {
//                            if (this.flags[lengthOffset + i]){
//                                this.flags[lengthOffset + i] = !this.flags[lengthOffset + i];
//                                this.lastSwitch[lengthOffset + i] = this.n;
//                            }
//                        }


                    }
                    else {
                        if (this.flags[lengthOffset + i]){
                            this.flags[lengthOffset + i] = false;
//                                this.flags[lengthOffset + i] = !this.flags[lengthOffset + i];
//                                this.lastSwitch[lengthOffset + i] = this.n;
                        }
                    }

                    if (this.flags[lengthOffset + i]) {
                        wid[i] = 1.0;
                        differences[lengthOffset + i] = 1;
                    }
                }

//                double[] centroid = this.getCentroid();
//                pw.println(Arrays.toString(centroid) + ", " + normalizedTimestampDifference + ", " + Arrays.toString(differences2) + ", " + anomaly + " / " + Arrays.toString(wi) + ", " + Arrays.toString(wid) + ", " + anomaly + " / " + Arrays.toString(winter) + ", " + anomaly);
//                pw.flush();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return differences;
    }

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
//                        int newSize = this.n - this.intraLastSwitch[i];
//                        if (newSize >= this.anomalyIntraWindowLength) {
                        if (this.intraHitWindows.get(i).newestSinceSize(this.n, anomalyIntraWindowLength) > (anomalyIntraWindowLength * anomalyIntraWindowProbability)) {
//                                this.intraFlags[i] = !this.intraFlags[i];
                            this.intraFlags[i] = true;
                            this.intraLastSwitch[i] = this.n;
//                                                            wi[i] = 1.0;
//                            }
                        }
                    }
//                } else {
//                    if (this.intraFlags[i]) {
//                        this.intraFlags[i] = !this.intraFlags[i];
//                        this.intraLastSwitch[i] = this.n;
//                    }
//                }
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

    boolean isWithinThreshold(CF cf, double distanceThreshold, int distanceFunction) {
        double distance = distance(cf, distanceFunction);
        return distance == 0 || distance <= distanceThreshold;
    }

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

    private double d3(CF cf1, CF cf2) {
        double dist = 0;
        double wCF1 = cf1.getW();
        double wCF2 = cf2.getW();
        double[] linearSumCF1 = cf1.getLinearSum();
        double[] linearSumCF2 = cf2.getLinearSum();
        double[] squareSumCF1 = cf1.getSquareSum();
        double[] squareSumCF2 = cf2.getSquareSum();

//        if (linearSumCF1 == null || linearSumCF2 == null || squareSumCF1 == null || squareSumCF2 == null) {
//            System.out.println();
//        }

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

//        double radius = 0.0;
//        for (int i = 0; i < centroid.length; i++) {
//            radius += this.squareSum[i] / this.weightSum - centroid[i] * centroid[i];
//        }
//
//        if (this.count > 1 && Math.sqrt(radius) == 0.0) {
//            System.out.println();
//        }
//
//        return Math.sqrt(radius);

        if (dist < 0.0){
            if(dist < -0.00000001 )
                System.err.println("d5 < 0 !!!");
            dist = 0.0;
        }

        return Math.sqrt(dist);
    }

    byte[] checkStandardDeviation(CF cf) {

        byte[] anomalyCount = new byte[this.linearSum.length];

//        if (this.n > 2 && this.normalCluster) {
        if (this.n > 2) {

            double[] standardDeviation = this.getStandardDeviation();
//            double[] centroid = this.getCentroid();

            double[] cfLinearSum = cf.getLinearSum();
            double cfW = cf.getW();

            double[] distanceArray = new double[this.linearSum.length];
            double[] centroidArray = this.getCentroid();
            double[] pointArray = cf.getCentroid();
            for (int i = 0; i < this.linearSum.length; i++) {
                if (standardDeviation[i] != 0.0 && !Double.isNaN(standardDeviation[i])) {
                    double distance = Math.abs(this.linearSum[i] / this.w - cfLinearSum[i] / cfW);
                    distanceArray[i] = distance;
                    if (distance > (3 * standardDeviation[i])) {
//                        System.out.println("Anomaly detected!");
                        anomalyCount[i] = 1;
                    }
                }
            }
//            System.out.println();
        }

        return anomalyCount;
    }

    double[] getCentroid() {
        double[] centroid = new double[this.linearSum.length];

        for (int i = 0; i < this.linearSum.length; i++) {
            centroid[i] = this.linearSum[i] / this.w;
        }

        return centroid;
    }

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

    public boolean isAnomalyCluster() {
        return anomalyCluster;
    }

    public void setAnomalyCluster(boolean[] anomalyCluster) {
        int count = 0;
        for (boolean b : anomalyCluster) {
            if (b) {
                count++;
            }
        }
        if (count > 0) {
//            this.normalCluster = false;
            this.anomalyCluster = true;
        } else {
//            this.normalCluster = true;
            this.anomalyCluster = false;
        }
    }

    public boolean isNormalCluster() {
        return this.normalCluster;
    }

    public boolean isNormalCluster2(AtomicInteger nLeafCFs, AtomicInteger nLeafCFsSum, AtomicInteger nLeafCFsSquared, MutableDouble nLeafCFsHSum) {
        double meanN = (double)nLeafCFsSum.get() / (double)nLeafCFs.get();
        double hmeanN = (double)nLeafCFs.get() / nLeafCFsHSum.getValue();

//        if (name.equals("192.168.2.106_bwd")) {
////            System.out.println("hmeanN " + hmeanN);
////            System.out.println("meanN " + meanN);
////            double stdN = Math.sqrt((nLeafCFsSquared.get() - (2 * nLeafCFsSum.get() * meanN) + nLeafCFs.get() * (meanN * meanN)) / nLeafCFs.get());
////            System.out.println("stdN " + stdN);
//            System.out.println("this.n  " + this.n);
//            System.out.println("hmeant " + hmeanN * this.normalClusterThreshold);
////            System.out.println("meant " + meanN * this.normalClusterThreshold);
//        }

//        double stdN = Math.sqrt((nLeafCFsSquared.get() - (2 * nLeafCFsSum.get() * meanN) + nLeafCFs.get() * (meanN * meanN)) / nLeafCFs.get());
//        double threeStD = (meanN - 2 * stdN);
//        if (threeStD > 1.0) {
//            System.out.println(threeStD);
//        }
//        if (this.n < (meanN * this.normalClusterThreshold)) {
//            return false;
//        }
        if (this.n < hmeanN * this.normalClusterThreshold) {
            return false;
        }
        else {
            return true;
        }
    }

    public long getTsDifference(LocalDateTime ts) {

//        if (SECONDS.between(this.timestamp, ts) < 0) {
//            System.out.println();
//        }

        return SECONDS.between(this.timestamp, ts);
    }

    public void setNormalCluster(boolean normalCluster) {
        this.normalCluster = normalCluster;
    }

    void addToChild(CF cf) {
        this.child.getCfList().add(cf);
    }

    public int getN() {
        return n;
    }

    public double getW() {
        return w;
    }

    public double[] getLinearSum() {
        return linearSum;
    }

    public double[] getSquareSum() {
        return squareSum;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public CFNode getChild() {
        return child;
    }

    public ArrayList<ADWIN> getWindows() {
        return windows;
    }

    public void setWindows(ArrayList<ADWIN> windows) {
        this.windows = windows;
    }

    void setChild(CFNode child) {
        this.child = child;
    }

    boolean hasChild() {
        return this.child != null;
    }

    public CF makeACopy(){

        CF temp = new CF();

        temp.n = this.n;
        temp.id = this.id;
        temp.w = this.w;

        double[] cfLS = this.getLinearSum();
        double[] lSum = new double[cfLS.length];
        System.arraycopy(cfLS, 0, lSum, 0, lSum.length);
        temp.linearSum = lSum;

        double[] cfSS = this.getSquareSum();
        double[] sSum = new double[cfLS.length];
        System.arraycopy(cfSS, 0, sSum, 0, sSum.length);
        temp.squareSum = sSum;

        temp.timestamp = this.timestamp;
        temp.creationTimestamp = this.creationTimestamp;

        temp.child = this.child;
        temp.changeTime = this.changeTime;

        temp.name = this.name;

        return temp;
    }

    public void setLinearSum(double[] linearSum) {
        this.linearSum = linearSum;
    }

    public void setSquareSum(double[] squareSum) {
        this.squareSum = squareSum;
    }
}
