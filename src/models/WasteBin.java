package models;

public abstract class WasteBin extends CityAsset {
    private double maxCapacity;
    private double currentFillLevel;
    private boolean isCriticalAlert;

    public WasteBin(String id, String loc, double maxCap) {
        super(id, loc);
        this.maxCapacity = maxCap;
        this.currentFillLevel = 0.0;
        this.isCriticalAlert = false;
    }

    public double getMaxCapacity() { return maxCapacity; }
    public double getCurrentFillLevel() { return currentFillLevel; }
    public double getFillPercentage() { return (currentFillLevel / maxCapacity) * 100; }
    public boolean hasCriticalAlert() { return isCriticalAlert; }

    public void addWaste(double amount) {
        this.currentFillLevel = Math.min(this.currentFillLevel + amount, maxCapacity);
        this.isCriticalAlert = getFillPercentage() >= 85.0;
    }

    public void emptyBin() {
        this.currentFillLevel = 0.0;
        this.isCriticalAlert = false;
    }
    public abstract String processWaste();
}
