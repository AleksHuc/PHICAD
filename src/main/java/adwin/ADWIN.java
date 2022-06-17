package adwin;

import java.util.LinkedList;

/**
 * The class implements an object that presents the ADWIN algorithm.
 */
public class ADWIN {
    //**

    private long index;
    private int checkStep;
    private int minimumHistogramSize;

    private double delta;

    private ExponentialHistogram exponentialHistogram;

    /**
     * The constructor creates new adaptive window (ADWIN) object from the given parameters.
     * @param delta double value that presents the maximum delta (confidence) value for the ADWIN windows.
     * @param checkStep int value that presents the number of iterations before we check for anomaly.
     * @param maxBins int value that presents the maximum number of bins the exponential histogram can have.
     * @param sizeOfBin int value that presents the maximum number of values inside a single bin.
     */
    public ADWIN(double delta, int checkStep, int maxBins, int sizeOfBin) {
        this.delta = delta;
        this.index = 0;
        this.checkStep = checkStep;
        this.minimumHistogramSize = 10;
        this.exponentialHistogram = new ExponentialHistogram(maxBins, sizeOfBin);
    }

    /**
     * The method adds new value to the ADWIN.
     * @param value double value that presents the input.
     * @return ChangeDifference object that presents the calculated maximum difference and if change was detected.
     */
    public ChangeDifference addNewValue(double value){
        boolean change = false;
        double maxDifference = 0.0;

        this.exponentialHistogram.addNewValue(value);
        index++;

        if (index % checkStep == 0 && this.exponentialHistogram.getTotalSize() > minimumHistogramSize) {

            LinkedList<HistogramBin> histogram = this.exponentialHistogram.getHistogram();

            int w0Length = 0;
            int w1Length = this.exponentialHistogram.getEhSize();

            double w0Total = 0.0;
            double w1Total = this.exponentialHistogram.getEhTotal();

            double currentDelta = Math.log((2 * Math.log(w1Length)) / this.delta);

            double variance = this.exponentialHistogram.getEhVariance();

            for (int i = (histogram.size() - 1); i >= 0; i--) {

                HistogramBin bin = histogram.get(i);
                int n = (int) Math.pow(2, i);

                for (int j = (bin.getTotal().size() - 1); j >= 0; j--) {

                    double currentTotal = bin.getTotal().get(j);

                    w0Length += n;
                    w1Length -= n;

                    if (w1Length > 0.0) {

                        w0Total += currentTotal;
                        w1Total -= currentTotal;

                        double meanDifference = Math.abs((w0Total / w0Length) - (w1Total / w1Length));

                        double m = (1.0 / w0Length) + (1.0 / w1Length);

                        double epsilon = Math.sqrt(2 * m * currentDelta * variance) + ((2.0 / 3.0) * m * currentDelta);

                        if (meanDifference > maxDifference) {
                            maxDifference = meanDifference;
                        }

                        if (meanDifference > epsilon) {
                            change = true;
                            this.exponentialHistogram.deleteOldestBin();
                            break;
                        }
                    }
                }

                if (change) {
                    break;
                }
            }
        }

        return new ChangeDifference(change, maxDifference);
    }

    /**
     * Method that returns the latest value from the Exponential Histogram.
     * @return double value that represent the latest value in the Exponential Histogram.
     */
    public double getLatestValue() {
        return this.exponentialHistogram.getLatestValue();
    }

    /**
     * Method that prints the entire ADWIN with the underlying Exponentional Histogram bins.
     */
    public void print() {
        int i = 0;
        System.out.println("----- EH -----");
        for (HistogramBin hb : this.exponentialHistogram.getHistogram()) {
            System.out.print(i + " T: ");
            for (double d : hb.getTotal()) {
                System.out.print(d + " ");
            }
            System.out.println();
            System.out.print(i + " V: ");
            for (double d : hb.getVariance()) {
                System.out.print(d + " ");
            }
            i++;
            System.out.println();
        }
    }
}
