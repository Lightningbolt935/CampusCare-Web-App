package com.srm;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

public class InchargeDashboard extends JFrame {
    private static final Color PRIMARY_BLUE = new Color(12, 77, 162);
    private static final Color INPUT_GRAY = new Color(238, 238, 238);
    private static final Color BORDER_GRAY = new Color(136, 136, 136);
    private static final Color WHITE = Color.WHITE;
    private static final Color BLACK = Color.BLACK;
    
    private String username;
    private String inchargeType;
    private JButton menuButton;
    private JPanel drawerPanel;
    private boolean drawerOpen = false;
    private JTable complaintsTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    
    private final String baseUrl = "http://127.0.0.1:5000";
    private Timer refreshTimer;

    public InchargeDashboard(String username, String inchargeType) {
        this.username = username;
        this.inchargeType = inchargeType;
        
        System.out.println("DEBUG: InchargeDashboard initialized");
        System.out.println("  Username: " + username);
        System.out.println("  Incharge Type: " + inchargeType);
        
        setTitle("Incharge Dashboard - " + inchargeType);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        initializeUI();
        fetchComplaints();
        startAutoRefresh();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Top panel with menu button and heading
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(WHITE);
        topPanel.setBorder(new LineBorder(BORDER_GRAY, 1));
        topPanel.setPreferredSize(new Dimension(0, 100));
        
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
        
        JLabel headingLabel = new JLabel("Incharge Dashboard");
        headingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headingLabel.setForeground(PRIMARY_BLUE);
        headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Complaints Management");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(BORDER_GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerCenter.add(Box.createVerticalStrut(20));
        headerCenter.add(headingLabel);
        headerCenter.add(Box.createVerticalStrut(5));
        headerCenter.add(subtitleLabel);
        
        topPanel.add(menuButton, BorderLayout.WEST);
        topPanel.add(headerCenter, BorderLayout.CENTER);
        
        // Main content panel with table
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(WHITE);
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 14));
        refreshButton.setBackground(PRIMARY_BLUE);
        refreshButton.setForeground(WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> fetchComplaints());
        buttonPanel.add(refreshButton);
        
        // Table setup
        String[] columnNames = {"ID", "Subject", "Faculty", "Room", "Status", "Worker", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        complaintsTable = new JTable(tableModel);
        complaintsTable.setRowHeight(30);
        complaintsTable.setFont(new Font("Arial", Font.PLAIN, 13));
        complaintsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        complaintsTable.getTableHeader().setBackground(INPUT_GRAY);
        complaintsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        complaintsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = complaintsTable.getSelectedRow();
                    if (row >= 0) {
                        showComplaintDetails(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(complaintsTable);
        scrollPane.setBorder(new LineBorder(BORDER_GRAY, 1));
        
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Drawer
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
        refreshTimer = new Timer(10000, e -> fetchComplaints());
        refreshTimer.start();
    }
    
    private void fetchComplaints() {
        refreshButton.setEnabled(false);
        refreshButton.setText("Loading...");
        
        new Thread(() -> {
            try {
                // FIXED: Use the username directly as the incharge parameter
                String inchargeParam = username;  // This will be "incharge_electrical", etc.
                
                System.out.println("DEBUG: Fetching complaints for incharge: " + inchargeParam);
                
                URL url = new URL(baseUrl + "/complaints?incharge=" + inchargeParam);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                
                int responseCode = connection.getResponseCode();
                System.out.println("DEBUG: Response code: " + responseCode);
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONArray complaints = new JSONArray(response.toString());
                    System.out.println("DEBUG: Received " + complaints.length() + " complaints");
                    
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setRowCount(0);
                        for (int i = 0; i < complaints.length(); i++) {
                            JSONObject complaint = complaints.getJSONObject(i);
                            Object[] row = {
                                complaint.getInt("id"),
                                complaint.optString("type", "N/A"),
                                complaint.optString("faculty", "N/A"),
                                complaint.optString("classroom", "N/A"),
                                complaint.optString("status", "Pending"),
                                complaint.optString("worker", "Not Assigned"),
                                complaint.optString("created_at", "N/A")
                            };
                            tableModel.addRow(row);
                        }
                        refreshButton.setEnabled(true);
                        refreshButton.setText("Refresh");
                        
                        if (complaints.length() == 0) {
                            System.out.println("DEBUG: No complaints found for this incharge");
                        }
                    });
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    refreshButton.setEnabled(true);
                    refreshButton.setText("Refresh");
                    JOptionPane.showMessageDialog(this, "Error fetching complaints: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void showComplaintDetails(int row) {
        int complaintId = (int) tableModel.getValueAt(row, 0);
        String subject = (String) tableModel.getValueAt(row, 1);
        String faculty = (String) tableModel.getValueAt(row, 2);
        String room = (String) tableModel.getValueAt(row, 3);
        String status = (String) tableModel.getValueAt(row, 4);
        String worker = (String) tableModel.getValueAt(row, 5);
        String date = (String) tableModel.getValueAt(row, 6);
        
        // Fetch full details from server
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "/complaints/" + complaintId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject complaint = new JSONObject(response.toString());
                    SwingUtilities.invokeLater(() -> showAssignmentDialog(complaint));
                }
                connection.disconnect();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error fetching complaint details: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void showAssignmentDialog(JSONObject complaint) {
        JDialog dialog = new JDialog(this, "Complaint Details", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Complaint Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(INPUT_GRAY);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        addInfoRow(infoPanel, "Subject:", complaint.optString("type", "N/A"));
        addInfoRow(infoPanel, "Faculty:", complaint.optString("faculty", "N/A"));
        addInfoRow(infoPanel, "Room:", complaint.optString("classroom", "N/A"));
        addInfoRow(infoPanel, "Date:", complaint.optString("created_at", "N/A"));
        
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Description
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 14));
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
        mainPanel.add(descArea);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Image link
        String imageLink = complaint.optString("image", "");
        if (!imageLink.isEmpty()) {
            JLabel imageLabel = new JLabel("Image Link:");
            imageLabel.setFont(new Font("Arial", Font.BOLD, 14));
            mainPanel.add(imageLabel);
            mainPanel.add(Box.createVerticalStrut(5));
            
            JTextField imageField = new JTextField(imageLink);
            imageField.setEditable(false);
            imageField.setFont(new Font("Arial", Font.PLAIN, 12));
            mainPanel.add(imageField);
            mainPanel.add(Box.createVerticalStrut(15));
        }
        
        // Assignment section
        JLabel assignLabel = new JLabel("Assignment & Status");
        assignLabel.setFont(new Font("Arial", Font.BOLD, 16));
        assignLabel.setForeground(PRIMARY_BLUE);
        mainPanel.add(assignLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Worker selection
        JLabel workerLabel = new JLabel("Assign Worker:");
        workerLabel.setFont(new Font("Arial", Font.BOLD, 13));
        mainPanel.add(workerLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        
        JComboBox<String> workerCombo = new JComboBox<>();
        workerCombo.addItem("Select a worker...");
        workerCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        fetchWorkers(workerCombo);
        mainPanel.add(workerCombo);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Status selection
        JLabel statusLabel = new JLabel("Update Status:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Pending", "In Progress", "Completed", "On Hold"});
        statusCombo.setSelectedItem(complaint.optString("status", "Pending"));
        statusCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(statusCombo);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // On Hold reason
        JLabel reasonLabel = new JLabel("Reason for Hold (if applicable):");
        reasonLabel.setFont(new Font("Arial", Font.BOLD, 13));
        reasonLabel.setVisible(false);
        
        JTextArea reasonArea = new JTextArea(3, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setFont(new Font("Arial", Font.PLAIN, 13));
        reasonArea.setBorder(new LineBorder(BORDER_GRAY, 1));
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
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
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(WHITE);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(INPUT_GRAY);
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setBackground(PRIMARY_BLUE);
        submitButton.setForeground(WHITE);
        submitButton.setPreferredSize(new Dimension(120, 40));
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> {
            handleAssignment(complaint.getInt("id"), workerCombo, statusCombo, reasonArea, dialog);
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);
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
    
    private void fetchWorkers(JComboBox<String> workerCombo) {
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "/workers");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONArray workers = new JSONArray(response.toString());
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < workers.length(); i++) {
                            JSONObject worker = workers.getJSONObject(i);
                            workerCombo.addItem(worker.getString("name"));
                        }
                    });
                }
                connection.disconnect();
            } catch (Exception e) {
                System.err.println("Error fetching workers: " + e.getMessage());
            }
        }).start();
    }
    
    private void handleAssignment(int complaintId, JComboBox<String> workerCombo, 
                                  JComboBox<String> statusCombo, JTextArea reasonArea, JDialog dialog) {
        String selectedWorker = (String) workerCombo.getSelectedItem();
        String selectedStatus = (String) statusCombo.getSelectedItem();
        String reason = reasonArea.getText().trim();
        
        if (selectedWorker == null || selectedWorker.equals("Select a worker...")) {
            JOptionPane.showMessageDialog(dialog, "Please select a worker", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if ("On Hold".equals(selectedStatus) && reason.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please provide a reason for putting complaint on hold", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "/complaints/" + complaintId + "/assign");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                JSONObject payload = new JSONObject();
                payload.put("worker", selectedWorker);
                payload.put("worker_job_type", "");
                payload.put("status", selectedStatus);
                payload.put("onHoldReason", "On Hold".equals(selectedStatus) ? reason : JSONObject.NULL);
                
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
                            "Complaint updated successfully!\nWorker " + selectedWorker + " has been assigned.\nStatus: " + selectedStatus,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        fetchComplaints();
                    } else {
                        JOptionPane.showMessageDialog(dialog, 
                            result.optString("message", "Failed to assign worker"),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                
                connection.disconnect();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialog, "Server connection failed: " + e.getMessage(), 
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
                JOptionPane.showMessageDialog(this, "History feature coming soon", "Info", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Logout":
                if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", 
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
        
        JLabel titleLabel = new JLabel("Incharge Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        profilePanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        profilePanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        JLabel usernameValue = new JLabel(username);
        usernameValue.setFont(new Font("Arial", Font.PLAIN, 14));
        profilePanel.add(usernameValue, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        profilePanel.add(typeLabel, gbc);
        gbc.gridx = 1;
        JLabel typeValue = new JLabel(inchargeType);
        typeValue.setFont(new Font("Arial", Font.PLAIN, 14));
        profilePanel.add(typeValue, gbc);
        
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
    
    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InchargeDashboard("incharge_electrical", "Incharge Electrical");
        });
    }
}