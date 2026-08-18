package ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class GraphPanel extends RoundedPanel {
    private ArrayList<Double> dataPoints;
    private Color lineColor = Color.decode("#38BDF8");
    private Color gridColor = Color.decode("#1E293B");

    public GraphPanel(Color bgColor) {
        super(20, bgColor);
        dataPoints = new ArrayList<>();
    }

    public void addDataPoint(double value) {
        dataPoints.add(value);
        if (dataPoints.size() > 30) {
            dataPoints.remove(0); // keep last 30 points
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dataPoints.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padding = 40;
        int width = getWidth() - (2 * padding);
        int height = getHeight() - (2 * padding);

        double maxScore = Collections.max(dataPoints);
        if (maxScore == 0) maxScore = 1;

        // Draw grid
        g2.setColor(gridColor);
        for (int i = 0; i <= 5; i++) {
            int y = padding + (i * height / 5);
            g2.drawLine(padding, y, getWidth() - padding, y);
            
            // Y-axis labels
            g2.setColor(Color.decode("#64748B"));
            String label = String.format("%.0f", maxScore * (5 - i) / 5.0);
            g2.drawString(label, padding - 35, y + 5);
            g2.setColor(gridColor);
        }

        // Draw points and lines
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(3f));

        double xScale = (double) width / (dataPoints.size() - 1 == 0 ? 1 : dataPoints.size() - 1);
        double yScale = (double) height / maxScore;

        for (int i = 0; i < dataPoints.size() - 1; i++) {
            int x1 = padding + (int) (i * xScale);
            int y1 = getHeight() - padding - (int) (dataPoints.get(i) * yScale);
            int x2 = padding + (int) ((i + 1) * xScale);
            int y2 = getHeight() - padding - (int) (dataPoints.get(i + 1) * yScale);
            g2.drawLine(x1, y1, x2, y2);
        }

        // Y-axis Title
        g2.setColor(Color.decode("#94A3B8"));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("Waste Generated (KG)", 10, 20);

        // Draw dots
        g2.setColor(Color.decode("#F8FAFC"));
        for (int i = 0; i < dataPoints.size(); i++) {
            int x = padding + (int) (i * xScale);
            int y = getHeight() - padding - (int) (dataPoints.get(i) * yScale);
            g2.fillOval(x - 4, y - 4, 8, 8);
            
            // X-axis labels
            if (i % 5 == 0 || i == dataPoints.size() - 1) {
                g2.setColor(Color.decode("#64748B"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString(String.valueOf(i), x - 4, getHeight() - padding + 15);
                g2.setColor(Color.decode("#F8FAFC"));
            }
        }
        
        // X-axis Title
        g2.setColor(Color.decode("#94A3B8"));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("Simulated Time (Days)", getWidth() / 2 - 50, getHeight() - 5);
    }
}
