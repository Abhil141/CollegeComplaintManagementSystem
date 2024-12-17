package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class ComplaintFrame {
    private JFrame frame;
    private JComboBox<String> categoryBox;
    private JTextArea descriptionArea;
    private JButton submitButton, trackButton, logoutButton;
    private String userId; // User ID passed from LoginFrame

    public ComplaintFrame(String userId) {
        this.userId = userId;

        frame = new JFrame("Complaint Registration");
        frame.setSize(600, 400);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        frame.add(panel);
        setupComponents(panel);

        frame.setVisible(true);
    }

    private void setupComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Register Complaint", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBounds(0, 20, 600, 30);
        panel.add(titleLabel);

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setBounds(50, 80, 100, 25);
        panel.add(categoryLabel);

        String[] categories = {"Maintenance", "IT Support", "Facilities", "Other"};
        categoryBox = new JComboBox<>(categories);
        categoryBox.setBounds(150, 80, 200, 30);
        panel.add(categoryBox);

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setBounds(50, 130, 100, 25);
        panel.add(descriptionLabel);

        descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBounds(150, 130, 400, 100);
        panel.add(scrollPane);

        submitButton = new JButton("Submit");
        submitButton.setBounds(150, 270, 100, 30);
        submitButton.addActionListener(e -> registerComplaint());
        panel.add(submitButton);

        trackButton = new JButton("Track Complaints");
        trackButton.setBounds(300, 270, 150, 30);
        trackButton.addActionListener(e -> openTrackingFrame());
        panel.add(trackButton);

        // Log Out Button
        logoutButton = new JButton("Log Out");
        logoutButton.setBounds(475, 20, 100, 30); // Positioned at the top-right corner
        logoutButton.addActionListener(e -> logout());
        logoutButton.setFocusPainted(false); // Remove focus ring (yellow dotted line)
        panel.add(logoutButton);
    }

    private void registerComplaint() {
        String category = (String) categoryBox.getSelectedItem();
        String description = descriptionArea.getText();

        // SQL Server connection details
        String url = "jdbc:sqlserver://localhost:1433;databaseName=ComplaintDB;user=root;password=Abhil@141;trustServerCertificate=true;";
        String dbUser = "root"; // SQL Server authentication username
        String dbPassword = "Abhil@141"; // SQL Server authentication password

        // SQL query for registering a complaint
        String insertQuery = "INSERT INTO complaints (user_id, category, description, status, created_at, updated_at) VALUES (?, ?, ?, 'Pending', GETDATE(), GETDATE())";

        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            // Insert the complaint into the database
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, category);
            preparedStatement.setString(3, description);
            preparedStatement.executeUpdate();

            // Fetch the inserted complaint details from the table
            String fetchQuery = "SELECT TOP 1 category, description FROM complaints WHERE user_id = ? ORDER BY created_at DESC";
            PreparedStatement fetchStatement = connection.prepareStatement(fetchQuery);
            fetchStatement.setString(1, userId);

            ResultSet resultSet = fetchStatement.executeQuery();
            String fetchedCategory = null;
            String fetchedDescription = null;

            if (resultSet.next()) {
                fetchedCategory = resultSet.getString("category");
                fetchedDescription = resultSet.getString("description");
            }

            JOptionPane.showMessageDialog(frame, "Complaint registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Get the user's email from the users table
            String userEmail = getUserEmail(userId);
            if (userEmail != null && fetchedCategory != null && fetchedDescription != null) {
                // Send email notification with fetched category and description
                sendEmailNotification(userEmail, fetchedCategory, fetchedDescription);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error registering complaint.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getUserEmail(String userId) {
        String email = null;

        // SQL query to fetch the user's email
        String query = "SELECT email FROM users WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=ComplaintDB;user=root;password=Abhil@141;trustServerCertificate=true;", "root", "Abhil@141");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                email = resultSet.getString("email");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error fetching user email.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return email;
    }

    private void sendEmailNotification(String recipient, String category, String description) {
        String senderEmail = "abhinavlanka141@gmail.com"; // Your Gmail address
        String appPassword = "bydy zwzq cyga xcbp"; // Your App Password

        String subject = "Complaint Registered Successfully";
        String body = "Dear User,\n\n"
                + "Your complaint has been successfully registered.\n\n"
                + "Here are the details of your complaint:\n"
                + "-------------------------------------------\n"
                + "Category: " + category + "\n"
                + "Description: " + description + "\n"
                + "Status: Pending\n"
                + "-------------------------------------------\n\n"
                + "We will review your complaint and get back to you as soon as possible.\n\n"
                + "Thank you for reaching out.\n\n"
                + "Best Regards,\n"
                + "Complaint Support Team";

        // Set mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Create session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        try {
            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(body);

            // Send email
            Transport.send(message);
            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error sending email notification: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openTrackingFrame() {
        frame.dispose(); // Close current frame
        new ComplaintTrackingFrame(userId, frame); // Open tracking frame and pass current frame
    }

    // Log out method to handle the log out functionality
    private void logout() {
        int response = JOptionPane.showConfirmDialog(frame, "Are you sure you want to log out?", "Confirm Log Out", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            frame.dispose(); // Close current frame
            new LoginFrame(); // Open the LoginFrame
        }
    }
}



