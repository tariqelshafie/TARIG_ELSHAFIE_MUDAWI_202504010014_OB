package ui;

import models.WasteBin;
import models.RecyclableBin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BinCard extends RoundedPanel {
    private WasteBin bin;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private Color cardBgNormal = Color.decode("#1E293B"); 
    private Color cardBgAlert = Color.decode("#4C1D95"); // Neon purple for alert
    private Color textPrimary = Color.decode("#F8FAFC");  
    private Color textSecondary = Color.decode("#94A3B8"); 
    private Color accentNeonBlue = Color.decode("#06B6D4"); // Cyan
    private Color accentNeonGreen = Color.decode("#10B981"); // Emerald
    private Color accentNeonRed = Color.decode("#F43F5E"); // Rose

    public BinCard(WasteBin bin) {
        super(15, Color.decode("#1E293B"));
        this.bin = bin;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        String typeStr = (bin instanceof RecyclableBin) ? "[RECYCLING]" : "[GENERAL]";
        JLabel title = new JLabel(bin.getAssetID() + " " + typeStr);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(textPrimary);
        
        JLabel locLabel = new JLabel(bin.getLocation());
        locLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        locLabel.setForeground(textSecondary);
        
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(locLabel, BorderLayout.SOUTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(220, 10));
        progressBar.setStringPainted(false); 
        progressBar.setBackground(Color.decode("#0F172A")); 
        progressBar.setBorderPainted(false);
        progressBar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
            protected Color getSelectionBackground() { return Color.black; }
            protected Color getSelectionForeground() { return Color.white; }
        });
        
        statusLabel = new JLabel("STATUS: OK");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(accentNeonGreen);

        add(headerPanel, BorderLayout.NORTH);
        add(progressBar, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        // Add subtle hover effect
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackgroundColor(Color.decode("#334155"));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackgroundColor(bin.hasCriticalAlert() ? cardBgAlert : cardBgNormal);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Window win = SwingUtilities.getWindowAncestor(BinCard.this);
                if (win instanceof JFrame) {
                    new BinProfileDialog((JFrame) win, bin).setVisible(true);
                }
            }
        });

        updateVisuals();
    }

    public void updateVisuals() {
        int fill = (int) bin.getFillPercentage();
        progressBar.setValue(fill);
        
        if (bin.hasCriticalAlert()) {
            progressBar.setForeground(accentNeonRed); 
            statusLabel.setText("CRITICAL: " + fill + "% - DISPATCH REQUIRED");
            statusLabel.setForeground(accentNeonRed);
            setBackgroundColor(cardBgAlert); 
        } else {
            progressBar.setForeground(accentNeonBlue); 
            statusLabel.setText("OPTIMAL: " + fill + "%");
            statusLabel.setForeground(accentNeonGreen);
            setBackgroundColor(cardBgNormal); 
        }
    }
}
