package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TXTReader {

    private String fileName;
    private int length;

    private double[][] data;

    public TXTReader(String fileName, int length) {
        this.fileName = fileName;
        this.length = length;
    }

    public void readData() {
        ArrayList<double[]> dataList = new ArrayList<>();
        try {
            FileReader fr = new FileReader(this.fileName);
            BufferedReader br = new BufferedReader(fr);

            int counter = 0;
            for(String line; (line = br.readLine()) != null; ) {
                String[] sFlow = line.split(" ");
                double[] singleLine = new double[sFlow.length];
                for (int i = 0; i < sFlow.length; i++) {
                    singleLine[i] = Double.valueOf(sFlow[i]);
                }
                dataList.add(singleLine);
                counter++;
                if (counter >= this.length) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.data = new double[dataList.size()][dataList.get(0).length];
        for (int i = 0; i < dataList.size(); i++) {
            this.data[i] = dataList.get(i);
        }
    }

    public void normalize() {

        double[] maxValue = new double[data[0].length];
        double[] minValue = new double[data[0].length];
        for (int i = 0; i < data[0].length; i++){
            maxValue[i] = 0.0;
            minValue[i] = Double.MAX_VALUE;
        }

        for (int i = 0; i < data.length; i++) {

            for (int j = 0; j < data[i].length; j++) {

                if (data[i][j] > maxValue[j]) {
                    maxValue[j] = data[i][j];
                }

                if (data[i][j] < minValue[j]) {
                    minValue[j] = data[i][j];
                }
            }
        }

        for (int i = 0; i < data.length; i++) {

            for (int j = 0; j < data[i].length; j++) {

                data[i][j] = (data[i][j] - minValue[j]) / (maxValue[j] - minValue[j]);
            }
        }
    }

    public double[][] getData() {
        return data;
    }
}
