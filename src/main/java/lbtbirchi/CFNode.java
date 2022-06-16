package lbtbirchi;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class CFNode {

    private boolean leafStatus;
    private int maxNodeEntries;
    private int distanceFunction;
    private double distanceThreshold;
    private ArrayList<CF> cfList;
    private CFNode nextCFLeaf;
    private CFNode previousCFLeaf;
    private CF parent;

    CFNode(int maxNodeEntries, int distanceFunction, double distanceThreshold, boolean leafStatus) {
        this.leafStatus = leafStatus;
        this.maxNodeEntries = maxNodeEntries;
        this.distanceFunction = distanceFunction;
        this.distanceThreshold = distanceThreshold;
        this.cfList = new ArrayList<>();
        this.nextCFLeaf = null;
        this.previousCFLeaf = null;
        this.parent = null;
    }

    SplitAndCF insertCF(CF cf, AtomicInteger nCFNodes){

//        System.out.println(name);

        if (this.cfList.size() == 0) {
            this.cfList.add(cf);
            cf.setParent(this);
            nCFNodes.getAndIncrement();
            return new SplitAndCF(false, cf);
        }

        CF closestCF = findClosestCF(cf);

        if (closestCF.hasChild()){
            SplitAndCF splitAndCF = closestCF.getChild().insertCF(cf, nCFNodes);
            if (!splitAndCF.isSplit()) {
                closestCF.update(cf);
                return new SplitAndCF(false, splitAndCF.getCf());
            }
            else{
                CF[] splitPair = splitCF(closestCF, nCFNodes);

                if (this.cfList.size() > this.maxNodeEntries) {
                    return new SplitAndCF(true, splitAndCF.getCf());
                }
                else {
                    mergingRefinement(splitPair, nCFNodes);
                    return new SplitAndCF(false, splitAndCF.getCf());
                }
            }
        }
//        else if (closestCF.isWithinThreshold(cf, this.distanceThreshold, this.distanceFunction)) {
        else if (closestCF.isWithinThreshold(cf, this.distanceThreshold, CFTree.D5_DIST)) {
            closestCF.update(cf);
            return new SplitAndCF(false, closestCF);
        }
        else if (this.cfList.size() < this.maxNodeEntries) {
            this.cfList.add(cf);
            cf.setParent(this);
            nCFNodes.getAndIncrement();
            return new SplitAndCF(false, closestCF);
        }
        else {
            this.cfList.add(cf);
            cf.setParent(this);
            nCFNodes.getAndIncrement();
            return new SplitAndCF(true, closestCF);
        }
    }

    double calculateDistance(CF cf) {
        if (this.cfList.size() == 0) {
            return 1.0;
        }

        double result;

        CF closestCF = findClosestCF(cf);
        if (closestCF.hasChild()){
            result = closestCF.getChild().calculateDistance(cf);
        }
        /// Special Distance ///
        else {
            result = closestCF.distance(cf, CFTree.D0_DIST);
        }

        return result;
    }

    private CF[] splitCF(CF cf, AtomicInteger nCFNodes){

        CFNode oldCFNode = cf.getChild();
        ArrayList<CF> oldCFlist = cf.getChild().getCfList();
        CF[] farthestCFPair = findFarthestCFPair(oldCFlist);

        CF newCF1 = new CF();
        CFNode newCFNode1 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, oldCFNode.isLeafStatus());
        newCF1.setChild(newCFNode1);
        newCFNode1.setParent(newCF1);

        CF newCF2 = new CF();
        CFNode newCFNode2 = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, oldCFNode.isLeafStatus());
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
        newCF1.setParent(this);
        newCF2.setParent(this);
        nCFNodes.getAndIncrement();

        return new CF[]{newCF1, newCF2};
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
                currentCF.setParent(newCF1.getChild());
                newCF1.update(currentCF);
            }
            else if (currentCF.equals(farthestCFPair[1])) {
                newCF2.addToChild(currentCF);
                currentCF.setParent(newCF2.getChild());
                newCF2.update(currentCF);
            }
            else {
                double distanceCF1 = farthestCFPair[0].distance(currentCF, this.distanceFunction);
                double distanceCF2 = farthestCFPair[1].distance(currentCF, this.distanceFunction);

                if (distanceCF1 <= distanceCF2) {
                    newCF1.addToChild(currentCF);
                    currentCF.setParent(newCF1.getChild());
                    newCF1.update(currentCF);
                }
                else {
                    newCF2.addToChild(currentCF);
                    currentCF.setParent(newCF2.getChild());
                    newCF2.update(currentCF);
                }
            }
        }
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
                    currentCF.setParent(newCF1.getChild());
                    newCF1.update(currentCF);
                }
                else {
                    newCF2.addToChild(currentCF);
                    currentCF.setParent(newCF2.getChild());
                    newCF2.update(currentCF);
                }
            }
            else{
                if (newCF2.getChild().getCfList().size() < this.maxNodeEntries) {
                    newCF2.addToChild(currentCF);
                    currentCF.setParent(newCF2.getChild());
                    newCF2.update(currentCF);
                }
                else {
                    newCF1.addToChild(currentCF);
                    currentCF.setParent(newCF1.getChild());
                    newCF1.update(currentCF);
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
            newCF.update(currentCF);
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
            CF newCF1 = new CF();
            CFNode newCFNode1;
            newCFNode1 = oldCFNode1;
            newCFNode1.resetCFList();
            newCF1.setChild(newCFNode1);
            newCFNode1.setParent(newCF1);

            CF newCF2 = new CF();
            CFNode newCFNode2;
            newCFNode2 = oldCFNode2;
            newCFNode2.resetCFList();
            newCF2.setChild(newCFNode2);
            newCFNode2.setParent(newCF2);

            redistributeCFs(oldCFNode1CFList, oldCFNode2CFList, closestCFPair, newCF1, newCF2);
            replaceClosestPairWithNewCFs(closestCFPair, newCF1, newCF2);
        }
        else {
            CF newCF = new CF();
            CFNode newCFNode = new CFNode(this.maxNodeEntries, this.distanceFunction, this.distanceThreshold, oldCFNode1.isLeafStatus());
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

                CFNode dummy = new CFNode(0, 0, 0, true);
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

        return pair1[0].equals(pair2[1]) && pair1[1].equals(pair2[0]);
    }

    public boolean isDummy() {
        return (this.maxNodeEntries == 0 && this.distanceThreshold == 0 && this.cfList.size() == 0 && (this.previousCFLeaf != null || this.nextCFLeaf != null));
    }

    @Override
    public boolean equals(Object o) {
        if (this.getClass() != o.getClass()) {
            return false;
        }

        CFNode cfNode = (CFNode) o;

        if (this.leafStatus != cfNode.isLeafStatus()) {
            return false;
        }

        if (this.maxNodeEntries != cfNode.getMaxNodeEntries()) {
            return false;
        }

        if (this.distanceFunction != cfNode.getDistanceFunction()) {
            return false;
        }

        if (this.distanceThreshold != cfNode.getDistanceThreshold()) {
            return false;
        }

        if (this.nextCFLeaf != null && cfNode.getNextCFLeaf() == null) {
            return false;
        }

        if (this.nextCFLeaf == null && cfNode.getNextCFLeaf() != null) {
            return false;
        }

//        if (this.nextCFLeaf != null && cfNode.getNextCFLeaf() != null && !this.nextCFLeaf.equals(cfNode.getNextCFLeaf())) {
//            return false;
//        }

        if (this.previousCFLeaf != null && cfNode.getPreviousCFLeaf() == null) {
            return false;
        }

        if (this.previousCFLeaf == null && cfNode.getPreviousCFLeaf() != null) {
            return false;
        }

//        if (this.previousCFLeaf != null && cfNode.getPreviousCFLeaf() != null && !this.previousCFLeaf.equals(cfNode.getPreviousCFLeaf())) {
//            return false;
//        }

        if (this.parent != null && cfNode.getParent() == null) {
            return false;
        }

        if (this.parent == null && cfNode.getParent() != null) {
            return false;
        }

//        if (this.parent != null && cfNode.getParent() != null && !this.parent.equals(cfNode.getParent())) {
//            return false;
//        }

        ArrayList<CF> cfNodeList = cfNode.getCfList();
        if (this.cfList.size() != cfNodeList.size()) {
            return false;
        }

//        for (int i = 0; i < this.cfList.size(); i++) {
//            if (!this.cfList.get(i).equals(cfNodeList.get(i))) {
//                return false;
//            }
//        }

        return true;
    }


    private void resetCFList() {
        this.cfList = new ArrayList<CF>();
    }

    public ArrayList<CF> getCfList() {
        return cfList;
    }

    public int getMaxNodeEntries() {
        return maxNodeEntries;
    }

    public int getDistanceFunction() {
        return distanceFunction;
    }

    public double getDistanceThreshold() {
        return distanceThreshold;
    }

    public CF getParent() {
        return parent;
    }

    public void setParent(CF parent) {
        this.parent = parent;
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

    public void print(String prefix, boolean isTail, PrintWriter writer) {

        int counter = 1;
        for (CF entrie : this.cfList) {

            StringBuilder centroidString = new StringBuilder("[");
            double[] sum = entrie.getLinearSum().clone();
            for (int i = 0; i < sum.length; i++) {
                centroidString.append(String.format(Locale.US, "%.2f", sum[i] / entrie.getN()));
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

            String stored = "";
            if (entrie.getStoredCFs() != null) {
                stored = Arrays.toString(entrie.getStoredCFs().keySet().toArray());
            }

            writer.println(prefix + (isTail ? "└── " : "├── ") + "N=" + String.format(Locale.US, "%.2f", entrie.getN()) + " C=" + centroidString.toString() + isLeaf + stored);

            if (entrie.hasChild()) {

                entrie.getChild().print(prefix + (isTail ? "    " : "│   "), false, writer);
            }
            counter++;
        }


    }
}
