package ui;

import models.WasteBin;
import models.RecyclableBin;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BinProfileDialog extends JDialog {
    private Color windowBg = Color.decode("#020617");
    private Color panelBg = Color.decode("#0F172A");
    private Color textBright = Color.decode("#F8FAFC");
    private Color textMuted = Color.decode("#64748B");
    private Color accentNeonBlue = Color.decode("#38BDF8"); 
    private Color accentNeonGreen = Color.decode("#34D399");

    public BinProfileDialog(JFrame parent, WasteBin bin) {
        super(parent, "Bin Profile: " + bin.getAssetID(), true);
        setSize(400, 500);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(windowBg);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(windowBg);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("BIN PROFILE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(textBright);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String type = (bin instanceof RecyclableBin) ? "RECYCLABLE" : "GENERAL WASTE";
        JLabel subTitle = new JLabel(bin.getAssetID() + " - " + type);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subTitle.setForeground(accentNeonBlue);
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(subTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        mainPanel.add(createDetailRow("Location", bin.getLocation()));
        mainPanel.add(createDetailRow("Max Capacity", bin.getMaxCapacity() + " KG"));
        mainPanel.add(createDetailRow("Current Fill", String.format("%.1f KG (%.1f%%)", bin.getCurrentFillLevel(), bin.getFillPercentage())));
        
        String status = bin.hasCriticalAlert() ? "CRITICAL ALERT" : "OPTIMAL";
        Color statusColor = bin.hasCriticalAlert() ? Color.decode("#F43F5E") : accentNeonGreen;
        mainPanel.add(createDetailRowColored("Status", status, statusColor));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        if (bin instanceof RecyclableBin) {
            RecyclableBin rBin = (RecyclableBin) bin;
            JLabel breakdownTitle = new JLabel("RECYCLABLES BREAKDOWN");
            breakdownTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            breakdownTitle.setForeground(textMuted);
            mainPanel.add(breakdownTitle);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            
            mainPanel.add(createDetailRow("Paper/Cardboard", String.format("%.1f KG", rBin.getPaperWeight())));
            mainPanel.add(createDetailRow("Plastic", String.format("%.1f KG", rBin.getPlasticWeight())));
            mainPanel.add(createDetailRow("Glass/Metal", String.format("%.1f KG", rBin.getMetalWeight())));
        }

        add(mainPanel, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("CLOSE");
        closeBtn.setBackground(panelBg);
        closeBtn.setForeground(textBright);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(new EmptyBorder(15, 0, 15, 0));
        closeBtn.addActionListener(e -> dispose());
        add(closeBtn, BorderLayout.SOUTH);
    }

    private JPanel createDetailRow(String label, String value) {
        return createDetailRowColored(label, value, textBright);
    }

    private JPanel createDetailRowColored(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(textMuted);
        
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(valueColor);
        
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }
}
