package ui;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BarChartPanel extends RoundedPanel {
    private HashMap<String, Double> data = new HashMap<>();
    
    public BarChartPanel() { super(20, Color.decode("#0F172A")); }
    
    public void addData(String location, double amount) {
        data.put(location, data.getOrDefault(location, 0.0) + amount);
        repaint();
    }
    
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int pad = 50;
        int w = getWidth() - pad * 2;
        int h = getHeight() - pad * 2;
        
        double maxVal = 0;
        for (double v : data.values()) if (v > maxVal) maxVal = v;
        if (maxVal == 0) maxVal = 1;
        
        int numBars = data.size();
        int barWidth = (w / numBars) - 10;
        
        int i = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            int barHeight = (int) ((entry.getValue() / maxVal) * h);
            int x = pad + (i * (w / numBars)) + 5;
            int y = getHeight() - pad - barHeight;
            
            g2.setColor(Color.decode("#8B5CF6"));
            g2.fillRect(x, y, barWidth, barHeight);
            
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            
            String loc = entry.getKey();
            if (loc.length() > 8) loc = loc.substring(0, 8) + "..";
            g2.drawString(loc, x, getHeight() - pad + 15);
            g2.drawString(String.format("%.0f", entry.getValue()), x, y - 5);
            
            i++;
        }
    }
}
