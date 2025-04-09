package AppointmentManagement;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;
import Frames.CalendarPanel;
import dao.AppointmentDAO;
import dao.DatabaseConnection;
import dao.PatientDAO;

public class AppointmentManagementFrame extends JFrame {
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JPanel detailPanel;
    private CardLayout cardLayout;
    private JTextField searchField;
    private JButton addButton, refreshButton, searchButton;

    public AppointmentManagementFrame() {
        setTitle("Appointment Management");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create toolbar
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(Color.WHITE);
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        searchButton = new JButton("Search");
        styleButton(searchButton, new Color(52, 152, 219));
        searchButton.addActionListener(e -> searchAppointments());

        refreshButton = new JButton("Refresh");
        styleButton(refreshButton, new Color(46, 204, 113));
        refreshButton.addActionListener(e -> loadAppointments());

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addPanel.setBackground(Color.WHITE);

        addButton = new JButton("Schedule New Appointment");
        styleButton(addButton, new Color(52, 152, 219));
        addButton.addActionListener(e -> openScheduleAppointmentFrame());

        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        addPanel.add(addButton);

        toolbarPanel.add(searchPanel, BorderLayout.WEST);
        toolbarPanel.add(addPanel, BorderLayout.EAST);

        // Main panels
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(680); // Reduce this value from 700 to give more space to details panel
        splitPane.setContinuousLayout(true);

        // Left panel for table
        JPanel tablePanel = createTablePanel();
        splitPane.setLeftComponent(tablePanel);

        // Right panel for details with card layout
        detailPanel = new JPanel();
        cardLayout = new CardLayout();
        detailPanel.setLayout(cardLayout);

        CalendarPanel calendarPanel = new CalendarPanel();
        detailPanel.add(calendarPanel, "CALENDAR");

        JButton calendarButton = new JButton("View Calendar");
        styleButton(calendarButton, new Color(52, 152, 219));
        calendarButton.addActionListener(e -> cardLayout.show(detailPanel, "CALENDAR"));
        addPanel.add(calendarButton); // Add to the toolbar


        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.add(new JLabel("Select an appointment or add a new one"));

        detailPanel.add(emptyPanel, "EMPTY");
        splitPane.setRightComponent(detailPanel);

        setLayout(new BorderLayout());
        add(toolbarPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        loadAppointments();
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    DatabaseConnection.getConnection().close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // Custom table renderer for action buttons (copy from PatientManagementFrame)
    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton editButton;
        private JButton deleteButton;

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(true);

            // Create edit button with more visible styling
            editButton = new JButton("Edit");
            editButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            editButton.setForeground(new Color(0, 0, 0));  // Explicit black color
            editButton.setBackground(new Color(255, 215, 0)); // Yellow
            editButton.setFocusPainted(false);
            editButton.setBorderPainted(true);
            editButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            editButton.setPreferredSize(new Dimension(70, 25));
            editButton.setUI(new BasicButtonUI()); // Override UI to prevent look and feel changes

            // Create delete button with more visible styling
            deleteButton = new JButton("Delete");
            deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteButton.setForeground(new Color(0, 0, 0));  // Explicit black color
            deleteButton.setBackground(new Color(255, 99, 71)); // Red-orange
            deleteButton.setFocusPainted(false);
            deleteButton.setBorderPainted(true);
            deleteButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            deleteButton.setPreferredSize(new Dimension(70, 25));
            deleteButton.setUI(new BasicButtonUI()); // Override UI to prevent look and feel changes

            // Add buttons to panel
            add(editButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            // Force foreground and background colors regardless of look and feel
            editButton.setForeground(new Color(0, 0, 0));
            editButton.setBackground(new Color(255, 215, 0));
            deleteButton.setForeground(new Color(0, 0, 0));
            deleteButton.setBackground(new Color(255, 99, 71));

            return this;
        }
    }
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true); // Change to true
        button.setOpaque(true);

        // Custom UI to prevent look and feel from overriding our style
        button.setUI(new BasicButtonUI() {
            @Override
            public void update(Graphics g, JComponent c) {
                if (c.isOpaque()) {
                    g.setColor(c.getBackground());
                    g.fillRect(0, 0, c.getWidth(), c.getHeight());
                }
                paint(g, c);
            }
        });

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
    }

    public void loadAppointments() {
        // Clear existing data
        tableModel.setRowCount(0);

        try {
            List<Object[]> appointments = AppointmentDAO.getAllAppointments();
            if (appointments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No appointments found in the database.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (Object[] appointment : appointments) {
                tableModel.addRow(new Object[] {
                        appointment[0],  // ID
                        appointment[1],  // Patient name
                        appointment[2],  // Procedure
                        formatDateTime((String)appointment[3]),  // Date & Time formatted
                        "₱" + String.format("%.2f", appointment[4]),  // Cost formatted
                        "₱" + String.format("%.2f", appointment[5]),  // Charge formatted
                        ""  // Placeholder for action buttons
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Helper method to format date-time for better display
    private String formatDateTime(String dateTimeStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            return outputFormat.format(inputFormat.parse(dateTimeStr));
        } catch (Exception e) {
            return dateTimeStr; // Return original if parsing fails
        }
    }

    private void searchAppointments() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAppointments();
            return;
        }

        try {
            tableModel.setRowCount(0);
            List<Object[]> appointments = AppointmentDAO.searchAppointments(keyword);

            for (Object[] appointment : appointments) {
                tableModel.addRow(new Object[] {
                        appointment[0],  // ID
                        appointment[1],  // Patient name
                        appointment[2],  // Procedure
                        appointment[3],  // Date & Time
                        "₱" + appointment[4],  // Cost
                        "₱" + appointment[5],  // Charge
                        "Actions"  // Placeholder for action buttons
                });
            }

            if (appointments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No appointments found matching: " + keyword,
                        "Search Results",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private JPanel createScheduleAppointmentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("Schedule New Appointment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Main form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        // Create form fields
        JComboBox<String> patientComboBox = new JComboBox<>();
        JTextField procedureField = createFormField(formPanel, "Procedure*:");

        // Date picker panel
        JLabel dateLabel = new JLabel("Date and Time*:");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel dateTimePanel = new JPanel();
        dateTimePanel.setLayout(new BoxLayout(dateTimePanel, BoxLayout.X_AXIS));
        dateTimePanel.setBackground(Color.WHITE);
        dateTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateTimePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateChooser.setPreferredSize(new Dimension(150, 30));

        SpinnerModel hourModel = new SpinnerNumberModel(12, 0, 23, 1);
        JSpinner hourSpinner = new JSpinner(hourModel);
        hourSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JSpinner.NumberEditor hourEditor = new JSpinner.NumberEditor(hourSpinner, "00");
        hourSpinner.setEditor(hourEditor);
        hourSpinner.setPreferredSize(new Dimension(60, 30));

        SpinnerModel minuteModel = new SpinnerNumberModel(0, 0, 59, 1);
        JSpinner minuteSpinner = new JSpinner(minuteModel);
        minuteSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JSpinner.NumberEditor minuteEditor = new JSpinner.NumberEditor(minuteSpinner, "00");
        minuteSpinner.setEditor(minuteEditor);
        minuteSpinner.setPreferredSize(new Dimension(60, 30));

        dateTimePanel.add(dateChooser);
        dateTimePanel.add(Box.createHorizontalStrut(5));
        dateTimePanel.add(new JLabel("Hour:"));
        dateTimePanel.add(hourSpinner);
        dateTimePanel.add(Box.createHorizontalStrut(5));
        dateTimePanel.add(new JLabel("Min:"));
        dateTimePanel.add(minuteSpinner);

        formPanel.add(dateLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(dateTimePanel);
        formPanel.add(Box.createVerticalStrut(15));

        JTextField costField = createFormField(formPanel, "Cost* (₱):");
        JTextField chargeField = createFormField(formPanel, "Charge* (₱):");

        // Description area
        JLabel descLabel = new JLabel("Description*:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea();
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setRows(8);

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setPreferredSize(new Dimension(400, 150));
        descScroll.setMinimumSize(new Dimension(400, 150));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(150, 150, 150));
        cancelButton.addActionListener(e -> cardLayout.show(detailPanel, "EMPTY"));

        JButton scheduleButton = new JButton("Schedule Appointment");
        styleButton(scheduleButton, new Color(41, 128, 185));

        // Load patients for dropdown

            patientComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            patientComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            patientComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel patientLabel = new JLabel("Patient*:");
            patientLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            patientLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            formPanel.add(patientLabel);
            formPanel.add(Box.createVerticalStrut(5));
            formPanel.add(patientComboBox);
            formPanel.add(Box.createVerticalStrut(15));

            List<Object[]> patients = PatientDAO.getAllPatients();
            for (Object[] patient : patients) {
                int id = (int) patient[0];
                String firstName = (String) patient[1];
                String middleName = (String) patient[2];
                String lastName = (String) patient[3];

                String displayName = String.format("[%d] %s %s %s",
                        id,
                        firstName,
                        (middleName != null && !middleName.isEmpty() ? middleName + " " : ""),
                        lastName);

                patientComboBox.addItem(displayName);

        }

        // Schedule button action listener
        scheduleButton.addActionListener(e -> {
            try {
                // Validation
                if (patientComboBox.getSelectedItem() == null ||
                        procedureField.getText().trim().isEmpty() ||
                        dateChooser.getDate() == null ||
                        costField.getText().trim().isEmpty() ||
                        chargeField.getText().trim().isEmpty() ||
                        descArea.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this,
                            "All fields are required!",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Extract patient ID from selected item
                String selectedPatient = patientComboBox.getSelectedItem().toString();
                int patientId = Integer.parseInt(selectedPatient.substring(
                        selectedPatient.indexOf("[") + 1,
                        selectedPatient.indexOf("]")
                ));

                // Construct date-time string in the format YYYY-MM-DD HH:MM:SS
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateTimeStr = String.format("%s %02d:%02d:00",
                        sdf.format(dateChooser.getDate()),
                        (Integer)hourSpinner.getValue(),
                        (Integer)minuteSpinner.getValue());

                // Schedule appointment
                boolean success = AppointmentDAO.scheduleAppointment(
                        patientId,
                        procedureField.getText().trim(),
                        dateTimeStr,
                        costField.getText().trim(),
                        chargeField.getText().trim(),
                        descArea.getText().trim()
                );

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Appointment scheduled successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadAppointments();
                    cardLayout.show(detailPanel, "EMPTY");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to schedule appointment!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Database Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid number format in input fields!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(scheduleButton);

        // Add description components to form
        formPanel.add(descLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(descScroll);
        formPanel.add(Box.createVerticalStrut(15));

        // Create scrollable view of the form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Assemble the panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table setup with columns
        String[] columns = {"ID", "Patient", "Procedure", "Date & Time", "Cost", "Charge", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column is editable
            }
        };

        appointmentTable = new JTable(tableModel);
        appointmentTable.setRowHeight(45);
        appointmentTable.setShowVerticalLines(true);
        appointmentTable.setShowHorizontalLines(true);
        appointmentTable.setGridColor(new Color(230, 230, 230));
        appointmentTable.setForeground(Color.BLACK);
        appointmentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        appointmentTable.getTableHeader().setBackground(new Color(240, 240, 245));
        appointmentTable.getTableHeader().setForeground(new Color(70, 70, 70));
        appointmentTable.setSelectionBackground(new Color(220, 238, 255));
        appointmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Set column widths
       appointmentTable.getColumnModel().getColumn(0).setMinWidth(0);
        appointmentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        appointmentTable.getColumnModel().getColumn(0).setPreferredWidth(0); // ID
        appointmentTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Patient
        appointmentTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Procedure
        appointmentTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Date & Time
        appointmentTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Cost
        appointmentTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Charge
        appointmentTable.getColumnModel().getColumn(6).setPreferredWidth(180); // Actions - wider column

        // Fix minimum width for actions column
        appointmentTable.getColumnModel().getColumn(6).setMinWidth(180);

        // Set actions column renderer
        appointmentTable.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());

        // Set default renderer for other columns to ensure black text
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
        defaultRenderer.setForeground(Color.BLACK);

        for (int i = 0; i < 6; i++) {
            appointmentTable.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
        }

        // Update mouse listener for action buttons with more precise detection
        appointmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = appointmentTable.columnAtPoint(e.getPoint());
                int row = appointmentTable.rowAtPoint(e.getPoint());

                if (row >= 0 && row < appointmentTable.getRowCount() && column == 6) {
                    Rectangle cellRect = appointmentTable.getCellRect(row, column, true);
                    int appointmentId = Integer.parseInt(appointmentTable.getValueAt(row, 0).toString());

                    // Calculate horizontal position within cell relative to center
                    int xOffset = e.getX() - cellRect.x;
                    int cellCenter = cellRect.width / 2;

                    // First button (Edit) is on the left side
                    if (xOffset < cellCenter) {
                        editAppointment(appointmentId);
                    }
                    // Second button (Delete) is on the right side
                    else {
                        deleteAppointment(appointmentId);
                    }
                }
            }
        });

        // Add to scroll pane and panel
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    private void openScheduleAppointmentFrame() {
        // Check if the "SCHEDULE_NEW" panel already exists
        if (detailPanel.getComponentCount() > 0) {
            for (Component component : detailPanel.getComponents()) {
                if ("SCHEDULE_NEW".equals(component.getName())) {
                    cardLayout.show(detailPanel, "SCHEDULE_NEW");
                    return;
                }
            }
        }

        // Create and add the "SCHEDULE_NEW" panel if it doesn't exist
        JPanel schedulePanel = createScheduleAppointmentPanel();
        schedulePanel.setName("SCHEDULE_NEW");
        detailPanel.add(schedulePanel, "SCHEDULE_NEW");
        cardLayout.show(detailPanel, "SCHEDULE_NEW");
    }

    public void editAppointment(int appointmentId) {
        JPanel editPanel = createEditAppointmentPanel(appointmentId);
        if (editPanel != null) {
            detailPanel.add(editPanel, "EDIT_" + appointmentId);
            cardLayout.show(detailPanel, "EDIT_" + appointmentId);
        }
    }

    public void deleteAppointment(int appointmentId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this appointment?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = AppointmentDAO.deleteAppointment(appointmentId);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Appointment deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadAppointments();
                    cardLayout.show(detailPanel, "EMPTY");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete appointment!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Database Error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private JPanel createEditAppointmentPanel(int appointmentId) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(450, 600)); // Increased width and added height
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("Edit Appointment #" + appointmentId);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Main scrollable panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        // Create form fields
        JComboBox<String> patientComboBox = new JComboBox<>();
        JTextField procedureField = createFormField(formPanel, "Procedure*:");
        JTextField dateField = createFormField(formPanel, "Date* (YYYY-MM-DD HH:MM:SS):");
        JTextField costField = createFormField(formPanel, "Cost* (₱):");
        JTextField chargeField = createFormField(formPanel, "Charge* (₱):");

        // Description area with improved sizing
        JLabel descLabel = new JLabel("Description*:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea();
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setRows(8); // Set more visible rows

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setPreferredSize(new Dimension(400, 150)); // Fixed size
        descScroll.setMinimumSize(new Dimension(400, 150));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(150, 150, 150));
        cancelButton.addActionListener(e -> cardLayout.show(detailPanel, "EMPTY"));

        JButton updateButton = new JButton("Update Appointment");
        styleButton(updateButton, new Color(41, 128, 185));

        // Load appointment data and patients for the dropdown
        try {
            // First load all patients for the dropdown
            patientComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            patientComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            patientComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel patientLabel = new JLabel("Patient*:");
            patientLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            patientLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            formPanel.add(patientLabel);
            formPanel.add(Box.createVerticalStrut(5));
            formPanel.add(patientComboBox);
            formPanel.add(Box.createVerticalStrut(15));

            List<Object[]> patients = PatientDAO.getAllPatients();
            Map<Integer, Integer> patientIdPositionMap = new java.util.HashMap<>();
            int selectedPatientPosition = 0;
            int counter = 0;

            for (Object[] patient : patients) {
                int id = (int) patient[0];
                String firstName = (String) patient[1];
                String middleName = (String) patient[2];
                String lastName = (String) patient[3];

                String displayName = String.format("[%d] %s %s %s",
                        id,
                        firstName,
                        (middleName != null && !middleName.isEmpty() ? middleName + " " : ""),
                        lastName);

                patientComboBox.addItem(displayName);
                patientIdPositionMap.put(id, counter++);
            }

            // Now load the appointment data
            Object[] appointment = AppointmentDAO.getAppointmentById(appointmentId);
            if (appointment != null) {
                // Set patient selection
                int patientId = (int) appointment[1];
                if (patientIdPositionMap.containsKey(patientId)) {
                    selectedPatientPosition = patientIdPositionMap.get(patientId);
                    patientComboBox.setSelectedIndex(selectedPatientPosition);
                }

                procedureField.setText((String) appointment[2]);
                dateField.setText((String) appointment[3]);
                costField.setText(appointment[4].toString());
                chargeField.setText(appointment[5].toString());
                descArea.setText((String) appointment[6]);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to load appointment data!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                cardLayout.show(detailPanel, "EMPTY");
                return null;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            cardLayout.show(detailPanel, "EMPTY");
            return null;
        }

        // Update button action (unchanged - your existing code)
        updateButton.addActionListener(e -> {
            try {
                // Validation
                if (patientComboBox.getSelectedItem() == null ||
                        procedureField.getText().trim().isEmpty() ||
                        dateField.getText().trim().isEmpty() ||
                        costField.getText().trim().isEmpty() ||
                        chargeField.getText().trim().isEmpty() ||
                        descArea.getText().trim().isEmpty()) {

                    JOptionPane.showMessageDialog(this,
                            "All fields are required!",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Extract patient ID from selected item
                String selectedPatient = patientComboBox.getSelectedItem().toString();
                int patientId = Integer.parseInt(selectedPatient.substring(
                        selectedPatient.indexOf("[") + 1,
                        selectedPatient.indexOf("]")
                ));

                // Update appointment
                boolean success = AppointmentDAO.updateAppointment(
                        appointmentId,
                        String.valueOf(patientId),
                        procedureField.getText().trim(),
                        dateField.getText().trim(),
                        costField.getText().trim(),
                        chargeField.getText().trim(),
                        descArea.getText().trim()
                );

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Appointment updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadAppointments();
                    cardLayout.show(detailPanel, "EMPTY");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update appointment!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Database Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid number format in input fields!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(updateButton);

        // Add description components to form
        formPanel.add(descLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(descScroll);
        formPanel.add(Box.createVerticalStrut(15));

        // Create scrollable view of the form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Assemble the panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JTextField createFormField(JPanel container, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(label);
        container.add(Box.createVerticalStrut(5));
        container.add(field);
        container.add(Box.createVerticalStrut(10));

        return field;
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppointmentManagementFrame());
    }
}