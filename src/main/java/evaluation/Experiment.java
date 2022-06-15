package evaluation;

import moa.classifiers.trees.HoeffdingTree;
import moa.classifiers.Classifier;
import moa.core.TimingUtils;
import moa.streams.ArffFileStream;

import com.yahoo.labs.samoa.instances.Instance;
import java.io.IOException;


public class Experiment {

    public Experiment(){
    }

    public void run(int numInstances, boolean isTesting){
        Classifier learner = new HoeffdingTree();
//        ArffFileStream stream = new ArffFileStream("data/CIC-IDS-2017/ARFF/Friday-WorkingHours-Afternoon-DDosOrdered_bwd.arff", -1);
        ArffFileStream stream = new ArffFileStream("data/CIC-IDS-2017/ARFF/Friday-WorkingHours-Afternoon-DDosOrdered_fwd.arff", -1);
        stream.prepareForUse();

        learner.setModelContext(stream.getHeader());
        learner.prepareForUse();

        int numberSamplesCorrect = 0;
        int numberSamples = 0;
        boolean preciseCPUTiming = TimingUtils.enablePreciseTiming();
        long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();
        while (stream.hasMoreInstances() && numberSamples < numInstances) {
            Instance trainInst = stream.nextInstance().getData();
            if (isTesting) {
                if (learner.correctlyClassifies(trainInst)){
                    numberSamplesCorrect++;
                }
            }
            numberSamples++;
            learner.trainOnInstance(trainInst);
        }
        double accuracy = 100.0 * (double) numberSamplesCorrect/ (double) numberSamples;
        double time = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread()- evaluateStartTime);
        System.out.println(numberSamples + " instances processed with " + accuracy + "% accuracy in "+time+" seconds.");
    }

    public static void main(String[] args) throws IOException {
        Experiment exp = new Experiment();
        exp.run(1000000, true);
    }
}