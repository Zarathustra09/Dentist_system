// src/Frames/MainFrame.java
package Frames;
import java.awt.GradientPaint;
import javax.swing.*;
import java.awt.*;
import AppointmentManagement.AppointmentManagementFrame;
import PatientManagement.PatientManagementFrame;
import dao.UserDAO;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import static dao.UserDAO.deleteUser;

public class MainFrame extends JFrame {
    private JButton patientManagementButton;
    private JButton appointmentManagementButton;

    public MainFrame() {
        setTitle("Clinic Management System");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel with a modern look
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 245));

        // Create header
        JPanel headerPanel = createHeaderPanel();

        // Create dashboard panel with management buttons
        JPanel dashboardPanel = createDashboardPanel();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(dashboardPanel, BorderLayout.CENTER);

        // Add the main panel to the frame
        add(mainPanel);

        // Make the frame visible
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(1000, 70));
        headerPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Clinic Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        // In createHeaderPanel() method, replace the current delete button code with:
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(new Color(41, 128, 185));

        JButton deleteUserButton = new JButton("Manage Users");
        deleteUserButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteUserButton.setForeground(Color.black);
        deleteUserButton.setBackground(new Color(52, 73, 94));
        deleteUserButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        deleteUserButton.setFocusPainted(false);
        deleteUserButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteUserButton.addActionListener(e -> deleteUser());

        rightPanel.add(deleteUserButton);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }
    private JButton createDashboardButton(String title, String description, String imagePath) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(41, 128, 185),
                        0, getHeight(), new Color(52, 73, 94)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Load and draw the image with better positioning and scaling
                try {
                    ImageIcon icon = new ImageIcon(getClass().getResource("/" + imagePath));
                    if (icon.getIconWidth() > 0) {
                        // Calculate dimensions for a centered, properly sized image
                        int imgWidth = Math.min(getWidth() - 40, 120);
                        int imgHeight = imgWidth;

                        // Scale image while maintaining aspect ratio
                        Image img = icon.getImage();
                        float aspect = (float)icon.getIconWidth() / icon.getIconHeight();
                        if (aspect > 1) {
                            imgHeight = (int)(imgWidth / aspect);
                        } else {
                            imgWidth = (int)(imgHeight * aspect);
                        }

                        // Center the image horizontally and position it in the upper portion
                        int x = (getWidth() - imgWidth) / 2;
                        int y = 30; // Fixed position from top

                        // Create a soft glow effect behind the image
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                        g2d.setColor(Color.WHITE);
                        g2d.fillOval(x-5, y-5, imgWidth+10, imgHeight+10);
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                        // Draw the image
                        g2d.drawImage(img, x, y, imgWidth, imgHeight, this);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image: " + imagePath);
                }

                g2d.dispose();
            }
        };

        button.setOpaque(false);
        button.setBorderPainted(true);
        button.setLayout(new BorderLayout());
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(400, 220));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        titleLabel.setForeground(Color.WHITE);

        JLabel descLabel = new JLabel("<html><p style='width:300px'>" + description + "</p></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        descLabel.setForeground(Color.WHITE);

        // Create an empty panel to push text to the bottom
        JPanel spacerPanel = new JPanel();
        spacerPanel.setOpaque(false);
        spacerPanel.setPreferredSize(new Dimension(1, 100)); // Adjust height as needed

        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(descLabel, BorderLayout.CENTER);

        button.add(spacerPanel, BorderLayout.CENTER);
        button.add(textPanel, BorderLayout.SOUTH);

        // Add hover and click effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255), 2, true));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }
    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new GridBagLayout());
        dashboardPanel.setBackground(new Color(240, 240, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(30, 30, 30, 30);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Patient Management Button with background
        patientManagementButton = createDashboardButton(
                "Patient Management",
                "Manage patient records, add, edit, and search for patients",
                "Assets/patient_icon.png"
        );
        patientManagementButton.setToolTipText("Manage patient records");
        patientManagementButton.addActionListener(e -> openPatientManagement());

        // Appointment Management Button with background
        appointmentManagementButton = createDashboardButton(
                "Appointment Management",
                "Schedule, view, edit, and cancel appointments",
                "Assets/appointment_icon.png.jpg"
        );
        appointmentManagementButton.setToolTipText("Manage appointments");
        appointmentManagementButton.addActionListener(e -> openAppointmentManagement());

        // Add buttons to dashboard
        gbc.gridx = 0;
        gbc.gridy = 0;
        dashboardPanel.add(patientManagementButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        dashboardPanel.add(appointmentManagementButton, gbc);

        return dashboardPanel;
    }

    private void openPatientManagement() {
        SwingUtilities.invokeLater(() -> new PatientManagementFrame());
    }

    private void openAppointmentManagement() {
        SwingUtilities.invokeLater(() -> new AppointmentManagementFrame());
    }
    private void deleteUser() {
        // Get all usernames from database
        String[] usernames = UserDAO.getAllUsernames();

        if (usernames.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "No users found in the system.",
                    "Delete User",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Create dropdown component for user selection
        JComboBox<String> usernameDropdown = new JComboBox<>(usernames);

        // Create custom panel for the dialog
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.add(new JLabel("Select user to delete:"));
        panel.add(usernameDropdown);

        // Show the dialog
        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Delete User",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        // Process result
        if (result == JOptionPane.OK_OPTION) {
            String selectedUsername = (String) usernameDropdown.getSelectedItem();

            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete user: " + selectedUsername + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = UserDAO.deleteUser(selectedUsername);
                if (success) {
                    JOptionPane.showMessageDialog(
                            this,
                            "User '" + selectedUsername + "' deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to delete user. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
    public static void main(String[] args) {
        try {
            // Set look and feel first
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Then create the frame which will style the buttons
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}