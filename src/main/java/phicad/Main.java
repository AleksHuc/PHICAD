package phicad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * The class presents the object for running the PHICAD anomaly detection algorithm.
 */
public class Main {

    /**
     * The method sets necessary parameters and runs the entire anomaly detection algorithm.
     * @param args String[] array that presents general parameters.
     */
    public static void main(String[] args) {
        long startTime = System.nanoTime();

        ArrayList<File> arrayOfFiles = new ArrayList<>();

        arrayOfFiles.add(new File("data/CIC-IDS-2017/Monday-WorkingHoursOrdered.pcap_ISCX.csv"));
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Tuesday-WorkingHoursOrdered.pcap_ISCX.csv"));
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Wednesday-workingHoursOrdered.pcap_ISCX.csv"));
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Morning-WebAttacksOrdered.pcap_ISCX.csv"));
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Afternoon-InfilterationOrdered.pcap_ISCX.csv"));
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-MorningOrdered.pcap_ISCX.csv"));
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-PortScanOrdered.pcap_ISCX.csv"));
        arrayOfFiles.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-DDosOrdered.pcap_ISCX.csv"));

        ArrayList<String[]> arrayOfAnomalies = new ArrayList<>();
        arrayOfAnomalies.add(new String[]{});
        arrayOfAnomalies.add(new String[]{"SSH-Patator", "FTP-Patator"});
        arrayOfAnomalies.add(new String[]{"DoS Slowhttptest", "DoS GoldenEye", "Heartbleed", "DoS slowloris", "DoS Hulk"});
        arrayOfAnomalies.add(new String[]{"Web Attack � Sql Injection", "Web Attack � XSS", "Web Attack � Brute Force"});
        arrayOfAnomalies.add(new String[]{"Infiltration"});
        arrayOfAnomalies.add(new String[]{"Bot"});
        arrayOfAnomalies.add(new String[]{"PortScan"});
        arrayOfAnomalies.add(new String[]{"DDoS"});

        String resultsPathName = "results/";
        File dir = new File(resultsPathName);
        purgeDirectory(dir);

        try {
            PrintWriter pw = new PrintWriter("results/result_strings5.txt");

//        long a1 = 137239324;
//        long b1 = 84957;
//        a1 += b1 * b1;
//        System.out.println(a1);

/*        System.out.println("Detection against itself. ----------------------------------------");

        int index = 0;
        for(File fileName : arrayOfFiles) {
            System.out.println(fileName.getName());
            ArrayList<File> arrayOfFiles2 = new ArrayList<>();
            arrayOfFiles2.add(fileName);
            String folderName = "results/" + fileName.getName().split("\\.")[0] + "/";
            File directory = new File(folderName);
            if (! directory.exists()){
                boolean creation = directory.mkdir();
            }
            else {
//                System.out.println(Arrays.toString(directory.listFiles()));
                for(File file: directory.listFiles()) {
                    boolean deletion = file.delete();
                }
            }

            if (arrayOfAnomalies.get(index).length > 0) {
                PHICAD ad = new PHICAD(arrayOfFiles2, folderName, arrayOfAnomalies.get(index));
//            ad.runAnalysis();
                ad.runMultipleAnalysis();
            }

            index++;
        }

        System.out.println("Detection against all. ----------------------------------------");

        index = 0;
        for (String[] array : arrayOfAnomalies) {
            System.out.println(arrayOfFiles.get(index).getName());
            String folderName = "results/" + arrayOfFiles.get(index).getName().split("\\.")[0] + "_all/";
            File directory = new File(folderName);
            if (! directory.exists()){
                boolean creation = directory.mkdir();
            }
            else {
//                System.out.println(Arrays.toString(directory.listFiles()));
                for(File file: directory.listFiles()) {
                    boolean deletion = file.delete();
                }
            }
            if (array.length > 0) {
                PHICAD ad = new PHICAD(arrayOfFiles, folderName, arrayOfAnomalies.get(index));
//            ad.runAnalysis();
                ad.runMultipleAnalysis();
            }
            index++;
        }*/

//        PHICAD ad = new PHICAD(arrayOfFiles, "results/", anomalies);
//        ad.runAnalysis();
//        ad.runMultipleAnalysis();

//        PHICAD ad = new PHICAD(arrayOfFiles, "results/", arrayOfAnomalies.get(0));
////        ad.runAnalysis(pw);
//        ad.runMultipleAnalysis(pw);

        // Search for the best perf file result

//        for (int i = 0; i < arrayOfFiles.size(); i++) {
//            long innerStartTime = System.nanoTime();
//
//            System.out.println(arrayOfFiles.get(i).getName());
//            pw.println(arrayOfFiles.get(i).getName());
//            pw.flush();
//
//            ArrayList<File> currentFileArray = new ArrayList<>();
//            currentFileArray.add(arrayOfFiles.get(i));
//            PHICAD ad = new PHICAD(currentFileArray, "results/", arrayOfAnomalies.get(i));
//
//            ad.runMultipleAnalysis(pw);
//
//            long innerEstimatedTime = System.nanoTime() - innerStartTime;
//            long seconds = TimeUnit.NANOSECONDS.toSeconds(innerEstimatedTime) % 60;
//            long minutes = TimeUnit.NANOSECONDS.toMinutes(innerEstimatedTime) % 60;
//            long hours = TimeUnit.NANOSECONDS.toHours(innerEstimatedTime) % 24;
//
//            pw.printf("Elapsed time: %02d h, %02d m, %02d s %n", hours, minutes, seconds);
//        }


//             Test the best per file results

            double[] thresholds = new double[]{
//                    0.01,
                    0.11, 0.24, 0.03, 0.01, 0.12, 0.17, 0.20};
            double[] lambda = new double[]{
//                    0.009,
                    0.001, 0.006, 0.008, 0.001, 0.008, 0.009, 0.008};

            for (int i = 0; i < arrayOfFiles.size(); i++) {
                long innerStartTime = System.nanoTime();

                String fileName = arrayOfFiles.get(i).getName().split("\\.")[0];

                System.out.println(fileName + " " + thresholds[i]);
                pw.println(arrayOfFiles.get(i).getName());
                pw.flush();

                resultsPathName = "results/" + fileName + "/";
                File folder = new File(resultsPathName);
                if (folder.exists() && folder.isDirectory()) {
                    purgeDirectory(folder);
                }
                else {
                    boolean newFolder = folder.mkdir();
                    if (!newFolder) {
                        System.out.println("New folder creation failed!");
                    }
                }

                ArrayList<File> currentFileArray = new ArrayList<>();
                currentFileArray.add(arrayOfFiles.get(i));
                PHICAD ad = new PHICAD(currentFileArray, resultsPathName, arrayOfAnomalies.get(i));

                ad.runAnalysis(pw, thresholds[i], lambda[i], fileName);

                long innerEstimatedTime = System.nanoTime() - innerStartTime;
                long seconds = TimeUnit.NANOSECONDS.toSeconds(innerEstimatedTime) % 60;
                long minutes = TimeUnit.NANOSECONDS.toMinutes(innerEstimatedTime) % 60;
                long hours = TimeUnit.NANOSECONDS.toHours(innerEstimatedTime) % 24;

                pw.printf("Elapsed time: %02d h, %02d m, %02d s %n", hours, minutes, seconds);
            }

            // Run entire dataset

//            PHICAD ad = new PHICAD(arrayOfFiles, "results/", anomalies);
//            ad.runMultipleAnalysis(pw);
////            ad.runAnalysis(pw, 0.16, 0.002);

            long estimatedTime = System.nanoTime() - startTime;
            long seconds = TimeUnit.NANOSECONDS.toSeconds(estimatedTime) % 60;
            long minutes = TimeUnit.NANOSECONDS.toMinutes(estimatedTime) % 60;
            long hours = TimeUnit.NANOSECONDS.toHours(estimatedTime) % 24;

            pw.printf("Elapsed time: %02d h, %02d m, %02d s %n", hours, minutes, seconds);

            pw.flush();
            pw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void purgeDirectory(File dir) {
        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                purgeDirectory(file);
            }
            boolean result = file.delete();
            if (!result) {
                System.out.println("Delete failed: " + file.getName());
            }
        }
    }
}
