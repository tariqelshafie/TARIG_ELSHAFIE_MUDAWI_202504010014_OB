package ui;
import javax.swing.*;
import java.awt.*;

public class PieChartPanel extends RoundedPanel {
    private double[] values = {0, 0, 0};
    private String[] labels = {"Paper/Cardboard", "Plastic", "Glass/Metal"};
    private Color[] colors = {Color.decode("#38BDF8"), Color.decode("#34D399"), Color.decode("#F43F5E")};
    
    public PieChartPanel() { super(20, Color.decode("#0F172A")); }
    
    public void updateData(double paper, double plastic, double metal) {
        values[0] += paper;
        values[1] += plastic;
        values[2] += metal;
        repaint();
    }
    
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        double total = values[0] + values[1] + values[2];
        if (total == 0) {
            g2.setColor(Color.WHITE);
            g2.drawString("No recycling data yet.", getWidth()/2 - 50, getHeight()/2);
            return;
        }
        
        int pad = 40;
        int size = Math.min(getWidth(), getHeight()) - pad * 2;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;
        
        int startAngle = 0;
        for (int i = 0; i < 3; i++) {
            int arcAngle = (int) Math.round(values[i] / total * 360.0);
            if (i == 2) arcAngle = 360 - startAngle; // ensure it fills exactly 360
            g2.setColor(colors[i]);
            g2.fillArc(x, y, size, size, startAngle, arcAngle);
            startAngle += arcAngle;
        }
        
        // Draw Legend
        int legendX = 20;
        int legendY = 20;
        for (int i = 0; i < 3; i++) {
            g2.setColor(colors[i]);
            g2.fillRect(legendX, legendY, 15, 15);
            g2.setColor(Color.WHITE);
            g2.drawString(String.format("%s (%.1f%%)", labels[i], (values[i]/total)*100), legendX + 25, legendY + 12);
            legendY += 25;
        }
    }
}
