package com.srm;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Objects; // Needed for the dropdown check

public class GymManagementGUI extends JFrame {

    // Database connection details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/db";
    private static final String USER = "root";
    private static final String PASSWORD = "tiger";

    // Swing components
    private final JTable memberTable;
    private final DefaultTableModel tableModel;
    private final JTextField idField = new JTextField(5);
    private final JTextField nameField = new JTextField(15);
    private final JTextField typeField = new JTextField(10);
    private final JTextField feesField = new JTextField(7);
    private final JButton addButton = new JButton("Register");
    private final JButton updateButton = new JButton("Update Fees");
    private final JButton attendanceButton = new JButton("Mark Attendance");
    private final JButton clearButton = new JButton("Clear");

    public GymManagementGUI() {
        setTitle("Gym Membership Management System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Table setup
        String[] columnNames = {"Member ID", "Name", "Membership Type", "Fees", "Attendance"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        memberTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(memberTable);
        add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Manage Member"));
        inputPanel.add(new JLabel("ID:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Type:"));
        inputPanel.add(typeField);
        inputPanel.add(new JLabel("Fees:"));
        inputPanel.add(feesField);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(attendanceButton);
        buttonPanel.add(clearButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        addListeners();

        // Load initial data
        setupDatabase();
        loadMembers();
    }

    private void addListeners() {
        memberTable.getSelectionModel().addListSelectionListener(e -> {
            // Check if the event is still adjusting and a row is selected
            if (!e.getValueIsAdjusting() && memberTable.getSelectedRow() != -1) {
                int row = memberTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(row, 0).toString());
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                typeField.setText(tableModel.getValueAt(row, 2).toString());
                feesField.setText(tableModel.getValueAt(row, 3).toString());
            }
        });

        addButton.addActionListener(e -> registerMember());
        updateButton.addActionListener(e -> updateFees());
        // Updated listener to show the attendance dialog
        attendanceButton.addActionListener(e -> showAttendanceDialog()); 
        clearButton.addActionListener(e -> clearFields());
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    private void setupDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String createTable = "CREATE TABLE IF NOT EXISTS GymMembers (" +
                    "member_id INT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "membership_type VARCHAR(50), " +
                    "fees DOUBLE, " +
                    "attendance_count INT DEFAULT 0)";
            stmt.executeUpdate(createTable);
        } catch (SQLException e) {
            showError("Database setup failed", e);
        }
    }

    private void loadMembers() {
        tableModel.setRowCount(0);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM GymMembers")) {

            while (rs.next()) {
                int id = rs.getInt("member_id");
                String name = rs.getString("name");
                String type = rs.getString("membership_type");
                double fees = rs.getDouble("fees");
                int attendance = rs.getInt("attendance_count");
                tableModel.addRow(new Object[]{id, name, type, fees, attendance});
            }
        } catch (SQLException e) {
            showError("Could not load members", e);
        }
    }

    private void registerMember() {
        // Basic input validation
        if (idField.getText().isEmpty() || nameField.getText().isEmpty() || typeField.getText().isEmpty() || feesField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled for registration.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO GymMembers (member_id, name, membership_type, fees) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(idField.getText()));
            pstmt.setString(2, nameField.getText());
            pstmt.setString(3, typeField.getText());
            pstmt.setDouble(4, Double.parseDouble(feesField.getText()));

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                loadMembers();
                clearFields();
                JOptionPane.showMessageDialog(this, "Member registered successfully!");
            }
        } catch (NumberFormatException nfe) {
             JOptionPane.showMessageDialog(this, "ID and Fees must be valid numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) { // MySQL error code for Duplicate entry for primary key
                 JOptionPane.showMessageDialog(this, "Member with ID " + idField.getText() + " already exists.", "Registration Error", JOptionPane.WARNING_MESSAGE);
            } else {
                 showError("Error registering member", ex);
            }
        }
    }

    private void updateFees() {
        // Basic input validation
        if (idField.getText().isEmpty() || feesField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Member ID and Fees must be filled to update fees.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE GymMembers SET fees = ? WHERE member_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, Double.parseDouble(feesField.getText()));
            pstmt.setInt(2, Integer.parseInt(idField.getText()));

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                loadMembers();
                clearFields();
                JOptionPane.showMessageDialog(this, "Fees updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Member not found.");
            }
        } catch (NumberFormatException nfe) {
             JOptionPane.showMessageDialog(this, "ID and Fees must be valid numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showError("Error updating fees", ex);
        }
    }
    
    // --- New/Modified Methods for Attendance Dropdown ---
    
    private void showAttendanceDialog() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a member or enter a Member ID to mark attendance.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create the JComboBox with options
        String[] options = {"Present", "Absent"};
        JComboBox<String> attendanceDropdown = new JComboBox<>(options);
        
        // Show the custom input dialog
        int result = JOptionPane.showConfirmDialog(
            this,
            new Object[]{"Mark attendance for Member ID: " + idField.getText(), attendanceDropdown},
            "Mark Attendance",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String status = (String) attendanceDropdown.getSelectedItem();
            // Pass the selected status to the main attendance logic
            markAttendance(status);
        }
    }

    // Renamed and modified the original markAttendance to accept a status
    private void markAttendance(String status) {
        if (Objects.equals(status, "Present")) {
            String sql = "UPDATE GymMembers SET attendance_count = attendance_count + 1 WHERE member_id = ?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(idField.getText()));

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    loadMembers();
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Attendance marked as Present. Count updated.");
                } else {
                    JOptionPane.showMessageDialog(this, "Member not found.");
                }
            } catch (NumberFormatException nfe) {
                 JOptionPane.showMessageDialog(this, "Member ID must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                showError("Error marking attendance", ex);
            }
        } else if (Objects.equals(status, "Absent")) {
            // Note: The attendance_count in the database is NOT decreased for 'Absent', 
            // as the original schema only supports counting presence.
            loadMembers(); 
            clearFields();
            JOptionPane.showMessageDialog(this, "Attendance marked as Absent. Member 'attendance_count' was NOT changed.");
        }
    }
    // --- End of New/Modified Methods ---

    private void clearFields() {
        memberTable.clearSelection();
        idField.setText("");
        nameField.setText("");
        typeField.setText("");
        feesField.setText("");
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this, message + "\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GymManagementGUI().setVisible(true));
    }
}