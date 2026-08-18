package models;

import java.util.Random;

public class RecyclableBin extends WasteBin {
    private double paperWeight = 0;
    private double plasticWeight = 0;
    private double metalWeight = 0;
    private Random random = new Random();

    public RecyclableBin(String id, String loc) { super(id, loc, 80.0); }
    
    @Override 
    public void addWaste(double amount) {
        double before = getCurrentFillLevel();
        super.addWaste(amount);
        double actualAdded = getCurrentFillLevel() - before;
        
        if (actualAdded > 0) {
            double p1 = 0.2 + random.nextDouble(); // Ensure at least some of each
            double p2 = 0.2 + random.nextDouble();
            double p3 = 0.2 + random.nextDouble();
            double total = p1 + p2 + p3;
            
            paperWeight += actualAdded * (p1 / total);
            plasticWeight += actualAdded * (p2 / total);
            metalWeight += actualAdded * (p3 / total);
        }
    }
    
    @Override
    public void emptyBin() {
        super.emptyBin();
        paperWeight = 0;
        plasticWeight = 0;
        metalWeight = 0;
    }

    public double getPaperWeight() { return paperWeight; }
    public double getPlasticWeight() { return plasticWeight; }
    public double getMetalWeight() { return metalWeight; }

    @Override public String processWaste() { return "RECYCLING PLANT ROUTE"; }
}
