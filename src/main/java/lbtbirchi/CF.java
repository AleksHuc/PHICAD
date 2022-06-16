package lbtbirchi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class CF {

    private double n;
    private double[] linearSum;
    private double[] squareSum;
    private CFNode child;
    private CFNode parent;
    private LinkedHashMap<String, ArrayList<latbirchi.CF>> storedCFs;

    public CF() {
        this.n = 0;
        this.linearSum = null;
        this.squareSum = null;
        this.child = null;
        this.parent = null;
        this.storedCFs = null;
    }

    public CF(double[] point){
        this.n = 1;
        this.child = null;
        this.linearSum = new double[point.length];
        System.arraycopy(point, 0, this.linearSum, 0, this.linearSum.length);

        this.squareSum = new double[point.length];
        for (int i = 0; i < this.squareSum.length; i++) {
            this.squareSum[i] = point[i] * point[i];
        }
    }

    public CF(double[] point, String profileID){
        this.n = 1;
        this.child = null;
        this.parent = null;

        this.linearSum = new double[point.length];
        System.arraycopy(point, 0, this.linearSum, 0, this.linearSum.length);

        this.squareSum = new double[point.length];
        for (int i = 0; i < this.squareSum.length; i++) {
            this.squareSum[i] = point[i] * point[i];
        }

        this.storedCFs = new LinkedHashMap<>();
        this.storedCFs.put(profileID, new ArrayList<>());
        this.storedCFs.get(profileID).add(new latbirchi.CF(point));
    }

    void update(CF cf) {
        this.n += cf.getN();

        double[] currentLinearSum = cf.getLinearSum();
        if(this.linearSum == null){
            this.linearSum = currentLinearSum.clone();
        }
        else {
            for (int i = 0; i < this.linearSum.length; i++) {
                this.linearSum[i] += currentLinearSum[i];
            }
        }

        double[] currentSquareSum = cf.getSquareSum();
        if(this.squareSum == null){
            this.squareSum = currentSquareSum.clone();
        }
        else {
            for (int i = 0; i < this.squareSum.length; i++) {
                this.squareSum[i] += currentSquareSum[i];
            }
        }

        if (this.child == null) {
            if (this.storedCFs == null) {
                this.storedCFs = new LinkedHashMap<>();
            }

            LinkedHashMap<String, ArrayList<latbirchi.CF>> cfstoredCFs = cf.getStoredCFs();

            for (String key : cfstoredCFs.keySet()) {
                if (this.storedCFs.containsKey(key)) {
                    this.storedCFs.get(key).addAll(cfstoredCFs.get(key));
                } else {
                    this.storedCFs.put(key, new ArrayList<>(cfstoredCFs.get(key)));
                }
            }
        }
    }

    public CF makeACopy(){
        CF temp = new CF();

        temp.n = this.n;

        double[] cfLS = this.getLinearSum();
        double[] lSum = new double[cfLS.length];
        System.arraycopy(cfLS, 0, lSum, 0, lSum.length);
        temp.linearSum = lSum;

        double[] cfSS = this.getSquareSum();
        double[] sSum = new double[cfLS.length];
        System.arraycopy(cfSS, 0, sSum, 0, sSum.length);
        temp.squareSum = sSum;

        temp.child = this.child;
        temp.parent = this.parent;

        LinkedHashMap<String, ArrayList<latbirchi.CF>> stored = new LinkedHashMap<>();

        for (String key : this.storedCFs.keySet()) {
            ArrayList<latbirchi.CF> list = new ArrayList<>(this.storedCFs.get(key));
            stored.put(key, list);
        }

        temp.storedCFs = stored;

        return temp;
    }

    void removeCFs(String profileID) {

        if (this.storedCFs != null && this.storedCFs.containsKey(profileID)) {

            double nSum = 0;
            double[] lsSum = null;
            double[] ssSum = null;

            ArrayList<latbirchi.CF> currentCFs = this.storedCFs.get(profileID);
            for (latbirchi.CF cf : currentCFs) {
                nSum += cf.getW();

                double[] currentLS = cf.getLinearSum();
                if (lsSum == null) {
                    lsSum = new double[currentLS.length];
                }
                for (int i = 0; i < currentLS.length; i++) {
                    lsSum[i] += currentLS[i];
                }

                double[] currentSS = cf.getSquareSum();
                if (ssSum == null) {
                    ssSum = new double[currentSS.length];
                }
                for (int i = 0; i < currentSS.length; i++) {
                    ssSum[i] += currentSS[i];
                }
            }

            if (nSum > 0.0 && lsSum != null && ssSum != null) {
                this.storedCFs.remove(profileID);
                this.decreaseValues(nSum, lsSum, ssSum);
            }
        }
    }

    public void decreaseValues(double nSum, double[] lsSum, double[] ssSum) {
        this.n -= nSum;

        for (int i = 0; i < lsSum.length; i++) {
            this.linearSum[i] -= lsSum[i];
        }

        for (int i = 0; i < ssSum.length; i++) {
            this.squareSum[i] -= ssSum[i];
        }

        CF parentCF = this.parent.getParent();
        if (parentCF != null) {
            parentCF.decreaseValues(nSum, lsSum, ssSum);
        }
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
        double wCF1 = cf1.getN();
        double wCF2 = cf2.getN();
        double[] linearSumCF1 = cf1.getLinearSum();
        double[] linearSumCF2 = cf2.getLinearSum();

        if (linearSumCF1 == null || linearSumCF2 == null) {
            System.out.println();
        }

        for (int i = 0; i < linearSumCF1.length; i++) {
            double diff = linearSumCF1[i]/wCF1 - linearSumCF2[i]/wCF2;
            dist += diff*diff;
        }

        if (dist < 0.0){
            if(dist < -0.00000001)
                System.err.println("d0 < 0 !!!");
            dist = 0.0;
        }

        return Math.sqrt(dist) / Math.sqrt(linearSumCF1.length);
//        return Math.sqrt(dist);
//        return dist / linearSumCF1.length;
    }

    private double d1(CF cf1, CF cf2) {
        double dist = 0;
        double wCF1 = cf1.getN();
        double wCF2 = cf2.getN();
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

        double wCF1 = cf1.getN();
        double wCF2 = cf2.getN();
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
        double wCF1 = cf1.getN();
        double wCF2 = cf2.getN();
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

    private double d4(CF cf1, CF cf2) {
        double dist = 0;
        double wCF1 = cf1.getN();
        double wCF2 = cf2.getN();
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

        double wCF1 = cf1.getN();
        double wCF2 = cf2.getN();
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
            if(dist < -0.00000001 )
                System.err.println("d5 < 0 !!!");
            dist = 0.0;
        }

        return Math.sqrt(dist);
    }

    double[] getCentroid() {
        double[] centroid = new double[this.linearSum.length];

        for (int i = 0; i < this.linearSum.length; i++) {
            centroid[i] = this.linearSum[i] / this.n;
        }

        return centroid;
    }

    public boolean equals(Object o) {

        if (this.getClass() != o.getClass()) {
            return false;
        }

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

//        if (this.child != null && !this.child.equals(cf.getChild())) {
//            return false;
//        }

        if (this.parent != null && cf.getParent() == null) {
            return false;
        }

        if (this.parent == null && cf.getParent() != null) {
            return false;
        }

//        if (this.parent != null && !this.parent.equals(cf.getParent())) {
//            return false;
//        }

        if(!Arrays.equals(this.linearSum, cf.getLinearSum())) {
            return false;
        }

        if(!Arrays.equals(this.squareSum, cf.getSquareSum())) {
            return false;
        }

        if (this.storedCFs != null && cf.getStoredCFs() == null) {
            return false;
        }

        if (this.storedCFs == null && cf.getStoredCFs() != null) {
            return false;
        }

        if (this.storedCFs != null && cf.getStoredCFs() != null && !this.storedCFs.equals(cf.getStoredCFs())) {
            return false;
        }

        return true;
    }

    void addToChild(CF cf) {
        this.child.getCfList().add(cf);
    }

    public double getN() {
        return n;
    }

    double[] getLinearSum() {
        return linearSum;
    }

    double[] getSquareSum() {
        return squareSum;
    }

    CFNode getChild() {
        return child;
    }

    public void setChild(CFNode child) {
        this.child = child;
    }

    boolean hasChild() {
        return this.child != null;
    }

    public CFNode getParent() {
        return parent;
    }

    public LinkedHashMap<String, ArrayList<latbirchi.CF>> getStoredCFs() {
        return storedCFs;
    }

    public void setParent(CFNode parent) {
        this.parent = parent;
    }

    public void setN(double w) {
        this.n = w;
    }

    public void setLinearSum(double[] lSum) {
        this.linearSum = lSum;
    }

    public void setSquareSum(double[] sSum) {
        this.squareSum = sSum;
    }

    public void setStoredCFs(LinkedHashMap<String, ArrayList<latbirchi.CF>> storedCFs) {
        this.storedCFs = storedCFs;
    }
}
