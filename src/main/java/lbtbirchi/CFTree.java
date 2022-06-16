package lbtbirchi;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

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

    public CFTree(int maxNodeEntries, int distanceFunction, double distanceThreshold, int maxNumberOfNodes) {
        this.maxNodeEntries = maxNodeEntries;
        this.distanceFunction = distanceFunction;
        this.counter = 0;
        this.distanceThreshold = distanceThreshold;
        this.root = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, true);
        this.leafList = new CFNode(0, 0, 0, true);
        this.leafList.setNextCFLeaf(this.root);
        this.nCFNodes = new AtomicInteger();
        this.nCFNodes.getAndIncrement();
        this.maxNumberOfNodes = maxNumberOfNodes;
    }

    public void insertPoint(double[] point, String profileID) {
        this.counter++;
        SplitAndCF splitAndCF = insertCF(new CF(point, profileID));
//
//        this.checkZeroCFs();
//        this.checkZeroCFs2(this.root);
    }

    public SplitAndCF insertCF(CF cf){

//        System.out.println(this.name + " " + this.counter);
//        this.print("results/test_p_tree.txt");
        SplitAndCF splitAndCF = this.root.insertCF(cf, this.nCFNodes);

        if (splitAndCF.isSplit()) {
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

        return splitAndCF;
    }

    private void splitRoot() {

        CF[] farthestCFPair = this.root.findFarthestCFPair(this.root.getCfList());

        CF newCF1 = new CF();
        CFNode newCFNode1 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, this.root.isLeafStatus());
        newCF1.setChild(newCFNode1);
        newCFNode1.setParent(newCF1);

        CF newCF2 = new CF();
        CFNode newCFNode2 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, this.root.isLeafStatus());
        newCF2.setChild(newCFNode2);
        newCFNode2.setParent(newCF2);

        CFNode newRootCFNode = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, false);
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

        double newDistanceThreshold = computeNewThreshold();

        CFTree newCFTree = new CFTree(this.maxNodeEntries, this.distanceFunction, newDistanceThreshold, this.maxNumberOfNodes);

        CFNode currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (CF cf : currentNode.getCfList()) {
                    newCFTree.insertCF(cf);
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

//        System.out.println("Level B new threshold: " + newCFTree.getDistanceThreshold());

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

    public double treeDistance(CFTree insertingTree) {

        double n = totalN(insertingTree);

        double nOfB = 0;
        double nOfSimilarCFs = 0;

        CFNode currentNode = insertingTree.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (CF cf : currentNode.getCfList()) {
                    double w = cf.getN()/n;
                    double result = this.root.calculateDistance(cf);
//                    if (result < 0.01) {
                    nOfSimilarCFs += w * 0.5 + (1.0 - result) * 0.5;
//                    }
                    nOfB += 1;
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

        double distance = 1.0 - nOfSimilarCFs / nOfB;

        if (Double.isNaN(distance)) {
            System.out.println("Distance is NaN!");
        }

//        System.out.println(distance);

        return distance;
    }

    private double totalN(CFTree tree){
        double n = 0.0;
        for (CF cf : tree.getRoot().getCfList()) {
            n += cf.getN();
        }
        return n;
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

    public void removeCFs(String profileID) {

        CFNode currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                ArrayList<CF> toDelete = new ArrayList<>();
                for (CF leaf : currentNode.getCfList()) {
                    if (leaf.getStoredCFs().containsKey(profileID)){
                        leaf.removeCFs(profileID);
                        if (leaf.getStoredCFs().size() == 0) {
                            toDelete.add(leaf);
                        }
                    }
                }

                for (CF deleteLeaf : toDelete) {
                    removeCFfromTree(deleteLeaf);
                    this.nCFNodes.getAndDecrement();
                }
//                this.checkZeroCFs();
//                this.checkZeroCFs2(this.root);
            }
            currentNode = currentNode.getNextCFLeaf();
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

    private int getCounter() {
        return counter;
    }

    public CFNode getRoot() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CFTree)) return false;
        CFTree cfTree = (CFTree) o;

        if (this.maxNodeEntries != cfTree.getMaxNodeEntries()) {
            return false;
        }

        if (this.distanceFunction != cfTree.getDistanceFunction()) {
            return false;
        }

        if (this.maxNumberOfNodes != cfTree.getMaxNumberOfNodes()) {
            return false;
        }

        if (this.counter != cfTree.getCounter()) {
            return false;
        }

        if (this.distanceThreshold != cfTree.getDistanceThreshold()) {
            return false;
        }

        if (this.nCFNodes.get() != cfTree.getnCFNodes().get()) {
            return false;
        }

        CFNode currentNode = this.leafList;
        CFNode currentNode2 = cfTree.getLeafList();

        while (currentNode != null) {

            if (currentNode2 == null) {
                return false;
            }

            if (!currentNode.equals(currentNode2)) {
                return false;
            }

            currentNode = currentNode.getNextCFLeaf();
            currentNode2 = currentNode2.getNextCFLeaf();
        }

        return this.getRoot().equals(cfTree.getRoot());
    }

    public void print(String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename + ".txt");

            double currentN = 0;
            double[] sum = null;
            for (CF cF : this.root.getCfList()){
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
                    centroidString.append(String.format(Locale.US, "%.2f", sum[i] / currentN));
                    if (i != sum.length - 1) {
                        centroidString.append(", ");
                    }
                }
            }
            centroidString.append("]");

            writer.println("└── " + "N=" + String.format(Locale.US, "%.2f", currentN) + " C=" + centroidString.toString());
            this.root.print("    ", false, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMaxNodeEntries() {
        return maxNodeEntries;
    }

    public int getDistanceFunction() {
        return distanceFunction;
    }

    public int getMaxNumberOfNodes() {
        return maxNumberOfNodes;
    }

    public double[] getCentroid() {
        ArrayList<lbtbirchi.CF> rootCFs = this.root.getCfList();
        double[] centroid = new double[rootCFs.get(0).getLinearSum().length];
        double n = 0.0;
        for (lbtbirchi.CF cf : rootCFs) {
            n += cf.getN();
            double[] ls = cf.getLinearSum();
            for (int i = 0; i < ls.length; i++) {
                centroid[i] += ls[i];
            }
        }

        for (int i = 0; i < centroid.length; i++) {
            centroid[i] /= n;
        }
        return centroid;
    }

    public double[] getLinearSum() {
        ArrayList<CF> rootCFs = this.root.getCfList();
        double[] linearSum = new double[rootCFs.get(0).getLinearSum().length];
        for (CF cf : rootCFs) {
            double[] ls = cf.getLinearSum();
            for (int i = 0; i < ls.length; i++) {
                linearSum[i] += ls[i];
            }
        }

        return linearSum;
    }

    public double[] getSquareSum() {
        ArrayList<CF> rootCFs = this.root.getCfList();
        double[] squareSum = new double[rootCFs.get(0).getSquareSum().length];
        for (CF cf : rootCFs) {
            double[] ss = cf.getSquareSum();
            for (int i = 0; i < ss.length; i++) {
                squareSum[i] += ss[i];
            }
        }

        return squareSum;
    }

    public double getN() {
        ArrayList<CF> rootCFs = this.root.getCfList();
        double n = 0.0;
        for (CF cf : rootCFs) {
            n += cf.getN();
        }

        return n;
    }


}
