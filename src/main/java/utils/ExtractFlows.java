package utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExtractFlows {

    public static void main(String args[]) {
        ArrayList<File> arrayOfFiles = new ArrayList<>();

        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedSatJun12Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedSunJun13Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedMonJun14Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedTueJun15-1Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedTueJun15-2Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedTueJun15-3Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedWedJun16-1Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedWedJun16-2Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedWedJun16-3Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedThuJun17-1Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedThuJun17-2Flows.csv"));
        arrayOfFiles.add(new File("data/ISCX-IDS-2012/TestbedThuJun17-3Flows.csv"));

//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Monday-WorkingHoursOrdered.pcap_ISCX.csv"));                              // 3.7.2017: 13:55:58 - 22:01:34
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Tuesday-WorkingHoursOrdered.pcap_ISCX.csv"));                             // 4.7.2017: 13:53:32 - 22:00:31
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Wednesday-workingHoursOrdered.pcap_ISCX.csv"));                           // 5.7.2017: 13:42:42 - 22:10:19
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Morning-WebAttacksOrdered.pcap_ISCX.csv"));         // 6.7.2017: 13:58:58 - 22:04:44
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Thursday-WorkingHours-Afternoon-InfilterationOrdered.pcap_ISCX.csv"));    //
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-MorningOrdered.pcap_ISCX.csv"));                      // 7.7.2017: 13:59:39 - 22:02:41
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-PortScanOrdered.pcap_ISCX.csv"));           //
//        arrayOfFiles.add(new File("data/CIC-IDS-2017/Friday-WorkingHours-Afternoon-DDosOrdered.pcap_ISCX.csv"));               //

        HashMap<String,ArrayList<String>> flows = new HashMap<>();

        flows.put("192.168.1.101_bwd", new ArrayList<>());
        flows.put("192.168.1.101_fwd", new ArrayList<>());
        flows.put("192.168.1.102_bwd", new ArrayList<>());
        flows.put("192.168.1.103_bwd", new ArrayList<>());
        flows.put("192.168.1.104_bwd", new ArrayList<>());
        flows.put("192.168.1.105_bwd", new ArrayList<>());
        flows.put("192.168.1.105_fwd", new ArrayList<>());
        flows.put("192.168.2.106_bwd", new ArrayList<>());
        flows.put("192.168.2.106_fwd", new ArrayList<>());
        flows.put("192.168.2.108_bwd", new ArrayList<>());
        flows.put("192.168.2.109_bwd", new ArrayList<>());
        flows.put("192.168.2.110_bwd", new ArrayList<>());
        flows.put("192.168.2.111_bwd", new ArrayList<>());
        flows.put("192.168.2.112_bwd", new ArrayList<>());
        flows.put("192.168.2.112_fwd", new ArrayList<>());
        flows.put("192.168.2.113_bwd", new ArrayList<>());
        flows.put("192.168.2.113_fwd", new ArrayList<>());
        flows.put("192.168.3.114_bwd", new ArrayList<>());
        flows.put("192.168.3.115_bwd", new ArrayList<>());
        flows.put("192.168.3.115_fwd", new ArrayList<>());
        flows.put("192.168.3.117_bwd", new ArrayList<>());
        flows.put("192.168.3.117_fwd", new ArrayList<>());
        flows.put("192.168.5.122_bwd", new ArrayList<>());


//        flows.put("52.6.13.28_bwd", new ArrayList<>());
//        flows.put("52.7.235.158_bwd", new ArrayList<>());
//        flows.put("172.16.0.1_fwd", new ArrayList<>());
//        flows.put("172.16.0.1_bwd", new ArrayList<>());
//        flows.put("192.168.10.5_fwd", new ArrayList<>());
//        flows.put("192.168.10.5_bwd", new ArrayList<>());
//        flows.put("192.168.10.8_fwd", new ArrayList<>());
//        flows.put("192.168.10.8_bwd", new ArrayList<>());
//        flows.put("192.168.10.9_fwd", new ArrayList<>());
//        flows.put("192.168.10.9_bwd", new ArrayList<>());
//        flows.put("192.168.10.12_fwd", new ArrayList<>());
//        flows.put("192.168.10.14_fwd", new ArrayList<>());
//        flows.put("192.168.10.14_bwd", new ArrayList<>());
//        flows.put("192.168.10.15_fwd", new ArrayList<>());
//        flows.put("192.168.10.15_bwd", new ArrayList<>());
//        flows.put("192.168.10.17_fwd", new ArrayList<>());
//        flows.put("192.168.10.50_fwd", new ArrayList<>());
//        flows.put("192.168.10.50_bwd", new ArrayList<>());
//        flows.put("192.168.10.51_bwd", new ArrayList<>());
//        flows.put("205.174.165.73_fwd", new ArrayList<>());
//        flows.put("205.174.165.73_bwd", new ArrayList<>());

        String firstLine = null;

        for (File currentFile : arrayOfFiles) {
            try {
                System.out.println(currentFile);
                FileReader fr = new FileReader(currentFile);
                BufferedReader br = new BufferedReader(fr);

                long counter = 0L;
                for(String line; (line = br.readLine()) != null; ) {

                    if(counter > 0) {

                        String[] sFlow = line.split(",");

                        if (sFlow.length > 0) {
                            for (String s : new String[]{sFlow[1].trim() + "_fwd", sFlow[3].trim() + "_bwd"}) {
                                if (flows.containsKey(s)) {
                                    flows.get(s).add(line);
                                }
                            }
                        }
                    }
                    else {
                        firstLine = line;
                    }
                    counter++;
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        int c = 0;
        for (Map.Entry<String, ArrayList<String>> entry : flows.entrySet()) {
            PrintWriter f0 = null;
            try {
                f0 = new PrintWriter(new FileWriter("data/ISCX-IDS-2012/" + c + "_" + entry.getKey() + "_Ordered_Flows.csv"));

                f0.print(firstLine + "\n");

                for (String l: entry.getValue()) {
                    f0.print(l + "\n");
                }

                f0.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            c++;
        }
    }
}
