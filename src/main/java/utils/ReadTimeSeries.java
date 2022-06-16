package utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.*;

public class ReadTimeSeries {

    public static void main(String[] args) {

        String fileEntry = "results/0_192/results_0_192_bwd.txt";
//        int dataLocation = 11;
//        String name = "_F_Source_Port";

        int dataLocation = 12;
        String name = "_F_Destination_Port";

        XYSeries series = new XYSeries(name);

        try {

            FileReader fr = new FileReader(fileEntry);
            BufferedReader br = new BufferedReader(fr);

            int counter = 0;
            PrintWriter pw = new PrintWriter(fileEntry.split("\\.")[0] + name + ".txt");
            for(String line; (line = br.readLine()) != null;) {
                String[] data = line.split(" ");

                pw.println(data[dataLocation]);
                series.add(counter, Double.parseDouble(data[dataLocation]));
//                System.out.println(line);
                counter++;
            }

            pw.flush();
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                name, "Consecutive flows",
                "Normalized Values", dataset,
                PlotOrientation.VERTICAL, true, true, false
        );

        chart.getPlot().setBackgroundPaint(Color.WHITE);


        try {
            ChartUtilities.saveChartAsJPEG(new File(fileEntry.split("\\.")[0] + name + ".png"), chart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
