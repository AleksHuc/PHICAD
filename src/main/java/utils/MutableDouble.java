package utils;

/**
 * The class implements the object that presents a mutable double.
 */
public class MutableDouble {

    private double value;

    /**
     * The constructor creates new MutableDouble object.
     * @param value double value that presents the current value of MutableDouble.
     */
    public MutableDouble(double value) {
        this.value = value;
    }

    /**
     * The method returns the current value of MutableDouble.
     * @return double value that presents the current value of MutableDouble.
     */
    public double getValue() {
        return value;
    }

    /**
     * The method sets the current value of MutableDouble.
     * @param value double value that presents the current value of MutableDouble.
     */
    public void setValue(double value) {
        this.value = value;
    }

    public void addValue(double value) {
        this.value += value;
    }
}
