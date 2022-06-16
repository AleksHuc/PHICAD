package latbirchi;

import utils.HitWindow;
import utils.MutableDouble;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static utils.Utils.concatTwo;

public class CFTree {

    static final int D0_DIST = 0;
    static final int D1_DIST = 1;
    static final int D2_DIST = 2;
    static final int D3_DIST = 3;
    static final int D4_DIST = 4;
    static final int D5_DIST = 5;

    private int maxNodeEntries;
    private int distanceFunction;
    private int maxNumberOfNodes;
    private int counter;
    private double distanceThreshold;
    private CFNode root;
    private CFNode leafList;
    private AtomicInteger nCFNodes;
    private AtomicInteger nLeafCFs;
    private AtomicInteger nLeafCFsSum;
    private MutableDouble nLeafCFsHSum;
    private AtomicInteger nLeafCFsSquared;
    private AtomicLong idCounter;
    private double lambda;
    private double delta;
    private int checkStep;
    private int maxBins;
    private int sizeOfBin;
    private double[] ndtdThresholds;
    private double normalClusterThreshold;
    private String name;

    private boolean[] interFlags;
    private int[] interLastSwitch;
    private ArrayList<HitWindow> interHitWindows;
    private int hitInterWindowLength = 1000;
    private double hitInterWindowProbability = 0.005;
    private int anomalyWindowLength = 40;
    private double anomalyWindowProbability = 0.0;

    private LocalDateTime timestamp;

    public CFTree(int maxNodeEntries, int distanceFunction, double distanceThreshold, int maxNumberOfNodes, double lambda, double delta, int checkStep, int maxBins, int sizeOfBin, double normalClusterThreshold, String name) {
        this.maxNodeEntries = maxNodeEntries;
        this.distanceFunction = distanceFunction;
        this.counter = 0;
        this.distanceThreshold = distanceThreshold;
        this.normalClusterThreshold = normalClusterThreshold;
        this.idCounter = new AtomicLong();
        this.root = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, true, this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name);
        this.leafList = new CFNode(0, 0, 0, true, this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name);
        this.leafList.setNextCFLeaf(this.root);
        this.nCFNodes = new AtomicInteger();
        this.nCFNodes.getAndIncrement();
        this.maxNumberOfNodes = maxNumberOfNodes;
        this.lambda = lambda;
        this.delta = delta;
        this.checkStep = checkStep;
        this.maxBins = maxBins;
        this.sizeOfBin = sizeOfBin;
        this.ndtdThresholds = null;
        this.nLeafCFs = new AtomicInteger();
        this.nLeafCFsSquared = new AtomicInteger();
        this.nLeafCFsSum = new AtomicInteger();
        this.nLeafCFsHSum = new MutableDouble(0.0);
        this.timestamp = null;
        this.name = name;


    }

    public SplitChangeDifference insertPoint(double[] point, LocalDateTime timestamp, String anomaly) {

//        System.out.println(Arrays.toString(point));

        this.counter++;
        this.timestamp = timestamp;
        return insertCF(new CF(point, timestamp, this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), name), true, anomaly);
    }

    private SplitChangeDifference insertCF(CF cf, boolean insertingNew, String anomaly){

//        System.out.println(this.name + " " + this.counter);
//        this.print("results/test_p_tree.txt");
        SplitChangeDifference splitChangeDifference = this.root.insertCF(cf, this.nCFNodes, this.nLeafCFs, this.nLeafCFsSum, this.nLeafCFsSquared, insertingNew, this.nLeafCFsHSum, anomaly, this.interHitWindows, this.interFlags, this.interLastSwitch, this.hitInterWindowLength, this.hitInterWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.counter);

        if (splitChangeDifference.isSplit()) {
            splitRoot();
        }

//        if (this.counter > 32) {
////            this.print("results/test_tree_" + this.counter + ".txt");
////            double meanN = (double)nLeafCFsSum.get() / (double)nLeafCFs.get();
////            double stdN = Math.sqrt((nLeafCFsSquared.get() - (2 * nLeafCFsSum.get() * meanN) + nLeafCFs.get() * (meanN * meanN)) / nLeafCFs.get());
////            double threeStD = (meanN - 2 * stdN);
////            if (threeStD > 1.0) {
////                System.out.println(threeStD);
////            }
////            this.ndtdThresholds = checkLeaves(cf.getTimestamp());
//        }

        if (this.nCFNodes.get() > this.maxNumberOfNodes) {
//            System.out.println("Rebuild START");
//            this.ndtdThresholds = checkLeaves(cf.getTimestamp());
            rebuildTree();
//            System.out.println("Rebuild END");
        }

        splitChangeDifference.setSplit(true);
        return splitChangeDifference;
    }

    private double[] checkLeaves(LocalDateTime timestamp) {

        int counter = 0;
        int counter2 = 0;
        int n = 0;
        long nSquared = 0;
        long timestampDifference = 0;
        long timestampDifferenceSquared = 0;
        double distance = 0;
        double distanceSquared = 0;

        CFNode currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {

                ArrayList<CF> currentCFList = currentNode.getCfList();
//                for (CF cf : currentNode.getCfList()) {
                for (int i = 0; i < currentCFList.size(); i++) {
                    counter++;

                    CF cf = currentCFList.get(i);

                    long currentN = cf.getN();
                    n += currentN;
//                    long nSquaredB = nSquared;
                    nSquared += currentN * currentN;
//                    if (nSquared < 0.0 || currentN < 0.0) {
//                        System.out.println();
//                    }
//                    nSquaredB += currentN * currentN;
//                    if (nSquaredB < 0.0 || currentN < 0.0) {
//                        System.out.println();
//                    }
//                    long currentDifference = SECONDS.between(cf.getTimestamp(), timestamp);
//                    timestampDifference += currentDifference;
//                    timestampDifferenceSquared += currentDifference * currentDifference;
//
//                    int startIndex = i + 1;
//                    CFNode currentNode2 = currentNode;
//                    if (i == currentCFList.size() - 1) {
//                        currentNode2 = currentNode.getNextCFLeaf();
//                        startIndex = 0;
//                    }
//
//                    while (currentNode2 != null) {
//                        if (!currentNode2.isDummy()) {
//                            ArrayList<CF> currentCFList2 = currentNode2.getCfList();
//                            for (int j = startIndex; j < currentCFList2.size(); j++) {
////                                CF cf2 = currentCFList2.get(j);
//
////                                double currentDistance = cf.distance(cf2, D0_DIST);
//                                double currentDistance = cf.distance(currentCFList2.get(j), D0_DIST);
//                                distance += currentDistance;
//                                distanceSquared += currentDistance * currentDistance;
////                                    System.out.println(currentDistance);
//                                counter2++;
//                            }
//                            startIndex = 0;
//                        }
//                        currentNode2 = currentNode2.getNextCFLeaf();
//                    }
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

        double meanN = (double)n / (double)counter;
        double stdN = Math.sqrt((nSquared - (2 * n * meanN) + counter * (meanN * meanN)) / counter);

        CF dummyCF = this.root.createCFForNode();
//        this.print("results/test_tree.txt");

        double newMeanN = (double)nLeafCFsSum.get() / (double)nLeafCFs.get();
        double newStdN = Math.sqrt((nLeafCFsSquared.get() - (2 * nLeafCFsSum.get() * newMeanN) + nLeafCFs.get() * (newMeanN * newMeanN)) / nLeafCFs.get());

        System.nanoTime();

//        double meanDifference = (double)timestampDifference / (double)counter;
//        double stdDifference = Math.sqrt((timestampDifferenceSquared - (2 * timestampDifference * meanDifference) + counter * (meanDifference * meanDifference)) / counter);
//
//        double meanDistance = distance / (double)counter2;
//        double stdDistance = Math.sqrt((distanceSquared - (2 * distance * meanDistance) + counter2 * (meanDistance * meanDistance)) / counter2);

//        System.out.println("meanN: " + meanN + " stdN: " + stdN);
//        System.out.println("nT: " + (meanN - 3 * stdN) + " nT(0.05): " + meanN * 0.05);

//        if ((meanN - 3 * stdN) > 0.0) {
//            System.out.println("meanN: " + meanN + " stdN: " + stdN);
//            System.out.println("nT: " + (meanN - 3 * stdN) + " nT(0.05): " + meanN * 0.05);
//        }

//        if (Double.isNaN(stdN)) {
//            System.out.println();
//        }

//        System.out.println("meanDifference: " + meanDifference + " stdDifference: " + stdDifference);
//        System.out.println("meanDistance: " + meanDistance + " stdDistance: " + stdDistance);
//        System.out.println("nT: " + (meanN - 3 * stdN) + " dT: " + (meanDifference + 3 * stdDifference));


        double threeStD = (meanN - 2 * stdN);

        double threshold = meanN * 0.05;

        if (threeStD > 0.0 && threeStD < threshold) {
            threshold = threeStD;
//            System.out.println("threeStD: " + threeStD + " " + meanN * 0.05);
        }

//        if (stdN > 0.0 && (meanN - 3 * stdN) > 0.0) {

        currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (CF cf : currentNode.getCfList()) {
//                    if (cf.getN() < (meanN - 3 * stdN) || SECONDS.between(cf.getTimestamp(), timestamp) > (meanDifference + 3 * stdDifference)) {
                    if (cf.getN() < threshold) {
                        cf.setNormalCluster(false);
                    } else {
//                            System.out.println("nT: " + (meanN - 3 * stdN));

                        cf.setNormalCluster(true);
//                        cf.setAnomalyCluster(false);
                    }
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }
//        }

//        return new double[]{meanN, stdN, meanDifference, meanDifference, meanDistance, stdDistance};
        return new double[]{meanN, stdN, 0.0, 0.0, 0.0, 0.0};
    }


    private void splitRoot() {

        CF[] farthestCFPair = this.root.findFarthestCFPair(this.root.getCfList());

        CF newCF1 = new CF(this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), name);
        CFNode newCFNode1 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, this.root.isLeafStatus(), this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name);
        newCF1.setChild(newCFNode1);

        CF newCF2 = new CF(this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), name);
        CFNode newCFNode2 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, this.root.isLeafStatus(), this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name);
        newCF2.setChild(newCFNode2);

        CFNode newRootCFNode = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, false, this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name);
        newRootCFNode.getCfList().add(newCF1);
        newRootCFNode.getCfList().add(newCF2);

        if (this.root.isLeafStatus()) {
            this.leafList.setNextCFLeaf(newCFNode1);
            newCFNode1.setPreviousCFLeaf(this.leafList);
            newCFNode1.setNextCFLeaf(newCFNode2);
            newCFNode2.setPreviousCFLeaf(newCFNode1);
        }

        this.root.redistributeCFs(this.root.getCfList(), farthestCFPair, newCF1, newCF2);
        this.root = newRootCFNode;
        this.nCFNodes.getAndAdd(2);
    }

    private void rebuildTree() {

        double newDistanceThreshold = computeNewThreshold();

        CFTree newCFTree = new CFTree(this.maxNodeEntries, this.distanceFunction, newDistanceThreshold, this.maxNumberOfNodes, this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, name);
        newCFTree.setIdCounter(this.getIdCounter());

        CFNode currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (CF cf : currentNode.getCfList()) {
                    newCFTree.insertCF(cf, false, "0.0");
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

//        System.out.println("Level A new threshold: " + newCFTree.getDistanceThreshold());

        this.distanceThreshold = newDistanceThreshold;
        this.counter = newCFTree.getCounter();
        this.root = newCFTree.getRoot();
        this.leafList = newCFTree.getLeafList();
        this.nCFNodes = newCFTree.getnCFNodes();
    }

    private double computeNewThreshold() {
        double averageDistance = 0;
        int n = 0;

        CFNode currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                CF[] closestPair = currentNode.findClosestCFPair(currentNode.getCfList());
                if (closestPair != null) {
                    averageDistance += closestPair[0].distance(closestPair[1], this.distanceFunction);
                    n++;
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

        double newThreshold = 0.0;

        if (n > 0) {
            newThreshold = averageDistance / n;
        }

        if (newThreshold <= this.distanceThreshold) {
            newThreshold = 1.1 * this.distanceThreshold;
        }

//        System.out.println(newThreshold);

        return newThreshold;
    }

    public ArrayList<double[]> getCentroidsAndRadius() {
        ArrayList<double[]> centroids = new ArrayList<>();

        CFNode currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (CF c : currentNode.getCfList()) {
                    double[] ls = c.getCentroid();
                    double r = 0;

                    double wCF1 = c.getW();

                    double[] linearSumCF1 = c.getLinearSum();
                    double[] squareSumCF1 = c.getSquareSum();

                    for(int i = 0; i < linearSumCF1.length; i++) {
                        double centroid = linearSumCF1[i] / wCF1;
                        r += squareSumCF1[i] / wCF1 - centroid * centroid;
                    }

                    if (r < 0.0){
                        if(r < -0.00000001 )
                            System.err.println("d5 < 0 !!!");
                        r = 0.0;
                    }

                    double[] ra = new double[]{Math.sqrt(r)};
                    centroids.add(concatTwo(ls, ra));

                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

        return centroids;
    }

    private int getCounter() {
        return counter;
    }

    private CFNode getRoot() {
        return root;
    }

    public CFNode getLeafList() {
        return leafList;
    }

    AtomicInteger getnCFNodes() {
        return nCFNodes;
    }

    double getDistanceThreshold() {
        return distanceThreshold;
    }

    public AtomicLong getIdCounter() {
        return idCounter;
    }

    public void setIdCounter(AtomicLong idCounter) {
        this.idCounter = idCounter;
    }

    public void print(String filename) {



        try {
            PrintWriter writer = new PrintWriter(filename + ".txt");

            double currentW = 0;
            int currentN = 0;
            double[] sum = null;
            for (CF cF : this.root.getCfList()){
                currentW += cF.getW();
                currentN += cF.getN();

                if (sum == null) {
                    sum = cF.getLinearSum().clone();
                }
                else{
                    for (int i = 0; i < cF.getLinearSum().length; i++) {
                        sum[i] += cF.getLinearSum()[i];
                    }
                }
            }

            StringBuilder centroidString = new StringBuilder("[");
            if (sum != null) {
                for (int i = 0; i < sum.length; i++) {
                    centroidString.append(String.format(Locale.US, "%.2f", sum[i] / currentW));
                    if (i != sum.length - 1) {
                        centroidString.append(", ");
                    }
                }
            }
            centroidString.append("]");



            writer.println("└── " + "N=" + currentN + " C=" + centroidString.toString() + "MCS=" + (double)this.nLeafCFsSum.get()/this.nLeafCFs.get());
            this.root.print("    ", false, writer, this.nLeafCFs, this.nLeafCFsSum, this.nLeafCFsSquared, this.timestamp, this.nLeafCFsHSum);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double[] getCentroid() {
        ArrayList<CF> rootCFs = this.root.getCfList();
        double[] centroid = new double[rootCFs.get(0).getLinearSum().length];
        double w = 0.0;
        for (CF cf : rootCFs) {
            w += cf.getW();
            double[] ls = cf.getLinearSum();
            for (int i = 0; i < ls.length; i++) {
                centroid[i] += ls[i];
            }
        }

        for (int i = 0; i < centroid.length; i++) {
            centroid[i] /= w;
        }
        return centroid;
    }

    public ArrayList<CF> getCopiedLeafArray() {

        ArrayList<CF> leafCFs = new ArrayList<>();

        CFNode currentNode = this.getLeafList();
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (CF cf : currentNode.getCfList()) {
                    leafCFs.add(cf.makeACopy());
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

        return leafCFs;
    }
}
