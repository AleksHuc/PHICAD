package tbirchi;

import utils.HitWindow;
import utils.MutableDouble;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The class implements an object that present the incremental cluster feature tree.
 */
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
    private double intraClusterThreshold;
    private String name;

    private boolean[] interFlags;
    private int[] interLastSwitch;
    private ArrayList<HitWindow> interHitWindows;
    private int hitWindowLength = 1000;
    private double hitWindowProbability = 0.005;
    private int anomalyWindowLength = 40;
    private double anomalyWindowProbability = 0.0;

    private LocalDateTime timestamp;

    /**
     * The constructor creates a new incremental cluster feature tree with the corresponding parameters.
     * @param maxNodeEntries int value that presents the maximum number of incremental cluster features that incremental cluster feature node can store.
     * @param maxNumberOfNodes int value that present the maximum number of incremental cluster feature nodes that incremental cluster feature tree can store.
     * @param lambda double value that presents the fading factor for forgetting old data.
     * @param distanceThreshold double value that presents the initial incremental cluster feature radius threshold.
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
     * @param distanceFunction int value that presents the type of distance function to be used by the incremental cluster feature.
     * @param name String value that presents the name of the corresponding incremental cluster feature.
     */
    public CFTree(int maxNodeEntries, int maxNumberOfNodes, double lambda, double distanceThreshold, int checkStep, int maxBins, int sizeOfBin, double delta, int largeWindowSize, double largeWindowProbability, int smallWindowSize, double smallWindowProbability, double clusterSizeThreshold, double intraClusterThreshold, int distanceFunction, String name) {
        this.maxNodeEntries = maxNodeEntries;
        this.distanceFunction = distanceFunction;
        this.counter = 0;
        this.distanceThreshold = distanceThreshold;
        this.normalClusterThreshold = clusterSizeThreshold;
        this.idCounter = new AtomicLong();
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
        this.hitWindowLength = largeWindowSize;
        this.hitWindowProbability = largeWindowProbability;
        this.anomalyWindowLength = smallWindowSize;
        this.anomalyWindowProbability = smallWindowProbability;
        this.intraClusterThreshold = intraClusterThreshold;
        this.root = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, true, this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
        this.leafList = new CFNode(0, 0, 0, true, this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
        this.leafList.setNextCFLeaf(this.root);
    }

    /**
     * The method insert a new data point to the incremental cluster feature tree.
     * @param point double array that present the input data point.
     * @param timestamp LocalDateTime object that presents the timestamp of the data point.
     * @return SplitChangeDifference object that presents whether certain incremental cluster feature should be split and the corresponding anomaly predictions from the current data point analysis.
     */
    public SplitChangeDifference insertPoint(double[] point, LocalDateTime timestamp) {
        this.counter++;
        this.timestamp = timestamp;
        return insertCF(new CF(point, timestamp, this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold), true);
    }

    /**
     * The method inserts a new incremental cluster feature into the root incremental cluster feature node and initiates the clustering procedure.
     * @param cf CF object that presents a new incremental cluster feature to be clustered into the incremental cluster feature tree.
     * @param insertingNew boolean value that present whether we are inserting a new incremental cluster feature or an existing one.
     * @return SplitChangeDifference object that presents whether certain incremental cluster feature should be split and the corresponding anomaly predictions from the current data point analysis.
     */
    private SplitChangeDifference insertCF(CF cf, boolean insertingNew){

        SplitChangeDifference splitChangeDifference = this.root.insertCF(cf, this.nCFNodes, this.nLeafCFs, this.nLeafCFsSum, this.nLeafCFsSquared, insertingNew, this.nLeafCFsHSum, this.interHitWindows, this.interFlags, this.interLastSwitch, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.counter);

        if (splitChangeDifference.isSplit()) {
            splitRoot();
        }

        if (this.nCFNodes.get() > this.maxNumberOfNodes) {
            rebuildTree();
        }

        splitChangeDifference.setSplit(true);
        return splitChangeDifference;
    }

    /**
     * The methods split the root incremental cluster feature node when it becomes full into two new child incremental cluster feature nodes and a new root incremental cluster feature node.
     */
    private void splitRoot() {

        CF[] farthestCFPair = this.root.findFarthestCFPair(this.root.getCfList());

        CF newCF1 = new CF(this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
        CFNode newCFNode1 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, this.root.isLeafStatus(), this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
        newCF1.setChild(newCFNode1);

        CF newCF2 = new CF(this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
        CFNode newCFNode2 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, this.root.isLeafStatus(), this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
        newCF2.setChild(newCFNode2);

        CFNode newRootCFNode = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, false, this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
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

    /**
     * The method rebuilds the entire incremental cluster feature tree with the new incremental cluster feature radius threshold to accommodate new data.
     */
    private void rebuildTree() {

        double newDistanceThreshold = computeNewThreshold();

        CFTree newCFTree = new CFTree(this.maxNodeEntries, this.maxNumberOfNodes, this.lambda, newDistanceThreshold, this.checkStep, this.maxBins, this.sizeOfBin, this.delta, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.normalClusterThreshold, this.intraClusterThreshold, this.distanceFunction, this.name);
        newCFTree.setIdCounter(this.getIdCounter());

        CFNode currentNode = this.leafList;
        while (currentNode != null) {
            if (!currentNode.isDummy()) {
                for (CF cf : currentNode.getCfList()) {
                    newCFTree.insertCF(cf, false);
                }
            }
            currentNode = currentNode.getNextCFLeaf();
        }

        this.distanceThreshold = newDistanceThreshold;
        this.counter = newCFTree.getCounter();
        this.root = newCFTree.getRoot();
        this.leafList = newCFTree.getLeafList();
        this.nCFNodes = newCFTree.getnCFNodes();
    }

    /**
     * The method calculates the new incremental cluster feature radius threshold from the average distance between all closest pairs of incremental cluster features.
     * @return double value that presents the new incremental cluster feature radius threshold.
     */
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

        return newThreshold;
    }

    /**
     * The method returns the current value of the counter.
     * @return int value that presents the current value of the counter.
     */
    private int getCounter() {
        return counter;
    }

    /**
     * the method returns the current root incremental cluster feature node.
     * @return CFNode object that presents the current root incremental cluster feature node.
     */
    private CFNode getRoot() {
        return root;
    }

    /**
     * The method returns the starting incremental cluster feature node of the leaf incremental cluster feature nodes list.
     * @return CFNode object that presents the starting incremental cluster feature node of the leaf incremental cluster feature nodes list.
     */
    public CFNode getLeafList() {
        return leafList;
    }

    /**
     * The method returns the current number of incremental cluster features in the incremental cluster feature tree.
     * @return AtomicInteger object that presents the current number of incremental cluster features in the incremental cluster feature tree.
     */
    AtomicInteger getnCFNodes() {
        return nCFNodes;
    }

    /**
     * The method returns the current value of the unique identifier counter object.
     * @return AtomigLong object that presents the current value of the unique identifier counter object.
     */
    public AtomicLong getIdCounter() {
        return idCounter;
    }

    /**
     * The method replaces the current unique identifier counter object with a new one from the input parameter.
     * @param idCounter AtomicLong object that presents the unique identifier counter object.
     */
    public void setIdCounter(AtomicLong idCounter) {
        this.idCounter = idCounter;
    }

    /**
     * The method prints the current incremental cluster feature tree in a print friendly format.
     * @param filename String object that present the name of the output file.
     */
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
}
