package org.example;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Properties;

public class AdminFrame {
    private JFrame frame;
    private JTable complaintsTable;
    private DefaultTableModel tableModel;
    private JButton resolveButton, rejectButton, refreshButton, logoutButton;

    public AdminFrame() {
        frame = new JFrame("Admin Panel - Complaint Management");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        frame.add(panel);

        JLabel titleLabel = new JLabel("Complaint Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        setupTable(panel);
        setupButtons(panel);

        frame.setVisible(true);
        loadComplaints();
    }

    private void setupTable(JPanel panel) {
        String[] columnNames = {"Complaint ID", "User ID", "Category", "Description", "Status", "Created At"};
        tableModel = new DefaultTableModel(columnNames, 0);
        complaintsTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(complaintsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
    }

    private void setupButtons(JPanel panel) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        resolveButton = new JButton("Resolve");
        resolveButton.addActionListener(this::updateStatus);
        buttonPanel.add(resolveButton);

        rejectButton = new JButton("Reject");
        rejectButton.addActionListener(this::updateStatus);
        buttonPanel.add(rejectButton);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadComplaints());
        buttonPanel.add(refreshButton);

        // Log Out Button
        logoutButton = new JButton("Log Out");
        logoutButton.addActionListener(e -> logout());
        buttonPanel.add(logoutButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadComplaints() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=ComplaintDB;user=root;password=Abhil@141;trustServerCertificate=true;";
        String query = "SELECT complaint_id, user_id, category, description, status, created_at FROM complaints";

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            tableModel.setRowCount(0); // Clear the table before loading new data

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("complaint_id"),
                        rs.getString("user_id"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                };
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading complaints: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatus(ActionEvent e) {
        int selectedRow = complaintsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a complaint to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int complaintId = (int) tableModel.getValueAt(selectedRow, 0); // Complaint ID from complaints table
        String userId = (String) tableModel.getValueAt(selectedRow, 1); // User ID
        String category = (String) tableModel.getValueAt(selectedRow, 2); // Category of the complaint
        String newStatus = e.getSource() == resolveButton ? "Resolved" : "Rejected";

        String url = "jdbc:sqlserver://localhost:1433;databaseName=ComplaintDB;user=root;password=Abhil@141;trustServerCertificate=true;";
        String updateQuery = "UPDATE complaints SET status = ?, updated_at = GETDATE() WHERE complaint_id = ?";
        String emailQuery = "SELECT email FROM users WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection(url)) {

            // Step 1: Update complaint status
            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setString(1, newStatus);
                updateStatement.setInt(2, complaintId);
                updateStatement.executeUpdate();
            }

            // Step 2: Retrieve user email from users table
            String userEmail = null;
            try (PreparedStatement emailStatement = connection.prepareStatement(emailQuery)) {
                emailStatement.setString(1, userId);
                try (ResultSet rs = emailStatement.executeQuery()) {
                    if (rs.next()) {
                        userEmail = rs.getString("email");
                    }
                }
            }

            // Step 3: Notify admin if email is not found
            if (userEmail == null || userEmail.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "User email not found for User ID: " + userId, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Step 4: Send email notification
            sendEmailNotification(userEmail, complaintId, category, newStatus);

            JOptionPane.showMessageDialog(frame, "Complaint status updated to " + newStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
            loadComplaints(); // Refresh the table

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void sendEmailNotification(String recipientEmail, int complaintId, String category, String status) {
        final String senderEmail = "abhinavlanka141@gmail.com";
        final String senderPassword = "bydy zwzq cyga xcbp";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Complaint Status Update");

            String emailBody = String.format(
                    "Dear User,\n\nYour complaint with ID %d under the category '%s' has been marked as '%s'.\n\nThank you for your patience.\n\nRegards,\nComplaint Management Team",
                    complaintId, category, status
            );

            message.setText(emailBody);
            Transport.send(message);

            JOptionPane.showMessageDialog(frame, "Email notification sent to " + recipientEmail, "Email Sent", JOptionPane.INFORMATION_MESSAGE);

        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(frame, "Error sending email: " + e.getMessage(), "Email Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        frame.dispose(); // Close the current admin frame
        new LoginFrame(); // Open the login frame
    }
}




