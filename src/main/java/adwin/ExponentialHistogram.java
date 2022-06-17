package adwin;

import java.util.ArrayList;
import java.util.LinkedList;

import static utils.Utils.listToArray;

/**
 * The class implements an object that presents the exponential histogram.
 */
public class ExponentialHistogram {

    private LinkedList<HistogramBin> histogram;
    private int maxBins;
    private int sizeOfBin;
    private int ehSize;
    private double ehTotal;

    /**
     * The constructor creates new FlowMessage object from given parameters.
     * @param maxBins int value that presents the maximum number of bins.
     * @param sizeOfBin int value that presents the maximum size of bin.
     */
    public ExponentialHistogram(int maxBins, int sizeOfBin) {
        this.maxBins = maxBins;
        this.sizeOfBin = sizeOfBin;
        this.ehSize = 0;
        this.ehTotal = 0.0;
        this.histogram = new LinkedList<>();
        this.histogram.addFirst(new HistogramBin(this.sizeOfBin));
    }

    /**
     * The method adds new value to the exponential histogram.
     * @param value double value that presents the input.
     */
    public void addNewValue(double value) {
        int index = 0;
        ehSize++;
        ehTotal += value;
        TotalVariance current = this.histogram.get(index).add(value, 0.0, index);
        if (current != null) {
            addNewValue(current, index + 1);
        }
    }

    /**
     * The method adds new TotalVariance to the exponential histogram.
     * @param current TotalVariance object that presents total and variance.
     * @param index int value that presents the index of the bin inside the exponential histogram.
     */
    public void addNewValue(TotalVariance current, int index) {
        if (index <= maxBins) {
            if (index >= histogram.size()) {
                this.histogram.add(new HistogramBin(this.sizeOfBin));
            }
            TotalVariance newCurrent = this.histogram.get(index).add(current.getTotal(), current.getVariance(), index);
            if (newCurrent != null) {
                addNewValue(newCurrent, index + 1);
            }
        }
        else {
            ehSize -= Math.pow(2, index);
            ehTotal -= current.getTotal();
        }
    }

    /**
     * The method calculates the size of the exponential histogram.
     * @return int value that presents the number of values stored in the exponential histogram.
     */
    public int getTotalSize() {
        int size = 0;
        for (int i = 0; i < this.histogram.size(); i++) {
            size += Math.pow(2, i) * this.histogram.get(i).getTotal().size();
        }
        return size;
    }

    /**
     * The method returns the histogram bins.
     * @return LinkedList&lt;HistogramBin&gt; object that holds all bins of the exponential histogram.
     */
    public LinkedList<HistogramBin> getHistogram() {
        return histogram;
    }

    /**
     * The method returns the size of the exponential histogram.
     * @return int value that represents the size of exponential histogram.
     */
    public int getEhSize() {
        return ehSize;
    }

    /**
     * The method returns the total of the exponential histogram.
     * @return int value that represents the total of exponential histogram.
     */
    public double getEhTotal() {
        return ehTotal;
    }

    /**
     * The method removes the oldest bin in the exponential histogram.
     */
    public void deleteOldestBin() {
        int index = this.histogram.size() - 1;
        HistogramBin oldestBin = this.histogram.get(index);
        LinkedList<Double> totals = oldestBin.getTotal();
        for(Double current : totals) {
            ehSize -= Math.pow(2, index);
            ehTotal -= current;
        }
        this.histogram.remove(index);
    }

    /**
     * The method calculates the variance of the exponential histogram.
     * @return double value that represents the variance of exponential histogram.
     */
    public double getEhVariance() {
        ArrayList<Double> totals = new ArrayList<>();
        ArrayList<Double> lengths = new ArrayList<>();
        ArrayList<Double> variances = new ArrayList<>();

        for (int i = 0; i < this.histogram.size(); i++) {
            LinkedList<Double> total = this.histogram.get(i).getTotal();
            LinkedList<Double> variance = this.histogram.get(i).getVariance();
            int length = (int)Math.pow(2, i);
            for (int j = 0; j < total.size(); j++) {
                totals.add(total.get(j));
                variances.add(variance.get(j));
                lengths.add((double)length);
            }
        }
        return this.histogram.get(0).calculateVariance(listToArray(totals), listToArray(variances), listToArray(lengths));
    }

    /**
     * Method that returns the latest value from the Exponential Histogram.
     * @return double value that represent the latest value in the Exponential Histogram.
     */
    public double getLatestValue() {
        double latestValue = 0.0;
        if (this.histogram.size() > 0 && this.histogram.get(0).getTotal().size() > 0) {
            latestValue = this.histogram.get(0).getTotal().get(0);
        }
        return latestValue;
    }
}
