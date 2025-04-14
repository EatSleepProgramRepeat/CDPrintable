/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This class creates the main window for the program.
 */

package com.CDPrintable;

import com.CDPrintable.MusicBrainzResources.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ProgramWindow {
    private final UserAgent userAgent;
    private JLabel fullUserAgentLabel = new JLabel();
    private final JPanel cdSearchPanel = new JPanel();
    private final JLabel searchStatusLabel = new JLabel("Status: Nothing's going on.");
    private static final ArrayList<String> idList = new ArrayList<>();

    /**
     * Creates a new ProgramWindow and sets up the GUI.
     */
    public ProgramWindow() {
        userAgent = new UserAgent("CDPrintable/" + Constants.VERSION, "example@example.com");

        JFrame frame = new JFrame("CD Printable v"+Constants.VERSION);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel tablePanel = tablePanel();
        JPanel findCDPanel = searchPanel();
        JPanel settingsPanel = settingsPanel();

        tabbedPane.addTab("Search", findCDPanel);
        tabbedPane.addTab("Table", tablePanel);
        tabbedPane.addTab("Settings", settingsPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);

        // Set the frame to be visible
        frame.setVisible(true);
    }

    /**
     * Gets a JPanel for the table panel. This is a helper method.
     * @return A JPanel with the table panel.
     */
    private JPanel tablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Set up all the tables for the cd
        String[] columnNames = {"CD Name", "Artist", "Genre", "Year", "Track Count"};
        JTable table = new JTable(new String[][] {new String[] {"None", "", "", "", ""}}, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);

        // Set up the panel
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Gets a JPanel for the search panel. This is a helper method.
     * @return A JPanel with the search panel.
     */
    private JPanel searchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Track List panel set-up
        JPanel trackListPanel = new JPanel(new BorderLayout());
        trackListPanel.setBorder(BorderFactory.createTitledBorder("Search Results"));

        // Search table set up
        JTable searchTable = new JTable(getTableModel("CDStub"));
        searchTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = searchTable.rowAtPoint(e.getPoint());
                int column = searchTable.columnAtPoint(e.getPoint());
                if (row != -1 && column != -1) {
                    clickSearch(row, column, searchTable);
                }
            }
        });
        JScrollPane trackListScrollPane = new JScrollPane(searchTable);
        trackListPanel.add(searchStatusLabel, BorderLayout.NORTH);
        trackListPanel.add(trackListScrollPane, BorderLayout.CENTER);

        // Add the Track List panel to the main panel
        panel.add(trackListPanel, BorderLayout.CENTER);

        // CD Search Panel set up
        cdSearchPanel.setBorder(BorderFactory.createTitledBorder("Search"));

        JTextField searchField = new JTextField(15);

        JComboBox<String> searchTypeComboBox = new JComboBox<>(new String[] {"CDStub", "Artist", "Release"});

        // Search button and event listener setup
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(_ -> {
            if (searchTypeComboBox.getSelectedItem() == null) {
                return;
            }

            String selectedItem = searchTypeComboBox.getSelectedItem().toString();
            if (selectedItem != null) {
                searchTable.setModel(getTableModel(selectedItem));
            } else {
                selectedItem = "";
            }

            setSearchStatus("Preforming search...", "blue");
            MusicBrainzJSONReader reader = sendRequest(selectedItem.toLowerCase(Locale.ROOT), searchField.getText());
            switch (selectedItem) {
                case "CDStub" -> Constants.THREAD_MANAGER.submit(() -> {
                    // Get CDStubs and set the table model
                    MusicBrainzCDStub[] cdStubs = reader.getCDStubs();
                    for (MusicBrainzCDStub cdStub : cdStubs) {
                        addId(cdStub.getId());
                    }
                    searchTable.setModel(reader.getCDStubsAsTableModel(cdStubs));
                    setSearchStatus("All done!", "green");
                });
                case "Artist" -> Constants.THREAD_MANAGER.submit(() -> {
                    // Get Artists and set the table model
                    MusicBrainzArtist[] artists = reader.getArtists();
                    for (MusicBrainzArtist artist : artists) {
                        addId(artist.getId());
                    }
                    searchTable.setModel(reader.getArtistsAsTableModel(artists));
                    setSearchStatus("All done!", "green");
                });
                case "Release" -> Constants.THREAD_MANAGER.submit(() -> {
                    // Get Releases and set the table model
                    MusicBrainzRelease[] releases = reader.getReleases();
                    for (MusicBrainzRelease release : releases) {
                        addId(release.getId());
                    }
                    searchTable.setModel(reader.getReleasesAsTableModel(releases));
                    setSearchStatus("All done!", "green");
                });
                default -> JOptionPane.showMessageDialog(panel, "Please select a search type.");
            }
            resizeColumnWidths(searchTable);
        });

        cdSearchPanel.setLayout(new FlowLayout());
        cdSearchPanel.add(searchTypeComboBox);
        cdSearchPanel.add(searchField);
        cdSearchPanel.add(searchButton);

        // Add the CD Search panel to the main panel
        panel.add(cdSearchPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Sends a request to the MusicBrainz API.
     * @param queryType The query type to send. E.g. "cdstub", "release", etc.
     * @param query The query to send.
     * @return a MusicBrainzJSONReader object with the response JSON already in it.
     */
    private MusicBrainzJSONReader sendRequest(String queryType, String query) {
        MusicBrainzRequest request = new MusicBrainzRequest(queryType, query);
        WebRequest webRequest = new WebRequest(request, userAgent);

        String response = null;
        try {
            response = webRequest.sendRequest();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, """
                    There was a fatal error when sending the request. Please try again or submit an issue on GitHub.
                    Here are some things to try:
                    • Check your internet connection.
                    • Remove any special characters from your query.""", "CDPrintable Severe Error", JOptionPane.ERROR_MESSAGE);
        }

        return new MusicBrainzJSONReader(Objects.requireNonNullElse(response, ""));

    }

    /**
     * Helper method to get a table model
     * @param model The model to get.
     */
    private DefaultTableModel getTableModel(String model) {
        switch (model) {
            case "CDStub" -> {
                String[] columnNames = {"Disc Name", "Artist", "Track Count", ""};
                String[][] data = {{"", "", "", ""}};
                return new DefaultTableModel(data, columnNames);
            }
            case "Artist" -> {
                String[] columnNames = {"Name", "Date Organised", "Birthdate", "Sort Name", "Gender", "Type", "Disambiguation", "Life Span", "Country", ""};
                String[][] data = {{"", "", "", "", "", "", "", ""}};
                return new DefaultTableModel(data, columnNames);
            }
            case "Release" -> {
                String[] columnNames = {"Release Name", "Artist", "Track Count", "Date", ""};
                String[][] data = {{"", "", "", ""}};
                return new DefaultTableModel(data, columnNames);
            }
            default -> {
                return new DefaultTableModel(new String [][] {{}}, new String[] {});
            }
        }
    }

    /**
     * Gets a JPanel for settings. This is another helper method.
     * @return A JPanel with the settings window.
     */
    private JPanel settingsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // User Agent settings:
        JPanel userAgentPanel = new JPanel(new BorderLayout());
        userAgentPanel.setBorder(BorderFactory.createTitledBorder("User Agent"));

        // Setup user agent text fields, labels, and document listeners
        JLabel userAgentLabel = new JLabel("User Agent:");
        JTextField userAgentField = new JTextField(15);
        userAgentField.setText(userAgent.getUserAgent());
        userAgentField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                userAgent.setUserAgent(userAgentField.getText(), fullUserAgentLabel);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                userAgent.setUserAgent(userAgentField.getText(), fullUserAgentLabel);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}   // Not used
        });

        // Set up the user agent field with labels and document listener.
        JLabel userAgentEmailLabel = new JLabel("User Agent Email:");
        JTextField userAgentEmailField = new JTextField(15);
        userAgentEmailField.setText(userAgent.getUserAgentEmail());
        userAgentEmailField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                userAgent.setUserAgentEmail(userAgentEmailField.getText(), fullUserAgentLabel);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                userAgent.setUserAgentEmail(userAgentEmailField.getText(), fullUserAgentLabel);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}   // Not used
        });

        fullUserAgentLabel = new JLabel(userAgent.toString());

        // Font settings
        JPanel fontPanel = new JPanel(new GridBagLayout());
        fontPanel.setBorder(BorderFactory.createTitledBorder("Font"));

        JLabel fontLabel = new JLabel("Font:");
        JTextField fontField = new JTextField(30);

        JLabel fontSizeLabel = new JLabel("Font Size:");
        JTextField fontSizeField = new JTextField(30);
        
        JPanel userAgentInputPanel = new JPanel(new GridBagLayout());
        JPanel fullAgentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Add all components to their panels
        gbc.gridx = 0;
        gbc.gridy = 0;
        userAgentInputPanel.add(userAgentLabel, gbc);
        fontPanel.add(fontLabel, gbc);

        gbc.gridx = 1;
        userAgentInputPanel.add(userAgentField, gbc);
        fontPanel.add(fontField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        userAgentInputPanel.add(userAgentEmailLabel, gbc);
        fontPanel.add(fontSizeLabel, gbc);

        gbc.gridx = 1;
        userAgentInputPanel.add(userAgentEmailField, gbc);
        fontPanel.add(fontSizeField, gbc);

        userAgentInputPanel.add(fullUserAgentLabel, gbc);
        fullAgentPanel.add(fullUserAgentLabel);

        // Add panels to the UA main panel
        userAgentPanel.add(fullAgentPanel, BorderLayout.NORTH);
        userAgentPanel.add(userAgentInputPanel, BorderLayout.CENTER);

        // Add subpanels to the main panel
        panel.add(userAgentPanel);
        panel.add(fontPanel);

        return panel;
    }

    /**
     * Helper method to resize a table column to fit the largest element.
     * @param table The table to resize.
     */
    private void resizeColumnWidths(JTable table) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = table.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(table, tableColumn.getHeaderValue(), false, false, -1, column)
                    .getPreferredSize().width;

            for (int row = 0; row < table.getRowCount(); row++) {
                Component cellRenderer = table.getCellRenderer(row, column)
                        .getTableCellRendererComponent(table, table.getValueAt(row, column), false, false, row, column);
                preferredWidth = Math.max(preferredWidth, cellRenderer.getPreferredSize().width);
            }

            tableColumn.setPreferredWidth(preferredWidth + 2); // Add padding
        }
    }

    /**
     * Sets the search status label.
     * @param status The status to set.
     * @param color The color to set.
     */
    public void setSearchStatus(String status, String color) {
        searchStatusLabel.setText("<html><font color='" + color + "'>Status: " + status + "</font></html>");
    }

    /**
     * Adds an ID to the list of IDs.
     * @param id The ID to add.
     */
    public static void addId(String id) {
        idList.add(id);
    }

    /**
     * Clears the list of IDs.
     */
    public static void clearIdList() {
        idList.clear();
    }

    /**
     * Helper method to search specific data (Artists, Releases, etc.) in the search table.
     * @param row The row where the object is located.
     * @param col The column where the object is located.
     */
    private void clickSearch(int row, int col, JTable table) {

    }
}