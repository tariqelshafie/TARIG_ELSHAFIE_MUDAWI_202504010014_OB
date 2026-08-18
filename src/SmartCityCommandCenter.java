import models.*;
import simulation.*;
import io.AssetFileManager;
import io.StatsManager;
import ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class SmartCityCommandCenter extends JFrame {
    private CopyOnWriteArrayList<WasteBin> cityBins;
    private AssetFileManager fileManager;
    private StatsManager statsManager;
    private RoutingMap routingMap;
    
    private JTextPane actionLog;
    private JPanel totalCollectedLabel, vehiclesDispatchedLabel, carbonSavedLabel;
    private JPanel cardsContainer;
    private ArrayList<BinCard> uiCards;

    private Random random = new Random();
    private Timer autoPlayTimer;
    private Timer mapTimer;
    private boolean isAutoPlaying = false;
    private JButton playPauseBtn;

    private int currentDay = 0; 
    private String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private CopyOnWriteArrayList<ActiveDispatch> activeDispatches = new CopyOnWriteArrayList<>();
    private MapVisualizerPanel mapVisualizer;

    // Midnight & Neon Palette
    private Color windowBg = Color.decode("#020617");
    private Color panelBg = Color.decode("#0F172A");
    private Color sidebarBg = Color.decode("#020617");
    private Color borderColor = Color.decode("#1E293B"); 
    private Color textBright = Color.decode("#F8FAFC");
    private Color textMuted = Color.decode("#64748B");
    private Color accentNeonBlue = Color.decode("#38BDF8"); 
    private Color accentNeonGreen = Color.decode("#34D399");
    private Color accentNeonRed = Color.decode("#F43F5E");

    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    
    // CRUD State
    private DefaultTableModel tableModel;
    private JTable assetTable;
    private JTextField idField;
    private JComboBox<String> locDropdown;
    private JComboBox<String> typeDropdown;
    private String selectedAssetID = null;
    
    // Analytics
    private GraphPanel graphPanel;
    private BarChartPanel barChartPanel;
    private PieChartPanel pieChartPanel;
    private JPanel analyticsCards;
    private CardLayout analyticsLayout;

    public SmartCityCommandCenter() {
        fileManager = new AssetFileManager();
        statsManager = new StatsManager();
        routingMap = new RoutingMap();
        cityBins = fileManager.loadBinsConfig();
        uiCards = new ArrayList<>();
        
        setTitle("SDG 11: SMART CITY COMMAND");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        getContentPane().setBackground(windowBg);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(windowBg);
        mainContentPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        mainContentPanel.add(buildSimulationTab(), "Simulation");
        mainContentPanel.add(buildManagementTab(), "Management");
        mainContentPanel.add(buildAnalyticsTab(), "Analytics");

        add(mainContentPanel, BorderLayout.CENTER);
        updateStats();

        autoPlayTimer = new Timer(2500, e -> simulateTime(1));
        
        mapTimer = new Timer(50, e -> {
            boolean rep = false;
            for (ActiveDispatch ad : activeDispatches) {
                if (ad.progress < 1.0) {
                    ad.progress += 0.02; // Slower, 50 frames
                    rep = true;
                }
            }
            if (activeDispatches.removeIf(ad -> ad.progress >= 1.0)) rep = true;
            if (rep) mapVisualizer.repaint();
        });
        mapTimer.start();
    }

    private void applyCustomScrollbars(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(sidebarBg);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor));

        JLabel titleLabel = new JLabel("<html><div style='text-align: center; color: #38BDF8;'>SMART CITY<br><span style='color: #F8FAFC;'>COMMAND</span></div></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(40, 10, 40, 10));

        JButton simBtn = createNavButton("LIVE MONITOR");
        JButton mgtBtn = createNavButton("ASSET MANAGER");
        JButton anaBtn = createNavButton("ANALYTICS");

        simBtn.addActionListener(e -> cardLayout.show(mainContentPanel, "Simulation"));
        mgtBtn.addActionListener(e -> cardLayout.show(mainContentPanel, "Management"));
        anaBtn.addActionListener(e -> cardLayout.show(mainContentPanel, "Analytics"));

        sidebar.add(titleLabel);
        sidebar.add(simBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(mgtBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(anaBtn);
        sidebar.add(Box.createVerticalGlue()); 

        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton("  " + text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(textMuted);
        btn.setBackground(sidebarBg);
        btn.setBorder(new EmptyBorder(18, 30, 18, 30));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(260, 60));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(panelBg);
                btn.setForeground(accentNeonBlue);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(sidebarBg);
                btn.setForeground(textMuted);
            }
        });
        return btn;
    }

    private JPanel buildSimulationTab() {
        JPanel simPanel = new JPanel(new BorderLayout(25, 25));
        simPanel.setBackground(windowBg);
        simPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        JPanel topPanel = new JPanel(new GridLayout(1, 3, 25, 0));
        topPanel.setBackground(windowBg);
        
        JPanel collectedWrapper = new JPanel(new BorderLayout());
        collectedWrapper.setOpaque(false);
        totalCollectedLabel = createStatLabel("TOTAL COLLECTED", "0.0 KG", accentNeonGreen);
        collectedWrapper.add(totalCollectedLabel, BorderLayout.CENTER);
        
        JPanel dispatchedWrapper = new JPanel(new BorderLayout());
        dispatchedWrapper.setOpaque(false);
        vehiclesDispatchedLabel = createStatLabel("DISPATCHES", "0", Color.decode("#FBBF24"));
        dispatchedWrapper.add(vehiclesDispatchedLabel, BorderLayout.CENTER);
        
        JPanel carbonWrapper = new JPanel(new BorderLayout());
        carbonWrapper.setOpaque(false);
        carbonSavedLabel = createStatLabel("CO2 SAVED", "0.0 KG", accentNeonBlue);
        carbonWrapper.add(carbonSavedLabel, BorderLayout.CENTER);

        topPanel.add(collectedWrapper);
        topPanel.add(dispatchedWrapper);
        topPanel.add(carbonWrapper);

        JPanel centralPanel = new JPanel(new BorderLayout(25, 0));
        centralPanel.setBackground(windowBg);

        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(windowBg);
        refreshCardsContainer(); 

        JScrollPane cardsScroll = new JScrollPane(cardsContainer);
        cardsScroll.setPreferredSize(new Dimension(360, 0));
        cardsScroll.getViewport().setBackground(windowBg);
        applyCustomScrollbars(cardsScroll);
        cardsScroll.getVerticalScrollBar().setUnitIncrement(20);
        
        RoundedPanel cardsWrapper = new RoundedPanel(20, windowBg);
        cardsWrapper.setLayout(new BorderLayout());
        cardsWrapper.add(cardsScroll, BorderLayout.CENTER);
        centralPanel.add(cardsWrapper, BorderLayout.WEST);

        actionLog = new JTextPane();
        actionLog.setEditable(false);
        actionLog.setBackground(panelBg); 
        actionLog.setFont(new Font("Consolas", Font.PLAIN, 14)); 
        actionLog.setMargin(new Insets(20, 20, 20, 20));
        
        JScrollPane logScroll = new JScrollPane(actionLog);
        applyCustomScrollbars(logScroll);
        
        JPanel logWrapper = new JPanel(new GridLayout(2, 1, 0, 20));
        logWrapper.setOpaque(false);

        mapVisualizer = new MapVisualizerPanel(activeDispatches);
        
        RoundedPanel logInner = new RoundedPanel(20, panelBg);
        logInner.setLayout(new BorderLayout());
        logInner.add(logScroll, BorderLayout.CENTER);
        
        JPanel logHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logHeader.setOpaque(false);
        JButton resetBtn = createActionButton("RESET STATS", accentNeonRed, windowBg);
        resetBtn.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this, "Reset all lifetime statistics?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                statsManager.resetStats();
                updateStats();
                appendToLog("\n=== LIFETIME STATISTICS RESET ===\n", accentNeonRed);
            }
        });
        logHeader.add(resetBtn);
        logInner.add(logHeader, BorderLayout.NORTH);

        logWrapper.add(mapVisualizer);
        logWrapper.add(logInner);

        centralPanel.add(logWrapper, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        controlsPanel.setOpaque(false);
        controlsPanel.setPreferredSize(new Dimension(0, 55));
        
        playPauseBtn = createActionButton("[▶] AUTO PLAY", accentNeonBlue, windowBg);
        playPauseBtn.addActionListener(e -> {
            isAutoPlaying = !isAutoPlaying;
            if (isAutoPlaying) {
                playPauseBtn.setText("[⏸] PAUSE AUTO");
                playPauseBtn.setBackground(Color.decode("#FBBF24")); 
                autoPlayTimer.start();
            } else {
                playPauseBtn.setText("[▶] AUTO PLAY");
                playPauseBtn.setBackground(accentNeonBlue);
                autoPlayTimer.stop();
            }
        });
        
        JButton dayBtn = createActionButton("+1 DAY", Color.decode("#6366F1"), windowBg);
        JButton weekBtn = createActionButton("+1 WEEK", Color.decode("#8B5CF6"), windowBg);
        JButton monthBtn = createActionButton("+1 MONTH", Color.decode("#D946EF"), windowBg);
        
        dayBtn.addActionListener(e -> simulateTime(1));
        weekBtn.addActionListener(e -> simulateTime(7));
        monthBtn.addActionListener(e -> simulateTime(30));

        controlsPanel.add(playPauseBtn);
        controlsPanel.add(dayBtn);
        controlsPanel.add(weekBtn);
        controlsPanel.add(monthBtn);

        simPanel.add(topPanel, BorderLayout.NORTH);
        simPanel.add(centralPanel, BorderLayout.CENTER);
        simPanel.add(controlsPanel, BorderLayout.SOUTH);

        return simPanel;
    }

    private JPanel buildManagementTab() {
        JPanel mgtPanel = new JPanel(new BorderLayout(25, 25));
        mgtPanel.setBackground(windowBg);
        mgtPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        RoundedPanel formPanel = new RoundedPanel(20, panelBg);
        formPanel.setLayout(new GridLayout(7, 1, 10, 10));
        formPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("ASSET DETAILS");
        titleLabel.setForeground(textBright);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        idField = createStyledTextField();
        
        ArrayList<String> validLocs = new ArrayList<>();
        for(String n : RoutingMap.CYBERJAYA_NODES) if(!n.equals(RoutingMap.DEPOT)) validLocs.add(n);
        
        locDropdown = new JComboBox<>(validLocs.toArray(new String[0]));
        locDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        locDropdown.setBackground(panelBg);
        locDropdown.setForeground(textBright);
        
        typeDropdown = new JComboBox<>(new String[]{"General Waste", "Recyclable"});
        typeDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeDropdown.setBackground(panelBg);
        typeDropdown.setForeground(textBright);

        formPanel.add(titleLabel);
        formPanel.add(new JLabel("<html><span style='color:#64748B;'>Asset ID</span></html>"));
        formPanel.add(idField);
        formPanel.add(new JLabel("<html><span style='color:#64748B;'>Location</span></html>"));
        formPanel.add(locDropdown);
        formPanel.add(new JLabel("<html><span style='color:#64748B;'>Asset Type</span></html>"));
        formPanel.add(typeDropdown);

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        btnPanel.setOpaque(false);

        JButton addBtn = createActionButton("ADD NEW ASSET", accentNeonGreen, windowBg);
        JButton updateBtn = createActionButton("UPDATE SELECTED", accentNeonBlue, windowBg);
        JButton delBtn = createActionButton("DELETE SELECTED", accentNeonRed, windowBg);

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(delBtn);

        JPanel leftWrapper = new JPanel(new BorderLayout(0, 25));
        leftWrapper.setOpaque(false);
        leftWrapper.add(formPanel, BorderLayout.NORTH);
        leftWrapper.add(btnPanel, BorderLayout.CENTER);
        leftWrapper.setPreferredSize(new Dimension(320, 0));

        String[] columns = {"Asset ID", "Type", "Location", "Capacity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        assetTable = new JTable(tableModel);
        assetTable.setBackground(panelBg);
        assetTable.setForeground(textBright);
        assetTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        assetTable.setRowHeight(45);
        assetTable.setShowGrid(false);
        assetTable.setIntercellSpacing(new Dimension(0, 0));
        assetTable.setSelectionBackground(Color.decode("#1E293B"));
        assetTable.setSelectionForeground(accentNeonBlue);
        
        assetTable.getTableHeader().setBackground(windowBg);
        assetTable.getTableHeader().setForeground(textMuted);
        assetTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        assetTable.getTableHeader().setPreferredSize(new Dimension(0, 50));
        assetTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        
        assetTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && assetTable.getSelectedRow() != -1) {
                int row = assetTable.getSelectedRow();
                selectedAssetID = (String) tableModel.getValueAt(row, 0);
                idField.setText(selectedAssetID);
                typeDropdown.setSelectedItem((String) tableModel.getValueAt(row, 1));
                locDropdown.setSelectedItem((String) tableModel.getValueAt(row, 2));
                idField.setEditable(false); 
                idField.setForeground(textMuted);
            }
        });

        JScrollPane tableScroll = new JScrollPane(assetTable);
        applyCustomScrollbars(tableScroll);
        tableScroll.getViewport().setBackground(panelBg);
        
        RoundedPanel tableWrapper = new RoundedPanel(20, panelBg);
        tableWrapper.setLayout(new BorderLayout());
        tableWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableWrapper.add(tableScroll, BorderLayout.CENTER);

        populateTable();

        addBtn.addActionListener(e -> {
            String id = idField.getText();
            String loc = (String) locDropdown.getSelectedItem();
            if (id.isEmpty() || loc == null) return;
            if (cityBins.stream().anyMatch(b -> b.getAssetID().equals(id))) return;
            
            String selectedType = (String) typeDropdown.getSelectedItem();
            WasteBin newBin = selectedType.equals("Recyclable") ? new RecyclableBin(id, loc) : new GeneralWasteBin(id, loc);
            cityBins.add(newBin);
            finishCrudAction();
        });

        updateBtn.addActionListener(e -> {
            if (selectedAssetID == null) return;
            String loc = (String) locDropdown.getSelectedItem();
            String type = (String) typeDropdown.getSelectedItem();
            
            cityBins.removeIf(b -> b.getAssetID().equals(selectedAssetID));
            WasteBin updatedBin = type.equals("Recyclable") ? new RecyclableBin(selectedAssetID, loc) : new GeneralWasteBin(selectedAssetID, loc);
            cityBins.add(updatedBin);
            finishCrudAction();
        });

        delBtn.addActionListener(e -> {
            if (selectedAssetID == null) return;
            cityBins.removeIf(b -> b.getAssetID().equals(selectedAssetID));
            finishCrudAction();
        });

        mgtPanel.add(leftWrapper, BorderLayout.WEST);
        mgtPanel.add(tableWrapper, BorderLayout.CENTER);
        return mgtPanel;
    }
    
    private JPanel buildAnalyticsTab() {
        JPanel anaPanel = new JPanel(new BorderLayout(25, 25));
        anaPanel.setBackground(windowBg);
        anaPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel title = new JLabel("ADVANCED ANALYTICS DASHBOARD");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(textBright);
        
        JComboBox<String> chartSelector = new JComboBox<>(new String[]{
            "Total Generation Over Time", "Total Waste By Location", "Recyclables Breakdown"
        });
        chartSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chartSelector.setBackground(panelBg);
        chartSelector.setForeground(textBright);
        chartSelector.addActionListener(e -> {
            String sel = (String) chartSelector.getSelectedItem();
            if(sel.contains("Time")) analyticsLayout.show(analyticsCards, "Line");
            else if(sel.contains("Location")) analyticsLayout.show(analyticsCards, "Bar");
            else analyticsLayout.show(analyticsCards, "Pie");
        });
        
        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(chartSelector, BorderLayout.EAST);
        
        analyticsLayout = new CardLayout();
        analyticsCards = new JPanel(analyticsLayout);
        analyticsCards.setOpaque(false);
        
        graphPanel = new GraphPanel(panelBg);
        barChartPanel = new BarChartPanel();
        pieChartPanel = new PieChartPanel();
        
        graphPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        barChartPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        pieChartPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        analyticsCards.add(graphPanel, "Line");
        analyticsCards.add(barChartPanel, "Bar");
        analyticsCards.add(pieChartPanel, "Pie");
        
        anaPanel.add(headerPanel, BorderLayout.NORTH);
        anaPanel.add(analyticsCards, BorderLayout.CENTER);
        
        return anaPanel;
    }

    private void finishCrudAction() {
        fileManager.saveBinsConfig(cityBins);
        populateTable();
        refreshCardsContainer();
        clearForm();
    }

    private void clearForm() {
        assetTable.clearSelection();
        selectedAssetID = null;
        idField.setText("");
        idField.setEditable(true);
        idField.setForeground(textBright);
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        for (WasteBin bin : cityBins) {
            String type = (bin instanceof RecyclableBin) ? "Recyclable" : "General";
            tableModel.addRow(new Object[]{bin.getAssetID(), type, bin.getLocation(), bin.getMaxCapacity() + " KG"});
        }
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bg.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bg); }
        });
        return btn;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setBackground(windowBg);
        tf.setForeground(textBright);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setCaretColor(textBright);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return tf;
    }

    private void refreshCardsContainer() {
        cardsContainer.removeAll();
        uiCards.clear();
        for (WasteBin bin : cityBins) {
            BinCard card = new BinCard(bin);
            uiCards.add(card);
            cardsContainer.add(card);
            cardsContainer.add(Box.createRigidArea(new Dimension(0, 15))); 
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JPanel createStatLabel(String title, String val, Color accent) {
        RoundedPanel p = new RoundedPanel(20, panelBg);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(textMuted);
        
        JLabel lblVal = new JLabel(val);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(textBright);
        
        p.add(lblTitle, BorderLayout.NORTH);
        p.add(lblVal, BorderLayout.CENTER);
        return p; 
    }

    private void updateStats() {
        updateStatLabelPanel((RoundedPanel) totalCollectedLabel, "TOTAL COLLECTED", String.format("%.1f KG", statsManager.getTotalCollected()));
        updateStatLabelPanel((RoundedPanel) vehiclesDispatchedLabel, "DISPATCHES", String.valueOf(statsManager.getVehiclesDispatched()));
        updateStatLabelPanel((RoundedPanel) carbonSavedLabel, "CO2 SAVED", String.format("%.1f KG", statsManager.getCarbonSaved()));
    }
    
    private void updateStatLabelPanel(RoundedPanel p, String title, String val) {
        p.removeAll();
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(textMuted);
        
        JLabel lblVal = new JLabel(val);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(textBright);
        
        p.add(lblTitle, BorderLayout.NORTH);
        p.add(lblVal, BorderLayout.CENTER);
        p.revalidate();
        p.repaint();
    }

    private void appendToLog(String msg, Color c) {
        javax.swing.text.StyleContext sc = javax.swing.text.StyleContext.getDefaultStyleContext();
        javax.swing.text.AttributeSet aset = sc.addAttribute(javax.swing.text.SimpleAttributeSet.EMPTY, javax.swing.text.StyleConstants.Foreground, c);
        
        int len = actionLog.getDocument().getLength();
        try {
            actionLog.getDocument().insertString(len, msg, aset);
        } catch (Exception e) { }
    }

    private void simulateTime(int days) {
        for(int d=0; d<days; d++) {
            if (actionLog.getDocument().getLength() > 0) appendToLog("\n", textMuted);
            
            String dayName = DAYS[currentDay % 7];
            appendToLog("=== STARTING DAILY CYCLE: " + dayName.toUpperCase() + " ===\n", Color.decode("#A78BFA"));

            double multiplier = 1.0;
            if (dayName.equals("Saturday") || dayName.equals("Sunday")) {
                multiplier = 1.5;
                appendToLog("[EVENT] Weekend Event! Cyberjaya is bustling. Waste generation increased by 50%!\n", Color.decode("#FBBF24"));
            } else if (random.nextDouble() < 0.2) {
                multiplier = 0.8;
                appendToLog("[EVENT] Heavy Rain. Less foot traffic today. (20% reduction)\n", Color.decode("#38BDF8"));
            }

            double dailyWasteGenerated = 0;
            double currentTotalCollected = statsManager.getTotalCollected();
            int currentVehiclesDispatched = statsManager.getVehiclesDispatched();
            double currentCarbonSaved = statsManager.getCarbonSaved();

            for (int i = 0; i < cityBins.size(); i++) {
                WasteBin bin = cityBins.get(i);
                double generated = (15 + (40 * random.nextDouble())) * multiplier;
                dailyWasteGenerated += generated;
                bin.addWaste(generated);

                appendToLog(String.format(" -> %s Fill Level: %.1f%%\n", bin.getAssetID(), bin.getFillPercentage()), textMuted);
                barChartPanel.addData(bin.getLocation(), generated);

                if (bin.hasCriticalAlert()) {
                    currentVehiclesDispatched++;
                    currentTotalCollected += bin.getCurrentFillLevel();

                    Dispatchable vehicle;
                    String vId;
                    if (bin instanceof RecyclableBin) {
                        vId = "V-" + currentVehiclesDispatched;
                        vehicle = new RecyclingVan(vId);
                        currentCarbonSaved += (bin.getCurrentFillLevel() * 1.5); 
                        RecyclableBin rb = (RecyclableBin) bin;
                        pieChartPanel.updateData(rb.getPaperWeight(), rb.getPlasticWeight(), rb.getMetalWeight());
                    } else {
                        vId = "T-" + currentVehiclesDispatched;
                        vehicle = new GarbageTruck(vId);
                    }

                    List<String> path = routingMap.getShortestPathList(bin.getLocation());
                    ActiveDispatch ad = new ActiveDispatch(vId, path);
                    activeDispatches.add(ad);

                    String vehicleLog = vehicle.deploy(bin, routingMap);
                    appendToLog("   [CRITICAL] " + vehicleLog + "\n", accentNeonRed);
                    fileManager.logDispatchEvent(vehicleLog);
                    bin.emptyBin();
                }
                uiCards.get(i).updateVisuals(); 
            }
            
            graphPanel.addDataPoint(dailyWasteGenerated);
            statsManager.saveStats(currentTotalCollected, currentVehiclesDispatched, currentCarbonSaved);
            currentDay++;
        }

        fileManager.saveBinsConfig(cityBins);
        updateStats();
        actionLog.setCaretPosition(actionLog.getDocument().getLength()); 
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
        catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new SmartCityCommandCenter().setVisible(true));
    }
}
