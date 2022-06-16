package adwin;

/**
 * The class implements object for storing total and variance of the exponential histograms.
 */
public class TotalVariance {

    private double total;
    private double variance;

    /**
     * The constructor creates new TotalVariance object from the given parameters.
     * @param total double value that presents the total.
     * @param variance double value that presents the variance.
     */
    public TotalVariance(double total, double variance) {
        this.total = total;
        this.variance = variance;
    }

    /**
     * The method returns the value of total variable.
     * @return double value that presents the value of total variable.
     */
    public double getTotal() {
        return total;
    }

    /**
     * The method returns the value of variance variable.
     * @return double value that presents the value of variance variable.
     */
    public double getVariance() {
        return variance;
    }
}
