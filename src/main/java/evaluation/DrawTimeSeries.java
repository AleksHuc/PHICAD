package evaluation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class DrawTimeSeries {

    HashMap<Integer,String> integerValues = new HashMap<>(){{
        put(1, "Difference");
        put(2, "Ground truth");
        put(3, "Predictions");
        put(4, "F Destination IP 1");
        put(5, "F Destination IP 2");
        put(6, "F Destination IP 3");
        put(7, "F Destination IP 4");
        put(8, "F Source Port");
        put(9, "F Destination Port");
        put(10, "F Protocol");
        put(11, "F Total Length of Bwd Packets");
        put(12, "F Total Length of Fwd Packets");
        put(13, "F Total Backward Packets");
        put(14, "F Total Fwd Packets");
        put(15, "F Flow Duration");
        put(16, "F Timestamp Weekday Sin");
        put(17, "F Timestamp Weekday Cos");
        put(18, "F Timestamp Hour Sin");
        put(19, "F Timestamp Hour Cos");
        put(20, "F Timestamp Minute Sin");
        put(21, "F Timestamp Minute Cos");
        put(22, "F Timestamp Second Sin");
        put(23, "F Timestamp Second Cos");
        put(24, "F Timestamp Difference");
        put(25, "P Destination IP 1");
        put(26, "P Destination IP 2");
        put(27, "P Destination IP 3");
        put(28, "P Destination IP 4");
        put(29, "P Source Port");
        put(30, "P Destination Port");
        put(31, "P Protocol");
        put(32, "P Total Length of Bwd Packets");
        put(33, "P Total Length of Fwd Packets");
        put(34, "P Total Backward Packets");
        put(35, "P Total Fwd Packets");
        put(36, "P Flow Duration");
        put(37, "P Timestamp Weekday Sin");
        put(38, "P Timestamp Weekday Cos");
        put(39, "P Timestamp Hour Sin");
        put(40, "P Timestamp Hour Cos");
        put(41, "P Timestamp Minute Sin");
        put(42, "P Timestamp Minute Cos");
        put(43, "P Timestamp Second Sin");
        put(44, "P Timestamp Second Cos");
        put(45, "P Timestamp Difference");
    }};

    public void draw(String resultsPathName, String fileName) {

        HashMap<String, HashMap<String, XYSeries>> xySeries = new HashMap<>();
        HashMap<String, AtomicLong> indexes = new HashMap<>();
        HashMap<String, Boolean> detections = new HashMap<>();

        int count = 0;

        int length = 0;

        for (final File fileEntry : Objects.requireNonNull(new File(resultsPathName).listFiles((dir, name) -> name.startsWith("results_" + fileName)))) {
            try {

                FileReader fr = new FileReader(fileEntry);
                BufferedReader br = new BufferedReader(fr);

                for(String line; (line = br.readLine()) != null;) {
                    String[] data = line.split(" ");

                    length = data.length;
//                    System.out.println(data.length);

                    if(!xySeries.containsKey(data[0])) {
                        xySeries.put(data[0], new HashMap<>());
                        for (int i = 1; i < data.length; i++) {
//                        for (Map.Entry<Integer, String> entry : integerValues.entrySet()) {
                            xySeries.get(data[0]).put(integerValues.get(i), new XYSeries(integerValues.get(i)));
                        }
                        indexes.put(data[0], new AtomicLong());
                        detections.put(data[0], false);
                    }
                    double index = 0.0;
                        for (int i = 1; i < data.length; i++) {
//                    for (Map.Entry<Integer, String> entry : integerValues.entrySet()) {
//                        System.out.println(indexes.get(data[0]).doubleValue());
                        xySeries.get(data[0]).get(integerValues.get(i)).add(indexes.get(data[0]).doubleValue(), Double.parseDouble(data[i]) + index);
                        index += 2;
                        if (i == 2 && Double.parseDouble(data[i]) > 0.0) {
                            if (!detections.get(data[0])) {
                                detections.put(data[0], true);
                                count++;
                            }
                        }
                    }
                    indexes.get(data[0]).getAndIncrement();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Count: " + count);

        for (Map.Entry<String, HashMap<String, XYSeries>> entry : xySeries.entrySet()) {

            if (detections.get(entry.getKey())) {
                String d = "1";

                XYSeriesCollection dataset = new XYSeriesCollection();

//                for (Map.Entry<Integer, String> entry2 : integerValues.entrySet()) {
                for (int i = 1; i < length; i++) {
                    dataset.addSeries(entry.getValue().get(integerValues.get(i)));
                }

                JFreeChart chart = ChartFactory.createXYLineChart(
                        "Profile features of " + entry.getKey(), "Consecutive flows",
                        "Normalized Values", dataset,
                        PlotOrientation.VERTICAL, true, true, false
                );

            chart.getPlot().setBackgroundPaint(Color.WHITE);

//                String d = "0";
//                if (detections.get(entry.getKey())) {
//                    d = "1";
//                }

                try {
                    ChartUtilities.saveChartAsJPEG(new File(resultsPathName + fileName + "_" + d + "_" + entry.getKey() + "_" + ".png"), chart, 1024, 1440);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
