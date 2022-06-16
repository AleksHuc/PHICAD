package utils;

import javafx.application.Application;
import javafx.stage.Stage;
import phicad.FlowMessage;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class DataGenerator extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        LinkedBlockingQueue<double[]> bQueue = new LinkedBlockingQueue<>();
        JavaFXRealTimeGraph graph = new JavaFXRealTimeGraph(primaryStage, bQueue);
        graph.run();
        try {
            for (int i = 0; i < 1000; i++) {

                double lower = 0.0;
                double[] vector = new double[10];
                for (int j = 0; j < 10; j++) {

                    vector[j] = ThreadLocalRandom.current().nextDouble(lower, lower + 1.0);
                    lower += 2;
                }

                bQueue.put(vector);
            }

            bQueue.put(new double[0]);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }


    public static void main (String[] args) {
        Application.launch(args);
    }

}
