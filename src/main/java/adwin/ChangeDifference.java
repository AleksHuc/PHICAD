package adwin;

/**
 * The class implements an object for storing change and difference of the ADWIN windows.
 */
public class ChangeDifference {

    private boolean change;
    private double difference;

    /**
     * The constructor creates new ChangeDifference object from a given parameter.
     * @param change boolean value that presents if change has been detected.
     * @param difference double value presents the maximum value of difference between sub windows inside each ADWIN window.
     */
    public ChangeDifference(boolean change, double difference) {
        this.change = change;
        this.difference = difference;
    }

    /**
     * The method returns the value of change variable.
     * @return boolean value that presents the value of change variable.
     */
    public boolean isChange() {
        return change;
    }

    /**
     * The method returns the value of difference variable.
     * @return double value that presents the value of difference variable.
     */
    public double getDifference() {
        return difference;
    }
}
