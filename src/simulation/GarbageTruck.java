package simulation;

import models.WasteBin;

public class GarbageTruck implements Dispatchable {
    private String truckID;
    public GarbageTruck(String id) { this.truckID = id; }
    @Override public String deploy(WasteBin targetBin, RoutingMap map) {
        String route = map.getShortestPath(targetBin.getLocation());
        return String.format("[TRUCK] %s collected General Waste at [%s]. Path: %s. Process: %s",
                truckID, targetBin.getLocation(), route, targetBin.processWaste());
    }
}
