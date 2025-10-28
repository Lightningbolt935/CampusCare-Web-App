package com.srm;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONArray;

public class FacultyDashboard extends JFrame {
    private static final Color PRIMARY_BLUE = new Color(12, 77, 162);
    private static final Color INPUT_GRAY = new Color(238, 238, 238);
    private static final Color BORDER_GRAY = new Color(136, 136, 136);
    private static final Color WHITE = Color.WHITE;
    private static final Color BLACK = Color.BLACK;
    
    private String username;
    private String userId;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> complaintTypeCombo;
    private JComboBox<String> floorCombo;
    private JComboBox<String> roomCombo;
    private JTextArea descriptionArea;
    private JTextField imageLinkField;
    private JButton submitButton;
    private JButton menuButton;
    private JPanel drawerPanel;
    private boolean drawerOpen = false;
    
    private final String[] categories = {"-- Select Category --", "electrical", "architecture", "plumbing"};
    private final String[] floors = {"-- Select Floor --", "1st Floor", "2nd Floor", "3rd Floor", "4th Floor", 
        "5th Floor", "6th Floor", "7th Floor", "8th Floor", "9th Floor", "10th Floor", "11th Floor", "12th Floor", "13th Floor"};
    
    private final String[] flr1 = {"Az 109-A","Az 109-B","Az 109-C","102-A-B-C","Sr 211-14","Gr 101","Gr 110","Wr 103","Wr 105","Wr 107","Wr 108","Eve 104"};
    private final String[] flr2 = {"Fr 201","Fr 218","Fr 222","Dr 202","Dr 203","Dr 219","Dr 223","Dr 224","Lh 204","Lh 205","Lh 220","Lh 221","Cpr 206","Cpr 207","Cpc 210","Wr 211","Wr 213","Wr 214","Wr 215","Wr 216","Adam 212","Sr 225","Sr 226","Sr 227","Sr 228"};
    private final String[] flr3 = {"Fr 321","Fr 311","Fr 306","Fr 307","Fr 308","Fr 309","Fr 310","Fr 301","Dr 322","Dr 323","Dr 303","Dr 302","Lh 320","Lh 319","Lh 305","Lh 304","Sr 327","Sr 324","Sr 325","Sr 326","Cls 318","Wr 317","Wr 316","Wr 315","Wr 314","Wr 312","Eve 313"};
    private final String[] flr4 = {"Ir 401","Ir 402","Cls 403","Cls 404","Cls 405","Cls 412","Cls 413","Cls 414","Cls 418","Cls 419","Wr 406","Wr 408","Wr 409","Wr 410","Wr 411","Adam 407","Fr 415","Dr 416","Dr 417"};
    private final String[] flr5 = {"Lh 504","Lh 505","Lh 517","Lh 518","Fr 519","Fr 501","Fr 520","Dr 502","Dr 503","Dr 521","Dr 522","Cls 523","Cls 524","Hdr 506","Hd 507","Hdc 510","Adam 512","Wr 511","Wr 513","Wr 514","Wr 515","Sr 525","Sr 528"};
    private final String[] flr6 = {"Lh 613","Lh 614","Lh 615","Lh 604","Lh 605","Lh 606","Fr 616","Fr 601","Dr 617","Dr 618","Dr 602","Dr 603","Cls 619","Cls 620","Sr 621","Sr 622","Sr 623","Sr 624","Wr 607","Wr 609","Wr 610","Wr 611","Wr 612","Eve 608"};
    private final String[] flr7 = {"Audi 712","702","711","Wi 701","Gr 703","Gr 704","Sr 713","Sr 716","Wr 705","Wr 707","Wr 708","Wr 709","Wr710","Eve 706"};
    private final String[] flr8 = {"Lh 804","Lh 805","Lh 817","Lh 818","Fr 819","Fr 801","Fr 820","Dr 802","Dr 803","Dr 821","Dr 822","Cls 823","Cls 824","Hdr 806","Hd 807","Hdc 810","Adam 812","Wr 811","Wr 813","Wr 814","Wr 815","Sr 825","Sr 828"};
    private final String[] flr9 = {"Lh 913","Lh 914","Lh 915","Lh 904","Lh 905","Lh 906","Fr 916","Fr 901","Dr 917","Dr 918","Dr 902","Dr 903","Cls 919","Cls 920","Sr 921","Sr 922","Sr 923","Sr 924","Wr 907","Wr 909","Wr 910","Wr 911","Wr 912","Eve 908"};
    private final String[] flr10 = {"Classroom J"};
    private final String[] flr11 = {"Lh 1104","Lh 1105","Lh 1117","Lh 1118","Fr 1119","Fr 1101","Fr 1120","Dr 1102","Dr 1103","Dr 1121","Dr 1122","Cls 1123","Cls 1124","Hdr 1106","Hd 1107","Hdc 1110","Adam 1112","Wr 1111","Wr 1113","Wr 1114","Wr 1115","Sr 1125","Sr 1128"};
    private final String[] flr12 = {"Classroom L"};
    private final String[] flr13 = {"Classroom M"};
    
    private final String[] electricalTypes = {"-- Select Type --", "Lights", "Fans", "AC", "Smart TV"};
    private final String[] architectureTypes = {"-- Select Type --", "Benches", "Walls", "Windows", "Doors"};
    private final String[] plumbingTypes = {"-- Select Type --", "Water Leak", "Wash Basin", "Water Supply", "Toilet", "Pipes"};

    public FacultyDashboard(String username, String userId) {
        this.username = username;
        this.userId = userId;
        setTitle("Faculty Dashboard - Submit Complaint");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 800);
        setLocationRelativeTo(null);
        setResizable(true);
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(WHITE);
        topPanel.setBorder(new LineBorder(BORDER_GRAY, 1));
        topPanel.setPreferredSize(new Dimension(0, 80));
        
        menuButton = new JButton("☰");
        menuButton.setFont(new Font("Arial", Font.BOLD, 20));
        menuButton.setBackground(WHITE);
        menuButton.setForeground(PRIMARY_BLUE);
        menuButton.setBorder(null);
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButton.setPreferredSize(new Dimension(60, 60));
        menuButton.addActionListener(e -> toggleDrawer());
        
        JLabel headingLabel = new JLabel("Submit a Complaint");
        headingLabel.setFont(new Font("Arial", Font.BOLD, 22));
        headingLabel.setForeground(PRIMARY_BLUE);
        headingLabel.setHorizontalAlignment(JLabel.CENTER);
        topPanel.add(menuButton, BorderLayout.WEST);
        topPanel.add(headingLabel, BorderLayout.CENTER);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(WHITE);
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        gbc.gridy = 0;
        contentPanel.add(createLabel("Select Category"), gbc);
        gbc.gridy = 1;
        categoryCombo = createComboBox(categories);
        categoryCombo.addActionListener(e -> updateComplaintTypes());
        contentPanel.add(categoryCombo, gbc);
        
        gbc.gridy = 2;
        contentPanel.add(createLabel("Select Complaint Type"), gbc);
        gbc.gridy = 3;
        complaintTypeCombo = createComboBox(new String[]{"-- Select Type --"});
        contentPanel.add(complaintTypeCombo, gbc);
        
        gbc.gridy = 4;
        contentPanel.add(createLabel("Select Floor"), gbc);
        gbc.gridy = 5;
        floorCombo = createComboBox(floors);
        floorCombo.addActionListener(e -> updateRooms());
        contentPanel.add(floorCombo, gbc);
        
        gbc.gridy = 6;
        contentPanel.add(createLabel("Select Room"), gbc);
        gbc.gridy = 7;
        roomCombo = createComboBox(new String[]{"-- Select Room --"});
        contentPanel.add(roomCombo, gbc);
        
        gbc.gridy = 8;
        contentPanel.add(createLabel("Problem Description"), gbc);
        gbc.gridy = 9;
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionArea.setBackground(INPUT_GRAY);
        descriptionArea.setBorder(new LineBorder(BORDER_GRAY, 1, true));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        contentPanel.add(new JScrollPane(descriptionArea), gbc);
        
        gbc.gridy = 10;
        contentPanel.add(createLabel("Image Drive Link (Optional)"), gbc);
        gbc.gridy = 11;
        imageLinkField = new JTextField();
        imageLinkField.setFont(new Font("Arial", Font.PLAIN, 14));
        imageLinkField.setBackground(INPUT_GRAY);
        imageLinkField.setBorder(new LineBorder(BORDER_GRAY, 1, true));
        imageLinkField.setPreferredSize(new Dimension(0, 35));
        contentPanel.add(imageLinkField, gbc);
        
        gbc.gridy = 12;
        submitButton = new JButton("SUBMIT");
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setBackground(PRIMARY_BLUE);
        submitButton.setForeground(WHITE);
        submitButton.setBorder(null);
        submitButton.setFocusPainted(false);
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitButton.setPreferredSize(new Dimension(150, 45));
        submitButton.addActionListener(e -> handleSubmit());
        gbc.insets = new Insets(30, 0, 10, 0);
        contentPanel.add(submitButton, gbc);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBackground(WHITE);
        scrollPane.setBorder(null);
        drawerPanel = createDrawerPanel();
        drawerPanel.setVisible(false);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(drawerPanel, BorderLayout.WEST);
        setVisible(true);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(PRIMARY_BLUE);
        return label;
    }
    
    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("Arial", Font.PLAIN, 14));
        combo.setBackground(INPUT_GRAY);
        combo.setBorder(new LineBorder(BORDER_GRAY, 1, true));
        combo.setPreferredSize(new Dimension(0, 35));
        return combo;
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
    
    private void updateComplaintTypes() {
        String selected = (String) categoryCombo.getSelectedItem();
        String[] types = new String[]{"-- Select Type --"};
        if ("electrical".equals(selected)) {
            types = electricalTypes;
        } else if ("architecture".equals(selected)) {
            types = architectureTypes;
        } else if ("plumbing".equals(selected)) {
            types = plumbingTypes;
        }
        complaintTypeCombo.removeAllItems();
        for (String type : types) {
            complaintTypeCombo.addItem(type);
        }
    }
    
    private void updateRooms() {
        String selected = (String) floorCombo.getSelectedItem();
        String[] rooms = new String[]{"-- Select Room --"};
        switch (selected) {
            case "1st Floor": rooms = flr1; break;
            case "2nd Floor": rooms = flr2; break;
            case "3rd Floor": rooms = flr3; break;
            case "4th Floor": rooms = flr4; break;
            case "5th Floor": rooms = flr5; break;
            case "6th Floor": rooms = flr6; break;
            case "7th Floor": rooms = flr7; break;
            case "8th Floor": rooms = flr8; break;
            case "9th Floor": rooms = flr9; break;
            case "10th Floor": rooms = flr10; break;
            case "11th Floor": rooms = flr11; break;
            case "12th Floor": rooms = flr12; break;
            case "13th Floor": rooms = flr13; break;
        }
        roomCombo.removeAllItems();
        for (String room : rooms) {
            roomCombo.addItem(room);
        }
    }
    
    private void handleSubmit() {
        String category = (String) categoryCombo.getSelectedItem();
        String complaintType = (String) complaintTypeCombo.getSelectedItem();
        String floor = (String) floorCombo.getSelectedItem();
        String room = (String) roomCombo.getSelectedItem();
        String description = descriptionArea.getText().trim();
        String imageLink = imageLinkField.getText().trim();
        
        if ("-- Select Category --".equals(category) || "-- Select Type --".equals(complaintType) || 
            "-- Select Floor --".equals(floor) || "-- Select Room --".equals(room) || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // FIXED: Map category to exact incharge name format
        final String assignedIncharge;
        if ("electrical".equals(category)) {
            assignedIncharge = "incharge_electrical";  // Changed to match database username
        } else if ("architecture".equals(category)) {
            assignedIncharge = "incharge_architecture";  // Changed to match database username
        } else if ("plumbing".equals(category)) {
            assignedIncharge = "incharge_plumbing";  // Changed to match database username
        } else {
            assignedIncharge = "";
        }
        
        System.out.println("DEBUG: Submitting complaint with assigned_incharge: " + assignedIncharge);
        
        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");
        new Thread(() -> submitComplaint(category, complaintType, floor, room, description, imageLink, assignedIncharge)).start();
    }
    
    private void submitComplaint(String category, String complaintType, String floor, String room, String description, String imageLink, String assignedIncharge) {
        try {
            URL url = new URL("http://127.0.0.1:5000/faculty");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            
            JSONObject requestData = new JSONObject();
            requestData.put("faculty_id", userId);
            requestData.put("category", category);
            requestData.put("type", complaintType);
            requestData.put("floor", floor);
            requestData.put("classroom", room);
            requestData.put("status", "Pending");
            requestData.put("description", description);
            requestData.put("image", imageLink);
            requestData.put("assigned_incharge", assignedIncharge);
            
            System.out.println("DEBUG: Sending JSON: " + requestData.toString());
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject responseData = new JSONObject(response.toString());
                
                SwingUtilities.invokeLater(() -> {
                    submitButton.setEnabled(true);
                    submitButton.setText("SUBMIT");
                    if (responseData.getBoolean("success")) {
                        JOptionPane.showMessageDialog(this, 
                            "Complaint submitted successfully!\nAssigned to: " + assignedIncharge, 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                    } else {
                        JOptionPane.showMessageDialog(this, responseData.getString("message"), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                submitButton.setEnabled(true);
                submitButton.setText("SUBMIT");
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    private void clearForm() {
        categoryCombo.setSelectedIndex(0);
        complaintTypeCombo.setSelectedIndex(0);
        floorCombo.setSelectedIndex(0);
        roomCombo.setSelectedIndex(0);
        descriptionArea.setText("");
        imageLinkField.setText("");
    }
    
    private void handleMenuAction(String action) {
        toggleDrawer();
        switch (action) {
            case "Profile": showProfileModal(); break;
            case "History": showHistoryModal(); break;
            case "Logout":
                if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    dispose();
                    new LoginScreen();
                }
                break;
        }
    }
    
    private void showProfileModal() {
        JDialog profileDialog = new JDialog(this, "Profile", true);
        profileDialog.setSize(400, 300);
        profileDialog.setLocationRelativeTo(this);
        profileDialog.setLayout(new BorderLayout());
        
        JPanel profilePanel = new JPanel();
        profilePanel.setBackground(WHITE);
        profilePanel.setLayout(new GridBagLayout());
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JLabel titleLabel = new JLabel("Faculty Profile");
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
        JLabel userIdLabel = new JLabel("Faculty ID:");
        userIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        profilePanel.add(userIdLabel, gbc);
        gbc.gridx = 1;
        JLabel userIdValue = new JLabel(userId);
        userIdValue.setFont(new Font("Arial", Font.PLAIN, 14));
        profilePanel.add(userIdValue, gbc);
        
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
    
    private void showHistoryModal() {
        JDialog historyDialog = new JDialog(this, "Complaint History", true);
        historyDialog.setSize(700, 500);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Your Complaint History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(WHITE);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel loadingLabel = new JLabel("Loading complaints...");
        loadingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(loadingLabel);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        
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
        historyDialog.add(scrollPane, BorderLayout.CENTER);
        historyDialog.add(bottomPanel, BorderLayout.SOUTH);
        historyDialog.setVisible(true);
        
        new Thread(() -> fetchComplaintHistory(contentPanel, loadingLabel)).start();
    }
    
    private void fetchComplaintHistory(JPanel contentPanel, JLabel loadingLabel) {
        try {
            URL url = new URL("http://127.0.0.1:5000/faculty/history/" + userId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                JSONObject responseData = new JSONObject(response.toString());
                JSONArray complaints = responseData.getJSONArray("complaints");
                
                SwingUtilities.invokeLater(() -> {
                    contentPanel.remove(loadingLabel);
                    if (complaints.length() == 0) {
                        JLabel noComplaintsLabel = new JLabel("No complaints found");
                        noComplaintsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                        noComplaintsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        contentPanel.add(noComplaintsLabel);
                    } else {
                        for (int i = 0; i < complaints.length(); i++) {
                            JSONObject complaint = complaints.getJSONObject(i);
                            JPanel complaintPanel = createComplaintCard(complaint);
                            contentPanel.add(complaintPanel);
                            contentPanel.add(Box.createVerticalStrut(10));
                        }
                    }
                    contentPanel.revalidate();
                    contentPanel.repaint();
                });
            }
            connection.disconnect();
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                contentPanel.remove(loadingLabel);
                JLabel errorLabel = new JLabel("Error loading complaints: " + e.getMessage());
                errorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                errorLabel.setForeground(Color.RED);
                errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(errorLabel);
                contentPanel.revalidate();
                contentPanel.repaint();
            });
        }
    }
    
    private JPanel createComplaintCard(JSONObject complaint) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(INPUT_GRAY);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_GRAY, 1, true), BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(INPUT_GRAY);
        
        try {
            String category = complaint.getString("category");
            String type = complaint.getString("type");
            String floor = complaint.getString("floor");
            String room = complaint.getString("classroom");
            String status = complaint.getString("status");
            String description = complaint.getString("description");
            
            JLabel categoryLabel = new JLabel("Category: " + category + " - " + type);
            categoryLabel.setFont(new Font("Arial", Font.BOLD, 14));
            categoryLabel.setForeground(PRIMARY_BLUE);
            
            JLabel locationLabel = new JLabel("Location: " + floor + " - " + room);
            locationLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            
            JLabel statusLabel = new JLabel("Status: " + status);
            statusLabel.setFont(new Font("Arial", Font.BOLD, 13));
            if ("Pending".equals(status)) {
                statusLabel.setForeground(new Color(255, 140, 0));
            } else if ("Completed".equals(status)) {
                statusLabel.setForeground(new Color(0, 128, 0));
            } else if ("In Progress".equals(status)) {
                statusLabel.setForeground(new Color(0, 0, 255));
            }
            
            JTextArea descArea = new JTextArea(description);
            descArea.setFont(new Font("Arial", Font.PLAIN, 12));
            descArea.setBackground(INPUT_GRAY);
            descArea.setEditable(false);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            descArea.setBorder(null);
            descArea.setRows(2);
            
            infoPanel.add(categoryLabel);
            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(locationLabel);
            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(statusLabel);
            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(descArea);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error displaying complaint");
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            errorLabel.setForeground(Color.RED);
            infoPanel.add(errorLabel);
        }
        
        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FacultyDashboard("Test Faculty", "FAC001");
        });
    }
}