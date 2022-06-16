package lctbirchi;

import utils.HitWindow;
import utils.MutableDouble;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CFTree {

    static final int D0_DIST = 0;
    static final int D1_DIST = 1;
    static final int D2_DIST = 2;
    static final int D3_DIST = 3;
    static final int D4_DIST = 4;
    static final int D5_DIST = 5;

    private int maxNodeEntries;
    private int distanceFunction;
    private int maxNumberOfNodesLB;
    private int maxNumberOfNodesLC;
    private int counter;
    private double distanceThresholdLB;
    private double distanceThresholdLC;
    private CFNode root;
    private CFNode leafList;
    private AtomicInteger nCFNodes;
    private AtomicInteger nLeafCFs;
    private AtomicInteger nLeafCFsSum;
    private MutableDouble nLeafCFsHSum;
    private AtomicInteger nLeafCFsSquared;
    private AtomicLong idCounter;
    private double delta;
    private int checkStep;
    private int maxBins;
    private int sizeOfBin;
    private double normalClusterThreshold;
    private String name;

    private boolean[] interFlags;
    private int[] interLastSwitch;
    private ArrayList<HitWindow> interHitWindows;
    private int hitInterWindowLength = 1000;
    private double hitInterWindowProbability = 0.005;
    private int anomalyWindowLength = 40;
    private double anomalyWindowProbability = 0.0;

    private HashMap<String,CF> storedProfiles;

    private LocalDateTime timestamp;

    public CFTree(int maxNodeEntries, int distanceFunction, int maxNumberOfNodesLB, int maxNumberOfNodesLC, double distanceThresholdLB, double distanceThresholdLC, double delta, int checkStep, int maxBins, int sizeOfBin, double normalClusterThreshold, String name) {
        this.maxNodeEntries = maxNodeEntries;
        this.distanceFunction = distanceFunction;
        this.counter = 0;
        this.distanceThresholdLB = distanceThresholdLB;
        this.distanceThresholdLC = distanceThresholdLC;
        this.maxNumberOfNodesLB = maxNumberOfNodesLB;
        this.maxNumberOfNodesLC = maxNumberOfNodesLC;
        this.normalClusterThreshold = normalClusterThreshold;
        this.idCounter = new AtomicLong();
        this.nCFNodes = new AtomicInteger();
        this.nCFNodes.getAndIncrement();
        this.delta = delta;
        this.checkStep = checkStep;
        this.maxBins = maxBins;
        this.sizeOfBin = sizeOfBin;
        this.nLeafCFs = new AtomicInteger();
        this.nLeafCFsSquared = new AtomicInteger();
        this.nLeafCFsSum = new AtomicInteger();
        this.nLeafCFsHSum = new MutableDouble(0.0);
        this.timestamp = null;
        this.name = name;
        this.storedProfiles = new HashMap<>();
        this.root = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, true, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        this.leafList = new CFNode(0, 0, 0, 0, true, 0, 0, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        this.leafList.setNextCFLeaf(this.root);

    }

    public SplitChangeDifference insertPoint(String id, ArrayList<latbirchi.CF> pointTree, LocalDateTime timestamp, boolean anomaly) {

//        System.out.println(Arrays.toString(point));

        this.counter++;
//        System.out.println("2nd level tree insertion: " + counter);
        this.timestamp = timestamp;

//        if (counter == 213) {
//            System.out.println(counter);
//        }

//        this.checkLeaves();
//        this.checkZeroCFs();
//        this.checkZeroCFs2(this.root);

        CF newCF = new CF(id, pointTree, timestamp, this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);

        if (this.storedProfiles.containsKey(id)) {
            CF previousClosestCF = this.storedProfiles.get(id);
            previousClosestCF.getTree().removeCFs(id);
            previousClosestCF.getStoredIDs().remove(id);
            previousClosestCF.setN(previousClosestCF.getN() - 1);
            CF backTrackCF = previousClosestCF.getParent().getParent();
            while (backTrackCF != null) {
                backTrackCF.getTree().removeCFs(id);
                backTrackCF.getStoredIDs().remove(id);
                backTrackCF.setN(backTrackCF.getN() - 1);
                backTrackCF = backTrackCF.getParent().getParent();
            }

            if (previousClosestCF.getTree().getRoot().getCfList().size() == 0) {
                this.removeCFfromTree(previousClosestCF);
                this.storedProfiles.remove(id);
                this.nCFNodes.getAndDecrement();
                this.nLeafCFs.getAndDecrement();
                this.nLeafCFsSum.getAndAdd(previousClosestCF.getN());
                this.nLeafCFsHSum.addValue(1.0/previousClosestCF.getN());
                this.nLeafCFsSquared.getAndAdd(previousClosestCF.getN() * previousClosestCF.getN());

                SplitChangeDifference splitChangeDifference = insertCF(newCF, true, anomaly);
                this.storedProfiles.put(id, splitChangeDifference.getCf());
//                if (("192.168.5.122_fwd").equals(id)) {
//                    previousClosestCF.getTree().print("results/" + "tree_192.168.5.122_fwd_inner_" + counter);
//                    previousClosestCF.getTree().checkZeroCFs();
//                    previousClosestCF.getTree().checkZeroCFs2(previousClosestCF.getTree().getRoot());
//                }
                return splitChangeDifference;
            }
            else {
                if (previousClosestCF.isWithinThreshold(newCF, this.distanceThresholdLC, this.distanceFunction)){
                    SplitChangeDifference splitChangeDifference = previousClosestCF.getParent().addToCF(newCF, previousClosestCF, this.nLeafCFs, this.nLeafCFsSum, this.nLeafCFsSquared, this.nLeafCFsHSum, anomaly, true);
                    backTrackCF = previousClosestCF.getParent().getParent();
                    while (backTrackCF != null) {
                        backTrackCF.update(newCF, anomaly);
                        backTrackCF = backTrackCF.getParent().getParent();
                    }
//                    if (("192.168.5.122_fwd").equals(id)) {
//                        previousClosestCF.getTree().print("results/" + "tree_192.168.5.122_fwd_inner_" + counter);
//                        previousClosestCF.getTree().checkZeroCFs();
//                        previousClosestCF.getTree().checkZeroCFs2(previousClosestCF.getTree().getRoot());
//                    }
                    return splitChangeDifference;
                }
                else {
                    SplitChangeDifference splitChangeDifference = insertCF(newCF, true, anomaly);
                    this.storedProfiles.put(id, splitChangeDifference.getCf());
//                    if (("192.168.5.122_fwd").equals(id)) {
//                        previousClosestCF.getTree().print("results/" + "tree_192.168.5.122_fwd_inner_" + counter);
//                        previousClosestCF.getTree().checkZeroCFs();
//                        previousClosestCF.getTree().checkZeroCFs2(previousClosestCF.getTree().getRoot());
//                    }
                    return splitChangeDifference;
                }
            }
        }
        else {
            SplitChangeDifference splitChangeDifference = insertCF(new CF(id, pointTree, timestamp, this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold), true, anomaly);
            this.storedProfiles.put(id, splitChangeDifference.getCf());
            return splitChangeDifference;
        }
    }

    private SplitChangeDifference insertCF(CF cf, boolean insertingNew, boolean anomaly){

//        this.counter++;
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

        if (this.nCFNodes.get() > this.maxNumberOfNodesLC) {
//            System.out.println("Rebuild START");
//            this.ndtdThresholds = checkLeaves(cf.getTimestamp());
            rebuildTree();
//            System.out.println("Rebuild END");
        }


        splitChangeDifference.setSplit(true);
        return splitChangeDifference;
    }

    public void checkZeroCFs() {
        CFNode currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (CF cf : currentNode.getCfList()) {
                    if (cf.getN() == 0) {
                        System.out.println("Zero CF.");
                    }
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

    }

    public void checkZeroCFs2(CFNode currentNode) {
        for (CF cf : currentNode.getCfList()) {
           if (cf.getN() == 0) {
               System.out.println("Zero CF.");
           }
           if (cf.hasChild()) {
               checkZeroCFs2(cf.getChild());
           }
        }
    }

    public void checkLeaves() {
        ArrayList<CF> leafListArray = new ArrayList<>();
        CFNode currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (CF cf : currentNode.getCfList()) {
                    leafListArray.add(cf);
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

        ArrayList<CF> leafTreeListArray =  new ArrayList<>();
        innerCheckLeaves(this.root, leafTreeListArray);

        if (leafListArray.size() != leafTreeListArray.size()) {
            System.out.println("Leaves numbers differ!");
        }

    }

    public void innerCheckLeaves(CFNode currentNode, ArrayList<CF> array) {
        for (CF cf : currentNode.getCfList()) {
            if (cf.hasChild()) {
                innerCheckLeaves(cf.getChild(), array);
            }
            else {
                array.add(cf);
            }
        }
    }

    public void removeCFfromTree(CF currentCF) {
        CFNode parent = currentCF.getParent();
        parent.getCfList().remove(currentCF);

        if (parent.getCfList().size() == 0) {
            CF parentCF = parent.getParent();
            if (parentCF != null) {
                parentCF.setChild(null);
                if (parent.getNextCFLeaf() != null) {
                    parent.getNextCFLeaf().setPreviousCFLeaf(parent.getPreviousCFLeaf());
                }
                if (parent.getPreviousCFLeaf() != null) {
                    parent.getPreviousCFLeaf().setNextCFLeaf(parent.getNextCFLeaf());
                }
                removeCFfromTree(parentCF);
                this.nCFNodes.getAndDecrement();
            }
        }
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

        CF newCF1 = new CF(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        CFNode newCFNode1 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.root.isLeafStatus(), this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        newCF1.setChild(newCFNode1);
        newCFNode1.setParent(newCF1);

        CF newCF2 = new CF(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        CFNode newCFNode2 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.root.isLeafStatus(), this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        newCF2.setChild(newCFNode2);
        newCFNode2.setParent(newCF2);

        CFNode newRootCFNode = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, false, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        newRootCFNode.getCfList().add(newCF1);
        newRootCFNode.getCfList().add(newCF2);
        newCF1.setParent(newRootCFNode);
        newCF2.setParent(newRootCFNode);

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

//        System.out.println("rebuildTree()");

        double newDistanceThreshold = computeNewThreshold();

        CFTree newCFTree = new CFTree(this.maxNodeEntries, this.distanceFunction, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.distanceThresholdLB, newDistanceThreshold, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, name);
        newCFTree.setIdCounter(this.getIdCounter());

        CFNode currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (CF cf : currentNode.getCfList()) {
                    newCFTree.insertCF(cf, false, false);
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

        System.out.println("Level C new threshold: " + newCFTree.getDistanceThresholdLC());
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

        if (newThreshold <= this.distanceThresholdLC) {
            newThreshold = 1.1 * this.distanceThresholdLC;
        }

//        System.out.println(newThreshold);

        return newThreshold;
    }

//    public ArrayList<double[]> getCentroidsAndRadius() {
//        ArrayList<double[]> centroids = new ArrayList<>();
//
//        CFNode currentNode = this.leafList;
//        while (currentNode != null) {
//            if (!currentNode.isDummy()) {
//                for (CF c : currentNode.getCfList()) {
//                    double[] ls = c.getCentroid();
//                    double r = 0;
//
//                    double wCF1 = c.getW();
//
//                    double[] linearSumCF1 = c.getLinearSum();
//                    double[] squareSumCF1 = c.getSquareSum();
//
//                    for(int i = 0; i < linearSumCF1.length; i++) {
//                        double centroid = linearSumCF1[i] / wCF1;
//                        r += squareSumCF1[i] / wCF1 - centroid * centroid;
//                    }
//
//                    if (r < 0.0){
//                        if(r < -0.00000001 )
//                            System.err.println("d5 < 0 !!!");
//                        r = 0.0;
//                    }
//
//                    double[] ra = new double[]{Math.sqrt(r)};
//                    centroids.add(concatTwo(ls, ra));
//
//                }
//            }
//            currentNode = currentNode.getNextCFLeaf();
//        }
//
//        return centroids;
//    }

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

    public AtomicLong getIdCounter() {
        return idCounter;
    }

    public void setIdCounter(AtomicLong idCounter) {
        this.idCounter = idCounter;
    }

    public void print(String filename) {



        try {
            PrintWriter writer = new PrintWriter(filename + ".txt");

            int currentN = 0;
            int innerN = 0;
            double[] sum = null;
            for (CF cF : this.root.getCfList()){
                currentN += cF.getN();
                innerN += cF.getTree().getN();

                if (sum == null) {
                    sum = cF.getTree().getLinearSum().clone();
                }
                else{
                    for (int i = 0; i < cF.getTree().getLinearSum().length; i++) {
                        sum[i] += cF.getTree().getLinearSum()[i];
                    }
                }
            }

            StringBuilder centroidString = new StringBuilder("[");
            if (sum != null) {
                for (int i = 0; i < sum.length; i++) {
                    centroidString.append(String.format(Locale.US, "%.2f", sum[i] / innerN));
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

    public HashMap<String, CF> getStoredProfiles() {
        return storedProfiles;
    }

    public double getDistanceThresholdLC() {
        return distanceThresholdLC;
    }
}
