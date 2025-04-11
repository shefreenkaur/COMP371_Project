    package ui;

    import logic.Reservation;
    import logic.ReservationManager;
    import logic.Room;

    import javax.swing.*;
    import javax.swing.border.EmptyBorder;
    import javax.swing.border.TitledBorder;
    import javax.swing.table.DefaultTableModel;
    import javax.swing.table.TableRowSorter;
    import javax.swing.text.MaskFormatter;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.KeyAdapter;
    import java.awt.event.KeyEvent;
    import java.awt.event.MouseAdapter;
    import java.awt.event.MouseEvent;
    import java.awt.image.BufferedImage;
    import java.io.File;
    import java.sql.Date;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Calendar;
    import java.util.function.BiConsumer;
    import java.util.regex.Pattern;


    /**
     * Main GUI class for the Hotel Management System.
     * This class implements the MVC pattern by separating UI components from business logic.
     *
     * @author YourName
     * @version 2.0
     */
    public class HotelManagementGUI extends JFrame {


        private static final long serialVersionUID = 1L;
        private static final String APP_TITLE = "Hotel Management System";
        private static final int FORM_PADDING = 12;
        private static final int BORDER_PADDING = 10;
        private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
        private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 12);
        private static final Color PRIMARY_COLOR = new Color(51, 102, 153);
        private static final Color SECONDARY_COLOR = new Color(240, 240, 240);
        private static final Color ACCENT_COLOR = new Color(255, 153, 0);

        private ReservationUIConnector reservationConnector;

        // UI Components
        private JTabbedPane tabbedPane;
        private JPanel reservationPanel;
        private JPanel roomPanel;
        private JPanel inventoryPanel;
        private JPanel billingPanel;
        private JPanel reportingPanel;

        // Reservation Form Components
        private JTextField guestNameField;
        private JTextField emailField;
        private JTextField phoneField;
        private JFormattedTextField checkInField;
        private JFormattedTextField checkOutField;
        private JComboBox<ReservationManager.RoomType> roomTypeComboBox;
        private JSpinner guestsSpinner;
        private JTextArea specialRequestsArea;
        private JButton addReservationButton;
        private JButton clearFormButton;

        // Reservation List Components
        private JTable reservationsTable;
        private DefaultTableModel reservationsTableModel;
        private JTextField searchField;
        private JButton viewReservationButton;
        private JButton checkInButton;
        private JButton checkOutButton;
        private JButton cancelReservationButton;
        private JPanel statusPanel;
        private JLabel statusLabel;

        /**
         * Constructor initializes the UI
         */
        public HotelManagementGUI() {
            // Set application properties
            setDefaultProperties();

            // Initialize components
            reservationConnector = new ReservationUIConnector();
            initComponents();
            loadData();

            // Set up event listeners
            setupEventListeners();
        }

        /**
         * Set default application properties
         */
        private void setDefaultProperties() {
            setTitle(APP_TITLE);
            setSize(1024, 768);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setIconImage(createAppIcon().getImage());

            // Set UI fonts
            setUIFont(new javax.swing.plaf.FontUIResource(REGULAR_FONT));
        }

        /**
         * Creates an application icon
         */
        private ImageIcon createAppIcon() {
            // This would ideally use a proper icon file
            // For now, we'll just create a simple colored icon
            int iconSize = 16;
            BufferedImage image = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(PRIMARY_COLOR);
            g2d.fillRect(0, 0, iconSize, iconSize);
            g2d.setColor(ACCENT_COLOR);
            g2d.fillRect(3, 3, iconSize - 6, iconSize - 6);
            g2d.dispose();
            return new ImageIcon(image);
        }

        /**
         * Sets the default font for all UI components
         */
        private void setUIFont(javax.swing.plaf.FontUIResource font) {
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof javax.swing.plaf.FontUIResource) {
                    UIManager.put(key, font);
                }
            }
        }

        /**
         * Initialize UI components
         */
        private void initComponents() {
            // Create tabbed pane with custom styling
            tabbedPane = new JTabbedPane();
            tabbedPane.setFont(HEADER_FONT);
            tabbedPane.setBackground(SECONDARY_COLOR);
            tabbedPane.setForeground(PRIMARY_COLOR);

            // Create panels for each tab
            createReservationPanel();
            createRoomPanel();
            createInventoryPanel();
            createBillingPanel();
            createReportingPanel();

            // Add panels to tabbed pane with icons
            tabbedPane.addTab("Reservations", createTabIcon("reservation"), reservationPanel);
            tabbedPane.addTab("Rooms", createTabIcon("room"), roomPanel);
            tabbedPane.addTab("Inventory", createTabIcon("inventory"), inventoryPanel);
            tabbedPane.addTab("Billing", createTabIcon("billing"), billingPanel);
            tabbedPane.addTab("Reports", createTabIcon("report"), reportingPanel);

            // Create status panel
            statusPanel = new JPanel(new BorderLayout());
            statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
            statusPanel.setBackground(SECONDARY_COLOR);

            statusLabel = new JLabel("Ready");
            statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            statusPanel.add(statusLabel, BorderLayout.WEST);

            JLabel versionLabel = new JLabel("v2.0");
            versionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            statusPanel.add(versionLabel, BorderLayout.EAST);

            // Add components to frame
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(tabbedPane, BorderLayout.CENTER);
            getContentPane().add(statusPanel, BorderLayout.SOUTH);
        }

        /**
         * Creates an icon for the tabs
         */
        private Icon createTabIcon(String iconType) {
            // Here we would normally load actual icons from resources
            // For simplicity, we'll create colored squares for now
            int iconSize = 14;
            BufferedImage image = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            Color iconColor;
            switch (iconType) {
                case "reservation":
                    iconColor = new Color(51, 102, 153);
                    break;
                case "room":
                    iconColor = new Color(76, 175, 80);
                    break;
                case "inventory":
                    iconColor = new Color(156, 39, 176);
                    break;
                case "billing":
                    iconColor = new Color(255, 87, 34);
                    break;
                case "report":
                    iconColor = new Color(255, 193, 7);
                    break;
                default:
                    iconColor = PRIMARY_COLOR;
            }

            g2d.setColor(iconColor);
            g2d.fillRect(0, 0, iconSize, iconSize);
            g2d.dispose();

            return new ImageIcon(image);
        }

        /**
         * Create reservation panel with form and list
         */
        private void createReservationPanel() {
            reservationPanel = new JPanel(new BorderLayout(10, 10));
            reservationPanel.setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
            reservationPanel.setBackground(SECONDARY_COLOR);

            // Create form panel
            JPanel formPanel = createReservationFormPanel();

            // Create list panel
            JPanel listPanel = createReservationListPanel();

            // Add form and list panels to main panel
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formPanel, listPanel);
            splitPane.setDividerLocation(350);
            splitPane.setOneTouchExpandable(true);
            splitPane.setDividerSize(8);
            splitPane.setBorder(null);
            reservationPanel.add(splitPane, BorderLayout.CENTER);
        }

        /**
         * Creates the reservation form panel
         */
        private JPanel createReservationFormPanel() {
            JPanel formPanel = new JPanel(new BorderLayout(10, 10));
            formPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                    "New Reservation",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    HEADER_FONT,
                    PRIMARY_COLOR
            ));
            formPanel.setBackground(SECONDARY_COLOR);

            // Create inner panel with GridBagLayout for the form
            JPanel innerFormPanel = new JPanel(new GridBagLayout());
            innerFormPanel.setBackground(SECONDARY_COLOR);
            innerFormPanel.setBorder(new EmptyBorder(FORM_PADDING, FORM_PADDING, FORM_PADDING, FORM_PADDING));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Guest Name
            gbc.gridx = 0;
            gbc.gridy = 0;
            JLabel nameLabel = new JLabel("Guest Name:");
            nameLabel.setFont(REGULAR_FONT);
            innerFormPanel.add(nameLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            guestNameField = new JTextField(20);
            guestNameField.setFont(REGULAR_FONT);
            innerFormPanel.add(guestNameField, gbc);

            // Check-In Date
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            JLabel checkInLabel = new JLabel("Check-In Date:");
            checkInLabel.setFont(REGULAR_FONT);
            innerFormPanel.add(checkInLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            // Use JFormattedTextField for dates with a mask
            checkInField = new JFormattedTextField(createDateFormatter());
            checkInField.setColumns(10);
            checkInField.setToolTipText("Format: YYYY-MM-DD");
            innerFormPanel.add(checkInField, gbc);

            JButton checkInCalendarButton = new JButton("...");
            checkInCalendarButton.setMargin(new Insets(1, 4, 1, 4));
            checkInCalendarButton.setToolTipText("Select date");
            checkInCalendarButton.addActionListener(e -> showDatePicker(checkInField));

            gbc.gridx = 2;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            innerFormPanel.add(checkInCalendarButton, gbc);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Check-Out Date
            gbc.gridx = 0;
            gbc.gridy = 2;
            JLabel checkOutLabel = new JLabel("Check-Out Date:");
            checkOutLabel.setFont(REGULAR_FONT);
            innerFormPanel.add(checkOutLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 2;
            checkOutField = new JFormattedTextField(createDateFormatter());
            checkOutField.setColumns(10);
            checkOutField.setToolTipText("Format: YYYY-MM-DD");
            innerFormPanel.add(checkOutField, gbc);

            JButton checkOutCalendarButton = new JButton("...");
            checkOutCalendarButton.setMargin(new Insets(1, 4, 1, 4));
            checkOutCalendarButton.setToolTipText("Select date");
            checkOutCalendarButton.addActionListener(e -> showDatePicker(checkOutField));

            gbc.gridx = 2;
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            innerFormPanel.add(checkOutCalendarButton, gbc);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Room Type
            gbc.gridx = 0;
            gbc.gridy = 3;
            JLabel roomTypeLabel = new JLabel("Room Type:");
            roomTypeLabel.setFont(REGULAR_FONT);
            innerFormPanel.add(roomTypeLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            roomTypeComboBox = new JComboBox<>();
            roomTypeComboBox.setFont(REGULAR_FONT);
            innerFormPanel.add(roomTypeComboBox, gbc);

            // Email
            gbc.gridx = 0;
            gbc.gridy = 4;
            JLabel emailLabel = new JLabel("Email:");
            emailLabel.setFont(REGULAR_FONT);
            innerFormPanel.add(emailLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 4;
            emailField = new JTextField(20);
            emailField.setFont(REGULAR_FONT);
            innerFormPanel.add(emailField, gbc);

            // Phone
            gbc.gridx = 0;
            gbc.gridy = 5;
            JLabel phoneLabel = new JLabel("Phone:");
            phoneLabel.setFont(REGULAR_FONT);
            innerFormPanel.add(phoneLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 5;
            phoneField = new JTextField(10);
            phoneField.setFont(REGULAR_FONT);
            innerFormPanel.add(phoneField, gbc);

            // Number of Guests
            gbc.gridx = 0;
            gbc.gridy = 6;
            JLabel guestsLabel = new JLabel("Number of Guests:");
            guestsLabel.setFont(REGULAR_FONT);
            innerFormPanel.add(guestsLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 6;
            SpinnerNumberModel guestsModel = new SpinnerNumberModel(1, 1, 10, 1);
            guestsSpinner = new JSpinner(guestsModel);
            guestsSpinner.setFont(REGULAR_FONT);
            innerFormPanel.add(guestsSpinner, gbc);

            // Special Requests
            gbc.gridx = 0;
            gbc.gridy = 7;
            JLabel requestsLabel = new JLabel("Special Requests:");
            requestsLabel.setFont(REGULAR_FONT);
            innerFormPanel.add(requestsLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 7;
            gbc.gridwidth = 2;
            gbc.gridheight = 2;
            gbc.fill = GridBagConstraints.BOTH;
            specialRequestsArea = new JTextArea(4, 20);
            specialRequestsArea.setFont(REGULAR_FONT);
            specialRequestsArea.setLineWrap(true);
            specialRequestsArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(specialRequestsArea);
            scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            innerFormPanel.add(scrollPane, gbc);

            formPanel.add(innerFormPanel, BorderLayout.CENTER);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(SECONDARY_COLOR);

            clearFormButton = new JButton("Clear");
            clearFormButton.setFont(REGULAR_FONT);
            clearFormButton.setForeground(PRIMARY_COLOR);
            clearFormButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            addReservationButton = new JButton("Add Reservation");
            addReservationButton.setFont(REGULAR_FONT);
            addReservationButton.setBackground(PRIMARY_COLOR);
            addReservationButton.setForeground(Color.WHITE);
            addReservationButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            buttonPanel.add(clearFormButton);
            buttonPanel.add(addReservationButton);

            formPanel.add(buttonPanel, BorderLayout.SOUTH);

            return formPanel;
        }

        /**
         * Creates the reservation list panel
         */
        private JPanel createReservationListPanel() {
            JPanel listPanel = new JPanel(new BorderLayout(10, 10));
            listPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                    "Reservations",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    HEADER_FONT,
                    PRIMARY_COLOR
            ));
            listPanel.setBackground(SECONDARY_COLOR);

            // Search panel
            JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
            searchPanel.setBackground(SECONDARY_COLOR);
            searchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JLabel searchLabel = new JLabel("Search:");
            searchLabel.setFont(REGULAR_FONT);
            searchPanel.add(searchLabel, BorderLayout.WEST);

            searchField = new JTextField();
            searchField.setFont(REGULAR_FONT);
            searchPanel.add(searchField, BorderLayout.CENTER);

            listPanel.add(searchPanel, BorderLayout.NORTH);

            // Create table with custom styling
            reservationsTableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make table non-editable
                }
            };

            // Add columns to table
            for (String columnName : reservationConnector.getReservationsTableColumns()) {
                reservationsTableModel.addColumn(columnName);
            }

            reservationsTable = new JTable(reservationsTableModel);
            reservationsTable.setFont(REGULAR_FONT);
            reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            reservationsTable.setRowHeight(25);
            reservationsTable.setGridColor(new Color(230, 230, 230));
            reservationsTable.setShowGrid(true);
            reservationsTable.setIntercellSpacing(new Dimension(5, 5));

            // Set table header style
            reservationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            reservationsTable.getTableHeader().setBackground(PRIMARY_COLOR);
            reservationsTable.getTableHeader().setForeground(Color.WHITE);
            reservationsTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
            reservationsTable.getTableHeader().setReorderingAllowed(false);

            // Enable sorting
            reservationsTable.setAutoCreateRowSorter(true);

            // Create scrollpane with custom styling
            JScrollPane tableScrollPane = new JScrollPane(reservationsTable);
            tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
            tableScrollPane.getViewport().setBackground(Color.WHITE);

            listPanel.add(tableScrollPane, BorderLayout.CENTER);

            // Create button panel
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.setBackground(SECONDARY_COLOR);
            buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            buttonPanel.add(Box.createHorizontalGlue());

            viewReservationButton = createActionButton("View Details", new Color(0, 150, 136));
            checkInButton = createActionButton("Check-In", new Color(76, 175, 80));
            checkOutButton = createActionButton("Check-Out", new Color(33, 150, 243));
            cancelReservationButton = createActionButton("Cancel", new Color(244, 67, 54));

            buttonPanel.add(viewReservationButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(checkInButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(checkOutButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            buttonPanel.add(cancelReservationButton);

            listPanel.add(buttonPanel, BorderLayout.SOUTH);

            return listPanel;
        }

        /**
         * Creates a styled action button
         */
        private JButton createActionButton(String text, Color color) {
            JButton button = new JButton(text);
            button.setFont(REGULAR_FONT);
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }

        /**
         * Creates a date formatter for the date fields
         */
        private MaskFormatter createDateFormatter() {
            MaskFormatter formatter = null;
            try {
                formatter = new MaskFormatter("####-##-##");
                formatter.setPlaceholderCharacter('_');
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return formatter;
        }

        /**
         * Shows a date picker dialog
         */
        private void showDatePicker(JFormattedTextField dateField) {
            // In a real application, this would use a proper date picker
            // For simplicity, we'll just show a message
            JOptionPane.showMessageDialog(this,
                    "This would show a date picker in a real application.",
                    "Date Picker", JOptionPane.INFORMATION_MESSAGE);
        }



         private void createRoomPanel() {
         roomPanel = new HotelUIs.RoomsUI();
         }

         private void createInventoryPanel() {
         inventoryPanel = new HotelUIs.InventoryUI();
         }

         private void createBillingPanel() {
         billingPanel = new HotelUIs.BillingUI();
         }

         private void createReportingPanel() {
         reportingPanel = new HotelUIs.ReportingUI();
         }









        /**
         * Set up event listeners for components
         */
        private void setupEventListeners() {
            // Add reservation button
            addReservationButton.addActionListener(this::addReservation);

            // Clear form button
            clearFormButton.addActionListener(e -> clearForm());

            // View reservation button
            viewReservationButton.addActionListener(this::viewReservationDetails);

            // Check-in button
            checkInButton.addActionListener(this::checkInReservation);

            // Check-out button
            checkOutButton.addActionListener(this::checkOutReservation);

            // Cancel reservation button
            cancelReservationButton.addActionListener(this::cancelReservation);

            // Double-click on reservation row
            reservationsTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        viewReservationDetails(null);
                    }
                }
            });

            // Search field
            searchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    filterReservations(searchField.getText());
                }
            });

            // Tab selection change
            tabbedPane.addChangeListener(e -> {
                int selectedIndex = tabbedPane.getSelectedIndex();
                String tabName = tabbedPane.getTitleAt(selectedIndex);
                updateStatusLabel("Selected: " + tabName);
            });
        }

        /**
         * Load initial data
         */
        private void loadData() {
            // Populate room type combo box
            reservationConnector.populateRoomTypeComboBox(roomTypeComboBox);

            // Set default dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            checkInField.setText(dateFormat.format(calendar.getTime()));

            calendar.add(Calendar.DAY_OF_MONTH, 1);
            checkOutField.setText(dateFormat.format(calendar.getTime()));

            // Set default number of guests
            guestsSpinner.setValue(1);

            // Load reservations for the next 30 days
            loadReservations();

            // Update status
            updateStatusLabel("Ready - " + dateFormat.format(new java.util.Date()));
        }

        /**
         * Load reservations for the next 30 days
         */
        private void loadReservations() {
            // Clear table
            reservationsTableModel.setRowCount(0);

            // Get current date and date 30 days from now
            Calendar calendar = Calendar.getInstance();
            Date startDate = new Date(calendar.getTimeInMillis());

            calendar.add(Calendar.DAY_OF_MONTH, 30);
            Date endDate = new Date(calendar.getTimeInMillis());

            try {
                // Show loading indicator
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                updateStatusLabel("Loading reservations...");

                // Get reservation data
                Object[][] data = reservationConnector.getReservationsTableData(startDate, endDate);

                // Add data to table model
                for (Object[] row : data) {
                    reservationsTableModel.addRow(row);
                }

                updateStatusLabel("Loaded " + data.length + " reservations");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading reservations: " + ex.getMessage(),
                        "Data Loading Error", JOptionPane.ERROR_MESSAGE);
                updateStatusLabel("Error loading reservations");
            } finally {
                // Reset cursor
                setCursor(Cursor.getDefaultCursor());
            }
        }

        /**
         * Filter reservations based on search text
         */
        private void filterReservations(String searchText) {
            if (searchText == null || searchText.trim().isEmpty()) {
                // If search is empty, remove filter
                ((TableRowSorter<DefaultTableModel>)reservationsTable.getRowSorter()).setRowFilter(null);
                updateStatusLabel("Showing all reservations");
                return;
            }

            // Case insensitive search on all columns
            RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(searchText.trim()));
            ((TableRowSorter<DefaultTableModel>)reservationsTable.getRowSorter()).setRowFilter(rowFilter);

            // Update status with count of filtered results
            int rowCount = reservationsTable.getRowCount();
            updateStatusLabel("Found " + rowCount + " matching reservations");
        }

        /**
         * Update the status label text
         */
        private void updateStatusLabel(String text) {
            statusLabel.setText(text);
        }

        /**
         * Validate form inputs before submission
         */
        private boolean validateFormInputs() {
            StringBuilder errors = new StringBuilder();

            // Check guest name
            if (guestNameField.getText().trim().isEmpty()) {
                errors.append("- Guest name is required\n");
            }

            // Check email format
            String email = emailField.getText().trim();
            if (!email.isEmpty() && !isValidEmail(email)) {
                errors.append("- Invalid email format\n");
            }

            // Check dates
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false);

                String checkInStr = checkInField.getText().trim();
                String checkOutStr = checkOutField.getText().trim();

                if (checkInStr.isEmpty() || checkInStr.contains("_")) {
                    errors.append("- Check-in date is required\n");
                } else {
                    java.util.Date checkInDate = dateFormat.parse(checkInStr);

                    if (checkOutStr.isEmpty() || checkOutStr.contains("_")) {
                        errors.append("- Check-out date is required\n");
                    } else {
                        java.util.Date checkOutDate = dateFormat.parse(checkOutStr);

                        // Check if check-out is after check-in
                        if (!checkOutDate.after(checkInDate)) {
                            errors.append("- Check-out date must be after check-in date\n");
                        }
                    }
                }
            } catch (ParseException e) {
                errors.append("- Invalid date format. Use YYYY-MM-DD\n");
            }

            // Check room type
            if (roomTypeComboBox.getSelectedIndex() == -1) {
                errors.append("- Room type must be selected\n");
            }

            if (errors.length() > 0) {
                JOptionPane.showMessageDialog(this,
                        "Please correct the following errors:\n" + errors.toString(),
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        }

        /**
         * Validates email format
         */
        private boolean isValidEmail(String email) {
            // Simple email validation regex
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            return Pattern.matches(emailRegex, email);
        }

        /**
         * Clear the reservation form
         */
        private void clearForm() {
            guestNameField.setText("");
            emailField.setText("");
            phoneField.setText("");

            // Reset dates to today/tomorrow
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            checkInField.setText(dateFormat.format(calendar.getTime()));

            calendar.add(Calendar.DAY_OF_MONTH, 1);
            checkOutField.setText(dateFormat.format(calendar.getTime()));

            // Reset other fields
            roomTypeComboBox.setSelectedIndex(0);
            guestsSpinner.setValue(1);
            specialRequestsArea.setText("");

            // Focus first field
            guestNameField.requestFocusInWindow();
        }

        /**
         * Add a new reservation
         */
        private void addReservation(ActionEvent e) {
            // First validate inputs
            if (!validateFormInputs()) {
                return;
            }

            // Check if room is available
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                updateStatusLabel("Checking room availability...");

                if (!reservationConnector.isRoomAvailable(checkInField, checkOutField, roomTypeComboBox)) {
                    JOptionPane.showMessageDialog(this,
                            "No rooms of the selected type are available for the specified dates.",
                            "Room Not Available", JOptionPane.WARNING_MESSAGE);
                    updateStatusLabel("Room not available");
                    return;
                }

                // Create reservation
                updateStatusLabel("Creating reservation...");
                boolean success = reservationConnector.createReservation(
                        guestNameField, emailField, phoneField, checkInField,
                        checkOutField, roomTypeComboBox, guestsSpinner, specialRequestsArea
                );

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Reservation created successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Clear form
                    clearForm();

                    // Reload reservations
                    loadReservations();
                    updateStatusLabel("Reservation created successfully");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error creating reservation. Please check your inputs.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    updateStatusLabel("Error creating reservation");
                }
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }

        /**
         * View reservation details
         */
        private void viewReservationDetails(ActionEvent e) {
            int selectedRow = reservationsTable.getSelectedRow();
            if (selectedRow >= 0) {
                selectedRow = reservationsTable.convertRowIndexToModel(selectedRow);
                int reservationId = (int) reservationsTableModel.getValueAt(selectedRow, 0);
                showReservationDetails(reservationId);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a reservation to view.",
                        "No Selection", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        /**
         * Shows the reservation details dialog
         */
        private void showReservationDetails(int reservationId) {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                updateStatusLabel("Loading reservation details...");

                Reservation reservation = reservationConnector.getReservation(reservationId);

                if (reservation != null) {
                    // Create detail dialog
                    JDialog detailDialog = new JDialog(this, "Reservation Details", true);
                    detailDialog.setLayout(new BorderLayout(10, 10));
                    detailDialog.setBackground(SECONDARY_COLOR);

                    // Create panel for details
                    JPanel detailPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                    detailPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                            BorderFactory.createEmptyBorder(15, 15, 15, 15)));
                    detailPanel.setBackground(Color.WHITE);

                    // Helper method to add detail rows
                    BiConsumer<String, String> addDetailRow = (label, value) -> {
                        JLabel labelComp = new JLabel(label);
                        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        labelComp.setForeground(PRIMARY_COLOR);

                        JLabel valueComp = new JLabel(value);
                        valueComp.setFont(REGULAR_FONT);

                        detailPanel.add(labelComp);
                        detailPanel.add(valueComp);
                    };

                    // Add reservation details
                    addDetailRow.accept("Reservation ID:", String.valueOf(reservation.getReservationId()));
                    addDetailRow.accept("Guest Name:", reservation.getFullName());
                    addDetailRow.accept("Email:", reservation.getEmail());
                    addDetailRow.accept("Phone:", reservation.getPhone());
                    addDetailRow.accept("Check-In Date:", reservation.getCheckInDate().toString());
                    addDetailRow.accept("Check-Out Date:", reservation.getCheckOutDate().toString());

                    // Add status with colored indicator
                    JLabel statusLabel = new JLabel("Status:");
                    statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    statusLabel.setForeground(PRIMARY_COLOR);

                    JPanel statusValuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                    statusValuePanel.setBackground(Color.WHITE);

                    JLabel statusIndicator = new JLabel("●");
                    switch (reservation.getStatus()) {
                        case "Confirmed":
                            statusIndicator.setForeground(new Color(76, 175, 80));
                            break;
                        case "Checked-in":
                            statusIndicator.setForeground(new Color(33, 150, 243));
                            break;
                        case "Checked-out":
                            statusIndicator.setForeground(new Color(156, 39, 176));
                            break;
                        case "Cancelled":
                            statusIndicator.setForeground(new Color(244, 67, 54));
                            break;
                        default:
                            statusIndicator.setForeground(Color.GRAY);
                    }

                    JLabel statusValue = new JLabel(reservation.getStatus());
                    statusValue.setFont(REGULAR_FONT);

                    statusValuePanel.add(statusIndicator);
                    statusValuePanel.add(statusValue);

                    detailPanel.add(statusLabel);
                    detailPanel.add(statusValuePanel);

                    // Continue with more details
                    addDetailRow.accept("Number of Guests:", String.valueOf(reservation.getTotalGuests()));
                    addDetailRow.accept("Room:", reservation.getRoomNumber());
                    addDetailRow.accept("Room Type:", reservation.getRoomTypeName());
                    addDetailRow.accept("Rate per Night:", "$" + reservation.getRatePerNight());
                    addDetailRow.accept("Total Nights:", String.valueOf(reservation.getNumberOfNights()));
                    addDetailRow.accept("Total Cost:", "$" + reservation.getTotalRoomCost());

                    // Special requests in a text area
                    JLabel requestsLabel = new JLabel("Special Requests:");
                    requestsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    requestsLabel.setForeground(PRIMARY_COLOR);

                    JTextArea requestsArea = new JTextArea(reservation.getSpecialRequests());
                    requestsArea.setEditable(false);
                    requestsArea.setLineWrap(true);
                    requestsArea.setWrapStyleWord(true);
                    requestsArea.setFont(REGULAR_FONT);
                    JScrollPane scrollPane = new JScrollPane(requestsArea);
                    scrollPane.setPreferredSize(new Dimension(200, 80));
                    scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

                    JPanel requestsPanel = new JPanel(new BorderLayout());
                    requestsPanel.setBackground(Color.WHITE);
                    requestsPanel.add(requestsLabel, BorderLayout.NORTH);
                    requestsPanel.add(scrollPane, BorderLayout.CENTER);

                    // Add components to dialog
                    detailDialog.add(detailPanel, BorderLayout.CENTER);
                    detailDialog.add(requestsPanel, BorderLayout.SOUTH);

                    // Add action buttons based on reservation status
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    buttonPanel.setBackground(SECONDARY_COLOR);

                    JButton closeButton = new JButton("Close");
                    closeButton.setFont(REGULAR_FONT);
                    closeButton.addActionListener(e -> detailDialog.dispose());

                    JButton actionButton = null;

                    if ("Confirmed".equals(reservation.getStatus())) {
                        actionButton = new JButton("Check In");
                        actionButton.setBackground(new Color(76, 175, 80));
                        actionButton.setForeground(Color.WHITE);
                        actionButton.addActionListener(e -> {
                            if (reservationConnector.updateReservationStatus(reservationId, "Checked-in")) {
                                JOptionPane.showMessageDialog(detailDialog,
                                        "Guest checked in successfully!",
                                        "Success", JOptionPane.INFORMATION_MESSAGE);
                                detailDialog.dispose();
                                loadReservations();
                            }
                        });
                    } else if ("Checked-in".equals(reservation.getStatus())) {
                        actionButton = new JButton("Check Out");
                        actionButton.setBackground(new Color(33, 150, 243));
                        actionButton.setForeground(Color.WHITE);
                        actionButton.addActionListener(e -> {
                            if (reservationConnector.updateReservationStatus(reservationId, "Checked-out")) {
                                JOptionPane.showMessageDialog(detailDialog,
                                        "Guest checked out successfully!",
                                        "Success", JOptionPane.INFORMATION_MESSAGE);
                                detailDialog.dispose();
                                loadReservations();
                            }
                        });
                    }

                    if (actionButton != null) {
                        actionButton.setFont(REGULAR_FONT);
                        buttonPanel.add(actionButton);
                    }

                    buttonPanel.add(closeButton);
                    detailDialog.add(buttonPanel, BorderLayout.PAGE_END);

                    // Set dialog size and show
                    detailDialog.setSize(500, 550);
                    detailDialog.setLocationRelativeTo(this);
                    detailDialog.setVisible(true);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading reservation details: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                updateStatusLabel("Error loading reservation details");
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }

        /**
         * Check in a reservation
         */
        private void checkInReservation(ActionEvent e) {
            int selectedRow = reservationsTable.getSelectedRow();
            if (selectedRow >= 0) {
                selectedRow = reservationsTable.convertRowIndexToModel(selectedRow);
                int reservationId = (int) reservationsTableModel.getValueAt(selectedRow, 0);
                String currentStatus = (String) reservationsTableModel.getValueAt(selectedRow, 6);

                if (!"Confirmed".equals(currentStatus)) {
                    JOptionPane.showMessageDialog(this,
                            "Only confirmed reservations can be checked in.",
                            "Invalid Status", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    updateStatusLabel("Processing check-in...");

                    boolean success = reservationConnector.updateReservationStatus(reservationId, "Checked-in");

                    if (success) {
                        JOptionPane.showMessageDialog(this,
                                "Guest checked in successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);

                        // Reload reservations
                        loadReservations();
                        updateStatusLabel("Guest checked in successfully");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Error checking in guest.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        updateStatusLabel("Error checking in guest");
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a reservation to check in.",
                        "No Selection", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        /**
         * Check out a reservation
         */
        private void checkOutReservation(ActionEvent e) {
            int selectedRow = reservationsTable.getSelectedRow();
            if (selectedRow >= 0) {
                selectedRow = reservationsTable.convertRowIndexToModel(selectedRow);
                int reservationId = (int) reservationsTableModel.getValueAt(selectedRow, 0);
                String currentStatus = (String) reservationsTableModel.getValueAt(selectedRow, 6);

                if (!"Checked-in".equals(currentStatus)) {
                    JOptionPane.showMessageDialog(this,
                            "Only checked-in reservations can be checked out.",
                            "Invalid Status", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    updateStatusLabel("Processing check-out...");

                    boolean success = reservationConnector.updateReservationStatus(reservationId, "Checked-out");

                    if (success) {
                        JOptionPane.showMessageDialog(this,
                                "Guest checked out successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);

                        // Reload reservations
                        loadReservations();
                        updateStatusLabel("Guest checked out successfully");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Error checking out guest.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        updateStatusLabel("Error checking out guest");
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a reservation to check out.",
                        "No Selection", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        /**
         * Cancel a reservation
         */
        private void cancelReservation(ActionEvent e) {
            int selectedRow = reservationsTable.getSelectedRow();
            if (selectedRow >= 0) {
                selectedRow = reservationsTable.convertRowIndexToModel(selectedRow);
                int reservationId = (int) reservationsTableModel.getValueAt(selectedRow, 0);
                String currentStatus = (String) reservationsTableModel.getValueAt(selectedRow, 6);

                if ("Checked-in".equals(currentStatus) || "Checked-out".equals(currentStatus)) {
                    JOptionPane.showMessageDialog(this,
                            "Checked-in or checked-out reservations cannot be cancelled.",
                            "Invalid Status", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int choice = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to cancel this reservation?",
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    try {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        updateStatusLabel("Processing cancellation...");

                        boolean success = reservationConnector.cancelReservation(reservationId);

                        if (success) {
                            JOptionPane.showMessageDialog(this,
                                    "Reservation cancelled successfully!",
                                    "Success", JOptionPane.INFORMATION_MESSAGE);

                            // Reload reservations
                            loadReservations();
                            updateStatusLabel("Reservation cancelled");
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Error cancelling reservation.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            updateStatusLabel("Error cancelling reservation");
                        }
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a reservation to cancel.",
                        "No Selection", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        /**
         * Main method to launch the application
         */
        public static void main(String[] args) {
            // Set look and feel to system default
            try {
                // Try to use Nimbus look and feel if available
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // If Nimbus is not available, fall back to system look and feel
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // Create and display the GUI
            SwingUtilities.invokeLater(() -> {
                try {
                    // Show a splash screen (simplified version)
                    JWindow splash = new JWindow();
                    JLabel splashLabel = new JLabel("Loading Hotel Management System...", JLabel.CENTER);
                    splashLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    splashLabel.setForeground(new Color(51, 102, 153));
                    splashLabel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(51, 102, 153), 2),
                            BorderFactory.createEmptyBorder(30, 30, 30, 30)));
                    splash.getContentPane().add(splashLabel);
                    splash.pack();
                    splash.setLocationRelativeTo(null);
                    splash.setVisible(true);

                    // Create the application with a slight delay to show splash
                    Timer timer = new Timer(1500, event -> {
                        splash.dispose();
                        HotelManagementGUI app = new HotelManagementGUI();
                        app.setVisible(true);
                    });
                    timer.setRepeats(false);
                    timer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Error starting application: " + e.getMessage(),
                            "Startup Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }