package evaluation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import phicad.MutableLocalDateTime;

import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class DrawCFTimeSeries {

    public static void main(String[] args) {

        HashMap<Integer, String> integerValues = new HashMap<>() {
            {
                put(0, "Destination IP 1");
                put(1, "Destination IP 2");
                put(2, "Destination IP 3");
                put(3, "Destination IP 4");
                put(4, "Source Port");
                put(5, "Destination Port");
                put(6, "Protocol");
                put(7, "Total Length of Bwd Packets");
                put(8, "Total Length of Fwd Packets");
                put(9, "Total Backward Packets");
                put(10, "Total Fwd Packets");
                put(11, "Flow Duration");
                put(12, "Update Rate");
                put(13, "D Destination IP 1");
                put(14, "D Destination IP 2");
                put(15, "D Destination IP 3");
                put(16, "D Destination IP 4");
                put(17, "D Source Port");
                put(18, "D Destination Port");
                put(19, "D Protocol");
                put(20, "D Total Length of Bwd Packets");
                put(21, "D Total Length of Fwd Packets");
                put(22, "D Total Backward Packets");
                put(23, "D Total Fwd Packets");
                put(24, "D Flow Duration");
                put(25, "Ground Truth");
            }
        };



        File folder = new File("results");
        String startsWith = "";
//        String startsWith = "192.168.2.113_bwd_0";
//        String startsWith = "192.168.2.112_fwd_57";
//        String startsWith = "192.168.2.112";
//        String startsWith = "192.168.2.101";
//        String startsWith = "192.168.5.122";
//        String startsWith = "192.168.2.106_bwd_0_CFTimeSeries.txt";
        try {
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (Character.isDigit(fileEntry.getName().charAt(0)) && fileEntry.getName().startsWith(startsWith) && !fileEntry.getName().endsWith(".png") && !fileEntry.isDirectory()) {

                    HashMap<Integer, XYSeries> data = new HashMap<>();

                    for (int i = 0; i < integerValues.size(); i++) {
                        data.put(i, new XYSeries(integerValues.get(i)));
                    }

                    HashMap<Integer, XYSeries> data2 = new HashMap<>();

                    for (int i = 0; i < integerValues.size(); i++) {
                        data2.put(i, new XYSeries(integerValues.get(i)));
                    }

                    HashMap<Integer, XYSeries> data3 = new HashMap<>();

                    for (int i = 0; i < 12; i++) {
                        data3.put(i, new XYSeries(integerValues.get(i)));
                    }
                    data3.put(12, new XYSeries(integerValues.get(25)));

                    System.out.println(fileEntry.getName());

                    FileReader fr = new FileReader(fileEntry);
                    BufferedReader br = new BufferedReader(fr);

                    boolean check = false;
                    int counter = 0;
                    boolean firstLine = false;
                    for (String line; (line = br.readLine()) != null; ) {
                        String[] firstSplit = line.split("/");
                        String[] a = firstSplit[0].split(",");
//                        double[] values = new double[a.length];
                        for (int i = 0; i < a.length; i++) {
                            String stripedValue = (a[i].replaceAll("[\\s :\\[\\]]",""));
//                            if (i == 13) {
//                                System.out.println(stripedValue);
//                            }

                            if (i==26) {
                                System.out.println(i);
                            }

                            data.get(i).add(counter, i * 2 + Double.parseDouble(stripedValue));
                            if (i == a.length - 1 && Double.parseDouble(stripedValue) > 0.0) {
                                check = true;
                            }
                        }

                        String[] b = firstSplit[1].split(",");
//                        double[] values2 = new double[b.length];
                        for (int i = 0; i < b.length; i++) {
                            String stripedValue = (b[i].replaceAll("[\\s :\\[\\]]",""));
//                            if (i == 13) {
//                                System.out.println(stripedValue);
//                            }

                            data2.get(i).add(counter, i * 2 + Double.parseDouble(stripedValue));
                        }

                        String[] c = firstSplit[2].split(",");
                        for (int i = 0; i < c.length; i++) {
                            String stripedValue = (c[i].replaceAll("[\\s :\\[\\]]",""));
//                            if (i == 13) {
//                                System.out.println(stripedValue);
//                            }


                            data3.get(i).add(counter, i * 2 + Double.parseDouble(stripedValue));

                        }



//                        String[] a = line.split("\\[")[1].split("]");
//                        String[] aa = a[0].split(", ");

//                        for (int i = 0; i < values.length; i++) {
//                            data.get(i).add(counter, i * 2 + Double.parseDouble(aa[i]));
////                            if (i == 4 && fileEntry.getName().startsWith("192.168.2.106_bwd_504_CFTimeSeries.txt")) {
////                                if (Double.parseDouble(aa[i]) > 0.003) {
////                                    System.out.println(aa[i]);
////                                    System.out.println(line);
////                                }
//
////                            }
//                        }
//                        data.get(12).add(counter, aa.length * 2 + Double.parseDouble(a[1]));

                        counter++;
                    }
                    if (check) {

                        XYSeriesCollection dataset = new XYSeriesCollection();

//                for (Map.Entry<Integer, String> entry2 : integerValues.entrySet()) {
                        for (int i = 0; i < data.size(); i++) {
//                        System.out.println(data.get(i).getKey());
                            dataset.addSeries(data.get(i));
//                        if (i == 4) {
//                            System.out.println(Arrays.toString(data.get(i).toArray()[1]));
//                        }
                        }

                        XYSeriesCollection dataset2 = new XYSeriesCollection();

//                for (Map.Entry<Integer, String> entry2 : integerValues.entrySet()) {
                        for (int i = 0; i < data2.size(); i++) {
//                        System.out.println(data.get(i).getKey());
                            dataset2.addSeries(data2.get(i));
//                        if (i == 4) {
//                            System.out.println(Arrays.toString(data.get(i).toArray()[1]));
//                        }
                        }

                        XYSeriesCollection dataset3 = new XYSeriesCollection();

//                for (Map.Entry<Integer, String> entry2 : integerValues.entrySet()) {
                        for (int i = 0; i < data3.size(); i++) {
//                        System.out.println(data.get(i).getKey());
                            dataset3.addSeries(data3.get(i));
//                        if (i == 4) {
//                            System.out.println(Arrays.toString(data.get(i).toArray()[1]));
//                        }
                        }

                        String[] labels = new String[integerValues.size() * 2];
                        int j = 0;
                        for (int i = 0; i < integerValues.size(); i++) {
                            labels[j] = integerValues.get(i);
                            labels[j + 1] = "";
                            j += 2;
                        }

                        String[] labels2 = new String[13 * 2];
                        j = 0;
                        for (int i = 0; i < 13; i++) {
                            if (i == 12) {
                                labels2[j] = integerValues.get(25);
                            }
                            else {
                                labels2[j] = integerValues.get(i);
                            }
                            labels2[j + 1] = "";
                            j += 2;
                        }

                        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);

                        NumberAxis domainAxis = new NumberAxis("Consecutive flows");
                        SymbolAxis rangeAxis = new SymbolAxis("Features", labels);
                        rangeAxis.setGridBandsVisible(false);
//                    rangeAxis.setTickUnit(new NumberTickUnit(2.0));

                        XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
                        JFreeChart chart = new JFreeChart("Profile features of " + fileEntry.getName(), plot);

                        XYDotRenderer dr = new XYDotRenderer();
                        dr.setDotHeight(2);
                        dr.setDotWidth(2);
                        plot.setRenderer(dr);
                        plot.setDomainCrosshairVisible(false);
                        plot.setRangeCrosshairVisible(false);

                        chart.removeLegend();

//                    JFreeChart chart = ChartFactory.createXYLineChart(
//                            "Profile features of " + fileEntry.getName(), "Consecutive flows",
//                            "Normalized Values", dataset,
//                            PlotOrientation.VERTICAL, true, true, false
//                    );


                        chart.getPlot().setBackgroundPaint(Color.WHITE);


//                String d = "0";
//                if (detections.get(entry.getKey())) {
//                    d = "1";
//                }

                        try {
                            ChartUtilities.saveChartAsJPEG(new File(fileEntry + ".png"), chart, 1920, 1080);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(false, true);

                        NumberAxis domainAxis2 = new NumberAxis("Consecutive flows");
                        SymbolAxis rangeAxis2 = new SymbolAxis("Windows", labels);
                        rangeAxis2.setGridBandsVisible(false);
//                    rangeAxis.setTickUnit(new NumberTickUnit(2.0));

                        XYPlot plot2 = new XYPlot(dataset2, domainAxis2, rangeAxis2, renderer2);
                        JFreeChart chart2 = new JFreeChart("Profile windows of " + fileEntry.getName(), plot2);

                        XYDotRenderer dr2 = new XYDotRenderer();
                        dr2.setDotHeight(2);
                        dr2.setDotWidth(2);
                        plot2.setRenderer(dr2);
                        plot2.setDomainCrosshairVisible(false);
                        plot2.setRangeCrosshairVisible(false);

                        chart2.removeLegend();

//                    JFreeChart chart = ChartFactory.createXYLineChart(
//                            "Profile features of " + fileEntry.getName(), "Consecutive flows",
//                            "Normalized Values", dataset,
//                            PlotOrientation.VERTICAL, true, true, false
//                    );


                        chart2.getPlot().setBackgroundPaint(Color.WHITE);


//                String d = "0";
//                if (detections.get(entry.getKey())) {
//                    d = "1";
//                }

                        try {
                            ChartUtilities.saveChartAsJPEG(new File(fileEntry + "_Windows.png"), chart2, 1920, 1080);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        XYLineAndShapeRenderer renderer3 = new XYLineAndShapeRenderer(false, true);

                        NumberAxis domainAxis3 = new NumberAxis("Consecutive flows");
                        SymbolAxis rangeAxis3 = new SymbolAxis("Features", labels2);
                        rangeAxis3.setGridBandsVisible(false);
//                    rangeAxis.setTickUnit(new NumberTickUnit(2.0));

                        XYPlot plot3 = new XYPlot(dataset3, domainAxis3, rangeAxis3, renderer3);
                        JFreeChart chart3 = new JFreeChart("Profile features of " + fileEntry.getName(), plot3);

                        XYDotRenderer dr3 = new XYDotRenderer();
                        dr3.setDotHeight(2);
                        dr3.setDotWidth(2);
                        plot3.setRenderer(dr3);
                        plot3.setDomainCrosshairVisible(false);
                        plot3.setRangeCrosshairVisible(false);

                        chart3.removeLegend();

//                    JFreeChart chart = ChartFactory.createXYLineChart(
//                            "Profile features of " + fileEntry.getName(), "Consecutive flows",
//                            "Normalized Values", dataset,
//                            PlotOrientation.VERTICAL, true, true, false
//                    );


                        chart3.getPlot().setBackgroundPaint(Color.WHITE);


//                String d = "0";
//                if (detections.get(entry.getKey())) {
//                    d = "1";
//                }

                        try {
                            ChartUtilities.saveChartAsJPEG(new File(fileEntry + "_Intra.png"), chart3, 1920, 1080);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
