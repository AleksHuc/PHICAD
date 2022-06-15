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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DrawTimeSeries2 {

    HashMap<Integer,String> integerValues = new HashMap<>(){{
        put(1, "ADWIN");
        put(2, "Ground truth");
        put(3, "Predictions");
        put(4, "Inter");
        put(5, "Intra");
        put(6, "Cluster");
        put(7, "F Destination IP 1");
        put(8, "F Destination IP 2");
        put(9, "F Destination IP 3");
        put(10, "F Destination IP 4");
        put(11, "F Source Port");
        put(12, "F Destination Port");
        put(13, "F Protocol");
        put(14, "F Total Length of Bwd Packets");
        put(15, "F Total Length of Fwd Packets");
        put(16, "F Total Backward Packets");
        put(17, "F Total Fwd Packets");
        put(18, "F Flow Duration");
        put(19, "F Timestamp Weekday Sin");
        put(20, "F Timestamp Weekday Cos");
        put(21, "F Timestamp Hour Sin");
        put(22, "F Timestamp Hour Cos");
        put(23, "F Timestamp Minute Sin");
        put(24, "F Timestamp Minute Cos");
        put(25, "F Timestamp Second Sin");
        put(26, "F Timestamp Second Cos");
        put(27, "F Timestamp Difference");
        put(28, "P Destination IP 1");
        put(29, "P Destination IP 2");
        put(30, "P Destination IP 3");
        put(31, "P Destination IP 4");
        put(32, "P Source Port");
        put(33, "P Destination Port");
        put(34, "P Protocol");
        put(35, "P Total Length of Bwd Packets");
        put(36, "P Total Length of Fwd Packets");
        put(37, "P Total Backward Packets");
        put(38, "P Total Fwd Packets");
        put(39, "P Flow Duration");
        put(40, "P Timestamp Weekday Sin");
        put(41, "P Timestamp Weekday Cos");
        put(42, "P Timestamp Hour Sin");
        put(43, "P Timestamp Hour Cos");
        put(44, "P Timestamp Minute Sin");
        put(45, "P Timestamp Minute Cos");
        put(46, "P Timestamp Second Sin");
        put(47, "P Timestamp Second Cos");
        put(48, "P Timestamp Difference");
    }};

    public void draw(String resultsPathName, String fileName) {

        HashMap<String, HashMap<String, XYSeries>> xySeries = new HashMap<>();
        HashMap<String, AtomicLong> indexes = new HashMap<>();
        HashMap<String, AtomicInteger> detections = new HashMap<>();

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

                    String aKey = data[0].split("_")[0] + "_" + data[0].split("_")[1];
//                    String aKey = data[0];

                    if(!xySeries.containsKey(aKey)) {
                        xySeries.put(aKey, new HashMap<>());
                        for (int i = 1; i < data.length; i++) {
//                        for (Map.Entry<Integer, String> entry : integerValues.entrySet()) {
                            xySeries.get(aKey).put(integerValues.get(i), new XYSeries(integerValues.get(i)));
                        }
                        indexes.put(aKey, new AtomicLong());
                        detections.put(aKey, new AtomicInteger());
                    }
                    double index = 0.0;
                    for (int i = 1; i < data.length; i++) {
//                    for (Map.Entry<Integer, String> entry : integerValues.entrySet()) {
//                        System.out.println(indexes.get(aKey).doubleValue());
                        xySeries.get(aKey).get(integerValues.get(i)).add(indexes.get(aKey).doubleValue(), Double.parseDouble(data[i]) + index);
                        index += 2;
                        if (i == 2 && Double.parseDouble(data[i]) > 0.0) {
                            if (detections.get(aKey).get() == 0) {
                                detections.get(aKey).getAndIncrement();
                                count++;
                            }
                            else {
                                detections.get(aKey).getAndIncrement();
                            }
                        }
                    }
                    indexes.get(aKey).getAndIncrement();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Count: " + count);

        for (Map.Entry<String, HashMap<String, XYSeries>> entry : xySeries.entrySet()) {

            String d = "0";
            if (detections.get(entry.getKey()).get() > 0) {

                d = "1";


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
