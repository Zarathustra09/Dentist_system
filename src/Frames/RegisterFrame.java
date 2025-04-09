package Frames;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import dao.UserDAO;

public class RegisterFrame extends JFrame {
    private JTextField usernameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton registerButton, backButton;

    public RegisterFrame() {
        setTitle("Clinic Management System - Register");
        setSize(500, 800);  // Increased height for more fields
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel(new BorderLayout(0, 25));  // Increased spacing
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));  // Increased padding
        mainPanel.setBackground(new Color(240, 240, 245));

        // Header panel
        JPanel headerPanel = createHeaderPanel();

        // Form panel
        JPanel formPanel = createFormPanel();

        // Button panel
        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(240, 240, 245));

        JLabel titleLabel = new JLabel("Clinic Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));  // Larger font
        titleLabel.setForeground(new Color(41, 128, 185));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Create New Account");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));  // Larger font
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(Box.createVerticalStrut(15));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(25));  // More space after title

        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)  // Increased padding
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 1.0;  // Make components fill horizontal space

        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));  // Larger font
        usernameField = new JTextField(20);  // Increased width
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));  // Larger font
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)  // Increased padding
        ));
        usernameField.setPreferredSize(new Dimension(300, 40));  // Set preferred size

        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));  // Larger font
        passwordField = new JPasswordField(20);  // Increased width
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));  // Larger font
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)  // Increased padding
        ));
        passwordField.setPreferredSize(new Dimension(300, 40));  // Set preferred size

        // Confirm Password field
        JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        confirmPasswordLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));  // Larger font
        confirmPasswordField = new JPasswordField(20);  // Increased width
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));  // Larger font
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)  // Increased padding
        ));
        confirmPasswordField.setPreferredSize(new Dimension(300, 40));  // Set preferred size

        // Email field
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));  // Larger font
        emailField = new JTextField(20);  // Increased width
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 16));  // Larger font
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)  // Increased padding
        ));
        emailField.setPreferredSize(new Dimension(300, 40));  // Set preferred size

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(16, 5, 8, 5);  // More space between fields
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(8, 5, 8, 5);
        formPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(16, 5, 8, 5);  // More space between fields
        formPanel.add(confirmPasswordLabel, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(8, 5, 8, 5);
        formPanel.add(confirmPasswordField, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(16, 5, 8, 5);  // More space between fields
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(8, 5, 8, 5);
        formPanel.add(emailField, gbc);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 0, 15));  // Increased spacing
        buttonPanel.setBackground(new Color(240, 240, 245));

        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));  // Larger font
        registerButton.setForeground(Color.BLACK);
        registerButton.setBackground(new Color(41, 128, 185));
        registerButton.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));  // Taller button
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(e -> register());

        backButton = new JButton("Already have an account? Login");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));  // Larger font
        backButton.setForeground(new Color(41, 128, 185));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> openLoginFrame());

        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        return buttonPanel;
    }

    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match!");
            return;
        }

        try {
            boolean success = UserDAO.register(username, password, email);
            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Registration successful!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                openLoginFrame();
            } else {
                showError("Registration failed! Username or email might already be in use.");
            }
        } catch (SQLException e) {
            showError("Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Registration Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void openLoginFrame() {
        new LoginFrame();
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterFrame::new);
    }
}