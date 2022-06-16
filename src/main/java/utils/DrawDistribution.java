package utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class DrawDistribution {
    public static void main(String[] args) {

        File folder = new File("results/");
        String startsWith = "tree_test";

        HashMap<String, XYSeries> results = new HashMap<>();
        results.put("Harmonic Mean a", new XYSeries("Harmonic Mean"));
        results.put("Mean a", new XYSeries("Mean"));
        results.put("STD a", new XYSeries("STD"));
        results.put("Harmonic Mean t", new XYSeries("Harmonic Mean"));
        results.put("Mean t", new XYSeries("Mean"));
        results.put("STD t", new XYSeries("STD"));
        results.put("Harmonic Mean n", new XYSeries("Harmonic Mean"));
        results.put("Mean n", new XYSeries("Mean"));
        results.put("STD n", new XYSeries("STD"));

        int outsideCounter = 0;

        TreeMap<Long, HashMap<String, TreeMap<Long, AtomicLong>>> data = new TreeMap<>();

        try{
            for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
//                System.out.println(fileEntry);

                if (fileEntry.isDirectory()) {
                    for (final File fileEntry2 : Objects.requireNonNull(fileEntry.listFiles())) {
                        if (fileEntry2.getName().startsWith(startsWith) && !fileEntry2.isDirectory()) {

                            String skey = fileEntry2.getName().split("_")[4].split("\\.")[0];

                            if (skey.equals("end")) {
                                skey = "2900000";
                            }

                            Long fileKey = Long.parseLong(skey);
                            data.putIfAbsent(fileKey, new HashMap<>());

                            HashMap<String, TreeMap<Long, AtomicLong>> currentData = data.get(fileKey);
                            currentData.putIfAbsent("n", new TreeMap<>());
                            currentData.putIfAbsent("t", new TreeMap<>());
                            currentData.putIfAbsent("a", new TreeMap<>());

                            FileReader fr = new FileReader(fileEntry2);
                            BufferedReader br = new BufferedReader(fr);
                            int counter = 0;
                            for(String line; (line = br.readLine()) != null; ) {

                                if (counter > 0) {
                                    String[] sLine = line.split(" ");

//                                    System.out.println(Arrays.toString(sLine));

                                    for (int i = 0; i < sLine.length; i++) {
                                        if (sLine[i].equals("L")) {
                                            for (int k = 0; k < sLine.length; k++) {
//                                                if (sLine[k].equals("N")) {
                                                    for (int j = 0; j < sLine.length; j++) {
                                                        if (sLine[j].startsWith("N=")) {
                                                            long n = Long.parseLong(sLine[j].split("=")[1]);
                                                            TreeMap<Long, AtomicLong> innerData = currentData.get("n");
                                                            innerData.putIfAbsent(n, new AtomicLong());
                                                            innerData.get(n).getAndIncrement();
                                                        }
                                                        else if (sLine[j].startsWith("TS:")) {
                                                            long ts = Long.parseLong(sLine[j+1]);
                                                            if (ts > 0) {
                                                                TreeMap<Long, AtomicLong> innerData = currentData.get("t");
                                                                innerData.putIfAbsent(ts, new AtomicLong());
                                                                innerData.get(ts).getAndIncrement();
                                                            }
//                                                            else if (ts < 0){
//                                                                System.nanoTime();
//                                                            }
                                                        }

                                                        else if (sLine[j].startsWith("CT:")) {
                                                            long ts = Long.parseLong(sLine[j+1]);
                                                            if (ts > 0) {
                                                                TreeMap<Long, AtomicLong> innerData = currentData.get("a");
                                                                innerData.putIfAbsent(ts, new AtomicLong());
                                                                innerData.get(ts).getAndIncrement();
                                                            }
//                                                            else if (ts < 0){
//                                                                System.out.println("TS < 0");
//                                                                System.nanoTime();
//                                                            }
                                                        }
                                                    }
//                                                }
                                            }
                                        }
                                    }
                                }
                                counter++;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map.Entry<Long, HashMap<String, TreeMap<Long, AtomicLong>>> entry : data.entrySet()) {

            for (Map.Entry<String, TreeMap<Long, AtomicLong>> entry2: entry.getValue().entrySet()) {

                XYSeries series = new XYSeries(entry2.getKey());

                double threshold = 0.1;

                double maxX = 0.0;
                double maxY = 0.0;

                double lsum = 0.0;
                double lsum2 = 0.0;
                double lsum3 = 0.0;
                double ssum = 0.0;

                long innerC = 0;
                for (Map.Entry<Long, AtomicLong> entry3: entry2.getValue().entrySet()) {
                    innerC += entry3.getValue().get();

                    if ((double)entry3.getKey() > maxX) {
                        maxX = (double)entry3.getKey();
                    }

                    if ((double)entry3.getValue().get() > maxY) {
                        maxY = (double)entry3.getValue().get();
                    }
                }

                System.out.println("-----------------------------------------------------------------");

                System.out.println(entry2.getKey() + "_" + entry.getKey());

                System.out.println("innerC: " + innerC);

                for (Map.Entry<Long, AtomicLong> entry3: entry2.getValue().entrySet()) {
//                    series.add((double)entry3.getKey() / maxX, (double)entry3.getValue().get() / maxY);
                    series.add((double)entry3.getKey(), (double)entry3.getValue().get());

                    lsum += (double)entry3.getKey()/maxX * (double)entry3.getValue().get();
                    lsum2 += (double)entry3.getKey() * (double)entry3.getValue().get();
                    lsum3 += (1.0 / (double)entry3.getKey() * (double)entry3.getValue().get());
//                    System.out.println(1.0 / (double)entry3.getKey());
                    ssum += (double)entry3.getKey()/maxX * (double)entry3.getValue().get() * (double)entry3.getKey()/maxX * (double)entry3.getValue().get();
                }

                System.out.println("hMean: " + innerC / lsum3);
                results.get("Harmonic Mean " + entry2.getKey()).add((double)entry.getKey(), innerC / lsum3);

                XYSeries seriesExp = new XYSeries("Gamma");
                double mean = lsum / innerC;
                double variance = (ssum - (2 * lsum * mean) + lsum * (mean * mean)) / lsum;

                System.out.println("Mean: " + lsum2 / innerC);
                results.get("Mean " + entry2.getKey()).add((double)entry.getKey(), lsum2 / innerC);

//                System.out.println(entry2.getKey() + "_" + entry.getKey() + "_lambda: " + 1.0/mean);
//                System.out.println(entry2.getKey() + "_" + entry.getKey() + "_std: " + Math.sqrt(1.0/(1.0/mean * 1.0/mean)));
//                System.out.println(entry2.getKey() + "_" + entry.getKey() + "_std: " + Math.sqrt((ssum - (2 * lsum * mean) + lsum * (mean * mean)) / lsum));
                results.get("STD " + entry2.getKey()).add((double)entry.getKey(), Math.sqrt((ssum - (2 * lsum2 * lsum2 / innerC) + lsum2 * ((lsum2 / innerC) * (lsum2 / innerC))) / lsum2));
                System.out.println("STD: " + Math.sqrt((ssum - (2 * lsum2 * lsum2 / innerC) + lsum2 * ((lsum2 / innerC) * (lsum2 / innerC))) / lsum2));

//                for (double i = 0.00000001; i < 1.01; i+= 0.01) {
//                    seriesExp.add(i, 1.0/mean * Math.pow(Math.E, -1.0/mean * i));
//                }

                double alpha = (mean * mean) / (variance - mean * mean);
                double beta = (variance - mean * mean) / (mean * mean);

//                System.out.println(entry2.getKey() + "_" + entry.getKey() + "_" + alpha + "_" + beta);

                for (double i = 0.000000000000001; i < 1.01; i+= 0.01) {
                    seriesExp.add(i, (Math.pow(beta, alpha) * Math.pow(i, alpha - 1.0) * Math.pow(Math.E, - beta * i)) / gamma(alpha));
                }

                XYSeriesCollection dataset = new XYSeriesCollection();
                dataset.addSeries(series);
//                dataset.addSeries(seriesExp);

                JFreeChart chart = ChartFactory.createXYLineChart(
                        "Distribution " + entry2.getKey(), "Value", "Counts", dataset,
                        PlotOrientation.VERTICAL, true, true, false
                );

                XYPlot plot = chart.getXYPlot();
                plot.setBackgroundPaint(Color.WHITE);

                NumberAxis domain = (NumberAxis) plot.getDomainAxis();
//                domain.setRange(-maxX/10.0, maxX + maxX/10.0);
//                domain.setRange(-maxX/10.0, maxX/10.0);
//                domain.setRange(-0.1, 1.1);
                domain.setRange(-1.1, maxX + 1.1);

                NumberAxis range = (NumberAxis) plot.getRangeAxis();
//                range.setRange(-maxY/10.0, maxY + maxY/10.0);
//                range.setRange(-0.1, 1.1);
                range.setRange(-1.1, maxY + 1.1);

                try {
                    ChartUtilities.saveChartAsJPEG(new File(folder.toString() + "/distribution_" + entry2.getKey() + "_" + entry.getKey() + ".png"), chart, 1024, 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                long cumsum = 0L;
                XYSeries ecdf = new XYSeries("Empirical Cumulative Distribution Function");

                boolean flag = true;
                boolean flag2 = true;
                for (Map.Entry<Long, AtomicLong> entryE : entry2.getValue().entrySet()) {
                    cumsum += entryE.getValue().get();

                    double y = (double)cumsum / (double)innerC;

                    double eps = 0.0;
                    if ((double)entryE.getKey() == 0.0) {
                        eps = 1.0;
                    }

                    if (cumsum > innerC/4.0 && flag2) {
//                        System.out.printf("Q1: " + innerC/4.0 + " for " + entry2.getKey() + "_" + entry.getKey() + " X: " + (double)entryE.getKey() + " Y: %f \n", y);
                        flag2 = false;
                    }

                    ecdf.add((double)entryE.getKey() + eps, y);
                }

//                System.out.println("0.05: for " + entry2.getKey() + "_" + entry.getKey() + " T: " + 0.05*(lsum2/innerC));

                XYSeriesCollection datasetEcdf = new XYSeriesCollection();
                datasetEcdf.addSeries(ecdf);

                JFreeChart chartEcdf = ChartFactory.createXYLineChart(
                        "Empirical Cumulative Distribution Function " + entry2.getKey() + "_" + entry.getKey(), "Value", "Counts", datasetEcdf,
                        PlotOrientation.VERTICAL, true, true, false
                );

                LogarithmicAxis yAxisEcdf = new LogarithmicAxis("Log10 Probability");
                LogarithmicAxis xAxisEcdf = new LogarithmicAxis("Log10 Value");

                XYPlot plotEcdf = chartEcdf.getXYPlot();
                plotEcdf.setDomainAxis(xAxisEcdf);
                plotEcdf.setRangeAxis(yAxisEcdf);
                plotEcdf.setDomainGridlinesVisible(true);
                plotEcdf.setDomainGridlinePaint(Color.LIGHT_GRAY);
                plotEcdf.setRangeGridlinesVisible(true);
                plotEcdf.setRangeGridlinePaint(Color.LIGHT_GRAY);
                plotEcdf.setBackgroundPaint(Color.WHITE);

                try {
                    ChartUtilities.saveChartAsJPEG(new File(folder.toString() + "/ecdf_" + entry2.getKey() + "_" + entry.getKey() + ".png"), chartEcdf, 1024, 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                XYSeries eccdf = new XYSeries("Empirical Complementary Cumulative Distribution Function");
                for (Map.Entry<Long, AtomicLong> entryE : entry2.getValue().entrySet()) {
                    cumsum -= entryE.getValue().get();

                    double y = (double)cumsum / innerC;
//                System.out.println(cumsum);


                    double eps = 0.0;
                    if ((double)entryE.getKey() == 0.0) {
                        eps = 1.0;
                    }
                    if (y != 0.0) {
                        eccdf.add((double)entryE.getKey() + eps, y);

                        if (y < threshold && flag) {
                            System.out.printf("Threshold: " + threshold + " for " + entry2.getKey() + "_" + entry.getKey() + " X: " + (double)entryE.getKey() + " Y: %f \n", y);
                            flag = false;
                        }

                    }
                }

                XYSeriesCollection datasetEccdf = new XYSeriesCollection();
                datasetEccdf.addSeries(eccdf);

                JFreeChart chartEccdf = ChartFactory.createXYLineChart(
                        "Empirical Complementary Cumulative Distribution Function " + entry2.getKey() + "_" + entry.getKey(), "Value", "Counts", datasetEccdf,
                        PlotOrientation.VERTICAL, true, true, false
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

                try {
                    ChartUtilities.saveChartAsJPEG(new File(folder.toString() + "/eccdf_" + entry2.getKey() + "_" + entry.getKey() + ".png"), chartEccdf, 1024, 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (String entry : new String[]{"a", "n", "t"}) {
            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(results.get("Mean " + entry));

            JFreeChart chartEcdf = ChartFactory.createXYLineChart(
                "Mean " + entry, "Value", "Counts", dataset,
                PlotOrientation.VERTICAL, true, true, false
            );

            try {
                ChartUtilities.saveChartAsJPEG(new File(folder.toString() + "/mean_" + entry + ".png"), chartEcdf, 1024, 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }

            XYSeriesCollection dataset2 = new XYSeriesCollection();
            dataset2.addSeries(results.get("Harmonic Mean " + entry));

            JFreeChart chartEcdf2 = ChartFactory.createXYLineChart(
                    "Harmonic Mean " + entry, "Value", "Counts", dataset2,
                    PlotOrientation.VERTICAL, true, true, false
            );

            try {
                ChartUtilities.saveChartAsJPEG(new File(folder.toString() + "/harmonicmean_" + entry + ".png"), chartEcdf2, 1024, 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }
    static double logGamma(double x) {
        double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
        double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
                + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
                +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
        return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
    }
    static double gamma(double x) { return Math.exp(logGamma(x)); }
}
