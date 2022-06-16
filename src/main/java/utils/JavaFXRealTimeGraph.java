package utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

public class JavaFXRealTimeGraph implements Runnable{

    private LinkedBlockingQueue<double[]> queue;
    private ScheduledExecutorService scheduledExecutorService;
    private Stage primaryStage;
    private final int WINDOW_SIZE = 100;
    private int counter = 0;
    private final HashMap<Integer, String> labels = new HashMap<>();

    public JavaFXRealTimeGraph (Stage primaryStage, LinkedBlockingQueue<double[]> queue) {
        this.primaryStage = primaryStage;
        this.queue = queue;

    }

    @Override
    public void run() {
        primaryStage.setTitle("JavaFX Realtime Graph");

        labels.put(1, "Difference");
        labels.put(2, "Ground truth");
        labels.put(3, "Predictions");
        labels.put(4, "Destination IP 1");
        labels.put(5, "Destination IP 2");
        labels.put(6, "Destination IP 3");
        labels.put(7, "Destination IP 4");
        labels.put(8, "Source Port");
        labels.put(9, "Destination Port");
        labels.put(10, "Protocol");
        labels.put(11, "Total Length of Bwd Packets");
        labels.put(12, "Total Length of Fwd Packets");
        labels.put(13, "Total Backward Packets");
        labels.put(14, "Total Fwd Packets");
        labels.put(15, "Flow Duration");
        labels.put(16, "Timestamp Weekday Sin");
        labels.put(17, "Timestamp Weekday Cos");
        labels.put(18, "Timestamp Hour Sin");
        labels.put(19, "Timestamp Hour Cos");
        labels.put(20, "Timestamp Minute Sin");
        labels.put(21, "Timestamp Minute Cos");
        labels.put(22, "Timestamp Second Sin");
        labels.put(23, "Timestamp Second Cos");
        labels.put(24, "Timestamp Difference");

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Consecutive flow");
        xAxis.setAnimated(false);
        yAxis.setLabel("Values");
        yAxis.setAnimated(false);

        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setAnimated(false);

        ArrayList<XYChart.Series<String, Number>> s = new ArrayList<>();

        for (int i = 0; i < labels.size(); i++) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(labels.get(i + 1));
            s.add(series);
            lineChart.getData().add(series);
        }

        Scene scene = new Scene(lineChart, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {

            try {
                double[] data = this.queue.take();

                if (data.length == 0) {
                    scheduledExecutorService.shutdown();
                }
                else {
                    Platform.runLater(() -> {
                        double lower = 0.0;
                        for (int i = 0; i < data.length; i++) {
                            XYChart.Series<String, Number> cs = s.get(i);
                            XYChart.Data<String, Number> d = new XYChart.Data<>(Integer.toString(counter), lower + data[i]);
//                            Rectangle rect = new Rectangle(0, 0);
//                            rect.setVisible(false);
//                            d.setNode(rect);
                            cs.getData().add(d);
                            if (cs.getData().size() > WINDOW_SIZE) {
                                cs.getData().remove(0);
                            }
                            lower += 2.0;
                        }
                        counter++;
                    });

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 0, 100, TimeUnit.MILLISECONDS);
    }

}
