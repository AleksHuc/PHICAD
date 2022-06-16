package utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ECDF {

    public static void main(String[] args) {

//        File folder = new File("data/ISCX-IDS-2012");
//        String startsWith = "Testbed";
//        int[] selectedValues = new int[]{8, 9, 10, 11};

        File folder = new File("data/CIC-IDS-2017");
        String startsWith = "";
        int[] selectedValues = new int[]{8, 9, 10, 11};
        HashMap<Integer, String> graphLegend = new HashMap<>();
        graphLegend.put(8, "Number of Forward Packets");
        graphLegend.put(9, "Number of Backward Packets");
        graphLegend.put(10, "Number of Forward Bytes");
        graphLegend.put(11, "Number of Backward Bytes");

        double threshold = 0.001;

//        File folder = new File("data/SensorDataset");
//        String startsWith = "";

        HashMap<Integer, TreeMap<Long, AtomicLong>> list = new HashMap<>();
        HashMap<Integer, String> legend = new HashMap<>();
        HashMap<Integer, AtomicLong> counts = new HashMap<>();
        HashMap<Integer, XYSeries> series = new HashMap<>();

        for (int i : selectedValues) {
            list.put(i, new TreeMap<>());
            counts.put(i, new AtomicLong());
        }

        try {
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
                if (fileEntry.getName().startsWith(startsWith) && !fileEntry.isDirectory()) {
                    System.out.println(fileEntry);

                    FileReader fr = new FileReader(fileEntry);
                    BufferedReader br = new BufferedReader(fr);
                    int counter = 0;
                    for(String line; (line = br.readLine()) != null; ) {

                        String[] sLine = line.split(",");

                        for (int i : selectedValues) {
                            String label = sLine[i];
                            TreeMap<Long, AtomicLong> map = list.get(i);
                            if (counter > 0) {
                                Long value = Double.valueOf(label).longValue();
                                if (map.containsKey(value)) {
                                    map.get(value).getAndIncrement();
                                }
                                else {
                                    map.put(value, new AtomicLong(1));
                                }
                                counts.get(i).getAndIncrement();

                            }
                            else {
                                legend.put(i, label);
                            }
                        }
                        counter++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i : selectedValues) {
            long cumsum = 0L;
            XYSeries ecdf = new XYSeries(legend.get(i).trim());

            boolean flag = true;
            for (Map.Entry<Long, AtomicLong> entry : list.get(i).entrySet()) {
                cumsum += entry.getValue().get();

                double y = (double)cumsum / (double)counts.get(i).get();

                double eps = 0.0;
                if ((double)entry.getKey() == 0.0) {
                    eps = 1.0;
                }

                ecdf.add((double)entry.getKey() + eps, y);
            }

//            XYSeriesCollection datasetEcdf = new XYSeriesCollection();
//            datasetEcdf.addSeries(ecdf);
//
//            JFreeChart chartEcdf = ChartFactory.createXYLineChart(
//                    "Empirical Cumulative Distribution Function " + legend.get(i), "Value", "Counts", datasetEcdf,
//                    PlotOrientation.VERTICAL, false, true, false
//            );
//
//            LogarithmicAxis yAxisEcdf = new LogarithmicAxis("Log10 Probability");
//            LogarithmicAxis xAxisEcdf = new LogarithmicAxis("Log10 Value");
//
//            XYPlot plotEcdf = chartEcdf.getXYPlot();
//            plotEcdf.setDomainAxis(xAxisEcdf);
//            plotEcdf.setRangeAxis(yAxisEcdf);
//            plotEcdf.setDomainGridlinesVisible(true);
//            plotEcdf.setDomainGridlinePaint(Color.LIGHT_GRAY);
//            plotEcdf.setRangeGridlinesVisible(true);
//            plotEcdf.setRangeGridlinePaint(Color.LIGHT_GRAY);
//            plotEcdf.setBackgroundPaint(Color.WHITE);
//
//            try {
//                ChartUtilities.saveChartAsJPEG(new File(folder.toString() + "/Labels/ecdf_" + i + "_" + legend.get(i).trim() + ".png"), chartEcdf, 1024, 1024);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            XYSeries eccdf = new XYSeries(graphLegend.get(i));
            for (Map.Entry<Long, AtomicLong> entry : list.get(i).entrySet()) {
                cumsum -= entry.getValue().get();

                double y = (double)cumsum / (double)counts.get(i).get();
//                System.out.println(cumsum);


                double eps = 0.0;
                if ((double)entry.getKey() == 0.0) {
                    eps = 1.0;
                }
                if (y != 0.0) {
                    eccdf.add((double)entry.getKey() + eps, y);

                    if (y < threshold && flag) {
                        System.out.printf("Threshold: " + threshold + " for " + legend.get(i).trim() + " X: " + (double)entry.getKey() + eps + " Y: %f \n", y);
                        flag = false;
                    }

                }
            }
            series.put(i, eccdf);

//            XYSeriesCollection datasetEccdf = new XYSeriesCollection();
//            datasetEccdf.addSeries(eccdf);
//
//            JFreeChart chartEccdf = ChartFactory.createXYLineChart(
////                    "Empirical Complementary Cumulative Distribution Function " + legend.get(i).trim(), "Value", "Counts", datasetEccdf,
//                    null, "Value", "Counts", datasetEccdf,
//                    PlotOrientation.VERTICAL, true, true, false
//            );
//
//            LogarithmicAxis yAxisEccdf = new LogarithmicAxis("Log10 Probability");
//            LogarithmicAxis xAxisEccdf = new LogarithmicAxis("Log10 Value");
//
//            XYPlot plotEccdf = chartEccdf.getXYPlot();
//            plotEccdf.setDomainAxis(xAxisEccdf);
//            plotEccdf.setRangeAxis(yAxisEccdf);
//            plotEccdf.setDomainGridlinesVisible(true);
//            plotEccdf.setDomainGridlinePaint(Color.LIGHT_GRAY);
//            plotEccdf.setRangeGridlinesVisible(true);
//            plotEccdf.setRangeGridlinePaint(Color.LIGHT_GRAY);
//            plotEccdf.setBackgroundPaint(Color.WHITE);
//
//            XYItemRenderer rendererEccdf = plotEccdf.getRenderer();
//            rendererEccdf.setSeriesStroke(0, new BasicStroke(3.0f));
//
//            Font font3 = new Font("Dialog", Font.PLAIN, 20);
//            plotEccdf.getDomainAxis().setLabelFont(font3);
//            plotEccdf.getRangeAxis().setLabelFont(font3);
//            plotEccdf.getDomainAxis().setTickLabelFont(font3);
//            plotEccdf.getRangeAxis().setTickLabelFont(font3);
//
//            try {
//                ChartUtilities.saveChartAsJPEG(new File(folder.toString() + "/Labels/eccdf_" + i + "_" + legend.get(i) + ".png"), chartEccdf, 1024, 1024);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        for (int i : new int[]{8, 10}){

            XYSeriesCollection datasetEccdf = new XYSeriesCollection();
            datasetEccdf.addSeries(series.get(i));
            datasetEccdf.addSeries(series.get(i+1));

            JFreeChart chartEccdf = ChartFactory.createXYLineChart(
//                    "Empirical Complementary Cumulative Distribution Function " + legend.get(i).trim(), "Value", "Counts", datasetEccdf,
                    null, "Value", "Counts", datasetEccdf,
                    PlotOrientation.VERTICAL, false, true, false
            );

            LogarithmicAxis yAxisEccdf = new LogarithmicAxis("Log10 Probability");
            LogarithmicAxis xAxisEccdf = new LogarithmicAxis("Log10 Value");

            XYPlot plotEccdf = chartEccdf.getXYPlot();
            plotEccdf.setDomainAxis(xAxisEccdf);
            plotEccdf.setRangeAxis(yAxisEccdf);
            plotEccdf.setDomainGridlinesVisible(true);
            plotEccdf.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plotEccdf.setRangeGridlinesVisible(true);
            plotEccdf.setRangeGridlinePaint(Color.LIGHT_GRAY);
            plotEccdf.setBackgroundPaint(Color.WHITE);

            XYItemRenderer rendererEccdf = plotEccdf.getRenderer();
            rendererEccdf.setSeriesStroke(0, new BasicStroke(3.0f));
            rendererEccdf.setSeriesStroke(1, new BasicStroke(3.0f));

            Font font3 = new Font("Dialog", Font.PLAIN, 20);
            Font font2 = new Font("Dialog", Font.PLAIN, 15);
            plotEccdf.getDomainAxis().setLabelFont(font3);
            plotEccdf.getRangeAxis().setLabelFont(font3);
            plotEccdf.getDomainAxis().setTickLabelFont(font2);
            plotEccdf.getRangeAxis().setTickLabelFont(font2);

            LegendTitle legendt = new LegendTitle(rendererEccdf);
            legendt.setItemFont(font2);
            legendt.setPosition(RectangleEdge.BOTTOM);
            chartEccdf.addLegend(legendt);

            try {
                ChartUtilities.saveChartAsJPEG(new File(folder.toString() + "/Labels/eccdf_" + i + "_" + legend.get(i) + ".png"), chartEccdf, 1024, 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
