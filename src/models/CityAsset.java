package models;

public abstract class CityAsset {
    protected String assetID;
    protected String location;
    public CityAsset(String id, String loc) { this.assetID = id; this.location = loc; }
    public String getAssetID() { return assetID; }
    public String getLocation() { return location; }
}
