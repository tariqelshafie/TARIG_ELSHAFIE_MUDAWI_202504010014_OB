package simulation;

import models.WasteBin;

public interface Dispatchable { 
    String deploy(WasteBin targetBin, RoutingMap map); 
}
