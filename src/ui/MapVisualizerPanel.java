package ui;

import simulation.ActiveDispatch;
import simulation.RoutingMap;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapVisualizerPanel extends RoundedPanel {
    private CopyOnWriteArrayList<ActiveDispatch> activeDispatches;
    
    private HashMap<String, Point> nodePositions = new HashMap<>();
    private Color nodeColor = Color.decode("#64748B");
    private Color edgeColor = Color.decode("#1E293B");
    private Color textMuted = Color.decode("#94A3B8");
    
    public MapVisualizerPanel(CopyOnWriteArrayList<ActiveDispatch> dispatches) {
        super(20, Color.decode("#0F172A"));
        this.activeDispatches = dispatches;
        
        nodePositions.put(RoutingMap.DEPOT, new Point(100, 300));
        nodePositions.put("DPULZE Mall", new Point(250, 200));
        nodePositions.put("Shaftsbury Square", new Point(200, 100));
        nodePositions.put("Multimedia University", new Point(400, 150));
        nodePositions.put("Tamarind Square", new Point(350, 300));
        nodePositions.put("Cyberjaya Lake Gardens", new Point(500, 350));
        nodePositions.put("Cyberjaya Hospital", new Point(550, 200));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pad = 30;
        int maxW = 600;
        int maxH = 400;
        double scaleX = (getWidth() - pad * 2.0) / maxW;
        double scaleY = (getHeight() - pad * 2.0) / maxH;
        
        g2.setColor(edgeColor);
        g2.setStroke(new BasicStroke(2f));
        drawEdge(g2, RoutingMap.DEPOT, "DPULZE Mall", scaleX, scaleY, pad);
        drawEdge(g2, RoutingMap.DEPOT, "Shaftsbury Square", scaleX, scaleY, pad);
        drawEdge(g2, "DPULZE Mall", "Tamarind Square", scaleX, scaleY, pad);
        drawEdge(g2, "DPULZE Mall", "Multimedia University", scaleX, scaleY, pad);
        drawEdge(g2, "Shaftsbury Square", "Multimedia University", scaleX, scaleY, pad);
        drawEdge(g2, "Multimedia University", "Cyberjaya Hospital", scaleX, scaleY, pad);
        drawEdge(g2, "Tamarind Square", "Cyberjaya Lake Gardens", scaleX, scaleY, pad);
        drawEdge(g2, "Cyberjaya Lake Gardens", "Cyberjaya Hospital", scaleX, scaleY, pad);

        for (String node : nodePositions.keySet()) {
            Point p = nodePositions.get(node);
            int x = pad + (int)(p.x * scaleX);
            int y = pad + (int)(p.y * scaleY);
            
            g2.setColor(nodeColor);
            g2.fillOval(x - 6, y - 6, 12, 12);
            
            g2.setColor(textMuted);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(node, x + 10, y + 4);
        }
        
        for (ActiveDispatch ad : activeDispatches) {
            drawTruck(g2, ad, scaleX, scaleY, pad);
        }
    }

    private void drawEdge(Graphics2D g2, String n1, String n2, double sx, double sy, int pad) {
        Point p1 = nodePositions.get(n1);
        Point p2 = nodePositions.get(n2);
        if(p1==null || p2==null) return;
        g2.drawLine(pad + (int)(p1.x * sx), pad + (int)(p1.y * sy), pad + (int)(p2.x * sx), pad + (int)(p2.y * sy));
    }
    
    private void drawTruck(Graphics2D g2, ActiveDispatch ad, double sx, double sy, int pad) {
        if(ad.pathNodes == null || ad.pathNodes.isEmpty()) return;
        
        int totalSegments = ad.pathNodes.size() - 1;
        if (totalSegments <= 0) return; // Truck is already there
        
        double segProgress = ad.progress * totalSegments;
        int currentSeg = (int) segProgress;
        if (currentSeg >= totalSegments) currentSeg = totalSegments - 1;
        
        double p = segProgress - currentSeg; // 0 to 1 inside the segment
        
        String n1 = ad.pathNodes.get(currentSeg);
        String n2 = ad.pathNodes.get(currentSeg + 1);
        
        Point p1 = nodePositions.get(n1);
        Point p2 = nodePositions.get(n2);
        if(p1 == null || p2 == null) return;
        
        int x1 = pad + (int)(p1.x * sx);
        int y1 = pad + (int)(p1.y * sy);
        int x2 = pad + (int)(p2.x * sx);
        int y2 = pad + (int)(p2.y * sy);
        
        int currX = x1 + (int)((x2 - x1) * p);
        int currY = y1 + (int)((y2 - y1) * p);
        
        // Draw truck dot (Neon Cyan)
        g2.setColor(Color.decode("#38BDF8"));
        g2.fillOval(currX - 8, currY - 8, 16, 16);
        
        // Add an outer glow
        g2.setColor(new Color(56, 189, 248, 50));
        g2.fillOval(currX - 14, currY - 14, 28, 28);
        
        // Label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.drawString(ad.truckID, currX - 8, currY - 12);
    }
}
