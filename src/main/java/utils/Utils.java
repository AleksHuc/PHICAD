package utils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static DateTimeFormatter[] TIME_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("dd/MM/yyyy H:m"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy H:m:s")
    };

    /**
     * The method transforms the given List of doubles to array of doubles.
     * @param list List&lt;Dobule&gt; that presents the given List of doubles.
     * @return double[] array of the give List of doubles.
     */
    public static double[] listToArray(List<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < array.length; i++){
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * The methods concatenates two double arrays.
     * @param a1 double[] array that present the first input double array.
     * @param a2 double[] array that present the second input double array.
     * @return double[] array that presents concatenation of the input double arrays.
     */
    public static double[] concatTwo(double[] a1, double[] a2) {
        double[] array = new double[a1.length + a2.length];

        System.arraycopy(a1, 0, array, 0, a1.length);
        System.arraycopy(a2, 0, array, a1.length, a2.length);

        return array;
    }

    public static void purgeDirectory(File dir) {
        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory())
                purgeDirectory(file);
            boolean result = file.delete();
            if (!result) {
                System.out.println("Delete failed: " + file.getName());
            }
        }
    }

    /**
     * The method parses time-stamp string.
     * @param timestampString String that presents the given time-stamp.
     * @param formats DateTimeFormatter[] array that presents the possible formats for parsing DateTime.
     * @return LocalDateTime that presents the given time-stamp.
     */
    public static LocalDateTime parseTimestamp(String timestampString, DateTimeFormatter[] formats) {

        int count = 0;
        for (int i = 9; i < timestampString.length(); i++) {
            final char c = timestampString.charAt(i);
            if (c == ':') {
                count++;
            }
        }

        return LocalDateTime.parse(timestampString, formats[count - 1]);
    }

    /**
     * The method normalizes the input value.
     * @param value double value that presents the input value to be normalized.
     * @param minValue double value that presents the minimum value that the input value can take.
     * @param maxValue double value that presents the maximum value that the input value can take.
     * @return double value that presents the normalized input value.
     */
    public static double normalize(double value, double minValue, double maxValue) {

        if (value < minValue) {
            value = minValue;
        }
        else if (value > maxValue) {
            value = maxValue;
        }
        return (value - minValue) / (maxValue - minValue);
    }

    /**
     * The methods uses trigonometric transformation on the given value.
     * @param minValue double value that presents the minimum value that the input value can take.
     * @param maxValue double value that presents the maximum value that the input value can take.
     * @param value double value that presents the input value to be transformed.
     * @return double[] array that presents the sine and cosine value of the input value after the transformation.
     */
    public static double[] trigonometricTransform(double minValue, double maxValue, double value) {
        double sin = Math.sin(rescale(minValue, maxValue, 0, 2 * Math.PI, value));
        double cos = Math.cos(rescale(minValue, maxValue, 0, 2 * Math.PI, value));
        return new double[]{sin, cos};
    }

    /**
     * The method rescales the input value to the new interval.
     * @param oldMinValue double value that presents the minimum value that the input value can take.
     * @param oldMaxValue double value that presents the maximum value that the input value can take.
     * @param newMinValue double value that presents the minimum value that the rescaled value can take.
     * @param newMaxValue double value that presents the maximum value that the rescaled value can take.
     * @param value double value that presents the input value to be rescaled.
     * @return double value that presents the rescaled input value.
     */
    public static double rescale(double oldMinValue, double oldMaxValue, double newMinValue, double newMaxValue, double value) {
        return (((value - oldMinValue) * (newMaxValue - newMinValue)) / (oldMaxValue - oldMinValue)) + newMinValue;
    }

    public static String[] reverseFlow(String[] flow, HashMap<Integer, Integer> reverseTable) {
        String[] reversedFlow = new String[flow.length];

        for (int i = 0; i < flow.length; i++) {
            if (reverseTable.containsKey(i)) {
                reversedFlow[i] = flow[reverseTable.get(i)];
                reversedFlow[reverseTable.get(i)] = flow[i];
            }
            else if (!reverseTable.containsValue(i)) {
                reversedFlow[i] = flow[i];
            }
        }

        return reversedFlow;
    }
}
