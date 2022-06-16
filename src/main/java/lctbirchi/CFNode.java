package lctbirchi;

import utils.HitWindow;
import utils.MutableDouble;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.temporal.ChronoUnit.SECONDS;

public class CFNode {

    private boolean leafStatus;
    private double normalClusterThreshold;
    private ArrayList<CF> cfList;
    private CFNode nextCFLeaf;
    private CFNode previousCFLeaf;
    private double delta;
    private int checkStep;
    private int maxBins;
    private int sizeOfBin;
    private int maxNumberOfNodesLB;
    private int maxNumberOfNodesLC;
    private int distanceFunction;
    private int maxNodeEntries;
    private double distanceThresholdLB;
    private double distanceThresholdLC;
    private CF parent;

    CFNode(int maxNodeEntries, int distanceFunction, double distanceThresholdLB, double distanceThresholdLC, boolean leafStatus, int maxNumberOfNodesLB, int maxNumberOfNodesLC, double delta, int checkStep, int maxBins, int sizeOfBin, double normalClusterThreshold) {
        this.leafStatus = leafStatus;
        this.distanceFunction = distanceFunction;
        this.maxNodeEntries = maxNodeEntries;
        this.distanceThresholdLB = distanceThresholdLB;
        this.distanceThresholdLC = distanceThresholdLC;
        this.maxNumberOfNodesLB = maxNumberOfNodesLB;
        this.maxNumberOfNodesLC = maxNumberOfNodesLC;
        this.cfList = new ArrayList<>();
        this.nextCFLeaf = null;
        this.previousCFLeaf = null;
        this.delta = delta;
        this.checkStep = checkStep;
        this.maxBins = maxBins;
        this.sizeOfBin = sizeOfBin;
        this.normalClusterThreshold = normalClusterThreshold;
        this.parent = null;
    }

    SplitChangeDifference insertCF(CF cf, AtomicInteger nCFNodes, AtomicInteger nLeafCFs, AtomicInteger nLeafCFsSum, AtomicInteger nLeafCFsSquared, boolean insertingNew, MutableDouble nLeafCFsHSum, boolean anomaly, ArrayList<HitWindow> interHitWindows, boolean[] interFlags, int[] interLastSwitch, int hitInterWindowLength, double hitInterWindowProbability, int anomalyWindowLength, double anomalyWindowProbability, int counter){

//        System.out.println(name);

        if (this.cfList.size() == 0) {
            this.cfList.add(cf);
            cf.setParent(this);
            nCFNodes.getAndIncrement();
            nLeafCFs.getAndIncrement();
            nLeafCFsSum.getAndAdd(cf.getN());
            nLeafCFsHSum.addValue(1.0/cf.getN());
            nLeafCFsSquared.getAndAdd(cf.getN() * cf.getN());
//            return false;
            return new SplitChangeDifference(false, cf,null, !cf.isNormalCluster(), null, null);
        }

        CF closestCF = findClosestCF(cf);

        if (closestCF.hasChild()){
            SplitChangeDifference splitChangeDifference = closestCF.getChild().insertCF(cf, nCFNodes, nLeafCFs, nLeafCFsSum, nLeafCFsSquared, insertingNew, nLeafCFsHSum, anomaly, interHitWindows, interFlags, interLastSwitch, hitInterWindowLength, hitInterWindowProbability, anomalyWindowLength, anomalyWindowProbability, counter);
            if (!splitChangeDifference.isSplit()) {
                closestCF.update(cf, false);
//                closestCF.getTree().checkZeroCFs();
//                closestCF.getTree().checkZeroCFs2(closestCF.getTree().getRoot());
//                return false;
                splitChangeDifference.setSplit(false);
                return splitChangeDifference;
            }
            else{
                CF[] splitPair = splitCF(closestCF, nCFNodes);

                if (this.cfList.size() > this.maxNodeEntries) {
//                    return true;
                    splitChangeDifference.setSplit(true);
                    return splitChangeDifference;
                }
                else {
                    mergingRefinement(splitPair, nCFNodes);
//                    return false;
                    splitChangeDifference.setSplit(false);
                    return splitChangeDifference;
                }
            }
        }
//        else if (closestCF.isWithinThreshold(cf, this.distanceThreshold, this.distanceFunction)) {
        else if (closestCF.isWithinThreshold(cf, this.distanceThresholdLC,  CFTree.D0_DIST)) {
            return addToCF(cf, closestCF, nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum, anomaly, insertingNew);
        }
        else if (this.cfList.size() < this.maxNodeEntries) {
            return addToCFNode(cf, false, nCFNodes, nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum, interHitWindows, interFlags, interLastSwitch, hitInterWindowLength, hitInterWindowProbability, anomalyWindowLength, anomalyWindowProbability, counter, anomaly);
        }
        else {
            return addToCFNode(cf, true, nCFNodes, nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum, interHitWindows, interFlags, interLastSwitch, hitInterWindowLength, hitInterWindowProbability, anomalyWindowLength, anomalyWindowProbability, counter, anomaly);
        }
    }

    SplitChangeDifference addToCF(CF cf, CF closestCF, AtomicInteger nLeafCFs, AtomicInteger nLeafCFsSum, AtomicInteger nLeafCFsSquared, MutableDouble nLeafCFsHSum, boolean anomaly, boolean insertingNew) {

        // Decrement the CF counters with the old value of closestCF.
        nLeafCFsSum.getAndSet(nLeafCFsSum.get() - closestCF.getN());
        nLeafCFsHSum.addValue(-1.0/closestCF.getN());
        nLeafCFsSquared.getAndSet(nLeafCFsSquared.get() - (closestCF.getN() * closestCF.getN()));

        // Update the closestCF with CF and get cluster changes and intra cluster anomaly flags.
        byte[] changeDifferences = closestCF.update(cf, anomaly);
//        closestCF.getTree().checkZeroCFs();
//        closestCF.getTree().checkZeroCFs2(closestCF.getTree().getRoot());

        // Increment the CF counters with the new value of closestCF.
        nLeafCFsSum.getAndAdd(closestCF.getN());
        nLeafCFsHSum.addValue(1.0/closestCF.getN());
        nLeafCFsSquared.getAndAdd(closestCF.getN() * closestCF.getN());

//        System.out.println( closestCF.getCreationTimestamp());
//        System.out.println( cf.getTimestamp());
//        System.out.println( SECONDS.between(closestCF.getCreationTimestamp(), cf.getTimestamp()));

        // Check for cluster anomaly.
        boolean clusterAnomaly = false;
        if (insertingNew && closestCF.getN() > 2 && SECONDS.between(closestCF.getCreationTimestamp(), cf.getTimestamp()) > 1 && !closestCF.isNormalCluster2(nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum)) {
            clusterAnomaly = true;
        }
//        if (insertingNew && closestCF.getN() > 2 && SECONDS.between(closestCF.getCreationTimestamp(), cf.getTimestamp()) > 1 && closestCF.isNormalCluster2(nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum)) {
////            System.out.println(SECONDS.between(closestCF.getCreationTimestamp(), cf.getTimestamp()));
////        if (insertingNew && nLeafCFsSum.get() > 100 && closestCF.isNormalCluster2(nLeafCFs, nLeafCFsSum, nLeafCFsSquared, nLeafCFsHSum)) {
//            closestCF.setNormalCluster(true);
//            closestCF.setChangeTime(SECONDS.between(closestCF.getCreationTimestamp(), cf.getTimestamp()));
//        }
//        else {
//            closestCF.setNormalCluster(false);
//        }

        // Check if cluster is already anomalous.
//        boolean clusterAnomaly = false;
////        if (closestCF.getN() > 100) {
//            clusterAnomaly = !closestCF.isNormalCluster();
////        }

//        byte[] interClusterAnomaly = null;
//        if (cf.isAnomalyCluster()) {
//            interClusterAnomaly = new byte[]{1};
//        } else {
//            interClusterAnomaly = new byte[]{0};
//        }

        return new SplitChangeDifference(false, closestCF, changeDifferences, clusterAnomaly, null, null);
    }

    SplitChangeDifference addToCFNode(CF cf, boolean split, AtomicInteger nCFNodes, AtomicInteger nLeafCFs, AtomicInteger nLeafCFsSum, AtomicInteger nLeafCFsSquared, MutableDouble nLeafCFsHSum, ArrayList<HitWindow> interHitWindows, boolean[] interFlags, int[] interLastSwitch, int hitInterWindowLength, double hitInterWindowProbability, int anomalyWindowLength, double anomalyWindowProbability, int counter, boolean anomaly) {
        // Add new CF to the leaf CF Node.
        this.cfList.add(cf);
        cf.setParent(this);

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
                    interFlags = new boolean[dummyCentroid.length];
                    interLastSwitch = new int[dummyCentroid.length];
                    interHitWindows = new ArrayList<>();
                    for (int j = 0; j < dummyCentroid.length; j++) {
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

//                    int newSize = counter - interLastSwitch[i];
//                    if (newSize >= anomalyWindowLength) {

                        // Check if detection frequency is then large enough on a smaller window.
                        if (interHitWindows.get(i).newestSinceSize(counter, anomalyWindowLength) >= (anomalyWindowLength * anomalyWindowProbability)) {
//                            interFlags[i] = !interFlags[i];
                            interFlags[i] = true;
                            interLastSwitch[i] = counter;
                        }
//                    }
                }
                // Otherwise negate the values in inter flags if they are true.
                else {
                    if (interFlags[i]){
                        interFlags[i] = false;
                    }
//                    if (interFlags[i]){
//                        interFlags[i] = !interFlags[i];
//                        interLastSwitch[i] = counter;
//                    }
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

        byte[] intraClusterAnomaly = null;
        if (anomaly) {
            cf.setLevel1Anomaly(true);
            cf.setLevel1AnomalyTimestamp(cf.getTimestamp());
            intraClusterAnomaly = new byte[3];
            intraClusterAnomaly[2] = 1;
        }

        return new SplitChangeDifference(split, cf, intraClusterAnomaly, false, null, interClusterAnomaly);
    }

    CF createCFForNode() {
        CF newCF = new CF(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);

        for (CF cCF : this.cfList) {
            newCF.update(cCF, false);
            if (cCF.isNormalCluster())  {
                newCF.setNormalCluster(true);
            }
        }
        return newCF;
    }

    private CF[] splitCF(CF cf, AtomicInteger nCFNodes){

        CFNode oldCFNode = cf.getChild();
        ArrayList<CF> oldCFlist = cf.getChild().getCfList();
        CF[] farthestCFPair = findFarthestCFPair(oldCFlist);

        CF newCF1 = new CF(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        CFNode newCFNode1 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, oldCFNode.isLeafStatus(), this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        newCF1.setChild(newCFNode1);
        newCFNode1.setParent(newCF1);

        CF newCF2 = new CF(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        CFNode newCFNode2 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, oldCFNode.isLeafStatus(), this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
        newCF2.setChild(newCFNode2);
        newCFNode2.setParent(newCF2);

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
        newCF1.setParent(this);
        newCF2.setParent(this);

        return new CF[]{newCF1, newCF2};
    }

    public void setParent(CF newCF1) {
        this.parent = newCF1;
    }

    private CF findClosestCF(CF cf) {
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

    private CF findDeepClosestCF(CF cf) {
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

    void redistributeCFs(ArrayList<CF> oldCFList, CF[] farthestCFPair, CF newCF1, CF newCF2){
        for (CF currentCF : oldCFList) {

            if (currentCF.equals(farthestCFPair[0])) {
                newCF1.addToChild(currentCF);
                newCF1.update(currentCF, false);
                currentCF.setParent(newCF1.getChild());
            }
            else if (currentCF.equals(farthestCFPair[1])) {
                newCF2.addToChild(currentCF);
                newCF2.update(currentCF, false);
                currentCF.setParent(newCF2.getChild());
            }
            else {
                double distanceCF1 = farthestCFPair[0].distance(currentCF, this.distanceFunction);
                double distanceCF2 = farthestCFPair[1].distance(currentCF, this.distanceFunction);

                if (distanceCF1 <= distanceCF2) {
                    newCF1.addToChild(currentCF);
                    newCF1.update(currentCF, false);
                    currentCF.setParent(newCF1.getChild());
                }
                else {
                    newCF2.addToChild(currentCF);
                    newCF2.update(currentCF, false);
                    currentCF.setParent(newCF2.getChild());
                }
            }

//            double distanceCF1 = farthestCFPair[0].distance(currentCF, this.distanceFunction);
//            double distanceCF2 = farthestCFPair[1].distance(currentCF, this.distanceFunction);
//
//            if (distanceCF1 <= distanceCF2) {
//                newCF1.addToChild(currentCF);
//                newCF1.update(currentCF);
//            }
//            else {
//                newCF2.addToChild(currentCF);
//                newCF2.update(currentCF);
//            }
        }
//        if (newCF1.getLinearSum() == null || newCF2.getLinearSum() == null) {
//            System.out.println();
//            for (CF currentCF : oldCFList) {
//                if (currentCF.equals(farthestCFPair[0])) {
//                    newCF1.addToChild(currentCF);
//                    newCF1.update(currentCF);
//                }
//                else if (currentCF.equals(farthestCFPair[1])) {
//                    newCF2.addToChild(currentCF);
//                    newCF2.update(currentCF);
//                }
//                else {
//                    double distanceCF1 = farthestCFPair[0].distance(currentCF, this.distanceFunction);
//                    double distanceCF2 = farthestCFPair[1].distance(currentCF, this.distanceFunction);
//
//                    if (distanceCF1 <= distanceCF2) {
//                        System.out.println();
//                    }
//                    else {
//                        System.out.println();
//                    }
//                }
//            }
//        }
    }

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
                    newCF1.update(currentCF, false);
                    currentCF.setParent(newCF1.getChild());
                }
                else {
                    newCF2.addToChild(currentCF);
                    newCF2.update(currentCF, false);
                    currentCF.setParent(newCF2.getChild());
                }
            }
            else{
                if (newCF2.getChild().getCfList().size() < this.maxNodeEntries) {
                    newCF2.addToChild(currentCF);
                    newCF2.update(currentCF, false);
                    currentCF.setParent(newCF2.getChild());
                }
                else {
                    newCF1.addToChild(currentCF);
                    newCF1.update(currentCF, false);
                    currentCF.setParent(newCF1.getChild());
                }
            }
        }
    }

    private void redistributeCFs(ArrayList<CF>  oldCFNode1CFList, ArrayList<CF>  oldCFNode2CFList, CF newCF) {
        ArrayList<CF> allCFs = new ArrayList<>();
        allCFs.addAll(oldCFNode1CFList);
        allCFs.addAll(oldCFNode2CFList);

        for (CF currentCF : allCFs) {
            newCF.addToChild(currentCF);
            newCF.update(currentCF, false);
            currentCF.setParent(newCF.getChild());
        }
    }

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
            CF newCF1 = new CF(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
            CFNode newCFNode1;
            newCFNode1 = oldCFNode1;
            newCFNode1.resetCFList();
            newCF1.setChild(newCFNode1);
            newCFNode1.setParent(newCF1);

            CF newCF2 = new CF(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
            CFNode newCFNode2;
            newCFNode2 = oldCFNode2;
            newCFNode2.resetCFList();
            newCF2.setChild(newCFNode2);
            newCFNode2.setParent(newCF2);

            redistributeCFs(oldCFNode1CFList, oldCFNode2CFList, closestCFPair, newCF1, newCF2);
            replaceClosestPairWithNewCFs(closestCFPair, newCF1, newCF2);
        }
        else {
            CF newCF = new CF(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
            CFNode newCFNode = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThresholdLB, this.distanceThresholdLC, oldCFNode1.isLeafStatus(), this.maxNumberOfNodesLB, this.maxNumberOfNodesLC, this.delta, this.checkStep, this.maxBins, this.sizeOfBin, this.normalClusterThreshold);
            newCF.setChild(newCFNode);
            newCFNode.setParent(newCF);

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

                CFNode dummy = new CFNode(0, 0, 0, 0, true, 0, 0, 0, 0, 0, 0, 0);

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

    private void replaceClosestPairWithNewCFs(CF[] closestCFPair, CF newCF1, CF newCF2) {
        for (int i = 0; i < this.cfList.size(); i++) {
            if (this.cfList.get(i).equals(closestCFPair[0])) {
                this.cfList.set(i, newCF1);
                newCF1.setParent(this);
            }
            else if (this.cfList.get(i).equals(closestCFPair[1])) {
                this.cfList.set(i, newCF2);
                newCF2.setParent(this);
            }
        }
    }

    private void replaceClosestPairWithNewCFs(CF[] closestCFPair, CF newCF) {
        for (int i = 0; i < this.cfList.size(); i++) {
            if (this.cfList.get(i).equals(closestCFPair[0])) {
                this.cfList.set(i, newCF);
                newCF.setParent(this);
            }
            else if (this.cfList.get(i).equals(closestCFPair[1])) {
                this.cfList.remove(i);
            }
        }
    }

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

    private boolean equals(CF[] pair1, CF[] pair2) {

        if (pair1[0].equals(pair2[0]) && pair1[1].equals(pair2[1])) {
            return true;
        }

        if (pair1[0].equals(pair2[1]) && pair1[1].equals(pair2[0])) {
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CFNode)) return false;
        CFNode cfNode = (CFNode) o;

        if (this.leafStatus != cfNode.isLeafStatus()) {
            return false;
        }

        if (this.normalClusterThreshold != cfNode.getNormalClusterThreshold()) {
            return false;
        }

        ArrayList<CF> cfNodeList = cfNode.getCfList();
        if (this.cfList.size() != cfNodeList.size()) {
            return false;
        }
//
//        for (int i = 0; i < this.cfList.size(); i++) {
//            if (!this.cfList.get(i).equals(cfNodeList.get(i))) {
//                return false;
//            }
//        }

        if (this.nextCFLeaf != null && cfNode.getNextCFLeaf() == null) {
            return false;
        }

        if (this.nextCFLeaf == null && cfNode.getNextCFLeaf() != null) {
            return false;
        }

//        if (this.nextCFLeaf != null && !this.nextCFLeaf.equals(cfNode.getNextCFLeaf())) {
//            return false;
//        }

        if (this.previousCFLeaf != null && cfNode.getPreviousCFLeaf() == null) {
            return false;
        }

        if (this.previousCFLeaf == null && cfNode.getPreviousCFLeaf() != null) {
            return false;
        }

//        if (this.previousCFLeaf != null && !this.previousCFLeaf.equals(cfNode.getPreviousCFLeaf())) {
//            return false;
//        }

        if (this.delta != cfNode.getDelta()) {
            return false;
        }

        if (this.checkStep != cfNode.getCheckStep()) {
            return false;
        }

        if (this.maxBins != cfNode.getMaxBins()) {
            return false;
        }

        if (this.sizeOfBin != cfNode.getSizeOfBin()) {
            return false;
        }

        if (this.maxNumberOfNodesLB != cfNode.getMaxNumberOfNodesLB()) {
            return false;
        }

        if (this.maxNumberOfNodesLC != cfNode.getMaxNumberOfNodesLC()) {
            return false;
        }

        if (this.distanceFunction != cfNode.getDistanceFunction()) {
            return false;
        }

        if (this.maxNodeEntries != cfNode.getMaxNodeEntries()) {
            return false;
        }

        if (this.distanceThresholdLB != cfNode.getDistanceThresholdLB()) {
            return false;
        }

        if (this.distanceThresholdLC != cfNode.getDistanceThresholdLC()) {
            return false;
        }

        if (this.parent != null && cfNode.getParent() == null) {
            return false;
        }

        if (this.parent == null && cfNode.getParent() != null) {
            return false;
        }

//        if (this.parent != null && !this.parent.equals(cfNode.getParent())) {
//            return false;
//        }

        return true;
    }

    public boolean isDummy() {
        return (this.maxNodeEntries == 0 && this.distanceThresholdLB == 0 && this.distanceThresholdLC == 0 && this.cfList.size() == 0 && (this.previousCFLeaf != null || this.nextCFLeaf != null));
    }

    private void resetCFList() {
        this.cfList = new ArrayList<CF>();
    }

    public ArrayList<CF> getCfList() {
        return cfList;
    }

    boolean isLeafStatus() {
        return leafStatus;
    }

    public CFNode getNextCFLeaf() {
        return nextCFLeaf;
    }

    CFNode getPreviousCFLeaf() {
        return previousCFLeaf;
    }

    void setNextCFLeaf(CFNode nextCFLeaf) {
        this.nextCFLeaf = nextCFLeaf;
    }

    void setPreviousCFLeaf(CFNode previousCFLeaf) {
        this.previousCFLeaf = previousCFLeaf;
    }

    public void print(String prefix, boolean isTail, PrintWriter writer, AtomicInteger nLeafCFs, AtomicInteger nLeafCFsSum, AtomicInteger nLeafCFsSquared, LocalDateTime timestamp, MutableDouble nLeafCFsHSum) {

        int counter = 1;
        for (CF entrie : this.cfList) {

            StringBuilder centroidString = new StringBuilder("[");
            double[] sum = entrie.getTree().getLinearSum().clone();
            for (int i = 0; i < sum.length; i++) {
//                centroidString.append(String.format(Locale.US, "%.2f", sum[i] / entrie.getW()));
                centroidString.append(sum[i] / entrie.getTree().getN());
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

            writer.println(prefix + (isTail ? "└── " : "├── ") + "N=" + entrie.getN() + "_" + entrie.getTree().getN() + " " + entrie.getStoredIDs().toString() + " C=" + centroidString.toString() + isLeaf + isNormal);

            if (entrie.hasChild()) {

                entrie.getChild().print(prefix + (isTail ? "    " : "│   "), false, writer, nLeafCFs, nLeafCFsSum, nLeafCFsSquared, timestamp, nLeafCFsHSum);
            }
            counter++;
        }


    }

    public double getNormalClusterThreshold() {
        return normalClusterThreshold;
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

    public int getMaxNumberOfNodesLB() {
        return maxNumberOfNodesLB;
    }

    public int getMaxNumberOfNodesLC() {
        return maxNumberOfNodesLC;
    }

    public int getDistanceFunction() {
        return distanceFunction;
    }

    public int getMaxNodeEntries() {
        return maxNodeEntries;
    }

    public double getDistanceThresholdLB() {
        return distanceThresholdLB;
    }

    public double getDistanceThresholdLC() {
        return distanceThresholdLC;
    }

    public CF getParent() {
        return parent;
    }
}
