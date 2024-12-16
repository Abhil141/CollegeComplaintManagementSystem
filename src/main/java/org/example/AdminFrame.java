package org.example;



import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class AdminFrame {
    private JFrame frame;
    private JTable complaintsTable;
    private DefaultTableModel tableModel;
    private JButton resolveButton, rejectButton, refreshButton;

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

        panel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadComplaints() {
        String url = "jdbc:mysql://localhost:3306/oom";
        String user = "root";
        String password = "Abhil@141";

        String query = "SELECT complaint_id, user_id, category, description, status, created_at FROM complaints";

        try (Connection connection = DriverManager.getConnection(url, user, password);
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

        int complaintId = (int) tableModel.getValueAt(selectedRow, 0);
        String newStatus = e.getSource() == resolveButton ? "Resolved" : "Rejected";

        String url = "jdbc:mysql://localhost:3306/oom";
        String user = "root";
        String password = "pritesh7896";

        String query = "UPDATE complaints SET status = ?, updated_at = NOW() WHERE complaint_id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, newStatus);
            preparedStatement.setInt(2, complaintId);
            preparedStatement.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Complaint status updated to " + newStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
            loadComplaints(); // Refresh the table

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

