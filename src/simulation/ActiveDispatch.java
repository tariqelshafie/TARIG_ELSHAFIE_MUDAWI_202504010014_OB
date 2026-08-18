package simulation;
import java.util.List;

public class ActiveDispatch {
    public String truckID;
    public List<String> pathNodes;
    public double progress; // 0.0 to 1.0
    
    public ActiveDispatch(String id, List<String> pathNodes) {
        this.truckID = id;
        this.pathNodes = pathNodes;
        this.progress = 0.0;
    }
}
