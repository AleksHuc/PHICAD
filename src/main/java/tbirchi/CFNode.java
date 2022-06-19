package tbirchi;

import utils.HitWindow;
import utils.MutableDouble;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * The class implements an object that present the incremental cluster feature node.
 */
public class CFNode {

    private boolean leafStatus;
    private int maxNodeEntries;
    private int distanceFunction;
    private double distanceThreshold;
    private double normalClusterThreshold;
    private ArrayList<CF> cfList;
    private CFNode nextCFLeaf;
    private CFNode previousCFLeaf;
    private double lambda;
    private double delta;
    private int checkStep;
    private int maxBins;
    private int sizeOfBin;
    private AtomicLong idCounter;
    private String name;
    private int hitWindowLength = 1000;
    private double hitWindowProbability = 0.05;
    private int anomalyWindowLength = 20;
    private double anomalyWindowProbability = 0.1;
    private double intraClusterThreshold;

    /**
     * The constructor creates a new incremental cluster features node with the corresponding parameters.
     * @param maxNodeEntries int value that presents the maximum number of incremental cluster features that incremental cluster feature node can store.
     * @param distanceFunction int value that presents the type of distance function to be used by the incremental cluster feature.
     * @param distanceThreshold double value that presents the initial incremental cluster feature radius threshold.
     * @param leafStatus boolean value that presents whether the incremental cluster feature is leaf or not.
     * @param lambda double value that presents the fading factor for forgetting old data.
     * @param delta double value that presents the maximum delta (confidence) value for the ADWIN windows.
     * @param checkStep int value that presents the number of iterations before we check for anomaly.
     * @param maxBins int value that presents the maximum number of bins the exponential histogram can have.
     * @param sizeOfBin int value that presents the maximum number of values inside a single bin.
     * @param normalClusterThreshold double value that presents the multiplication factor for the harmonic mean to determine normal clusters based on their size.
     * @param idCounter AtomicLong object that presents the atomic long value for determining unique identifiers for new incremental cluster features.
     * @param name String value that presents the name of the corresponding incremental cluster feature.
     * @param largeWindowSize int value that presents the size of the long-term window of short-term models.
     * @param largeWindowProbability double value that presents the maximum probability of a given detection mechanism triggering to still be considered useful.
     * @param smallWindowSize int value that presents the size of the short-term window of short-term models.
     * @param smallWindowProbability double value that presents the minimum probability of a given detection mechanism triggering to still be considered useful.
     * @param intraClusterThreshold double value that presents the multiplication factor for the standard deviation to determine distance threshold.
     */
    CFNode(int maxNodeEntries, int distanceFunction, double distanceThreshold, boolean leafStatus, double lambda, double delta, int checkStep, int maxBins, int sizeOfBin, double normalClusterThreshold, AtomicLong idCounter, String name, int largeWindowSize, double largeWindowProbability, int smallWindowSize, double smallWindowProbability, double intraClusterThreshold) {
        this.leafStatus = leafStatus;
        this.maxNodeEntries = maxNodeEntries;
        this.distanceFunction = distanceFunction;
        this.distanceThreshold = distanceThreshold;
        this.cfList = new ArrayList<>();
        this.nextCFLeaf = null;
        this.previousCFLeaf = null;
        this.lambda = lambda;
        this.delta = delta;
        this.checkStep = checkStep;
        this.maxBins = maxBins;
        this.sizeOfBin = sizeOfBin;
        this.normalClusterThreshold = normalClusterThreshold;
        this.idCounter = idCounter;
        this.name = name;
        this.intraClusterThreshold = intraClusterThreshold;
        this.hitWindowLength = largeWindowSize;
        this.hitWindowProbability = largeWindowProbability;
        this.anomalyWindowLength = smallWindowSize;
        this.anomalyWindowProbability = smallWindowProbability;
    }

    /**
     * The method inserts an incremental cluster feature into the current one.
     * @param cf CF object that present the incremental cluster feature to be inserted.
     * @param nCFNodes AtomicInteger object that presents atomic int value for counting the number of incremental cluster feature nodes in the tree.
     * @param nLeafCFs AtomicInteger object that presents atomic int value for counting the number of leaf incremental cluster features in the tree.
     * @param nLeafCFsSum AtomicInteger object that presents atomic int value for counting the linear sum of number of leaf incremental cluster features in the tree.
     * @param nLeafCFsSquared AtomicInteger object that presents atomic int value for counting the square sum of number of leaf incremental cluster features in the tree.
     * @param insertingNew boolean value that presents whether we are inserting a new incremental cluster feature or an existing one in the incremental cluster feature.
     * @param nLeafCFsHSum MutableDouble object that presents atomic double value for counting the linear sum of one divided with number of leaf incremental cluster features in the tree.
     * @param interHitWindows ArrayList&lt;HitWindow&gt; object that presents list of landmark windows od the short-term model that store the times when certain detection mechanisms triggered.
     * @param interFlags boolean array that presents if any mechanisms satisfied the short-term model requirements for their detection to be propagated further.
     * @param interLastSwitch int array that presents when a certain detection mechanism lastly triggered.
     * @param hitInterWindowLength int value that presents the length of the entire short-term window.
     * @param hitInterWindowProbability double value that presents the probability threshold that the detection mechanism must not exceed in entire short-term window to produce trustful prediction.
     * @param anomalyWindowLength int value that presents the length of the newest part of the short-term window.
     * @param anomalyWindowProbability double value that presents the probability threshold that the detection mechanism must exceed in the newest part of the short-term window to produce trustful prediction.
     * @param counter int counter that presents current consecutive number of the data points analyzed.
     * @return SplitChangeDifference object that presents whether certain incremental cluster feature should be split and the corresponding anomaly predictions from the current data point analysis.
     */
    SplitChangeDifference insertCF(CF cf, AtomicInteger nCFNodes, AtomicInteger nLeafCFs, AtomicInteger nLeafCFsSum, AtomicInteger nLeafCFsSquared, boolean insertingNew, MutableDouble nLeafCFsHSum, ArrayList<HitWindow> interHitWindows, boolean[] interFlags, int[] interLastSwitch, int hitInterWindowLength, double hitInterWindowProbability, int anomalyWindowLength, double anomalyWindowProbability, int counter){

        if (this.cfList.size() == 0) {
            this.cfList.add(cf);
            nCFNodes.getAndIncrement();
            nLeafCFs.getAndIncrement();
            nLeafCFsSum.getAndAdd(cf.getN());
            nLeafCFsHSum.addValue(1.0/cf.getN());
            nLeafCFsSquared.getAndAdd(cf.getN() * cf.getN());
            return new SplitChangeDifference(false, null, !cf.isNormalCluster(), null, null);
        }

        CF closestCF = findClosestCF(cf);

        if (closestCF.hasChild()){
            SplitChangeDifference splitChangeDifference = closestCF.getChild().insertCF(cf, nCFNodes, nLeafCFs, nLeafCFsSum, nLeafCFsSquared, insertingNew, nLeafCFsHSum, interHitWindows, interFlags, interLastSwitch, hitInterWindowLength, hitInterWindowProbability, anomalyWindowLength, anomalyWindowProbability, counter);
            if (!splitChangeDifference.isSplit()) {
                closestCF.update(cf);
                splitChangeDifference.setSplit(false);
                return splitChangeDifference;
            }
            else{
                CF[] splitPair = splitCF(closestCF, nCFNodes);

                if (this.cfList.size() > this.maxNodeEntries) {
                    splitChangeDifference.setSplit(true);
                    return splitChangeDifference;
                }
                else {
                    mergingRefinement(splitPair, nCFNodes);
                    splitChangeDifference.setSplit(false);
                    return splitChangeDifference;
                }
            }
        }
//        else if (closestCF.isWithinThreshold(cf, this.distanceThreshold, this.distanceFunction)) {
        else if (closestCF.isWithinThreshold(cf, this.distanceThreshold, CFTree.D5_DIST)) {
            return addToCF(cf, closestCF, nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum, insertingNew);
        }
        else if (this.cfList.size() < this.maxNodeEntries) {
            return addToCFNode(cf, false, nCFNodes, nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum, interHitWindows, interFlags, interLastSwitch, hitInterWindowLength, hitInterWindowProbability, anomalyWindowLength, anomalyWindowProbability, counter);
        }
        else {
            return addToCFNode(cf, true, nCFNodes, nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum, interHitWindows, interFlags, interLastSwitch, hitInterWindowLength, hitInterWindowProbability, anomalyWindowLength, anomalyWindowProbability, counter);
        }
    }

    /**
     * The method inserts a new incremental cluster feature to the closest incremental cluster feature.
     * @param cf CF object that present the incremental cluster feature to be inserted.
     * @param closestCF CF object that present the incremental cluster feature that is the closest to the the incremental cluster feature to be inserted.
     * @param nLeafCFs AtomicInteger object that presents atomic int value for counting the number of leaf incremental cluster features in the tree.
     * @param nLeafCFsSum AtomicInteger object that presents atomic int value for counting the linear sum of number of leaf incremental cluster features in the tree.
     * @param nLeafCFsSquared AtomicInteger object that presents atomic int value for counting the square sum of number of leaf incremental cluster features in the tree.
     * @param nLeafCFsHSum MutableDouble object that presents atomic double value for counting the linear sum of one divided with number of leaf incremental cluster features in the tree.
     * @param insertingNew boolean value that presents whether we are inserting a new incremental cluster feature or an existing one in the incremental cluster feature.
     * @return SplitChangeDifference object that presents whether certain incremental cluster feature should be split and the corresponding anomaly predictions from the current data point analysis.
     */
    SplitChangeDifference addToCF(CF cf, CF closestCF, AtomicInteger nLeafCFs, AtomicInteger nLeafCFsSum, AtomicInteger nLeafCFsSquared, MutableDouble nLeafCFsHSum, boolean insertingNew) {

        // Decrement the CF counters with the old value of closestCF.
        nLeafCFsSum.getAndSet(nLeafCFsSum.get() - closestCF.getN());
        nLeafCFsHSum.addValue(-1.0/closestCF.getN());
        nLeafCFsSquared.getAndSet(nLeafCFsSquared.get() - (closestCF.getN() * closestCF.getN()));

        // Update the closestCF with CF and get cluster changes and intra cluster anomaly flags.
        byte[] changeDifferences = closestCF.update(cf);

        // Increment the CF counters with the new value of closestCF.
        nLeafCFsSum.getAndAdd(closestCF.getN());
        nLeafCFsHSum.addValue(1.0/closestCF.getN());
        nLeafCFsSquared.getAndAdd(closestCF.getN() * closestCF.getN());

        // Check for cluster anomaly.
        boolean clusterAnomaly = false;
        if (insertingNew && closestCF.getN() > 2 && SECONDS.between(closestCF.getCreationTimestamp(), cf.getTimestamp()) > 1 && !closestCF.isNormalCluster2(nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum)) {
            clusterAnomaly = true;
        }
        return new SplitChangeDifference(false, changeDifferences, clusterAnomaly, null, null);
    }

    /**
     * The method inserts a new incremental cluster feature to the closest incremental cluster feature node.
     * @param cf CF object that present the incremental cluster feature to be inserted.
     * @param split boolean value that presents whether the current incremental cluster feature node should be split or not.
     * @param nCFNodes AtomicInteger object that presents atomic int value for counting the number of incremental cluster feature nodes in the tree.
     * @param nLeafCFs AtomicInteger object that presents atomic int value for counting the number of leaf incremental cluster features in the tree.
     * @param nLeafCFsSum AtomicInteger object that presents atomic int value for counting the linear sum of number of leaf incremental cluster features in the tree.
     * @param nLeafCFsSquared AtomicInteger object that presents atomic int value for counting the square sum of number of leaf incremental cluster features in the tree.
     * @param nLeafCFsHSum MutableDouble object that presents atomic double value for counting the linear sum of one divided with number of leaf incremental cluster features in the tree.
     * @param interHitWindows ArrayList&lt;HitWindow&gt; object that presents list of landmark windows od the short-term model that store the times when certain detection mechanisms triggered.
     * @param interFlags boolean array that presents if any mechanisms satisfied the short-term model requirements for their detection to be propagated further.
     * @param interLastSwitch int array that presents when a certain detection mechanism lastly triggered.
     * @param hitInterWindowLength int value that presents the length of the entire short-term window.
     * @param hitInterWindowProbability double value that presents the probability threshold that the detection mechanism must not exceed in entire short-term window to produce trustful prediction.
     * @param anomalyWindowLength int value that presents the length of the newest part of the short-term window.
     * @param anomalyWindowProbability double value that presents the probability threshold that the detection mechanism must exceed in the newest part of the short-term window to produce trustful prediction.
     * @param counter int counter that presents current consecutive number of the data points analyzed.
     * @return SplitChangeDifference object that presents whether certain incremental cluster feature should be split and the corresponding anomaly predictions from the current data point analysis.
     */
    SplitChangeDifference addToCFNode(CF cf, boolean split, AtomicInteger nCFNodes, AtomicInteger nLeafCFs, AtomicInteger nLeafCFsSum, AtomicInteger nLeafCFsSquared, MutableDouble nLeafCFsHSum, ArrayList<HitWindow> interHitWindows, boolean[] interFlags, int[] interLastSwitch, int hitInterWindowLength, double hitInterWindowProbability, int anomalyWindowLength, double anomalyWindowProbability, int counter) {
        // Add new CF to the leaf CF Node.
        this.cfList.add(cf);

        // Increment the CF counters.
        nCFNodes.getAndIncrement();
        nLeafCFs.getAndIncrement();
        nLeafCFsSum.getAndAdd(cf.getN());
        nLeafCFsHSum.addValue(1.0/cf.getN());
        nLeafCFsSquared.getAndAdd(cf.getN() * cf.getN());

        // Create a dummy CF from all CFs in the leaf CF Node and check for inter cluster anomaly.
        CF dummyCF = createCFForNode();
        double[] dummyCentroid = dummyCF.getCentroid();
        byte[] interClusterAnomaly = dummyCF.checkStandardDeviation(cf);
        for (int i = 0; i < interClusterAnomaly.length; i++) {
            if (interClusterAnomaly[i] > 0) {

                // If the inter hit windows are empty then create them.
                if (interHitWindows == null) {
                    interFlags = new boolean[cf.getLinearSum().length];
                    interLastSwitch = new int[cf.getLinearSum().length];
                    interHitWindows = new ArrayList<>();
                    for (int j = 0; j < cf.getLinearSum().length; j++) {
                        HitWindow hw = new HitWindow(hitInterWindowLength);
                        interHitWindows.add(hw);
                    }
                }

                // Add current anomaly detection.
                interHitWindows.get(i).add(counter, 1.0);

                // Calculate the detection frequency threshold.
                double t = hitInterWindowLength * hitInterWindowProbability;
                if (counter < hitInterWindowLength) {
                    t = counter * hitInterWindowProbability;
                }

                // Check if detection frequency in a give hit window is less then threshold.
                if (interHitWindows.get(i).newestSinceSize(counter, hitInterWindowLength) < t) {

                        // Check if detection frequency is then large enough on a smaller window.
                        if (interHitWindows.get(i).newestSinceSize(counter, anomalyWindowLength) >= (anomalyWindowLength * anomalyWindowProbability)) {
                            interFlags[i] = true;
                            interLastSwitch[i] = counter;
                        }
                }
                // Otherwise negate the values in inter flags if they are true.
                else {
                    if (interFlags[i]){
                        interFlags[i] = false;
                    }
                }
            }
        }

        // If any inter flag was set to true then set CF as anomalous.
        if (interFlags != null) {
            cf.setAnomalyCluster(interFlags);
            if (cf.isAnomalyCluster()) {
                interClusterAnomaly = new byte[]{1};
            } else {
                interClusterAnomaly = new byte[]{0};
            }
        }

        return new SplitChangeDifference(split, null, false, null, interClusterAnomaly);
    }

    /**
     * The method creates a new incremental cluster feature from all the child incremental cluster features in the current incremental cluster feature node.
     * @return CF object that presents the incremental cluster feature of the current incremental cluster feature node.
     */
    CF createCFForNode() {
        CF newCF = new CF(this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, -1L, this.name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);

        for (CF cCF : this.cfList) {
            newCF.update(cCF);
            if (cCF.isNormalCluster())  {
                newCF.setNormalCluster(true);
            }
        }
        return newCF;
    }

    /**
     * The method splits the incremental cluster feature node.
     * @param cf CF object that present the parent of the incremental cluster feature node that will be split.
     * @param nCFNodes AtomicInteger object that presents atomic int value for counting the number of incremental cluster feature nodes in the tree.
     * @return CF array that presents two new incremental cluster features and accompanying incremental cluster feature nodes that are the results of splitting.
     */
    private CF[] splitCF(CF cf, AtomicInteger nCFNodes){

        CFNode oldCFNode = cf.getChild();
        ArrayList<CF> oldCFlist = cf.getChild().getCfList();
        CF[] farthestCFPair = findFarthestCFPair(oldCFlist);

        CF newCF1 = new CF(this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), this.name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
        CFNode newCFNode1 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, oldCFNode.isLeafStatus(), this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, this.name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
        newCF1.setChild(newCFNode1);

        CF newCF2 = new CF(this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), this.name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
        CFNode newCFNode2 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, oldCFNode.isLeafStatus(), this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, this.name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
        newCF2.setChild(newCFNode2);

        if (oldCFNode.isLeafStatus()) {
            CFNode previousCFLeaf = oldCFNode.getPreviousCFLeaf();
            CFNode nextCFLeaf = oldCFNode.getNextCFLeaf();

            if (previousCFLeaf != null) {
                previousCFLeaf.setNextCFLeaf(newCFNode1);
            }

            if (nextCFLeaf != null) {
                nextCFLeaf.setPreviousCFLeaf(newCFNode2);
            }

            newCFNode1.setPreviousCFLeaf(previousCFLeaf);
            newCFNode1.setNextCFLeaf(newCFNode2);
            newCFNode2.setPreviousCFLeaf(newCFNode1);
            newCFNode2.setNextCFLeaf(nextCFLeaf);
        }

        redistributeCFs(oldCFlist, farthestCFPair, newCF1, newCF2);

        this.cfList.remove(cf);
        this.cfList.add(newCF1);
        this.cfList.add(newCF2);
        nCFNodes.getAndIncrement();

        return new CF[]{newCF1, newCF2};
    }

    /**
     * The method finds the closest incremental cluster feature in the current incremental cluster feature node.
     * @param cf CF object that presents the incremental cluster feature for which we are searching the closest incremental cluster feature in the current incremental cluster feature node.
     * @return CF object that presents the closest incremental cluster feature.
     */
    private CF findClosestCF2(CF cf) {
        double minDistance = Double.MAX_VALUE;
        CF closestCF = null;
        for (CF currentCF : this.cfList) {
            double currentDistance = currentCF.distance(cf, this.distanceFunction);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                closestCF = currentCF;
            }
        }
        return closestCF;
    }
    /**
     * The method finds the closest incremental cluster feature in the current incremental cluster feature node by searching in the child incremental cluster feature nodes if available.
     * @param cf CF object that presents the incremental cluster feature for which we are searching the closest incremental cluster feature in the current incremental cluster feature node.
     * @return CF object that presents the closest incremental cluster feature.
     */
    private CF findClosestCF(CF cf) {
        double minDistance = Double.MAX_VALUE;
        CF closestCF = null;
        for (CF currentCF : this.cfList) {
            double currentDistance = Double.MAX_VALUE;
            if (currentCF.hasChild()) {
                double minDistanceInner = Double.MAX_VALUE;
                for (CF currentInnerCF : currentCF.getChild().getCfList()){
                    double currentDistanceInner = currentInnerCF.distance(cf, this.distanceFunction);

                    if (currentDistanceInner < minDistanceInner) {
                        minDistanceInner = currentDistanceInner;
                    }
                }
                currentDistance = minDistanceInner;
            }
            else {
                currentDistance = currentCF.distance(cf, this.distanceFunction);
            }
//            if (!currentCF.equals(cf) && currentDistance < minDistance) {
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                closestCF = currentCF;
            }
        }
        return closestCF;
    }

    /**
     * The method finds the two incremental cluster features that are the farthest apart.
     * @param cfList ArrayList&lt;CF&gt; object that present the list of incremental cluster features.
     * @return CF array that presents the two incremental cluster features that are the farthest apart.
     */
    CF[] findFarthestCFPair(ArrayList<CF> cfList) {
        if (cfList.size() < 2) {
            return null;
        }

        double maxDistance = -1;
        CF[] farthestPair = new CF[2];

        for (int i = 0; i < cfList.size() - 1; i++) {
            for (int j = i + 1; j < cfList.size(); j++) {
                CF cf1 = cfList.get(i);
                CF cf2 = cfList.get(j);

                double distance = cf1.distance(cf2, this.distanceFunction);
                if (distance > maxDistance) {
                    maxDistance = distance;
                    farthestPair[0] = cf1;
                    farthestPair[1] = cf2;
                }
            }
        }

        return farthestPair;
    }

    /**
     * The method redistributes incremental cluster features into the two new incremental cluster features that are the farthest apart.
     * @param oldCFList ArrayList&lt;CF&gt; object that present the list of incremental cluster features.
     * @param farthestCFPair CF array that presents the two incremental cluster features that are the farthest apart.
     * @param newCF1 CF object that presents a new empty incremental cluster feature.
     * @param newCF2 CF object that presents a new empty incremental cluster feature.
     */
    void redistributeCFs(ArrayList<CF> oldCFList, CF[] farthestCFPair, CF newCF1, CF newCF2){
        for (CF currentCF : oldCFList) {

            if (currentCF.equals(farthestCFPair[0])) {
                newCF1.addToChild(currentCF);
                newCF1.update(currentCF);
            }
            else if (currentCF.equals(farthestCFPair[1])) {
                newCF2.addToChild(currentCF);
                newCF2.update(currentCF);
            }
            else {
                double distanceCF1 = farthestCFPair[0].distance(currentCF, this.distanceFunction);
                double distanceCF2 = farthestCFPair[1].distance(currentCF, this.distanceFunction);

                if (distanceCF1 <= distanceCF2) {
                    newCF1.addToChild(currentCF);
                    newCF1.update(currentCF);
                }
                else {
                    newCF2.addToChild(currentCF);
                    newCF2.update(currentCF);
                }
            }
        }
    }

    /**
     * The method redistributes incremental cluster features into the two new incremental cluster features that are the closest.
     * @param oldCFNode1CFList ArrayList&lt;CF&gt; object that present the list of incremental cluster features.
     * @param oldCFNode2CFList ArrayList&lt;CF&gt; object that present the list of incremental cluster features.
     * @param closestCFPair CF array that presents the two incremental cluster features that are the closest.
     * @param newCF1 CF object that presents a new empty incremental cluster feature.
     * @param newCF2 CF object that presents a new empty incremental cluster feature.
     */
    private void redistributeCFs (ArrayList<CF> oldCFNode1CFList, ArrayList<CF> oldCFNode2CFList, CF[] closestCFPair, CF newCF1, CF newCF2) {
        ArrayList<CF> allCFList = new ArrayList<>();
        allCFList.addAll(oldCFNode1CFList);
        allCFList.addAll(oldCFNode2CFList);

        for (CF currentCF : allCFList) {
            double distanceCF1 = closestCFPair[0].distance(currentCF, this.distanceFunction);
            double distanceCF2 = closestCFPair[1].distance(currentCF, this.distanceFunction);

            if (distanceCF1 <= distanceCF2) {
                if (newCF1.getChild().getCfList().size() < this.maxNodeEntries) {
                    newCF1.addToChild(currentCF);
                    newCF1.update(currentCF);
                }
                else {
                    newCF2.addToChild(currentCF);
                    newCF2.update(currentCF);
                }
            }
            else{
                if (newCF2.getChild().getCfList().size() < this.maxNodeEntries) {
                    newCF2.addToChild(currentCF);
                    newCF2.update(currentCF);
                }
                else {
                    newCF1.addToChild(currentCF);
                    newCF1.update(currentCF);
                }
            }
        }
    }

    /**
     * The method insert all incremental cluster features from lists into a new incremental cluster feature.
     * @param oldCFNode1CFList ArrayList&lt;CF&gt; object that present the list of incremental cluster features.
     * @param oldCFNode2CFList ArrayList&lt;CF&gt; object that present the list of incremental cluster features.
     * @param newCF CF object that presents a new empty incremental cluster feature.
     */
    private void redistributeCFs(ArrayList<CF>  oldCFNode1CFList, ArrayList<CF>  oldCFNode2CFList, CF newCF) {
        ArrayList<CF> allCFs = new ArrayList<>();
        allCFs.addAll(oldCFNode1CFList);
        allCFs.addAll(oldCFNode2CFList);

        for (CF currentCF : allCFs) {
            newCF.addToChild(currentCF);
            newCF.update(currentCF);
        }
    }

    /**
     * This method refines the tree by redistributing the incremental cluster features into better discriminating pair of incremental cluster features.
     * @param splitPair CF array that presents the two incremental cluster features that will be used for the refinement of the tree.
     * @param nCFNodes AtomicInteger object that presents atomic int value for counting the number of incremental cluster feature nodes in the tree.
     */
    private void mergingRefinement(CF[] splitPair, AtomicInteger nCFNodes) {

        CF[] closestCFPair = findClosestCFPair(this.cfList);

        if (closestCFPair == null) {
            return;
        }

        if (equals(closestCFPair, splitPair)) {
            return;
        }

        CFNode oldCFNode1 = closestCFPair[0].getChild();
        CFNode oldCFNode2 = closestCFPair[1].getChild();

        ArrayList<CF> oldCFNode1CFList = oldCFNode1.getCfList();
        ArrayList<CF> oldCFNode2CFList = oldCFNode2.getCfList();

        if (oldCFNode1.isLeafStatus() != oldCFNode2.isLeafStatus()) {
            System.err.println("ERROR: Nodes at the same level must have same leaf status");
            System.exit(2);
        }

        if ((oldCFNode1CFList.size() + oldCFNode2CFList.size()) > this.maxNodeEntries) {
            CF newCF1 = new CF(this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), this.name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
            CFNode newCFNode1;
            newCFNode1 = oldCFNode1;
            newCFNode1.resetCFList();
            newCF1.setChild(newCFNode1);

            CF newCF2 = new CF(this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), this.name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
            CFNode newCFNode2;
            newCFNode2 = oldCFNode2;
            newCFNode2.resetCFList();
            newCF2.setChild(newCFNode2);

            redistributeCFs(oldCFNode1CFList, oldCFNode2CFList, closestCFPair, newCF1, newCF2);
            replaceClosestPairWithNewCFs(closestCFPair, newCF1, newCF2);
        }
        else {
            CF newCF = new CF(this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter.getAndIncrement(), this.name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
            CFNode newCFNode = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, oldCFNode1.isLeafStatus(), this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, this.name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
            newCF.setChild(newCFNode);

            redistributeCFs(oldCFNode1CFList, oldCFNode2CFList, newCF);

            if (oldCFNode1.isLeafStatus() && oldCFNode2.isLeafStatus()) {
                if (oldCFNode1.getPreviousCFLeaf() != null) {
                    oldCFNode1.getPreviousCFLeaf().setNextCFLeaf(newCFNode);
                }
                if (oldCFNode1.getNextCFLeaf() != null) {
                    oldCFNode1.getNextCFLeaf().setPreviousCFLeaf(newCFNode);
                }
                newCFNode.setPreviousCFLeaf(oldCFNode1.getPreviousCFLeaf());
                newCFNode.setNextCFLeaf(oldCFNode1.getNextCFLeaf());

                CFNode dummy = new CFNode(0, 0, 0, true, this.lambda, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold, this.idCounter, name, this.hitWindowLength, this.hitWindowProbability, this.anomalyWindowLength, this.anomalyWindowProbability, this.intraClusterThreshold);
                if (oldCFNode2.getPreviousCFLeaf() != null) {
                    oldCFNode2.getPreviousCFLeaf().setNextCFLeaf(dummy);
                }
                if (oldCFNode2.getNextCFLeaf() != null) {
                    oldCFNode2.getNextCFLeaf().setPreviousCFLeaf(dummy);
                }
                dummy.setPreviousCFLeaf(oldCFNode2.getPreviousCFLeaf());
                dummy.setNextCFLeaf(oldCFNode2.getNextCFLeaf());
            }

            replaceClosestPairWithNewCFs(closestCFPair, newCF);
            nCFNodes.getAndDecrement();
        }

    }

    /**
     * The method replaces the closest pair of incremental cluster features with two new incremental cluster features.
     * @param closestCFPair CF array that presents the two incremental cluster features that are the closest.
     * @param newCF1 CF object that presents a new empty incremental cluster feature.
     * @param newCF2 CF object that presents a new empty incremental cluster feature.
     */
    private void replaceClosestPairWithNewCFs(CF[] closestCFPair, CF newCF1, CF newCF2) {
        for (int i = 0; i < this.cfList.size(); i++) {
            if (this.cfList.get(i).equals(closestCFPair[0])) {
                this.cfList.set(i, newCF1);
            }
            else if (this.cfList.get(i).equals(closestCFPair[1])) {
                this.cfList.set(i, newCF2);
            }
        }
    }

    /**
     * The method replaces the closest pair of incremental cluster features with a new incremental cluster features.
     * @param closestCFPair CF array that presents the two incremental cluster features that are the closest.
     * @param newCF CF object that presents a new empty incremental cluster feature.
     */
    private void replaceClosestPairWithNewCFs(CF[] closestCFPair, CF newCF) {
        for (int i = 0; i < this.cfList.size(); i++) {
            if (this.cfList.get(i).equals(closestCFPair[0])) {
                this.cfList.set(i, newCF);
            }
            else if (this.cfList.get(i).equals(closestCFPair[1])) {
                this.cfList.remove(i);
            }
        }
    }

    /**
     * The method finds the closest two incremental cluster features from the list.
     * @param cfList ArrayList&lt;CF&gt; object that present the list of incremental cluster features.
     * @return CF array that presents the two incremental cluster features that are the closest.
     */
    CF[] findClosestCFPair(ArrayList<CF> cfList) {
        if (cfList.size() < 2) {
            return null;
        }

        double minDistance = Double.MAX_VALUE;
        CF[] closestPair = new CF[2];

        for (int i = 0; i < cfList.size() - 1; i++) {
            for (int j = i + 1; j < cfList.size(); j++) {
                CF cf1 = cfList.get(i);
                CF cf2 = cfList.get(j);

                double distance = cf1.distance(cf2, this.distanceFunction);
                if (distance < minDistance) {
                    closestPair[0] = cf1;
                    closestPair[1] = cf2;
                    minDistance = distance;
                }
            }
        }

        return closestPair;
    }

    /**
     * The method determines if two incremental cluster feature pairs are equal.
     * @param pair1 CF array that presents the two incremental cluster features.
     * @param pair2 CF array that presents the two incremental cluster features.
     * @return boolean value that present whether the two pairs are equal or not.
     */
    private boolean equals(CF[] pair1, CF[] pair2) {

        if (pair1[0].equals(pair2[0]) && pair1[1].equals(pair2[1])) {
            return true;
        }

        if (pair1[0].equals(pair2[1]) && pair1[1].equals(pair2[0])) {
            return true;
        }

        return false;
    }

    /**
     * The method returns a flag that determine whether the current incremental cluster feature node is a temporary or not.
     * @return boolean value that presents whether the current incremental cluster feature node is a temporary or not.
     */
    public boolean isDummy() {
        return (this.maxNodeEntries == 0 && this.distanceThreshold == 0 && this.cfList.size() == 0 && (this.previousCFLeaf != null || this.nextCFLeaf != null));
    }

    /**
     * The method reset the current incremental cluster feature node child incremental cluster feature list.
     */
    private void resetCFList() {
        this.cfList = new ArrayList<CF>();
    }

    /**
     * The method returns the current incremental cluster feature node child incremental cluster feature list.
     * @return ArrayList&lt;CF&gt; object that present the current incremental cluster feature node child incremental cluster feature list.
     */
    public ArrayList<CF> getCfList() {
        return cfList;
    }

    /**
     * The method returns a flag that determines whether the current incremental cluster feature node is a leaf or not.
     * @return boolean value that presents whether the current incremental cluster feature node is a leaf or not.
     */
    boolean isLeafStatus() {
        return leafStatus;
    }

    /**
     * The method returns the next leaf incremental cluster feature node.
     * @return CFNode object that presents the next leaf incremental cluster feature node.
     */
    public CFNode getNextCFLeaf() {
        return nextCFLeaf;
    }

    /**
     * The method returns the previous leaf incremental cluster feature node.
     * @return CFNode object that presents the previous leaf incremental cluster feature node.
     */
    private CFNode getPreviousCFLeaf() {
        return previousCFLeaf;
    }

    /**
     * The method sets the pointer for the next leaf incremental cluster feature node to the input parameter specified.
     * @param nextCFLeaf CFNode object that presents the next leaf incremental cluster feature node.
     */
    void setNextCFLeaf(CFNode nextCFLeaf) {
        this.nextCFLeaf = nextCFLeaf;
    }

    /**
     * The method sets the pointer for the previous leaf incremental cluster feature node to the input parameter specified.
     * @param previousCFLeaf CFNode object that presents the previous leaf incremental cluster feature node.
     */
    void setPreviousCFLeaf(CFNode previousCFLeaf) {
        this.previousCFLeaf = previousCFLeaf;
    }

    /**
     * The method prints the current incremental cluster feature node in a print friendly format.
     * @param prefix String object that presents the initial offset of the print-out.
     * @param isTail boolean value that presents whether the current incremental cluster feature node is the last in the parent incremental cluster feature node.
     * @param writer PrintWriter object that presents the text output stream.
     * @param nLeafCFs AtomicInteger object that presents atomic int value for counting the number of leaf incremental cluster features in the tree.
     * @param nLeafCFsSum AtomicInteger object that presents atomic int value for counting the linear sum of number of leaf incremental cluster features in the tree.
     * @param nLeafCFsSquared AtomicInteger object that presents atomic int value for counting the square sum of number of leaf incremental cluster features in the tree.
     * @param timestamp LocalDateTime object that present the timestamp to be printed out.
     * @param nLeafCFsHSum MutableDouble object that presents atomic double value for counting the linear sum of one divided with number of leaf incremental cluster features in the tree.
     */
    public void print(String prefix, boolean isTail, PrintWriter writer, AtomicInteger nLeafCFs, AtomicInteger nLeafCFsSum, AtomicInteger nLeafCFsSquared, LocalDateTime timestamp, MutableDouble nLeafCFsHSum) {

        int counter = 1;
        for (CF entrie : this.cfList) {

            StringBuilder centroidString = new StringBuilder("[");
            double[] sum = entrie.getLinearSum().clone();
            for (int i = 0; i < sum.length; i++) {
//                centroidString.append(String.format(Locale.US, "%.2f", sum[i] / entrie.getW()));
                centroidString.append(sum[i] / entrie.getW());
                if (i != sum.length - 1) {
                    centroidString.append(", ");
                }
            }
            centroidString.append("]");

            if (counter == this.cfList.size()) {
                isTail = true;
            }

            String isLeaf = "";
            if (this.leafStatus) {
                isLeaf = " L";
            }

            String isNormal = "";
            if (entrie.getWindows() != null) {
                isNormal = " A";
                if (entrie.isNormalCluster2(nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum)) {
                    isNormal = " N";
                }
                isNormal += " CT: " + entrie.getChangeTime();
                isNormal += " TS: " + entrie.getTsDifference(timestamp);
                isNormal += " nW: " + entrie.getWindows().size();
            }

            writer.println(prefix + (isTail ? "└── " : "├── ") + "N=" + entrie.getN() + " C=" + centroidString.toString() + isLeaf + isNormal);

            if (entrie.hasChild()) {

                entrie.getChild().print(prefix + (isTail ? "    " : "│   "), false, writer, nLeafCFs, nLeafCFsSum, nLeafCFsSquared, timestamp, nLeafCFsHSum);
            }
            counter++;
        }

    }
}
