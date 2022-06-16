package adwin;

import java.util.LinkedList;

/**
 * This class implements an object that presents a single Bin in exponential histogram.
 */
public class HistogramBin {

    private int sizeOfBin;
    private LinkedList<Double> total;
    private LinkedList<Double> variance;

    /**
     * The constructor creates new HistogramBin object from a given parameter.
     * @param sizeOfBin int value that determines the maximum size of the bin.
     */
    public HistogramBin(int sizeOfBin) {
        this.sizeOfBin = sizeOfBin;
        this.total = new LinkedList<>();
        this.variance = new LinkedList<>();
    }

    /**
     * The method adds total and variance to the bin.
     * @param total double value that presents total.
     * @param variance double value that presents the variance.
     * @param index int value that presents the index of the bin inside the exponential histogram.
     * @return null if the maximum length of bin is not achieved or TotalVariance object owith the total and variance of the oldest two totals and variances in the bin otherwise.
     */
    public TotalVariance add(double total, double variance, int index){
        this.total.addFirst(total);
        this.variance.addFirst(variance);
        if (this.total.size() == (this.sizeOfBin + 1)) {
            return calculateTotalVariance(index);
        }
        else {
            return null;
        }
    }

    /**
     * The method removes the oldes two totals and variances from the bin and calculates their total and variance.
     * @param index int value that presents the index of the bin inside the exponential histogram.
     * @return TotalVariance object with the total and variance of the oldest two totals and variances in the bin.
     */
    TotalVariance calculateTotalVariance(int index) {
        Double total1 = this.total.pollLast();
        Double total2 = this.total.pollLast();
        Double variance1 = this.variance.pollLast();
        Double variance2 = this.variance.pollLast();
        if (total1 != null && total2 != null && variance1 != null && variance2 != null) {
            double n = Math.pow(2, index);
            double variance = calculateVariance(new double[]{total1, total2}, new double[]{variance1, variance2}, new double[]{n, n});
            double total = total1 + total2;
            return new TotalVariance(total, variance);
        }
        else {
            return null;
        }
    }

    /**
     * The method calculates variance from totals, variances and lengths.
     * @param totals double[] array of totals.
     * @param variances double[] array of variances.
     * @param lengths double[] array of lengths.
     * @return double value that presents variance.
     */
    double calculateVariance(double[] totals, double[] variances, double[] lengths) {
        double length = sumArray(lengths);
        double totalmean = sumArray(totals) / length;
        double totalVariance = 0.0;
        for (int i = 0; i < totals.length; i++) {
            double n = lengths[i];
            double mean = totals[i] / n;
            double variance = variances[i];
            double difference = mean - totalmean;
            totalVariance += n * (variance + Math.pow(difference, 2));
        }
        return totalVariance / length;
    }

    /**
     * The method that returns current totals.
     * @return LinkedList&lt;Double&gt; object that presents current totals.
     */
    public LinkedList<Double> getTotal() {
        return total;
    }

    /**
     * The method that returns current variances.
     * @return LinkedList&lt;Double&gt; object that presents current variances.
     */
    public LinkedList<Double> getVariance() {
        return variance;
    }

    /**
     * The method that returns current variances.
     * @param array double[] array of lengths.
     * @return LinkedList&lt;Double&gt; object that presents current variances.
     */
    public static double sumArray(double[] array) {
        double sum = 0;
        for (double value : array){
            sum += value;
        }
        return sum;
    }
}
