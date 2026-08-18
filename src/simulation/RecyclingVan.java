package simulation;

import models.WasteBin;

public class RecyclingVan implements Dispatchable {
    private String vanID;
    public RecyclingVan(String id) { this.vanID = id; }
    @Override public String deploy(WasteBin targetBin, RoutingMap map) {
        String route = map.getShortestPath(targetBin.getLocation());
        return String.format("[VAN] %s collected Recyclables at [%s]. Path: %s. Process: %s",
                vanID, targetBin.getLocation(), route, targetBin.processWaste());
    }
}
