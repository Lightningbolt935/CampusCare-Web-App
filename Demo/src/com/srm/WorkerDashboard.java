package com.srm;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONArray;

public class WorkerDashboard extends JFrame {
    private static final Color PRIMARY_BLUE = new Color(12, 77, 162);
    private static final Color INPUT_GRAY = new Color(238, 238, 238);
    private static final Color BORDER_GRAY = new Color(136, 136, 136);
    private static final Color WHITE = Color.WHITE;
    private static final Color BLACK = Color.BLACK;
    private static final Color STATUS_PENDING = new Color(255, 152, 0);
    private static final Color STATUS_PROGRESS = new Color(33, 150, 243);
    private static final Color STATUS_COMPLETED = new Color(76, 175, 80);
    private static final Color STATUS_HOLD = new Color(244, 67, 54);
    
    private String workerName;
    private JButton menuButton;
    private JPanel drawerPanel;
    private boolean drawerOpen = false;
    private JTable complaintsTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JLabel statsLabel;
    
    private final String baseUrl = "http://127.0.0.1:5000";
    private Timer refreshTimer;
    
    private int pendingCount = 0;
    private int inProgressCount = 0;
    private int completedCount = 0;

    public WorkerDashboard(String workerName) {
        this.workerName = workerName;
        
        System.out.println("DEBUG: WorkerDashboard initialized for: " + workerName);
        
        setTitle("Worker Dashboard - " + workerName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setResizable(true);
        
        initializeUI();
        fetchAssignedComplaints();
        startAutoRefresh();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Top panel with menu button and heading
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(WHITE);
        topPanel.setBorder(new LineBorder(BORDER_GRAY, 1));
        topPanel.setPreferredSize(new Dimension(0, 120));
        
        menuButton = new JButton("☰");
        menuButton.setFont(new Font("Arial", Font.BOLD, 20));
        menuButton.setBackground(WHITE);
        menuButton.setForeground(PRIMARY_BLUE);
        menuButton.setBorder(null);
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButton.setPreferredSize(new Dimension(60, 60));
        menuButton.addActionListener(e -> toggleDrawer());
        
        JPanel headerCenter = new JPanel();
        headerCenter.setLayout(new BoxLayout(headerCenter, BoxLayout.Y_AXIS));
        headerCenter.setBackground(WHITE);
        
        JLabel headingLabel = new JLabel("Worker Dashboard");
        headingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headingLabel.setForeground(PRIMARY_BLUE);
        headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel nameLabel = new JLabel(workerName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setForeground(BORDER_GRAY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statsLabel = new JLabel("Loading statistics...");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statsLabel.setForeground(BORDER_GRAY);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerCenter.add(Box.createVerticalStrut(15));
        headerCenter.add(headingLabel);
        headerCenter.add(Box.createVerticalStrut(5));
        headerCenter.add(nameLabel);
        headerCenter.add(Box.createVerticalStrut(5));
        headerCenter.add(statsLabel);
        
        topPanel.add(menuButton, BorderLayout.WEST);
        topPanel.add(headerCenter, BorderLayout.CENTER);
        
        // Main content panel with table
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(WHITE);
        
        refreshButton = new JButton("🔄 Refresh");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 14));
        refreshButton.setBackground(PRIMARY_BLUE);
        refreshButton.setForeground(WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> fetchAssignedComplaints());
        buttonPanel.add(refreshButton);
        
        // Table setup
        String[] columnNames = {"ID", "Category", "Type", "Room", "Floor", "Description", "Status", "Faculty"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        complaintsTable = new JTable(tableModel);
        complaintsTable.setRowHeight(35);
        complaintsTable.setFont(new Font("Arial", Font.PLAIN, 13));
        complaintsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        complaintsTable.getTableHeader().setBackground(INPUT_GRAY);
        complaintsTable.getTableHeader().setReorderingAllowed(false);
        complaintsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Custom cell renderer for status column
        complaintsTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String status = value.toString();
                    switch (status) {
                        case "Pending":
                            c.setBackground(STATUS_PENDING);
                            c.setForeground(WHITE);
                            break;
                        case "In Progress":
                            c.setBackground(STATUS_PROGRESS);
                            c.setForeground(WHITE);
                            break;
                        case "Completed":
                            c.setBackground(STATUS_COMPLETED);
                            c.setForeground(WHITE);
                            break;
                        case "On Hold":
                            c.setBackground(STATUS_HOLD);
                            c.setForeground(WHITE);
                            break;
                        default:
                            c.setBackground(WHITE);
                            c.setForeground(BLACK);
                    }
                    setHorizontalAlignment(CENTER);
                    setFont(new Font("Arial", Font.BOLD, 12));
                }
                return c;
            }
        });
        
        // Set column widths
        complaintsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        complaintsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        complaintsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        complaintsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        complaintsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        complaintsTable.getColumnModel().getColumn(5).setPreferredWidth(250);
        complaintsTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        complaintsTable.getColumnModel().getColumn(7).setPreferredWidth(150);
        
        complaintsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = complaintsTable.getSelectedRow();
                    if (row >= 0) {
                        showComplaintDetailsDialog(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(complaintsTable);
        scrollPane.setBorder(new LineBorder(BORDER_GRAY, 1));
        
        JLabel instructionsLabel = new JLabel("💡 Double-click on any complaint to view details and update status");
        instructionsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        instructionsLabel.setForeground(BORDER_GRAY);
        instructionsLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(instructionsLabel, BorderLayout.SOUTH);
        
        drawerPanel = createDrawerPanel();
        drawerPanel.setVisible(false);
        
        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(drawerPanel, BorderLayout.WEST);
        
        setVisible(true);
    }
    
    private JPanel createDrawerPanel() {
        JPanel drawer = new JPanel();
        drawer.setBackground(INPUT_GRAY);
        drawer.setLayout(new BoxLayout(drawer, BoxLayout.Y_AXIS));
        drawer.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        drawer.setPreferredSize(new Dimension(200, 0));
        
        JButton backButton = new JButton("← Back");
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.setBackground(INPUT_GRAY);
        backButton.setBorder(null);
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.setForeground(BLACK);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> toggleDrawer());
        drawer.add(backButton);
        drawer.add(Box.createVerticalStrut(20));
        
        JLabel navTitle = new JLabel("NAVIGATION");
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        navTitle.setFont(new Font("Arial", Font.BOLD, 12));
        navTitle.setForeground(PRIMARY_BLUE);
        drawer.add(navTitle);
        drawer.add(Box.createVerticalStrut(10));
        
        String[] menuItems = {"Profile", "History", "Logout"};
        for (String item : menuItems) {
            JButton menuItem = new JButton(item);
            menuItem.setAlignmentX(Component.LEFT_ALIGNMENT);
            menuItem.setMaximumSize(new Dimension(170, 40));
            menuItem.setBackground(WHITE);
            menuItem.setBorder(new LineBorder(BORDER_GRAY, 1));
            menuItem.setFont(new Font("Arial", Font.PLAIN, 13));
            menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
            menuItem.addActionListener(e -> handleMenuAction(item));
            drawer.add(menuItem);
            drawer.add(Box.createVerticalStrut(8));
        }
        
        drawer.add(Box.createVerticalGlue());
        return drawer;
    }
    
    private void toggleDrawer() {
        drawerOpen = !drawerOpen;
        drawerPanel.setVisible(drawerOpen);
        revalidate();
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(15000, e -> fetchAssignedComplaints());
        refreshTimer.start();
    }
    
    private void fetchAssignedComplaints() {
        refreshButton.setEnabled(false);
        refreshButton.setText("Loading...");
        
        new Thread(() -> {
            try {
                String encodedName = java.net.URLEncoder.encode(workerName, "UTF-8");
                URL url = new URL(baseUrl + "/worker/complaints?worker=" + encodedName);
                
                System.out.println("DEBUG: Fetching complaints for worker: " + workerName);
                
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                
                int responseCode = connection.getResponseCode();
                System.out.println("DEBUG: Response code: " + responseCode);
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONArray complaints = new JSONArray(response.toString());
                    System.out.println("DEBUG: Received " + complaints.length() + " complaints");
                    
                    pendingCount = 0;
                    inProgressCount = 0;
                    completedCount = 0;
                    
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0);
                        
                        for (int i = 0; i < complaints.length(); i++) {
                            JSONObject complaint = complaints.getJSONObject(i);
                            
                            String status = complaint.optString("status", "Pending");
                            switch (status) {
                                case "Pending": pendingCount++; break;
                                case "In Progress": inProgressCount++; break;
                                case "Completed": completedCount++; break;
                            }
                            
                            String description = complaint.optString("description", "N/A");
                            if (description.length() > 50) {
                                description = description.substring(0, 47) + "...";
                            }
                            
                            Object[] row = {
                                complaint.getInt("id"),
                                complaint.optString("category", "N/A"),
                                complaint.optString("type", "N/A"),
                                complaint.optString("classroom", "N/A"),
                                complaint.optString("floor", "N/A"),
                                description,
                                status,
                                complaint.optString("faculty", "N/A")
                            };
                            tableModel.addRow(row);
                        }
                        
                        updateStatsLabel();
                        refreshButton.setEnabled(true);
                        refreshButton.setText("🔄 Refresh");
                        
                        if (complaints.length() == 0) {
                            System.out.println("DEBUG: No complaints assigned to this worker yet");
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        refreshButton.setEnabled(true);
                        refreshButton.setText("🔄 Refresh");
                        JOptionPane.showMessageDialog(this,
                            "Error fetching complaints. Response code: " + responseCode,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    refreshButton.setEnabled(true);
                    refreshButton.setText("🔄 Refresh");
                    JOptionPane.showMessageDialog(this,
                        "Error fetching complaints: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void updateStatsLabel() {
        int total = pendingCount + inProgressCount + completedCount;
        statsLabel.setText(String.format(
            "📊 Total: %d  |  ⏳ Pending: %d  |  🔧 In Progress: %d  |  ✅ Completed: %d",
            total, pendingCount, inProgressCount, completedCount
        ));
    }
    
    private void showComplaintDetailsDialog(int row) {
        int complaintId = (int) tableModel.getValueAt(row, 0);
        
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "/complaints/" + complaintId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject complaint = new JSONObject(response.toString());
                    SwingUtilities.invokeLater(() -> showUpdateDialog(complaint));
                }
                connection.disconnect();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Error fetching complaint details: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void showUpdateDialog(JSONObject complaint) {
        JDialog dialog = new JDialog(this, "Complaint Details & Update", true);
        dialog.setSize(650, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(WHITE);
        
        JLabel titleLabel = new JLabel("Complaint #" + complaint.getInt("id"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(INPUT_GRAY);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_GRAY, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        addInfoRow(infoPanel, "Category:", complaint.optString("category", "N/A"));
        addInfoRow(infoPanel, "Type:", complaint.optString("type", "N/A"));
        addInfoRow(infoPanel, "Room:", complaint.optString("classroom", "N/A"));
        addInfoRow(infoPanel, "Floor:", complaint.optString("floor", "N/A"));
        addInfoRow(infoPanel, "Faculty:", complaint.optString("faculty", "N/A"));
        addInfoRow(infoPanel, "Date:", complaint.optString("created_at", "N/A"));
        
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 14));
        descLabel.setForeground(PRIMARY_BLUE);
        mainPanel.add(descLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        
        JTextArea descArea = new JTextArea(complaint.optString("description", "No description"));
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Arial", Font.PLAIN, 13));
        descArea.setBackground(INPUT_GRAY);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        descArea.setRows(3);
        mainPanel.add(descArea);
        mainPanel.add(Box.createVerticalStrut(15));
        
        String imageLink = complaint.optString("image", "");
        if (!imageLink.isEmpty()) {
            JLabel imageLabel = new JLabel("Image Link:");
            imageLabel.setFont(new Font("Arial", Font.BOLD, 14));
            imageLabel.setForeground(PRIMARY_BLUE);
            mainPanel.add(imageLabel);
            mainPanel.add(Box.createVerticalStrut(5));
            
            JTextField imageField = new JTextField(imageLink);
            imageField.setEditable(false);
            imageField.setFont(new Font("Arial", Font.PLAIN, 12));
            imageField.setBackground(INPUT_GRAY);
            mainPanel.add(imageField);
            mainPanel.add(Box.createVerticalStrut(15));
        }
        
        JSeparator separator = new JSeparator();
        mainPanel.add(separator);
        mainPanel.add(Box.createVerticalStrut(15));
        
        JLabel updateLabel = new JLabel("Update Status");
        updateLabel.setFont(new Font("Arial", Font.BOLD, 18));
        updateLabel.setForeground(PRIMARY_BLUE);
        mainPanel.add(updateLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        JLabel currentStatusLabel = new JLabel("Current Status:");
        currentStatusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        mainPanel.add(currentStatusLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        
        JLabel statusValue = new JLabel(complaint.optString("status", "Pending"));
        statusValue.setFont(new Font("Arial", Font.BOLD, 16));
        String currentStatus = complaint.optString("status", "Pending");
        switch (currentStatus) {
            case "Pending": statusValue.setForeground(STATUS_PENDING); break;
            case "In Progress": statusValue.setForeground(STATUS_PROGRESS); break;
            case "Completed": statusValue.setForeground(STATUS_COMPLETED); break;
            case "On Hold": statusValue.setForeground(STATUS_HOLD); break;
        }
        mainPanel.add(statusValue);
        mainPanel.add(Box.createVerticalStrut(15));
        
        JLabel newStatusLabel = new JLabel("Update to:");
        newStatusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        mainPanel.add(newStatusLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        
        JComboBox<String> statusCombo = new JComboBox<>(
            new String[]{"Pending", "In Progress", "Completed", "On Hold"}
        );
        statusCombo.setSelectedItem(currentStatus);
        statusCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        statusCombo.setMaximumSize(new Dimension(300, 35));
        statusCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(statusCombo);
        mainPanel.add(Box.createVerticalStrut(10));
        
        JLabel reasonLabel = new JLabel("Reason for Hold (if applicable):");
        reasonLabel.setFont(new Font("Arial", Font.BOLD, 13));
        reasonLabel.setVisible(false);
        
        JTextArea reasonArea = new JTextArea(3, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setFont(new Font("Arial", Font.PLAIN, 13));
        reasonArea.setBorder(new LineBorder(BORDER_GRAY, 1));
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setMaximumSize(new Dimension(600, 80));
        reasonScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        reasonScroll.setVisible(false);
        
        statusCombo.addActionListener(e -> {
            boolean isOnHold = "On Hold".equals(statusCombo.getSelectedItem());
            reasonLabel.setVisible(isOnHold);
            reasonScroll.setVisible(isOnHold);
            dialog.revalidate();
        });
        
        mainPanel.add(reasonLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(reasonScroll);
        mainPanel.add(Box.createVerticalStrut(20));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(INPUT_GRAY);
        cancelButton.setPreferredSize(new Dimension(130, 45));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton updateButton = new JButton("Update Status");
        updateButton.setFont(new Font("Arial", Font.BOLD, 14));
        updateButton.setBackground(PRIMARY_BLUE);
        updateButton.setForeground(WHITE);
        updateButton.setPreferredSize(new Dimension(150, 45));
        updateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateButton.addActionListener(e -> {
            handleStatusUpdate(complaint.getInt("id"), statusCombo, reasonArea, dialog);
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(updateButton);
        mainPanel.add(buttonPanel);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(lblLabel);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(lblValue);
    }
    
    private void handleStatusUpdate(int complaintId, JComboBox<String> statusCombo,
                                    JTextArea reasonArea, JDialog dialog) {
        String newStatus = (String) statusCombo.getSelectedItem();
        String reason = reasonArea.getText().trim();
        
        if ("On Hold".equals(newStatus) && reason.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                "Please provide a reason for putting complaint on hold",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "/complaints/" + complaintId + "/status");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                JSONObject payload = new JSONObject();
                payload.put("status", newStatus);
                if ("On Hold".equals(newStatus)) {
                    payload.put("onHoldReason", reason);
                }
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                int responseCode = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseCode == HttpURLConnection.HTTP_OK ?
                        connection.getInputStream() : connection.getErrorStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JSONObject result = new JSONObject(response.toString());
                
                SwingUtilities.invokeLater(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        JOptionPane.showMessageDialog(dialog,
                            "Status updated successfully to: " + newStatus,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        fetchAssignedComplaints();
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                            result.optString("message", "Failed to update status"),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                
                connection.disconnect();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialog,
                        "Server connection failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void handleMenuAction(String action) {
        toggleDrawer();
        switch (action) {
            case "Profile":
                showProfileDialog();
                break;
            case "History":
                showWorkHistoryDialog();
                break;
            case "Logout":
                if (JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (refreshTimer != null) {
                        refreshTimer.stop();
                    }
                    dispose();
                    new LoginScreen();
                }
                break;
        }
    }
    
    private void showProfileDialog() {
        JDialog profileDialog = new JDialog(this, "Profile", true);
        profileDialog.setSize(400, 250);
        profileDialog.setLocationRelativeTo(this);
        profileDialog.setLayout(new BorderLayout());
        
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBackground(WHITE);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JLabel titleLabel = new JLabel("Worker Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        profilePanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        profilePanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        JLabel nameValue = new JLabel(workerName);
        nameValue.setFont(new Font("Arial", Font.PLAIN, 14));
        profilePanel.add(nameValue, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel statsLabel = new JLabel("Assignments:");
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        profilePanel.add(statsLabel, gbc);
        gbc.gridx = 1;
        JLabel statsValue = new JLabel(String.format("%d total, %d completed",
            pendingCount + inProgressCount + completedCount, completedCount));
        statsValue.setFont(new Font("Arial", Font.PLAIN, 14));
        profilePanel.add(statsValue, gbc);
        
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(PRIMARY_BLUE);
        closeButton.setForeground(WHITE);
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> profileDialog.dispose());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        profilePanel.add(closeButton, gbc);
        
        profileDialog.add(profilePanel, BorderLayout.CENTER);
        profileDialog.setVisible(true);
    }
    
    private void showWorkHistoryDialog() {
        JDialog historyDialog = new JDialog(this, "Work History", true);
        historyDialog.setSize(800, 550);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Your Work History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(WHITE);
        JLabel filterLabel = new JLabel("Filter by Status:");
        filterLabel.setFont(new Font("Arial", Font.BOLD, 13));
        JComboBox<String> filterCombo = new JComboBox<>(
            new String[]{"All", "Pending", "In Progress", "Completed", "On Hold"}
        );
        filterCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        filterPanel.add(filterLabel);
        filterPanel.add(filterCombo);
        topPanel.add(filterPanel, BorderLayout.SOUTH);
        
        String[] columnNames = {"ID", "Category", "Type", "Room", "Status", "Date"};
        DefaultTableModel historyModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(30);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 13));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(INPUT_GRAY);
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object[] row = {
                tableModel.getValueAt(i, 0),
                tableModel.getValueAt(i, 1),
                tableModel.getValueAt(i, 2),
                tableModel.getValueAt(i, 3),
                tableModel.getValueAt(i, 6),
                "Recent"
            };
            historyModel.addRow(row);
        }
        
        filterCombo.addActionListener(e -> {
            String filter = (String) filterCombo.getSelectedItem();
            historyModel.setRowCount(0);
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String status = (String) tableModel.getValueAt(i, 6);
                if ("All".equals(filter) || status.equals(filter)) {
                    Object[] row = {
                        tableModel.getValueAt(i, 0),
                        tableModel.getValueAt(i, 1),
                        tableModel.getValueAt(i, 2),
                        tableModel.getValueAt(i, 3),
                        status,
                        "Recent"
                    };
                    historyModel.addRow(row);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(BORDER_GRAY, 1));
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(PRIMARY_BLUE);
        closeButton.setForeground(WHITE);
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> historyDialog.dispose());
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(closeButton);
        
        historyDialog.add(topPanel, BorderLayout.NORTH);
        historyDialog.add(contentPanel, BorderLayout.CENTER);
        historyDialog.add(bottomPanel, BorderLayout.SOUTH);
        historyDialog.setVisible(true);
    }
    
    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WorkerDashboard("Rajesh Kumar");
        });
    }
}