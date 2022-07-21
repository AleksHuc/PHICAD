package phicad;

import utils.FlowMessage;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

import static utils.Utils.concatTwo;

/**
 * The class implements a worker that runs PHICAD in parallel.
 */
public class Worker implements Callable{

    private int[] selectedValues;
    private DateTimeFormatter[] formats;
    private ArrayList<String> selectedValuesProcessing;
    private String threadName;
    private String resultsPathName2;
    private BlockingQueue linkedBlockingQueue;
    private HashMap<Integer,long[]> minMaxValues;
    private List<Double> parameters;
    private String[] anomalies;
    private boolean printOut;
    private int pointLength = 21;
    private int distanceFunction = 0;

    /**
     * The constructor creates new Worker object with the given parameters.
     * @param threadName String object that presents the unique name of the current worker.
     * @param linkedBlockingQueue LinkedBlockingQueue object that is used by the worker to receive new data for analysis.
     * @param minMaxValues HashMap&lt;Integer,long[]&gt; object that presents the minimum and maximum values for each network flow feature we analyze.
     * @param selectedValues Integer array that presents the indexes of network flow features to be used in analysis.
     * @param selectedValuesProcessing ArrayList&lt;String&gt; object that presents how each selected network feature should be analyzed.
     * @param parameters List&lt;Double&gt; object that presents the current configuration of the parameters used for analysis.
     * @param anomalies String array that presents the labels that present anomalies in the dataset.
     * @param resultsPathName2 String object that presents the given output folder for the result files.
     * @param printOut boolean value that present if verbose print in enabled.
     */
    Worker(String threadName, LinkedBlockingQueue linkedBlockingQueue, HashMap<Integer,long[]> minMaxValues, int[] selectedValues,
           ArrayList<String> selectedValuesProcessing, List<Double> parameters, String[] anomalies, String resultsPathName2, boolean printOut
    ) {

        this.selectedValuesProcessing = selectedValuesProcessing;
        this.threadName = threadName;
        this.linkedBlockingQueue = linkedBlockingQueue;
        this.minMaxValues = minMaxValues;
        this.selectedValues = selectedValues;
        this.parameters = parameters;
        this.formats = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("dd/MM/yyyy H:m"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy H:m:s")
        };
        this.anomalies = anomalies;
        this.resultsPathName2 = resultsPathName2;
        this.printOut = printOut;
    }

    /**
     * The method presents the call method for starting the Worker thread.
     */
    @Override
    public Object call() {
        HashMap<String, Profile> profiles = new HashMap<>();

        long counter = 0L;

        try {

            final HashMap<String, PrintWriter> pw2 = new HashMap<>();

            while (true) {
                FlowMessage data = (FlowMessage) this.linkedBlockingQueue.take();

                if (data.getFlow()[0].equals("END")) {
                    break;
                }

                String id = String.valueOf(counter);

                String currentIP = data.getFlow()[0];
                Profile profile = profiles.get(currentIP);
                if (profile == null) {
                    profile = new Profile(minMaxValues, selectedValues, selectedValuesProcessing, 0, pointLength, formats, printOut, distanceFunction, currentIP,
                            parameters.get(0).intValue(), parameters.get(1).intValue(), parameters.get(2), parameters.get(3), parameters.get(4).intValue(), parameters.get(5).intValue(), parameters.get(6).intValue(), parameters.get(7), parameters.get(8).intValue(), parameters.get(9), parameters.get(10).intValue(), parameters.get(11), parameters.get(12), parameters.get(13));
                    profiles.put(currentIP, profile);
                    pw2.put(currentIP + "_fwd", new PrintWriter(resultsPathName2 + "results_" + threadName + "_" + currentIP + "_bwd.txt"));
                    pw2.put(currentIP + "_bwd", new PrintWriter(resultsPathName2 + "results_" + threadName + "_" + currentIP + "_fwd.txt"));
                }

                double groundTruth = 0.0;
                String currentLabel = data.getFlow()[data.getFlow().length - 1].trim();
                for (String label : anomalies) {
                    if (currentLabel.equals(label)) {
                        groundTruth = 1.0;
                    }
                }

                double[] anomaly = profile.updateProfile(data);

                double[] response = new double[]{anomaly[1], groundTruth, anomaly[0], anomaly[2], anomaly[3], anomaly[4]};

                if (printOut) {
                    response = concatTwo(response, Arrays.copyOfRange(anomaly, 5, anomaly.length));
                }

                StringBuilder builder = new StringBuilder();
                builder.append(currentIP);
                builder.append("_");
                builder.append(data.getDirection());
                builder.append("_");
                builder.append(id);
                builder.append(" ");
                for (double s : response) {
                    builder.append(s);
                    builder.append(" ");
                }
                String writableResponse = builder.toString();

                PrintWriter current = pw2.get(currentIP + "_" + data.getDirection());

                current.println(writableResponse);
                current.flush();
                counter++;
            }

            for (Map.Entry<String, PrintWriter> entry : pw2.entrySet()) {
                entry.getValue().flush();
                entry.getValue().close();
            }

            return counter;

        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
        return counter;
    }
}
