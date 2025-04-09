package PatientManagement;
import java.awt.Desktop;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import dao.PatientDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.regex.Pattern;
import javax.swing.table.DefaultTableCellRenderer;
import static dao.PatientDAO.updatePatient;
public class PatientManagementFrame extends JFrame {
    private JPanel uploadedFilesPanel;
    private JPanel mainPanel;
    private JList<String> uploadedFilesList;
    private DefaultListModel<String> listModel;
    private List<String> uploadedFilePaths = new ArrayList<>();
    private JTextField firstNameField, middleNameField, lastNameField, emailField, phoneField;
    private JTable filesTable;
    private DefaultTableModel filesTableModel;
    private JTextArea addressArea;
    private JDateChooser birthdayChooser;
    private JTextField filePathField;
    private JButton uploadButton;
    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton refreshButton, addButton, searchButton;
    private JPanel detailPanel;
    private CardLayout cardLayout;

    public PatientManagementFrame() {
        setTitle("Patient Management");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize fields
        firstNameField = new JTextField();
        middleNameField = new JTextField();
        lastNameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        addressArea = new JTextArea();
        birthdayChooser = new JDateChooser();
        filePathField = new JTextField();

        // Main panel with border layout (now a class field)
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 245));

        // Create components
        JPanel headerPanel = createHeaderPanel();
        JPanel tablePanel = createTablePanel();
        detailPanel = createDetailPanel();

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(detailPanel, BorderLayout.EAST);

        // Initialize files list
        initializeUploadedFilesList();

        add(mainPanel);
        loadPatients();
        setVisible(true);


    }
    private void processSelectedFiles(File[] selectedFiles) {
        try {
            System.out.println("Processing selected files...");

            // Check for duplicate filenames before copying
            List<String> existingFileNames = new ArrayList<>();
            for (String path : uploadedFilePaths) {
                existingFileNames.add(new File(path).getName());
            }

            int filesCopied = 0;
            int filesSkipped = 0;
            int filesRejected = 0;

            for (File file : selectedFiles) {
                System.out.println("Processing file: " + file.getAbsolutePath());

                // Skip empty files
                if (file.length() == 0) {
                    System.err.println("File rejected (empty): " + file.getName());
                    filesRejected++;
                    continue;
                }

                // Check file size (5MB limit)
                if (file.length() > 5 * 1024 * 1024) {
                    System.err.println("File rejected (too large): " + file.getName());
                    filesRejected++;
                    continue;
                }

                // Generate unique filename to prevent overwriting
                String originalFileName = file.getName();
                String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;

                // Skip files with same name
                if (existingFileNames.contains(originalFileName)) {
                    System.out.println("File skipped (duplicate): " + originalFileName);
                    filesSkipped++;
                    continue;
                }

                // Ensure destination directory exists
                File destinationDir = new File("src/PatientFiles/");
                if (!destinationDir.exists()) {
                    boolean dirCreated = destinationDir.mkdirs();
                    if (!dirCreated) {
                        System.err.println("Failed to create destination directory: " + destinationDir.getAbsolutePath());
                        continue;
                    }
                }

                File destinationFile = new File(destinationDir, uniqueFileName);

                // Copy file
                try {
                    Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    uploadedFilePaths.add(destinationFile.getAbsolutePath());
                    existingFileNames.add(originalFileName);
                    filesCopied++;
                    System.out.println("File copied successfully: " + destinationFile.getAbsolutePath());
                } catch (IOException ioException) {
                    System.err.println("Error copying file: " + file.getName() + " - " + ioException.getMessage());
                }
            }

            // Update UI
            updateFilesTable();
            updateUploadedFilesList();
            setupFileViewingForTable();

            // Show results in a toast message
            StringBuilder message = new StringBuilder();
            if (filesCopied > 0) {
                message.append(filesCopied).append(" file(s) uploaded successfully. ");
            }
            if (filesSkipped > 0) {
                message.append(filesSkipped).append(" duplicate file(s) skipped. ");
            }
            if (filesRejected > 0) {
                message.append(filesRejected).append(" file(s) rejected (too large/empty). ");
            }

            if (message.length() > 0) {
                showToast(message.toString().trim(), new Color(41, 128, 185));
            }

        } catch (Exception e) {
            System.err.println("Error processing files: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error processing files: " + e.getMessage(),
                    "Upload Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private void uploadFiles() {
        try {
            System.out.println("Starting file upload process...");

            // Avoid using JFileChooser's automatic directory detection
            // Specify a simple, regular directory path instead
            JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.home")));
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
            // Disable Windows shell folder functionality
            System.setProperty("sun.awt.noerasebackground", "true");
            UIManager.put("FileChooser.useSystemExtensionHiding", Boolean.FALSE);

            fileChooser.setDialogTitle("Select Patient Files");
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            // Use a simpler file filter implementation
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) return true;
                    String name = f.getName().toLowerCase();
                    return name.endsWith(".pdf") ||
                            name.endsWith(".jpg") ||
                            name.endsWith(".jpeg") ||
                            name.endsWith(".png") ||
                            name.endsWith(".doc") ||
                            name.endsWith(".docx");
                }

                @Override
                public String getDescription() {
                    return "Patient Documents (*.pdf, *.jpg, *.jpeg, *.png, *.doc, *.docx)";
                }
            });

            // Disable features that may trigger sorting issues
            fileChooser.setDragEnabled(false);

            int result;
            try {
                result = fileChooser.showOpenDialog(this);
            } catch (Exception e) {
                System.err.println("File chooser error: " + e.getMessage());

                // Fall back to an even more basic file chooser
                fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(true);
                result = fileChooser.showOpenDialog(this);
            }

            if (result == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                System.out.println("Files selected: " + selectedFiles.length);

                // Process files only if files were selected
                if (selectedFiles != null && selectedFiles.length > 0) {
                    processSelectedFiles(selectedFiles);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in file upload: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error during file selection: " + e.getMessage(),
                    "Upload Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // Reset properties that were changed
            System.clearProperty("sun.awt.noerasebackground");
        }
    }
    private JTable createEnhancedPatientTable() {
        JTable table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(224, 242, 254));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);

        // Enable sorting
        table.setAutoCreateRowSorter(true);

        // Add hover effect
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                table.setRowSelectionInterval(row, row);
            }
        });

        return table;
    }
    private void showToast(String message, Color backgroundColor) {
        JDialog toast = new JDialog();
        toast.setUndecorated(true);
        toast.setLayout(new BorderLayout());
        toast.setSize(300, 50);
        toast.setLocationRelativeTo(null);

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(backgroundColor);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        toast.add(label, BorderLayout.CENTER);
        toast.setVisible(true);

        // Auto-close after 3 seconds
        new Timer(3000, e -> toast.dispose()).start();
    }
    private void addPatient() {
        String firstName = firstNameField.getText();
        String middleName = middleNameField.getText();
        String lastName = lastNameField.getText();
        String birthday = new SimpleDateFormat("yyyy-MM-dd").format(birthdayChooser.getDate());
        String email = emailField.getText();
        String phone = phoneField.getText();
        String address = addressArea.getText();
        List<String> filePaths = uploadedFilePaths;

        if (PatientDAO.addPatient(firstName, middleName, lastName, birthday, email, phone, address, filePaths)) {
            JOptionPane.showMessageDialog(this, "Patient added successfully!");
            loadPatients();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add patient.");
        }
    }
    private void initializeUploadedFilesList() {
        try {
            // Create models and components
            listModel = new DefaultListModel<>();
            uploadedFilesList = new JList<>(listModel);
            uploadedFilesList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            uploadedFilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            uploadedFilesList.setFixedCellHeight(25);

            // Create scroll pane with safe dimensions
            JScrollPane listScrollPane = new JScrollPane(uploadedFilesList);
            listScrollPane.setPreferredSize(new Dimension(300, 120));

            // Create panel with a simple layout
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            panel.add(listScrollPane, BorderLayout.CENTER);

            // Add a remove button
            JButton removeButton = new JButton("Remove Selected");
            removeButton.addActionListener(e -> {
                int selectedRow = filesTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Convert row index if sorting is enabled
                    if (filesTable.getRowSorter() != null) {
                        selectedRow = filesTable.getRowSorter().convertRowIndexToModel(selectedRow);
                    }

                    // Get the file path
                    String filePath = uploadedFilePaths.get(selectedRow);
                    File file = new File(filePath);

                    // Attempt to delete the file
                    if (file.exists() && file.delete()) {
                        // Remove the file from the list and update the UI
                        uploadedFilePaths.remove(selectedRow);
                        updateFilesTable();
                        updateUploadedFilesList();

                        // Notify the IDE of file system changes
                        try {
                            java.nio.file.Path path = file.toPath().getParent();
                            java.nio.file.Files.list(path).close(); // Trigger a directory read
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }

                        JOptionPane.showMessageDialog(this, "File removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete the file. It might be open or locked.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No file selected to remove.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(removeButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            uploadedFilesPanel = panel;
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to a simple panel if initialization fails
            uploadedFilesPanel = new JPanel(new BorderLayout());
            uploadedFilesPanel.add(new JLabel("Error initializing files panel"), BorderLayout.CENTER);
        }
        uploadedFilesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    int index = uploadedFilesList.getSelectedIndex();
                    if (index != -1 && index < uploadedFilePaths.size()) {
                        String filePath = uploadedFilePaths.get(index);
                        try {
                            File file = new File(filePath);
                            if (file.exists()) {
                                Desktop.getDesktop().open(file);
                            } else {
                                JOptionPane.showMessageDialog(PatientManagementFrame.this,
                                        "File does not exist: " + file.getAbsolutePath(),
                                        "File Not Found",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(PatientManagementFrame.this,
                                    "Error opening file: " + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
    }
    private void removeSelectedFile() {
        int selectedIndex = uploadedFilesList.getSelectedIndex();
        if (selectedIndex != -1) {
            uploadedFilePaths.remove(selectedIndex);
            updateUploadedFilesList();
        } else {
            JOptionPane.showMessageDialog(this, "No file selected to remove.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateUploadedFilesList() {
        try {
            // Initialize if needed
            if (listModel == null || uploadedFilesPanel == null) {
                initializeUploadedFilesList();
            }

            // Clear existing items
            listModel.clear();

            // Add files with safety checks
            if (uploadedFilePaths == null || uploadedFilePaths.isEmpty()) {
                listModel.addElement("No files uploaded");
                return;
            }

            for (String path : uploadedFilePaths) {
                if (path != null) {
                    File file = new File(path);
                    listModel.addElement(file.getName());
                }
            }

            // Refresh UI
            if (uploadedFilesPanel != null) {
                uploadedFilesPanel.revalidate();
                uploadedFilesPanel.repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Silent failure with logging
            System.err.println("Error updating files list: " + e.getMessage());
        }
    }
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Left side - Title
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Patient Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("View, add, update, or delete patient records");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(220, 220, 220));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        // Right side - Search and Add
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setOpaque(false);

        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(150, 30));

        searchButton = new JButton("Search");
        styleButton(searchButton, new Color(52, 152, 219));
        searchButton.addActionListener(e -> searchPatients());

        addButton = new JButton("Add New Patient");
        styleButton(addButton, new Color(46, 204, 113));
        addButton.addActionListener(e -> showAddPatient());

        refreshButton = new JButton("Refresh");
        refreshButton.setToolTipText("Refresh patient list");
        styleButton(refreshButton, new Color(52, 152, 219));
        refreshButton.addActionListener(e -> loadPatients());

        controlPanel.add(searchField);
        controlPanel.add(searchButton);
        controlPanel.add(refreshButton);
        controlPanel.add(addButton);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(controlPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
    panel.setBackground(new Color(240, 240, 245));

    // Create column names
    String[] columnNames = {"ID", "First Name", "Middle Name", "Last Name", "Birthday",
            "Email", "Phone", "Address", "Actions"};

    // Create table model
    tableModel = new DefaultTableModel() {
        public boolean isCellEditable(int row, int column) {
            return column == 8; // Only actions column is editable
        }
    };
    tableModel.setColumnIdentifiers(columnNames);

    // Create table
    patientTable = new JTable(tableModel);
    patientTable.setRowHeight(35);
    patientTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    patientTable.setSelectionBackground(new Color(224, 242, 254));
    patientTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
    patientTable.getTableHeader().setOpaque(false);
    patientTable.getTableHeader().setBackground(new Color(52, 73, 94));
    patientTable.getTableHeader().setForeground(Color.BLACK);
    patientTable.getColumnModel().getColumn(8).setPreferredWidth(160);
    patientTable.getColumnModel().getColumn(8).setMinWidth(160);

    // Set column header for the Actions column
    patientTable.getColumnModel().getColumn(8).setHeaderValue("Actions (View/Edit/Delete)");

    // Set action column renderer
    patientTable.getColumnModel().getColumn(8).setCellRenderer(new ActionButtonRenderer());

    // Add mouse listener for action buttons
    patientTable.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            int column = patientTable.getColumnModel().getColumnIndexAtX(e.getX());
            int row = e.getY() / patientTable.getRowHeight();

            // Check if click was on a valid row in the Actions column
            if (row >= 0 && row < patientTable.getRowCount() && column == 8) {
                int patientId = Integer.parseInt(patientTable.getValueAt(row, 0).toString());
                Rectangle cellRect = patientTable.getCellRect(row, column, false);
                int buttonWidth = cellRect.width / 3;

                int relativeX = e.getX() - cellRect.x;

                if (relativeX < buttonWidth) {
                    // View button
                    showViewPatient(patientId);
                } else if (relativeX < buttonWidth * 2) {
                    // Edit button
                    showEditPatient(patientId);
                } else {
                    // Delete button
                    confirmDeletePatient(patientId);
                }
            }
        }
    });

    // Add tooltips to the table
    patientTable.addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            int row = patientTable.rowAtPoint(p);
            int col = patientTable.columnAtPoint(p);

            // Set tooltip for the Actions column
            if (col == 8 && row >= 0 && row < patientTable.getRowCount()) {
                patientTable.setToolTipText("Click to View, Edit, or Delete");
            } else {
                patientTable.setToolTipText(null);
            }
        }
    });

    JScrollPane scrollPane = new JScrollPane(patientTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
}

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel();
        cardLayout = new CardLayout();
        panel.setLayout(cardLayout);

        // Create an empty panel as the default view
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension(350, 0));
        emptyPanel.setBackground(new Color(240, 240, 245));

        // Add panels to card layout
        panel.add(emptyPanel, "EMPTY");

        return panel;
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
    private void loadPatients() {
        try {
            // Clear existing data
            tableModel.setRowCount(0);

            // Get all patients
            List<Object[]> patients = PatientDAO.getAllPatients();

            // Add patients to table model
            for (Object[] patient : patients) {
                Object[] rowData = new Object[9]; // 8 data columns + 1 action column
                System.arraycopy(patient, 0, rowData, 0, patient.length);
                rowData[8] = ""; // Placeholder for action buttons
                tableModel.addRow(rowData);
            }

            // Set reasonable column widths to prevent dimension issues
            if (patientTable.getColumnModel().getColumnCount() > 0) {
                // Hide the first column (ID column)
                patientTable.getColumnModel().getColumn(0).setMinWidth(0);
                patientTable.getColumnModel().getColumn(0).setMaxWidth(0);
                patientTable.getColumnModel().getColumn(0).setPreferredWidth(0);
                // ID
                patientTable.getColumnModel().getColumn(1).setMaxWidth(150); // First Name
                patientTable.getColumnModel().getColumn(2).setMaxWidth(150); // Middle Name
                patientTable.getColumnModel().getColumn(3).setMaxWidth(150); // Last Name
                patientTable.getColumnModel().getColumn(4).setMaxWidth(100); // Birthday
                patientTable.getColumnModel().getColumn(5).setMaxWidth(200); // Email
                patientTable.getColumnModel().getColumn(6).setMaxWidth(120); // Phone
                patientTable.getColumnModel().getColumn(7).setMaxWidth(200); // Address
                patientTable.getColumnModel().getColumn(8).setMaxWidth(150); // Actions
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading patients: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private void searchPatients() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadPatients();
            return;
        }

        try {
            // Clear existing data
            tableModel.setRowCount(0);

            // Search patients
            List<Object[]> patients = PatientDAO.searchPatient(keyword);

            // Add patients to table model
            for (Object[] patient : patients) {
                Object[] rowData = new Object[9]; // 8 data columns + 1 action column
                System.arraycopy(patient, 0, rowData, 0, patient.length);
                rowData[8] = ""; // Placeholder for action buttons
                tableModel.addRow(rowData);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error searching patients: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createAddPatientPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("Add New Patient");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Form panel with proper layout settings
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create form fields with fixed height
        JTextField fnField = createFormField(formPanel, "First Name*:");
        JTextField mnField = createFormField(formPanel, "Middle Name:");
        JTextField lnField = createFormField(formPanel, "Last Name*:");

        // Assign to class fields for later access
        firstNameField = fnField;
        middleNameField = mnField;
        lastNameField = lnField;

        // Birthday field
        JLabel birthdayLabel = new JLabel("Birthday*:");
        birthdayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        birthdayLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(birthdayLabel);
        formPanel.add(Box.createVerticalStrut(5));


        // Create a fixed-size panel for the date chooser
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.setBackground(Color.WHITE);
        datePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        datePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Important: Set a specific preferred size to avoid rendering issues
        birthdayChooser.setPreferredSize(new Dimension(200, 30));
        datePanel.add(birthdayChooser, BorderLayout.CENTER);

        formPanel.add(datePanel);
        formPanel.add(Box.createVerticalStrut(10));

        // Email and Phone fields
        JTextField emField = createFormField(formPanel, "Email*:");
        JTextField phField = createFormField(formPanel, "Phone*:");

        // Assign to class fields
        emailField = emField;
        phoneField = phField;

        // Address field
        JLabel addressLabel = new JLabel("Address*:");
        addressLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(addressLabel);
        formPanel.add(Box.createVerticalStrut(5));

        // Create address text area with fixed dimensions
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JScrollPane addressScrollPane = new JScrollPane(addressArea);
        addressScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        addressScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        addressScrollPane.setPreferredSize(new Dimension(300, 100));

        formPanel.add(addressScrollPane);
        formPanel.add(Box.createVerticalStrut(15));

        // Files section with strict dimension control
        JLabel filesLabel = new JLabel("Patient Files:");
        filesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(filesLabel);
        formPanel.add(Box.createVerticalStrut(5));

        // Initialize files table with safe dimensions
        String[] columnNames = {"File Name", "Type", "Size"};
        filesTableModel = new DefaultTableModel(columnNames, 0);
        filesTable = new JTable(filesTableModel);
        filesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filesTable.setRowHeight(25);

        // Set column widths to prevent layout issues
        filesTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        filesTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        filesTable.getColumnModel().getColumn(2).setPreferredWidth(70);

        JScrollPane filesScrollPane = new JScrollPane(filesTable);
        filesScrollPane.setPreferredSize(new Dimension(300, 120));
        filesScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(filesScrollPane);
        formPanel.add(Box.createVerticalStrut(5));

        setupFileViewingForTable();

        // File upload buttons in a controlled panel
        JPanel fileButtonsPanel = new JPanel();
        fileButtonsPanel.setLayout(new BoxLayout(fileButtonsPanel, BoxLayout.X_AXIS));
        fileButtonsPanel.setOpaque(false);
        fileButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton uploadButton = new JButton("Upload Files");
        uploadButton.addActionListener(e -> uploadFiles());

        // Add spacing between buttons
        fileButtonsPanel.add(uploadButton);
        fileButtonsPanel.add(Box.createHorizontalStrut(10));

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> {
            int selectedRow = filesTable.getSelectedRow();
            if (selectedRow != -1) {
                uploadedFilePaths.remove(selectedRow);
                updateFilesTable();
                updateUploadedFilesList();
            }
        });

        fileButtonsPanel.add(removeButton);
        formPanel.add(fileButtonsPanel);
        formPanel.add(Box.createVerticalStrut(15));

        // Create a proper scrollable container
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(150, 150, 150));
        cancelButton.addActionListener(e -> cardLayout.show(detailPanel, "EMPTY"));

        JButton addButton = new JButton("Add Patient");
        styleButton(addButton, new Color(46, 204, 113));
        addButton.addActionListener(e -> addNewPatient());

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);

        // Assemble the panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    private void showAddPatient() {
        // Reset uploaded files list
        uploadedFilePaths = new ArrayList<>();

        // Remove existing ADD panel if it exists
        Component[] components = detailPanel.getComponents();
        for (Component component : components) {
            if ("ADD".equals(component.getName())) {
                detailPanel.remove(component);
                break;
            }
        }

        try {
            // Create fresh form fields
            firstNameField = new JTextField();
            middleNameField = new JTextField();
            lastNameField = new JTextField();
            emailField = new JTextField();
            phoneField = new JTextField();
            addressArea = new JTextArea();
            birthdayChooser = new JDateChooser();

            // Initialize uploaded files list if null
            if (uploadedFilesPanel == null) {
                initializeUploadedFilesList();
            }

            // Create a new add patient panel with the fresh fields
            JPanel addPatientPanel = createAddPatientPanel();
            addPatientPanel.setName("ADD");
            detailPanel.add(addPatientPanel, "ADD");

            // Show add patient panel
            cardLayout.show(detailPanel, "ADD");

            // Clear table model before updating
            if (filesTableModel != null) {
                filesTableModel.setRowCount(0);
            }

            // Update UI
            updateFilesTable();
            setupFileViewingForTable();
            detailPanel.revalidate();
            detailPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error creating form: " + e.getMessage(),
                    "Form Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void showViewPatient(int patientId) {
        try {
            // Reset uploaded files
            uploadedFilePaths = new ArrayList<>();

            // Remove existing VIEW panel if it exists
            Component[] components = detailPanel.getComponents();
            for (Component component : components) {
                if ("VIEW".equals(component.getName())) {
                    detailPanel.remove(component);
                    break;
                }
            }

            // Get patient data
            Object[] patientData = PatientDAO.getPatientById(patientId);
            if (patientData == null) {
                JOptionPane.showMessageDialog(this, "Error loading patient data.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get patient files
            uploadedFilePaths = PatientDAO.getPatientFiles(patientId);

            // Create new form fields to avoid reference issues
            firstNameField = new JTextField();
            middleNameField = new JTextField();
            lastNameField = new JTextField();
            emailField = new JTextField();
            phoneField = new JTextField();
            addressArea = new JTextArea();
            birthdayChooser = new JDateChooser();
            birthdayChooser.setDateFormatString("yyyy-MM-dd");

            // Create a new view patient panel
            JPanel viewPanel = createAddPatientPanel();
            viewPanel.setName("VIEW");

            // Change the title
            Component topComponent = viewPanel.getComponent(0);
            if (topComponent instanceof JLabel) {
                ((JLabel) topComponent).setText("View Patient #" + patientId);
            }

            // Remove action buttons
            Component bottomComponent = viewPanel.getComponent(2);
            if (bottomComponent instanceof JPanel) {
                viewPanel.remove(bottomComponent);
            }

            // Fill in patient data
            firstNameField.setText((String) patientData[1]);
            middleNameField.setText((String) patientData[2]);
            lastNameField.setText((String) patientData[3]);

            try {
                birthdayChooser.setDate(java.sql.Date.valueOf((String) patientData[4]));
            } catch (Exception e) {
                System.err.println("Error setting date: " + e.getMessage());
            }

            emailField.setText((String) patientData[5]);
            phoneField.setText((String) patientData[6]);
            addressArea.setText((String) patientData[7]);

            // Set all fields to read-only
            firstNameField.setEditable(false);
            middleNameField.setEditable(false);
            lastNameField.setEditable(false);
            emailField.setEditable(false);
            phoneField.setEditable(false);
            addressArea.setEditable(false);
            birthdayChooser.setEnabled(false);

            // Add to card layout and show
            detailPanel.add(viewPanel, "VIEW");
            cardLayout.show(detailPanel, "VIEW");

            // Update file displays
            updateFilesTable();
            updateUploadedFilesList();

            detailPanel.revalidate();
            detailPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading patient information: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void showEditPatient(int patientId) {
        try {
            // Reset uploaded files
            uploadedFilePaths = new ArrayList<>();

            // Remove existing EDIT panel if it exists
            Component[] components = detailPanel.getComponents();
            for (Component component : components) {
                if ("EDIT".equals(component.getName())) {
                    detailPanel.remove(component);
                    break;
                }
            }

            // Get patient data
            Object[] patientData = PatientDAO.getPatientById(patientId);
            if (patientData == null) {
                JOptionPane.showMessageDialog(this, "Error loading patient data.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get patient files
            uploadedFilePaths = PatientDAO.getPatientFiles(patientId);

            // Create new form fields to avoid reference issues
            firstNameField = new JTextField();
            middleNameField = new JTextField();
            lastNameField = new JTextField();
            emailField = new JTextField();
            phoneField = new JTextField();
            addressArea = new JTextArea();
            birthdayChooser = new JDateChooser();
            birthdayChooser.setDateFormatString("yyyy-MM-dd");

            // Create a new add patient panel
            JPanel editPanel = createAddPatientPanel();
            editPanel.setName("EDIT");

            // Change the title - access as Component instead of Container
            Component topComponent = editPanel.getComponent(0);
            if (topComponent instanceof JLabel) {
                ((JLabel) topComponent).setText("Edit Patient #" + patientId);
            }

            // Update the submit button action - access as Component instead of Container
            Component bottomComponent = editPanel.getComponent(2);
            if (bottomComponent instanceof JPanel) {
                JPanel buttonPanel = (JPanel) bottomComponent;
                if (buttonPanel.getComponentCount() > 1) {
                    Component buttonComponent = buttonPanel.getComponent(1);
                    if (buttonComponent instanceof JButton) {
                        JButton updateButton = (JButton) buttonComponent;
                        updateButton.setText("Update Patient");
                        updateButton.setBackground(new Color(41, 128, 185));

                        // Remove existing listeners and add new one
                        for (ActionListener al : updateButton.getActionListeners()) {
                            updateButton.removeActionListener(al);
                        }
                        updateButton.addActionListener(e -> updatePatient(patientId));
                    }
                }
            }

            // Fill in patient data
            firstNameField.setText((String) patientData[1]);
            middleNameField.setText((String) patientData[2]);
            lastNameField.setText((String) patientData[3]);

            try {
                birthdayChooser.setDate(java.sql.Date.valueOf((String) patientData[4]));
            } catch (Exception e) {
                System.err.println("Error setting date: " + e.getMessage());
            }

            emailField.setText((String) patientData[5]);
            phoneField.setText((String) patientData[6]);
            addressArea.setText((String) patientData[7]);

            // Add to card layout and show
            detailPanel.add(editPanel, "EDIT");
            cardLayout.show(detailPanel, "EDIT");

            // Update file displays
            updateFilesTable();
            updateUploadedFilesList();


            updateFilesTable();
            setupFileViewingForTable();
            detailPanel.revalidate();
            detailPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading patient information: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmDeletePatient(int patientId) {
    try {
        // Check if patient has appointments
        List<Object[]> appointments = PatientDAO.getPatientAppointments(patientId);

        if (!appointments.isEmpty()) {
            // Show a generic message about existing appointments
            String message = "This patient has existing appointments.\n\n" +
                             "Do you want to delete these appointments and the patient?";

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    message,
                    "Appointments Found",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                boolean success = PatientDAO.deletePatientWithAppointments(patientId);
                if (success) {
                    showToast("Patient and appointments deleted successfully!", new Color(46, 204, 113));
                    loadPatients();
                    cardLayout.show(detailPanel, "EMPTY");
                }
            }
        } else {
            // No appointments, confirm normal deletion
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete patient #" + patientId + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                boolean success = PatientDAO.deletePatient(patientId);
                if (success) {
                    showToast("Patient deleted successfully!", new Color(46, 204, 113));
                    loadPatients();
                    cardLayout.show(detailPanel, "EMPTY");
                }
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
    private void updatePatient(int patientId) {
        // Validate input fields
        if (!validateInputFields(firstNameField, lastNameField, birthdayChooser, emailField, phoneField, addressArea)) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields correctly.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Collect data from input fields
        String firstName = firstNameField.getText().trim();
        String middleName = middleNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String birthday = new SimpleDateFormat("yyyy-MM-dd").format(birthdayChooser.getDate());
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();
        List<String> filePaths = uploadedFilePaths;

        try {
            // Call DAO to update patient
            boolean isUpdated = PatientDAO.updatePatient(patientId, firstName, middleName, lastName, birthday, email, phone, address, filePaths);

            if (isUpdated) {
                JOptionPane.showMessageDialog(this, "Patient updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatients(); // Refresh the patient list
                cardLayout.show(detailPanel, "EMPTY"); // Return to the default view
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update patient. Please try again.", "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            JOptionPane.showMessageDialog(this, "An error occurred while updating the patient: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private JPanel createEditPatientPanel(int patientId) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("Edit Patient #" + patientId);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Create a container panel with BoxLayout for the form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create form fields with fixed dimensions
        JTextField fnField = createFormField(formPanel, "First Name*:");
        JTextField mnField = createFormField(formPanel, "Middle Name:");
        JTextField lnField = createFormField(formPanel, "Last Name*:");

        // Assign to class fields for later access
        firstNameField = fnField;
        middleNameField = mnField;
        lastNameField = lnField;

        // Birthday field with fixed dimensions
        JDateChooser dateChooser = createDateField(formPanel, "Birthday*:");
        birthdayChooser = dateChooser;

        // Email and Phone fields
        JTextField emField = createFormField(formPanel, "Email*:");
        JTextField phField = createFormField(formPanel, "Phone*:");

        // Assign to class fields
        emailField = emField;
        phoneField = phField;

        // Address section with fixed dimensions
        JLabel addressLabel = new JLabel("Address*:");
        addressLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Configure address text area
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JScrollPane addressScrollPane = new JScrollPane(addressArea);
        addressScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        addressScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        addressScrollPane.setPreferredSize(new Dimension(300, 100));

        formPanel.add(addressLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(addressScrollPane);
        formPanel.add(Box.createVerticalStrut(15));

        // Files section with fixed dimensions
        JLabel filesLabel = new JLabel("Patient Files:");
        filesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(filesLabel);
        formPanel.add(Box.createVerticalStrut(5));

        // Configure files table
        filesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filesTable.setRowHeight(25);

        // Set column widths
        if (filesTable.getColumnModel().getColumnCount() >= 3) {
            filesTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            filesTable.getColumnModel().getColumn(1).setPreferredWidth(80);
            filesTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        }

        JScrollPane filesScrollPane = new JScrollPane(filesTable);
        filesScrollPane.setPreferredSize(new Dimension(300, 120));
        filesScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        filesScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(filesScrollPane);
        formPanel.add(Box.createVerticalStrut(5));

        // File upload buttons with proper layout
        // Modify the fileButtonsPanel in createAddPatientPanel() method
        JPanel fileButtonsPanel = new JPanel();
        fileButtonsPanel.setLayout(new BoxLayout(fileButtonsPanel, BoxLayout.X_AXIS));
        fileButtonsPanel.setOpaque(false);
        fileButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton uploadButton = new JButton("Upload Files");
        uploadButton.addActionListener(e -> uploadFiles());

        JButton viewButton = new JButton("View Selected");
        viewButton.addActionListener(e -> viewSelectedFile());

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> {
            int selectedRow = filesTable.getSelectedRow();
            if (selectedRow != -1) {
                uploadedFilePaths.remove(selectedRow);
                updateFilesTable();
                updateUploadedFilesList();
            }
        });

        fileButtonsPanel.add(uploadButton);
        fileButtonsPanel.add(Box.createHorizontalStrut(10));
        fileButtonsPanel.add(viewButton);
        fileButtonsPanel.add(Box.createHorizontalStrut(10));
        fileButtonsPanel.add(removeButton);
        formPanel.add(fileButtonsPanel);

        // Uploaded files section
        JLabel uploadedFilesLabel = new JLabel("Uploaded Files:");
        uploadedFilesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        uploadedFilesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(uploadedFilesLabel);
        formPanel.add(Box.createVerticalStrut(5));

        // Update the files list
        updateUploadedFilesList();

        // Add the files list panel
        formPanel.add(uploadedFilesPanel);
        formPanel.add(Box.createVerticalStrut(10));

        // Put the form container in a scroll pane
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(150, 150, 150));
        cancelButton.addActionListener(e -> cardLayout.show(detailPanel, "EMPTY"));

        JButton updateButton = new JButton("Update Patient");
        styleButton(updateButton, new Color(41, 128, 185));
        updateButton.addActionListener(e -> updatePatient(patientId));

        buttonPanel.add(cancelButton);
        buttonPanel.add(updateButton);

        // Add components to main panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    private void loadPatientData(int patientId) {
        Object[] patientData = PatientDAO.getPatientById(patientId);
        if (patientData != null) {
            System.out.println("Patient data length: " + patientData.length);
            for (int i = 0; i < patientData.length; i++) {
                System.out.println("patientData[" + i + "]: " + patientData[i]);
            }

            firstNameField.setText((String) patientData[1]);
            middleNameField.setText((String) patientData[2]);
            lastNameField.setText((String) patientData[3]);
            birthdayChooser.setDate(java.sql.Date.valueOf((String) patientData[4]));
            emailField.setText((String) patientData[5]);
            phoneField.setText((String) patientData[6]);
            addressArea.setText((String) patientData[7]);

            // File paths are loaded separately via PatientDAO.getPatientFiles() in createEditPatientPanel
        } else {
            JOptionPane.showMessageDialog(this, "Error loading patient data. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // Keep only one updateFilesTable method and improve it
    private void updateFilesTable() {
        // Clear the table
        filesTableModel.setRowCount(0);

        // Reset the column identifiers first to ensure proper structure
        String[] columnNames = {"File Name", "Type", "Size"};
        filesTableModel.setColumnIdentifiers(columnNames);

        // Add each file with a more descriptive presentation (limit rows for safety)
        int maxRows = Math.min(uploadedFilePaths.size(), 100); // Safety limit
        for (int i = 0; i < maxRows; i++) {
            String filePath = uploadedFilePaths.get(i);
            File file = new File(filePath);
            String fileName = file.getName();
            String fileType = getFileType(fileName);
            String fileSize = getFileSize(file);

            // Add to table with limited string length
            filesTableModel.addRow(new Object[]{
                    limitString(fileName, 50),
                    fileType,
                    fileSize
            });
        }

        // Set reasonable column widths
        if (filesTable.getColumnModel().getColumnCount() >= 3) {
            filesTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            filesTable.getColumnModel().getColumn(1).setPreferredWidth(50);
            filesTable.getColumnModel().getColumn(2).setPreferredWidth(50);

            // Set maximum widths to prevent excessive sizing
            filesTable.getColumnModel().getColumn(0).setMaxWidth(300);
            filesTable.getColumnModel().getColumn(1).setMaxWidth(100);
            filesTable.getColumnModel().getColumn(2).setMaxWidth(100);
        }
    }

    // Helper method to limit string length
    private String limitString(String input, int maxLength) {
        if (input == null) return "";
        return input.length() <= maxLength ? input : input.substring(0, maxLength - 3) + "...";
    }

    // Helper method to get file type
    private String getFileType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "PDF";
            case "jpg":
            case "jpeg": return "Image";
            case "png": return "Image";
            default: return "Unknown";
        }
    }

    private String getFileSize(File file) {
        long size = file.length();
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new java.text.DecimalFormat("#,##0.#")
                .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];


    }
    // Helper method to get human-readable file size


    // Add the ActionButtonRenderer class here, as a separate class
    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton viewButton;
        private JButton editButton;
        private JButton deleteButton;

        public ActionButtonRenderer() {
            // Use GridLayout with 1 row, 3 columns to arrange buttons horizontally
            setLayout(new GridLayout(1, 3, 5, 0));
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setOpaque(true);

            // Create view button
            viewButton = new JButton("View");
            viewButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            viewButton.setForeground(Color.BLACK);
            viewButton.setBackground(new Color(135, 206, 250)); // Light blue
            viewButton.setFocusPainted(false);
            viewButton.setBorderPainted(true);
            viewButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            viewButton.setUI(new BasicButtonUI());

            // Create edit button
            editButton = new JButton("Edit");
            editButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            editButton.setForeground(Color.BLACK);
            editButton.setBackground(new Color(255, 215, 0)); // Yellow
            editButton.setFocusPainted(false);
            editButton.setBorderPainted(true);
            editButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            editButton.setUI(new BasicButtonUI());

            // Create delete button
            deleteButton = new JButton("Delete");
            deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            deleteButton.setForeground(Color.BLACK);
            deleteButton.setBackground(new Color(255, 99, 71)); // Red-orange
            deleteButton.setFocusPainted(false);
            deleteButton.setBorderPainted(true);
            deleteButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            deleteButton.setUI(new BasicButtonUI());

            // Add buttons to panel
            add(viewButton);
            add(editButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            // Set background based on selection state
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            // Ensure button colors are consistent
            viewButton.setForeground(Color.BLACK);
            viewButton.setBackground(new Color(135, 206, 250));
            editButton.setForeground(Color.BLACK);
            editButton.setBackground(new Color(255, 215, 0));
            deleteButton.setForeground(Color.BLACK);
            deleteButton.setBackground(new Color(255, 99, 71));

            return this;
        }
    }
    private JDateChooser createDateField(JPanel container, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create the date chooser
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateChooser.setDateFormatString("yyyy-MM-dd");

        // Set border to match other fields
        dateChooser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        // Fix the layout constraints
        dateChooser.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));

        // Important: Use a panel with BorderLayout to properly size the date chooser
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.setBackground(Color.WHITE);
        datePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        datePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        datePanel.add(dateChooser, BorderLayout.CENTER);

        // Add components to the main container
        container.add(label);
        container.add(Box.createVerticalStrut(5));
        container.add(datePanel);
        container.add(Box.createVerticalStrut(10));

        return dateChooser;
    }
    private JTextField createFormField(JPanel container, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(label);
        container.add(Box.createVerticalStrut(5));
        container.add(textField);
        container.add(Box.createVerticalStrut(10));

        return textField;
    }
    private boolean validateInputFields(JTextField firstNameField, JTextField lastNameField,
                                        JDateChooser birthdayChooser, JTextField emailField,
                                        JTextField phoneField, JTextArea addressArea) {
        StringBuilder errorMessage = new StringBuilder("Please correct the following errors:\n");
        boolean hasError = false;

        if (firstNameField.getText().trim().isEmpty()) {
            errorMessage.append("- First Name is required\n");
            hasError = true;
        }

        if (lastNameField.getText().trim().isEmpty()) {
            errorMessage.append("- Last Name is required\n");
            hasError = true;
        }

        if (birthdayChooser.getDate() == null) {
            errorMessage.append("- Birthday is required\n");
            hasError = true;
        }

        if (emailField.getText().trim().isEmpty()) {
            errorMessage.append("- Email is required\n");
            hasError = true;
        } else if (!isValidEmail(emailField.getText().trim())) {
            errorMessage.append("- Email format is invalid\n");
            hasError = true;
        }

        if (phoneField.getText().trim().isEmpty()) {
            errorMessage.append("- Phone is required\n");
            hasError = true;
        } else if (!isValidPhone(phoneField.getText().trim())) {
            errorMessage.append("- Phone must start with 09 and have 11 digits\n");
            hasError = true;
        }

        if (addressArea.getText().trim().isEmpty()) {
            errorMessage.append("- Address is required\n");
            hasError = true;
        }

        if (hasError) {
            JOptionPane.showMessageDialog(null, errorMessage.toString(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return pat.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^09\\d{9}$");
    }
    private void addNewPatient() {
        if (!validateInputFields(firstNameField, lastNameField, birthdayChooser, emailField, phoneField, addressArea)) {
            return; // Stop if validation fails
        }

        try {
            // Add debug statements to check values
            System.out.println("First Name: " + firstNameField.getText());
            System.out.println("Last Name: " + lastNameField.getText());
            System.out.println("Email: " + emailField.getText());
            System.out.println("First name field visible: " + firstNameField.isVisible());
            System.out.println("First name field showing: " + firstNameField.isShowing());
            System.out.println("First name bounds: " + firstNameField.getBounds());

            String firstName = firstNameField.getText().trim();
            String middleName = middleNameField.getText().trim();
            String lastName = lastNameField.getText().trim();

            // Check if date is null
            if (birthdayChooser.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Birthday date is not set", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String birthday = new SimpleDateFormat("yyyy-MM-dd").format(birthdayChooser.getDate());
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressArea.getText().trim();

            // Call the DAO method and handle the result
            boolean result = PatientDAO.addPatient(firstName, middleName, lastName, birthday, email, phone, address, uploadedFilePaths);

            if (result) {
                showToast("Patient added successfully!", new Color(46, 204, 113));
                loadPatients();
                cardLayout.show(detailPanel, "EMPTY");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add patient. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    // Add this method to your PatientManagementFrame class
    private void viewSelectedFile() {
        int selectedRow = filesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a file to view",
                    "No File Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Convert row index from view to model if sorting is enabled
        if (filesTable.getRowSorter() != null) {
            selectedRow = filesTable.getRowSorter().convertRowIndexToModel(selectedRow);
        }

        if (selectedRow < uploadedFilePaths.size()) {
            String filePath = uploadedFilePaths.get(selectedRow);
            openFile(filePath);
        }
    }

    private void openFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "File does not exist: " + file.getAbsolutePath(),
                            "File Not Found",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error opening file: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    private void setupFileViewingForTable() {
        // Check if filesTable exists first
        if (filesTable == null) {
            return; // Exit early if table doesn't exist yet
        }

        // Remove any existing listeners to avoid duplicates
        for (MouseListener listener : filesTable.getMouseListeners()) {
            filesTable.removeMouseListener(listener);
        }

        // Make the table non-editable to prevent cell editing
        // Change this line:
        // filesTableModel.setDefaultEditor(Object.class, null);
        // To this:
        filesTable.setDefaultEditor(Object.class, null);

        // Add double-click listener to view files
        filesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    int row = filesTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        // Select the row that was clicked
                        filesTable.setRowSelectionInterval(row, row);
                        viewSelectedFile();
                    }
                }
            }
        });

        // Add keyboard listener for Enter key
        filesTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "viewFile");
        filesTable.getActionMap().put("viewFile", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                viewSelectedFile();
            }
        });

        // Add tooltip to inform users
        filesTable.setToolTipText("Double-click or press Enter to view file");
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PatientManagementFrame());
    }
}