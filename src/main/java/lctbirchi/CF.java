package lctbirchi;

import adwin.ADWIN;
import adwin.ChangeDifference;
import utils.HitWindow;
import utils.MutableDouble;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.temporal.ChronoUnit.SECONDS;

public class CF {

    private boolean normalCluster;
    private boolean anomalyCluster;
    private int n;
    private double normalClusterThreshold;
    private long changeTime;
    private CFNode child;
    private LocalDateTime timestamp;
    private LocalDateTime creationTimestamp;
    private ArrayList<ADWIN> windows;
    private ArrayList<ADWIN> windowsD;
    private ArrayList<HitWindow> hitWindows;
    private ArrayList<HitWindow> intraHitWindows;
    public PrintWriter pw;

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
    private boolean level1Anomaly = false;
    private LocalDateTime level1AnomalyTimestamp = null;

    private double[] previousCentroid;
    private lbtbirchi.CFTree tree;
    private CFNode parent;
    private HashSet<String> storedIDs;
    private int distanceFunction;
    private int maxNumberOfNodesLB;
    private int maxNumberOfNodesLC;
    private double distanceThresholdLB;
    private double distanceThresholdLC;
    private int maxNodeEntries;

    public CF(int maxNodeEntries, int distanceFunction, double distanceThresholdLB, double distanceThresholdLC, int maxNumberOfNodesLB, int maxNumberOfNodesLC, double delta, int checkStep, int maxBins, int sizeOfBin, double normalClusterThreshold) {
        this.n = 0;
        this.child = null;
        this.timestamp = null;
        this.windows = null;
        this.delta = delta;
        this.checkStep = checkStep;
        this.maxBins = maxBins;
        this.sizeOfBin = sizeOfBin;
        this.normalCluster = true;
        this.anomalyCluster = false;
        this.normalClusterThreshold = normalClusterThreshold;
        this.maxNodeEntries = maxNodeEntries;
        this.distanceFunction = distanceFunction;
        this.maxNumberOfNodesLB = maxNumberOfNodesLB;
        this.maxNumberOfNodesLC = maxNumberOfNodesLC;
        this.distanceThresholdLB = distanceThresholdLB;
        this.distanceThresholdLC = distanceThresholdLC;
        this.parent = null;
        this.tree = new lbtbirchi.CFTree(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.maxNumberOfNodesLB);
        this.storedIDs = null;
        this.previousCentroid = null;
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

    public CF(String id, ArrayList<latbirchi.CF> pointTree, LocalDateTime timestamp, int maxNodeEntries, int distanceFunction, double distanceThresholdLB, double distanceThresholdLC, int maxNumberOfNodesLB, int maxNumberOfNodesLC, double delta, int checkStep, int maxBins, int sizeOfBin, double normalClusterThreshold){
        this.n = 1;
        this.timestamp = timestamp;
        this.creationTimestamp = timestamp;
        this.changeTime = 0;
        this.child = null;
        this.windows = new ArrayList<>();
        this.windowsD = new ArrayList<>();
        this.delta = delta;
        this.checkStep = checkStep;
        this.maxBins = maxBins;
        this.sizeOfBin = sizeOfBin;
        this.normalCluster = true;
        this.anomalyCluster = false;
        this.normalClusterThreshold = normalClusterThreshold;

        this.maxNodeEntries = maxNodeEntries;
        this.distanceFunction = distanceFunction;
        this.maxNumberOfNodesLB = maxNumberOfNodesLB;
        this.maxNumberOfNodesLC = maxNumberOfNodesLC;
        this.distanceThresholdLB = distanceThresholdLB;
        this.distanceThresholdLC = distanceThresholdLC;

        this.tree = new lbtbirchi.CFTree(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.maxNumberOfNodesLB);
        this.storedIDs = new HashSet<>();
        this.storedIDs.add(id);

        for (latbirchi.CF currentCF : pointTree) {
            this.tree.insertCF(copyLeafCF(currentCF, id));
        }

        this.previousCentroid = this.tree.getCentroid();

        this.intraFlags = new boolean[this.previousCentroid.length];;
        this.flags = new boolean[2 * this.previousCentroid.length + 1];
        this.lastSwitch = new int[2 * this.previousCentroid.length + 1];
        this.intraLastSwitch = new int[this.previousCentroid.length];
        this.windowsRate = new int[2 * this.previousCentroid.length + 1];
        this.hitWindows = new ArrayList<>();
        this.intraHitWindows = new ArrayList<>();


        for (int i = 0; i < this.previousCentroid.length + 1; i++) {
            ADWIN currentADWIN = new ADWIN(this.delta, this.checkStep, this.maxBins, this.sizeOfBin);
            if (i == this.previousCentroid.length) {
                currentADWIN.addNewValue(0);
            }
            else {
                currentADWIN.addNewValue(this.previousCentroid[i]);
            }
            this.windows.add(currentADWIN);
        }

        for (int i = 0; i < this.previousCentroid.length; i++) {
            ADWIN currentADWIN = new ADWIN(this.delta, this.checkStep, this.maxBins, this.sizeOfBin);
            currentADWIN.addNewValue(this.previousCentroid[i]);
            this.windowsD.add(currentADWIN);
        }

        for (int i = 0; i < 2 * this.previousCentroid.length + 1; i++) {
            HitWindow hw = new HitWindow(hitWindowLength);
            this.hitWindows.add(hw);
        }

        for (int i = 0; i < this.previousCentroid.length; i++) {
            HitWindow hw = new HitWindow(hitIntraWindowLength);
            this.intraHitWindows.add(hw);
        }
    }

    public lbtbirchi.CF copyLeafCF(latbirchi.CF cf, String id) {
        if (cf.getChild() == null) {
            lbtbirchi.CF newCF = new lbtbirchi.CF();
            newCF.setN(cf.getW());
            newCF.setChild(null);

            double[] cfLS = cf.getLinearSum();
            double[] lSum = new double[cfLS.length];
            System.arraycopy(cfLS, 0, lSum, 0, lSum.length);

            newCF.setLinearSum(lSum);

            double[] cfSS = cf.getSquareSum();
            double[] sSum = new double[cfSS.length];
            System.arraycopy(cfSS, 0, sSum, 0, sSum.length);

            newCF.setSquareSum(sSum);

            LinkedHashMap<String, ArrayList<latbirchi.CF>> storedCFs = new LinkedHashMap<>();
            ArrayList<latbirchi.CF> list = new ArrayList<>();
            list.add(cf);
            storedCFs.put(id, list);
            newCF.setStoredCFs(storedCFs);

            return newCF;
        }
        else {
            System.err.println("ERROR: CF is not leaf!");
            return null;
        }

    }

    byte[] update(CF cf, boolean anomaly) {

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

        byte[] differences = this.updateWindows(cf, timestampDifference);

        return this.determineAnomalies(differences, cf, anomaly);
    }

    byte[] determineAnomalies(byte[] differences, CF cf, boolean anomaly) {
        byte[] anomalies = new byte[3];
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

        if (anomaly) {
            this.level1AnomalyTimestamp = cf.getTimestamp();
            this.level1Anomaly = true;
        }
        else {
            LocalDateTime cfTimestamp = cf.getTimestamp();
            if (this.level1Anomaly && this.level1AnomalyTimestamp != null && cfTimestamp != null && this.timestamp != null) {
                long timestampDifference = SECONDS.between(this.timestamp, cfTimestamp);
                if (timestampDifference > 5){
                    this.level1Anomaly = false;
                }
            }
        }

        if (this.level1Anomaly) {
            anomalies[2] = 1;
        }

        return anomalies;
    }

    byte[] updateWindows(CF cf, long timestampDifference) {

        byte[] differences = null;

        if (this.windows != null && cf.getN() == 1) {

            double normalizedTimestampDifference = Math.abs(timestampDifference) / 6000.0;
            if (normalizedTimestampDifference > 1.0) {
                normalizedTimestampDifference = 1.0;
            }

            double[] wi = new double[this.windows.size()];
            double[] wid = new double[this.windowsD.size()];

            double[] centroid = this.tree.getCentroid();

            try {
                differences = new byte[2*centroid.length + 1];
                for (int i = 0; i < centroid.length + 1; i++) {

                    double currentValue;
                    if (i == centroid.length) {
                        currentValue = Math.abs(normalizedTimestampDifference);
                    } else {
                        currentValue = centroid[i];
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

                int lengthOffset = centroid.length + 1;
                double[] differences2 = new double[centroid.length];
                for (int i = 0; i < centroid.length; i++) {
                    ChangeDifference cd = this.windowsD.get(i).addNewValue(Math.abs(this.windows.get(i).getLatestValue() - centroid[i]));
                    differences2[i] = Math.abs(this.windows.get(i).getLatestValue() - centroid[i]);

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
        }

        // Update the number of flows held by the cf.
        this.n += cf.getN();

        lbtbirchi.CFNode currentNode = cf.getTree().getLeafList();
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (lbtbirchi.CF innerCF : currentNode.getCfList()) {
                    this.tree.insertCF(innerCF.makeACopy());
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

        if (this.storedIDs == null) {
            this.storedIDs = new HashSet<>();
        }

        this.storedIDs.addAll(cf.getStoredIDs());

        return timestampDifference;
    }

    public Collection<String> getStoredIDs() {
        return this.storedIDs;
    }

    public lbtbirchi.CFTree getTree() {
        return this.tree;
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
//            case CFTree.D1_DIST:
//                dist = d1(this, cf);
//                break;
//            case CFTree.D2_DIST:
//                dist = d2(this, cf);
//                break;
//            case CFTree.D3_DIST:
//                dist = d3(this, cf);
//                break;
//            case CFTree.D4_DIST:
//                dist = d4(this, cf);
//                break;
//            case CFTree.D5_DIST:
//                dist = d5(this, cf);
//                break;
        }

        return dist;
    }

    private double d0(CF cf1, CF cf2) {
        return cf1.getTree().treeDistance(cf2.getTree());
    }

    byte[] checkStandardDeviation(CF cf) {

        double[] centroid = this.getCentroid();
        double[] centroidCF = cf.getCentroid();

        byte[] anomalyCount = new byte[centroid.length];

//        if (this.n > 2 && this.normalCluster) {
        if (this.n > 2) {

            double[] standardDeviation = this.getStandardDeviation();

//            double[] distanceArray = new double[centroid.length];
            for (int i = 0; i < centroid.length; i++) {
                if (standardDeviation[i] != 0.0 && !Double.isNaN(standardDeviation[i])) {
                    double distance = Math.abs(centroid[i] - centroidCF[i]);
//                    distanceArray[i] = distance;
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

    public CFNode getParent() {
        return parent;
    }

    double[] getCentroid() {
        return this.tree.getCentroid();
    }

    double[] getStandardDeviation() {
        double[] ls = this.tree.getLinearSum();
        double[] ss = this.tree.getSquareSum();
        double n = this.tree.getN();
        double[] standardDeviation = new double[ls.length];

        for (int i = 0; i < ls.length; i++) {
            standardDeviation[i] = Math.sqrt((ss[i] - (2 * ls[i] * (ls[i] / n)) + n * ((ls[i] / n) * (ls[i] / n))) / n);
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

        if (this.child != null && cf.getChild() == null) {
            return false;
        }

        if (this.child == null && cf.getChild() != null) {
            return false;
        }

        if (this.timestamp != null && cf.getTimestamp() == null) {
            return false;
        }

        if (this.timestamp == null && cf.getTimestamp() != null) {
            return false;
        }

        if (this.timestamp != null && !this.timestamp.equals(cf.getTimestamp())) {
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

        if (this.delta != cf.getDelta()) {
            return false;
        }

        if (this.checkStep != cf.getCheckStep()) {
            return false;
        }

        if (this.maxBins != cf.getMaxBins()) {
            return false;
        }

        if (this.sizeOfBin != cf.getSizeOfBin()) {
            return false;
        }

        if (this.normalCluster != cf.isNormalCluster()) {
            return false;
        }

        if (this.anomalyCluster != cf.isAnomalyCluster()) {
            return false;
        }

        if (this.normalClusterThreshold != cf.getNormalClusterThreshold()) {
            return false;
        }

        if (this.maxNodeEntries != cf.getMaxNodeEntries()) {
            return false;
        }

        if (this.distanceFunction != cf.getDistanceFunction()) {
            return false;
        }

        if (this.maxNumberOfNodesLB != cf.getMaxNumberOfNodesLB()) {
            return false;
        }

        if (this.maxNumberOfNodesLC != cf.getMaxNumberOfNodesLC()) {
            return false;
        }

        if (this.distanceThresholdLB != cf.getDistanceThresholdLB()) {
            return false;
        }

        if (this.distanceThresholdLC != cf.getDistanceThresholdLC()) {
            return false;
        }

        if (this.parent != null && cf.getParent() == null) {
            return false;
        }

        if (this.parent == null && cf.getParent() != null) {
            return false;
        }

        if (this.storedIDs.size() != cf.getStoredIDs().size()) {
            return false;
        }

        if (this.storedIDs != null && cf.getStoredIDs() != null && !this.storedIDs.equals(cf.getStoredIDs())) {
            return false;
        }

        if (this.previousCentroid != null && cf.getPreviousCentroid() != null && !this.previousCentroid.equals(cf.getPreviousCentroid())) {
            return false;
        }

        if (this.tree != null && cf.getTree() != null && !this.tree.equals(cf.getTree())) {
            return false;
        }

//        if (this.child != null && !this.child.equals(cf.getChild())) {
//            return false;
//        }



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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    CFNode getChild() {
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

    public void setParent(CFNode cfNode) {
        this.parent = cfNode;
    }

    public void setN(int n) {
        this.n = n;
    }

    public void setAnomalyCluster(boolean anomalyCluster) {
        this.anomalyCluster = anomalyCluster;
    }

    public boolean isLevel1Anomaly() {
        return level1Anomaly;
    }

    public void setLevel1Anomaly(boolean level1Anomaly) {
        this.level1Anomaly = level1Anomaly;
    }

    public LocalDateTime getLevel1AnomalyTimestamp() {
        return level1AnomalyTimestamp;
    }

    public void setLevel1AnomalyTimestamp(LocalDateTime level1AnomalyTimestamp) {
        this.level1AnomalyTimestamp = level1AnomalyTimestamp;
    }

    public double getNormalClusterThreshold() {
        return normalClusterThreshold;
    }

    public ArrayList<ADWIN> getWindowsD() {
        return windowsD;
    }

    public ArrayList<HitWindow> getHitWindows() {
        return hitWindows;
    }

    public ArrayList<HitWindow> getIntraHitWindows() {
        return intraHitWindows;
    }

    public PrintWriter getPw() {
        return pw;
    }

    public boolean[] getFlags() {
        return flags;
    }

    public boolean[] getIntraFlags() {
        return intraFlags;
    }

    public int[] getLastSwitch() {
        return lastSwitch;
    }

    public int[] getIntraLastSwitch() {
        return intraLastSwitch;
    }

    public double getDelta() {
        return delta;
    }

    public int getCheckStep() {
        return checkStep;
    }

    public int getMaxBins() {
        return maxBins;
    }

    public int getSizeOfBin() {
        return sizeOfBin;
    }

    public int[] getWindowsRate() {
        return windowsRate;
    }

    public int getHitWindowLength() {
        return hitWindowLength;
    }

    public double getHitWindowProbability() {
        return hitWindowProbability;
    }

    public int getAnomalyWindowLength() {
        return anomalyWindowLength;
    }

    public double getAnomalyWindowProbability() {
        return anomalyWindowProbability;
    }

    public int getHitIntraWindowLength() {
        return hitIntraWindowLength;
    }

    public double getHitIntraWindowProbability() {
        return hitIntraWindowProbability;
    }

    public int getAnomalyIntraWindowLength() {
        return anomalyIntraWindowLength;
    }

    public double getAnomalyIntraWindowProbability() {
        return anomalyIntraWindowProbability;
    }

    public double[] getPreviousCentroid() {
        return previousCentroid;
    }

    public int getDistanceFunction() {
        return distanceFunction;
    }

    public int getMaxNumberOfNodesLB() {
        return maxNumberOfNodesLB;
    }

    public int getMaxNumberOfNodesLC() {
        return maxNumberOfNodesLC;
    }

    public double getDistanceThresholdLB() {
        return distanceThresholdLB;
    }

    public double getDistanceThresholdLC() {
        return distanceThresholdLC;
    }

    public int getMaxNodeEntries() {
        return maxNodeEntries;
    }
}
