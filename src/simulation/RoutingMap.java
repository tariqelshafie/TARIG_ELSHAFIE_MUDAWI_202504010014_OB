package simulation;

import java.util.*;

public class RoutingMap {
    public static final String DEPOT = "Cyberjaya Depot";
    public static final String[] CYBERJAYA_NODES = {
        DEPOT, "DPULZE Mall", "Multimedia University", "Cyberjaya Lake Gardens", 
        "Tamarind Square", "Shaftsbury Square", "Cyberjaya Hospital"
    };

    private Map<String, Map<String, Integer>> graph = new HashMap<>();

    public RoutingMap() {
        for (String node : CYBERJAYA_NODES) graph.put(node, new HashMap<>());
        
        // Add bidirectional edges (distances in KM or relative weights)
        addEdge(DEPOT, "DPULZE Mall", 3);
        addEdge(DEPOT, "Shaftsbury Square", 4);
        addEdge("DPULZE Mall", "Tamarind Square", 2);
        addEdge("DPULZE Mall", "Multimedia University", 2);
        addEdge("Shaftsbury Square", "Multimedia University", 3);
        addEdge("Multimedia University", "Cyberjaya Hospital", 1);
        addEdge("Tamarind Square", "Cyberjaya Lake Gardens", 2);
        addEdge("Cyberjaya Lake Gardens", "Cyberjaya Hospital", 3);
    }

    private void addEdge(String src, String dest, int distance) {
        graph.get(src).put(dest, distance);
        graph.get(dest).put(src, distance);
    }

    public List<String> getShortestPathList(String destination) {
        if (!graph.containsKey(destination)) return Collections.singletonList(destination);

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(DEPOT, 0);
        queue.add(DEPOT);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(destination)) break;

            for (Map.Entry<String, Integer> neighborEntry : graph.get(current).entrySet()) {
                String neighbor = neighborEntry.getKey();
                int newDist = distances.get(current) + neighborEntry.getValue();

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        List<String> path = new ArrayList<>();
        String step = destination;
        if (previous.get(step) == null && !step.equals(DEPOT)) return Collections.singletonList(destination);

        while (step != null) {
            path.add(step);
            step = previous.get(step);
        }
        Collections.reverse(path);
        
        return path;
    }
    
    public String getShortestPath(String destination) {
        List<String> p = getShortestPathList(destination);
        return String.join(" -> ", p);
    }
}
