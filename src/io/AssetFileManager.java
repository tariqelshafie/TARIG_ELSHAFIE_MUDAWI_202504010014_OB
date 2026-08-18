package io;

import models.WasteBin;
import models.GeneralWasteBin;
import models.RecyclableBin;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

public class AssetFileManager {
    private static final String BINS_CONFIG_FILE = "../bins_config.csv";
    private static final String DISPATCH_LOG_FILE = "../dispatch_logs.csv";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CopyOnWriteArrayList<WasteBin> loadBinsConfig() {
        CopyOnWriteArrayList<WasteBin> bins = new CopyOnWriteArrayList<>();
        File file = new File(BINS_CONFIG_FILE);

        if (!file.exists()) {
            bins.add(new GeneralWasteBin("G1-DP", "DPULZE Mall"));
            bins.add(new RecyclableBin("R1-DP", "DPULZE Mall"));
            saveBinsConfig(bins);
            return bins;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 3) continue;
                String type = data[0];
                String id = data[1];
                String loc = data[2];

                if (type.equals("GENERAL")) bins.add(new GeneralWasteBin(id, loc));
                else if (type.equals("RECYCLABLE")) bins.add(new RecyclableBin(id, loc));
            }
        } catch (IOException e) { e.printStackTrace(); }
        return bins;
    }

    public void saveBinsConfig(CopyOnWriteArrayList<WasteBin> bins) {
        try (PrintWriter out = new PrintWriter(new FileWriter(BINS_CONFIG_FILE))) {
            out.println("Type,ID,Location,Capacity_KG");
            for (WasteBin bin : bins) {
                out.println(String.format("%s,%s,%s,%.1f",
                        (bin instanceof GeneralWasteBin) ? "GENERAL" : "RECYCLABLE",
                        bin.getAssetID(), bin.getLocation(), bin.getMaxCapacity()));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void logDispatchEvent(String vehicleLog) {
        try (PrintWriter out = new PrintWriter(new FileWriter(DISPATCH_LOG_FILE, true))) {
            out.println(dateFormat.format(new Date()) + "," + vehicleLog);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
