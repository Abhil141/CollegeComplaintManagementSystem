package org.example;

import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class HomepageFrame {
    private JFrame frame;
    private JButton loginButton;

    public HomepageFrame() {
        // Set JTattoo theme
        setLookAndFeel();

        // Initialize frame properties
        frame = new JFrame("College Complaint Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        frame.add(mainPanel);
        setupComponents(mainPanel);

        frame.setVisible(true);
    }

    private void setLookAndFeel() {
        try {
            // JTattoo properties configuration
            Properties props = new Properties();
            props.put("logoString", ""); // Removes default logo text
            AcrylLookAndFeel.setCurrentTheme(props);

            // Set JTattoo look-and-feel
            UIManager.setLookAndFeel(new AcrylLookAndFeel());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupComponents(JPanel panel) {
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);

        // Title Label
        JLabel titleLabel = new JLabel("Welcome", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBounds(0, 20, 600, 50);
        titleLabel.setForeground(new Color(50, 50, 50));
        panel.add(titleLabel);

        // Subtitle Label
        JLabel subtitleLabel = new JLabel("College Complaint Management System", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setBounds(0, 80, 600, 30);
        subtitleLabel.setForeground(new Color(100, 100, 100));
        panel.add(subtitleLabel);

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setBounds(220, 200, 140, 40);
        loginButton.setFocusable(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder());
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Close the homepage
                new LoginFrame(); // Open the login frame
            }
        });
        panel.add(loginButton);
    }

    public static void main(String[] args) {
        new HomepageFrame();
    }
}
