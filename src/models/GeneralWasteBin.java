package models;

public class GeneralWasteBin extends WasteBin {
    public GeneralWasteBin(String id, String loc) { super(id, loc, 100.0); }
    @Override public String processWaste() { return "LANDFILL ROUTE"; }
}
