package Frames;

import javax.swing.*;
import java.awt.*;
import dao.UserDAO;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    public LoginFrame() {
        setTitle("Clinic Management System - Login");
        setSize(500, 700);  // Adjusted size for better proportion
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

        // Check if any account exists and hide the register button if true
        if (UserDAO.hasAnyAccount()) {
            registerButton.setVisible(false);
        }

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

        JLabel subtitleLabel = new JLabel("User Login");
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
                BorderFactory.createEmptyBorder(35, 35, 35, 35)  // Increased padding
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);  // Increased vertical spacing
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 5, 10, 5);  // More space between fields
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 5, 10, 5);
        formPanel.add(passwordField, gbc);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 0, 15));  // Increased spacing
        buttonPanel.setBackground(new Color(240, 240, 245));

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));  // Larger font
        loginButton.setForeground(Color.BLACK);
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));  // Taller button
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> login());

        registerButton = new JButton("Need an account? Register");
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));  // Larger font
        registerButton.setForeground(new Color(41, 128, 185));
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> openRegisterFrame());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        return buttonPanel;
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and Password are required!");
            return;
        }

        boolean success = UserDAO.login(username, password);
        if (success) {
            new MainFrame();
            dispose();
        } else {
            showError("Invalid username or password!");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Login Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void openRegisterFrame() {
        new RegisterFrame();
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}