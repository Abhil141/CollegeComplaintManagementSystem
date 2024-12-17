package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ComplaintTrackingFrame {
    private JFrame frame;
    private String userId;

    public ComplaintTrackingFrame(String userId, JFrame previousFrame) {
        this.userId = userId;

        frame = new JFrame("Track Complaints");
        frame.setSize(800, 400);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        frame.add(panel);

        JLabel titleLabel = new JLabel("Your Complaints", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        JTable complaintsTable = new JTable(new DefaultTableModel(new Object[]{"Complaint ID", "Category", "Description", "Status", "Created At"}, 0));
        DefaultTableModel tableModel = (DefaultTableModel) complaintsTable.getModel();
        loadComplaints(tableModel);

        JScrollPane scrollPane = new JScrollPane(complaintsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            frame.dispose(); // Close the tracking frame
            previousFrame.setVisible(true); // Show the previous frame
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void loadComplaints(DefaultTableModel tableModel) {
        // SQL Server connection details
        String url = "jdbc:sqlserver://localhost:1433;databaseName=ComplaintDB;user=root;password=Abhil@141;trustServerCertificate=true;";
        String dbUser = "root"; // SQL Server authentication username
        String dbPassword = "Abhil@141"; // SQL Server authentication password

        // SQL Server query for fetching complaints
        String query = "SELECT complaint_id, category, description, status, created_at FROM complaints WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int complaintId = resultSet.getInt("complaint_id");
                String category = resultSet.getString("category");
                String description = resultSet.getString("description");
                String status = resultSet.getString("status");
                String createdAt = resultSet.getString("created_at");

                tableModel.addRow(new Object[]{complaintId, category, description, status, createdAt});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading complaints.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

