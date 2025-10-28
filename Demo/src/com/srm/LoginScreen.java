package com.srm;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.json.JSONObject;
import org.json.JSONException;

public class LoginScreen extends JFrame {
    // Minimal & Professional Color Palette
    private static final Color PRIMARY_CYAN = new Color(0, 142, 204);  // #008ECC
    private static final Color LIGHT_GRAY = new Color(248, 249, 250);
    private static final Color MEDIUM_GRAY = new Color(206, 212, 218);
    private static final Color DARK_GRAY = new Color(73, 80, 87);
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color WHITE = Color.WHITE;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton showPasswordButton;
    private JButton loginButton;
    private JLabel imageLabel;
    private boolean showPassword = false;

    public LoginScreen() {
        setTitle("SRM - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Main container with split design
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(WHITE);
        
        // LEFT SIDE - Branding section with cyan background
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(PRIMARY_CYAN);
        leftPanel.setPreferredSize(new Dimension(400, 600));
        leftPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.gridx = 0;
        leftGbc.insets = new Insets(20, 40, 20, 40);
        
        // Logo
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(160, 160));
        loadLogo();
        leftGbc.gridy = 0;
        leftPanel.add(imageLabel, leftGbc);
        
        // Brand title
        JLabel brandTitle = new JLabel("SRM");
        brandTitle.setFont(new Font("Arial", Font.BOLD, 48));
        brandTitle.setForeground(WHITE);
        brandTitle.setHorizontalAlignment(JLabel.CENTER);
        leftGbc.gridy = 1;
        leftGbc.insets = new Insets(20, 40, 10, 40);
        leftPanel.add(brandTitle, leftGbc);
        
        // Brand subtitle
        JLabel brandSubtitle = new JLabel("Complaints Management");
        brandSubtitle.setFont(new Font("Arial", Font.PLAIN, 18));
        brandSubtitle.setForeground(new Color(230, 245, 255));
        brandSubtitle.setHorizontalAlignment(JLabel.CENTER);
        leftGbc.gridy = 2;
        leftGbc.insets = new Insets(0, 40, 40, 40);
        leftPanel.add(brandSubtitle, leftGbc);
        
        // RIGHT SIDE - Login form section with light gray background
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(LIGHT_GRAY);
        rightPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.gridx = 0;
        rightGbc.anchor = GridBagConstraints.WEST;
        rightGbc.insets = new Insets(10, 60, 10, 60);
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 32));
        welcomeLabel.setForeground(TEXT_DARK);
        rightGbc.gridy = 0;
        rightGbc.insets = new Insets(0, 60, 10, 60);
        rightPanel.add(welcomeLabel, rightGbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Please login to your account");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        subtitleLabel.setForeground(DARK_GRAY);
        rightGbc.gridy = 1;
        rightGbc.insets = new Insets(0, 60, 40, 60);
        rightPanel.add(subtitleLabel, rightGbc);
        
        // Username label
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameLabel.setForeground(DARK_GRAY);
        rightGbc.gridy = 2;
        rightGbc.insets = new Insets(10, 60, 8, 60);
        rightPanel.add(usernameLabel, rightGbc);
        
        // Username field
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 15));
        usernameField.setBackground(WHITE);
        usernameField.setForeground(TEXT_DARK);
        usernameField.setCaretColor(PRIMARY_CYAN);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MEDIUM_GRAY, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        usernameField.setPreferredSize(new Dimension(380, 45));
        
        // Focus effect
        usernameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_CYAN, 2),
                    BorderFactory.createEmptyBorder(11, 14, 11, 14)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MEDIUM_GRAY, 1),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
                ));
            }
        });
        
        rightGbc.gridy = 3;
        rightGbc.insets = new Insets(0, 60, 15, 60);
        rightPanel.add(usernameField, rightGbc);
        
        // Password label
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordLabel.setForeground(DARK_GRAY);
        rightGbc.gridy = 4;
        rightGbc.insets = new Insets(10, 60, 8, 60);
        rightPanel.add(passwordLabel, rightGbc);
        
        // Password field container
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(WHITE);
        passwordPanel.setBorder(BorderFactory.createLineBorder(MEDIUM_GRAY, 1));
        passwordPanel.setPreferredSize(new Dimension(380, 45));
        
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 15));
        passwordField.setBackground(WHITE);
        passwordField.setForeground(TEXT_DARK);
        passwordField.setCaretColor(PRIMARY_CYAN);
        passwordField.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 10));
        
        showPasswordButton = new JButton("👁");
        showPasswordButton.setFont(new Font("Arial", Font.PLAIN, 16));
        showPasswordButton.setBackground(WHITE);
        showPasswordButton.setForeground(DARK_GRAY);
        showPasswordButton.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 15));
        showPasswordButton.setFocusPainted(false);
        showPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordButton.setContentAreaFilled(false);
        showPasswordButton.addActionListener(e -> togglePasswordVisibility());
        
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showPasswordButton, BorderLayout.EAST);
        
        // Password focus effect
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordPanel.setBorder(BorderFactory.createLineBorder(PRIMARY_CYAN, 2));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                passwordPanel.setBorder(BorderFactory.createLineBorder(MEDIUM_GRAY, 1));
            }
        });
        
        rightGbc.gridy = 5;
        rightGbc.insets = new Insets(0, 60, 30, 60);
        rightPanel.add(passwordPanel, rightGbc);
        
        // Login button
        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Arial", Font.BOLD, 15));
        loginButton.setBackground(PRIMARY_CYAN);
        loginButton.setForeground(WHITE);
        loginButton.setBorder(BorderFactory.createEmptyBorder(13, 30, 13, 30));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(380, 48));
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        
        // Button hover effect
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (loginButton.isEnabled()) {
                    loginButton.setBackground(new Color(0, 120, 180));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (loginButton.isEnabled()) {
                    loginButton.setBackground(PRIMARY_CYAN);
                }
            }
        });
        
        loginButton.addActionListener(e -> handleLogin());
        rightGbc.gridy = 6;
        rightGbc.insets = new Insets(10, 60, 20, 60);
        rightPanel.add(loginButton, rightGbc);
        
        // Add panels to main container
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        setVisible(true);
    }
    
    private void loadLogo() {
        System.out.println("=== LOGO LOADING ATTEMPT ===");
        
        // Try loading from classpath
        String[] resourcePaths = {
            "/com/srm/logo.png",
            "/logo.png",
            "logo.png",
            "/resources/logo.png"
        };
        
        for (String path : resourcePaths) {
            try {
                java.net.URL imageURL = getClass().getResource(path);
                
                if (imageURL != null) {
                    Image img = null;
                    try {
                        img = ImageIO.read(imageURL);
                    } catch (Exception e1) {
                        try {
                            InputStream stream = imageURL.openStream();
                            img = ImageIO.read(stream);
                            stream.close();
                        } catch (Exception e2) {
                            try {
                                img = Toolkit.getDefaultToolkit().getImage(imageURL);
                                MediaTracker tracker = new MediaTracker(this);
                                tracker.addImage(img, 0);
                                tracker.waitForAll();
                            } catch (Exception e3) {
                            }
                        }
                    }
                    
                    if (img != null && img.getWidth(null) > 0) {
                        Image scaledImg = img.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(scaledImg));
                        System.out.println("✓ Logo loaded from: " + path);
                        return;
                    }
                }
            } catch (Exception e) {
            }
        }
        
        // Try loading from file system
        String[] filePaths = {
            "logo.png",
            "src/logo.png",
            "src/com/srm/logo.png",
            "resources/logo.png"
        };
        
        for (String path : filePaths) {
            try {
                File imageFile = new File(path);
                if (imageFile.exists() && imageFile.length() > 0) {
                    Image img = ImageIO.read(imageFile);
                    if (img != null) {
                        Image scaledImg = img.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(scaledImg));
                        System.out.println("✓ Logo loaded from: " + path);
                        return;
                    }
                }
            } catch (Exception e) {
            }
        }
        
        System.out.println("⚠ Using placeholder logo");
        createMinimalPlaceholder();
    }
    
    private void createMinimalPlaceholder() {
        int size = 140;
        java.awt.image.BufferedImage placeholder = new java.awt.image.BufferedImage(
            size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Simple white circle
        g2d.setColor(WHITE);
        g2d.fillOval(10, 10, size - 20, size - 20);
        
        // Border
        g2d.setColor(new Color(230, 245, 255));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(10, 10, size - 20, size - 20);
        
        // Simple building icon
        g2d.setColor(PRIMARY_CYAN);
        int centerX = size / 2;
        int centerY = size / 2;
        
        // Building blocks
        g2d.fillRect(centerX - 25, centerY - 10, 18, 30);
        g2d.fillRect(centerX + 7, centerY - 10, 18, 30);
        
        // Roof
        g2d.fillRect(centerX - 28, centerY - 18, 56, 8);
        
        // Windows
        g2d.setColor(WHITE);
        g2d.fillRect(centerX - 20, centerY - 5, 4, 5);
        g2d.fillRect(centerX - 20, centerY + 5, 4, 5);
        g2d.fillRect(centerX - 12, centerY - 5, 4, 5);
        g2d.fillRect(centerX - 12, centerY + 5, 4, 5);
        
        g2d.fillRect(centerX + 12, centerY - 5, 4, 5);
        g2d.fillRect(centerX + 12, centerY + 5, 4, 5);
        g2d.fillRect(centerX + 20, centerY - 5, 4, 5);
        g2d.fillRect(centerX + 20, centerY + 5, 4, 5);
        
        // Door
        g2d.fillRect(centerX - 4, centerY + 12, 8, 8);
        
        g2d.dispose();
        imageLabel.setIcon(new ImageIcon(placeholder));
    }
    
    private void togglePasswordVisibility() {
        showPassword = !showPassword;
        if (showPassword) {
            passwordField.setEchoChar((char) 0);
            showPasswordButton.setText("🙈");
        } else {
            passwordField.setEchoChar('•');
            showPasswordButton.setText("👁");
        }
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        loginButton.setEnabled(false);
        loginButton.setText("LOGGING IN...");
        
        new Thread(() -> performLogin(username, password)).start();
    }
    
    private void performLogin(String username, String password) {
        try {
            URL url = new URL("http://localhost:5000/explore");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JSONObject responseData = new JSONObject(response.toString());
                
                SwingUtilities.invokeLater(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGIN");
                    loginButton.setBackground(PRIMARY_CYAN);
                    
                    if (responseData.getBoolean("success")) {
                        String userRole = responseData.getJSONObject("user").getString("role");
                        String userId = responseData.getJSONObject("user").optString("id", username);
                        String userName = responseData.getJSONObject("user").optString("name", username);
                        
                        JOptionPane.showMessageDialog(this, 
                            "Welcome " + userName + "!", "Login Success", JOptionPane.INFORMATION_MESSAGE);
                        
                        handleLoginSuccess(userRole, username, userId, userName);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            responseData.getString("message"), "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGIN");
                    loginButton.setBackground(PRIMARY_CYAN);
                    JOptionPane.showMessageDialog(this, 
                        "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
                });
            }
            
            connection.disconnect();
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                loginButton.setEnabled(true);
                loginButton.setText("LOGIN");
                loginButton.setBackground(PRIMARY_CYAN);
                JOptionPane.showMessageDialog(this, 
                    "Error: Could not connect to server\n" + e.getMessage(), 
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    private void handleLoginSuccess(String userRole, String username, String userId, String userName) {
        try {
            this.dispose();
            
            switch (userRole.toLowerCase()) {
                case "faculty":
                    new FacultyDashboard(username, userId);
                    break;
                    
                case "incharge":
                    String inchargeType = determineInchargeType(username);
                    new InchargeDashboard(username, inchargeType);
                    break;
                    
                case "admin":
                    showPlaceholderDashboard("Admin Dashboard", userRole, userName);
                    break;
                    
                case "worker":
                    new WorkerDashboard(userName);
                    break;
                    
                default:
                    JOptionPane.showMessageDialog(null, "Unknown role: " + userRole, 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    new LoginScreen();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error opening dashboard: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            new LoginScreen();
        }
    }
    
    private String determineInchargeType(String username) {
        String lowerUsername = username.toLowerCase();
        
        if (lowerUsername.contains("electrical") || lowerUsername.contains("electric")) {
            return "Incharge Electrical";
        } else if (lowerUsername.contains("architecture") || lowerUsername.contains("arch")) {
            return "Incharge Architecture";
        } else if (lowerUsername.contains("plumbing") || lowerUsername.contains("plumb")) {
            return "Incharge Plumbing";
        }
        
        String[] options = {"Incharge Electrical", "Incharge Architecture", "Incharge Plumbing"};
        String selection = (String) JOptionPane.showInputDialog(
            null,
            "Please select your incharge type:",
            "Incharge Type",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        return selection != null ? selection : "Incharge Electrical";
    }
    
    private void showPlaceholderDashboard(String title, String role, String username) {
        JFrame dashboard = new JFrame(title);
        dashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboard.setSize(600, 400);
        dashboard.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setBackground(LIGHT_GRAY);
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        JLabel welcomeLabel = new JLabel("Welcome " + username + " (" + role + ")");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setForeground(TEXT_DARK);
        panel.add(welcomeLabel, gbc);
        
        gbc.gridy = 1;
        JLabel comingSoonLabel = new JLabel("Dashboard coming soon...");
        comingSoonLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        comingSoonLabel.setForeground(DARK_GRAY);
        panel.add(comingSoonLabel, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setBackground(PRIMARY_CYAN);
        logoutButton.setForeground(WHITE);
        logoutButton.setPreferredSize(new Dimension(120, 40));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> {
            dashboard.dispose();
            new LoginScreen();
        });
        panel.add(logoutButton, gbc);
        
        dashboard.add(panel);
        dashboard.setVisible(true);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> new LoginScreen());
    }
}