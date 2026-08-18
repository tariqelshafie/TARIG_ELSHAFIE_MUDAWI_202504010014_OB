package io;

import java.io.*;
import java.util.Properties;

public class StatsManager {
    private static final String STATS_FILE = "../stats.properties";
    private Properties properties;

    public StatsManager() {
        properties = new Properties();
        loadStats();
    }

    private void loadStats() {
        File file = new File(STATS_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void saveStats(double totalCollected, int vehiclesDispatched, double carbonSaved) {
        properties.setProperty("totalCollected", String.valueOf(totalCollected));
        properties.setProperty("vehiclesDispatched", String.valueOf(vehiclesDispatched));
        properties.setProperty("carbonSaved", String.valueOf(carbonSaved));

        try (FileOutputStream out = new FileOutputStream(STATS_FILE)) {
            properties.store(out, "Smart City Statistics");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public double getTotalCollected() {
        return Double.parseDouble(properties.getProperty("totalCollected", "0.0"));
    }

    public int getVehiclesDispatched() {
        return Integer.parseInt(properties.getProperty("vehiclesDispatched", "0"));
    }

    public double getCarbonSaved() {
        return Double.parseDouble(properties.getProperty("carbonSaved", "0.0"));
    }
    
    public void resetStats() {
        properties.clear();
        saveStats(0.0, 0, 0.0);
    }
}
