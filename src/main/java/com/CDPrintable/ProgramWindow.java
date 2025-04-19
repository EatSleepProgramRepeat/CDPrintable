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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;

public class ProgramWindow {
    private final UserAgent userAgent;
    private final JPanel cdSearchPanel = new JPanel();
    private final JLabel searchStatusLabel = new JLabel("Status: Nothing's going on.");
    private static final ArrayList<String> idList = new ArrayList<>();
    private final MusicBrainzLabelGenerator labelGenerator = new MusicBrainzLabelGenerator();

    /**
     * Creates a new ProgramWindow and sets up the GUI.
     */
    public ProgramWindow() {
        String userAgentWebAddress = ConfigManager.getProperty("userAgentWebAddress");
        if (userAgentWebAddress == null) {
            userAgentWebAddress = "https://github.com/EatSleepProgramRepeat/CDPrintable";
        }
        userAgent = new UserAgent("CDPrintable/" + Constants.VERSION, userAgentWebAddress);

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

        // Make the input field
        JTextField searchField = new JTextField(15);

        // Search type combo box setup
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

            setSearchStatus("Performing search...", "blue");
            clearIdList();
            String finalSelectedItem = selectedItem;
            Constants.THREAD_MANAGER.submit(() -> {
                int[] columnsToHighlight = null;
                MusicBrainzJSONReader reader = new MusicBrainzJSONReader(sendRequest(finalSelectedItem.toLowerCase(Locale.ROOT), searchField.getText(), false));
                switch (finalSelectedItem) {
                    case "CDStub" -> {
                        columnsToHighlight = new int[]{0, 1};
                        MusicBrainzCDStub[] cdStubs = reader.getCDStubs();
                        for (MusicBrainzCDStub cdStub : cdStubs) {
                            addId(cdStub.getId());
                        }
                        setSearchStatus("All done!", "green");
                        searchTable.setModel(reader.getCDStubsAsTableModel(cdStubs));
                    }
                    case "Artist" -> {
                        MusicBrainzArtist[] artists = reader.getArtists();
                        for (MusicBrainzArtist artist : artists) {
                            addId(artist.getId());
                        }
                        setSearchStatus("All done!", "green");
                        searchTable.setModel(reader.getArtistsAsTableModel(artists));
                    }
                    case "Release" -> {
                        columnsToHighlight = new int[]{0, 1};
                        MusicBrainzRelease[] releases = reader.getReleases();
                        for (MusicBrainzRelease release : releases) {
                            addId(release.getId());
                        }
                        setSearchStatus("All done!", "green");
                        searchTable.setModel(reader.getReleasesAsTableModel(releases));
                    }
                    default -> JOptionPane.showMessageDialog(panel, "Please select a search type.");
                }

                // Reapply the custom renderer after setting the new model
                if (columnsToHighlight != null) {
                    for (int column : columnsToHighlight) {
                        searchTable.getColumnModel().getColumn(column).setCellRenderer(new LightBlueColumnRenderer());
                    }
                }
                resizeColumnWidths(searchTable);
            });
        });

        // Set up enter key and text box
        searchField.addActionListener(_ -> searchButton.doClick());

        cdSearchPanel.setLayout(new FlowLayout());
        cdSearchPanel.add(searchTypeComboBox);
        cdSearchPanel.add(searchField);
        cdSearchPanel.add(searchButton);

        // Add the CD Search panel to the main panel
        panel.add(cdSearchPanel, BorderLayout.SOUTH);

        // Add a label that informs the user about ClickSearch.
        panel.add(new JLabel("Click a row in a column that is highlighted in light blue to search or add something to the final label."), BorderLayout.NORTH);

        return panel;
    }

    /**
     * Sends a request to the MusicBrainz API and returns the response as a string.
     * @param queryType The type of query (e.g., "artist", "cdstub", "tracks", "release").
     * @param query The query string.
     * @param isTrackList Whether to use the track list URL builder.
     * @return The response from the API as a string.
     */
    private String sendRequest(String queryType, String query, boolean isTrackList) {
        MusicBrainzRequest request = new MusicBrainzRequest(queryType, query);
        String url = isTrackList ? request.buildTrackListURL() : request.buildRequestURL();
        WebRequest webRequest = new WebRequest(url, userAgent);

        try {
            String response = webRequest.sendRequest();
            if (response == null || response.isEmpty()) {
                throw new IOException("Empty or null response from the server.");
            }
            return response;
        } catch (IOException | URISyntaxException e) {
            JOptionPane.showMessageDialog(null, """
                There was a severe problem when sending the web request.
                Please check your internet connection or query and try again.""",
                    "Error", JOptionPane.ERROR_MESSAGE);
            setSearchStatus("Error", "red");
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to get a table model
     * @param model The model to get.
     */
    private DefaultTableModel getTableModel(String model) {
        String[][] data = {{"", "", "", "", "", "", "", "", ""}};
        switch (model) {
            case "CDStub" -> {
                return createTableModel(new String[]{"Disc Name", "Artist", "Track Count"}, data);
            }
            case "Artist" -> {
                return createTableModel(new String[]{"Artist Name", "Date Organised", "Birthdate", "Sort Name", "Gender", "Type", "Disambiguation", "Life Span", "Country"}, data);
            }
            case "Release" -> {
                return createTableModel(new String[]{"Release Name", "Artist", "Track Count", "Date"}, data);
            }
            default -> {
                return createTableModel(new String[]{}, new String[][]{{}});
            }
        }
    }

    /**
     * Little helper method to create table models from Strings
     * @param columnNames The column names.
     * @param data The data for the table.
     * @return A DefaultTableModel with the data and column names.
     */
    private DefaultTableModel createTableModel(String[] columnNames, String[][] data) {
        return new DefaultTableModel(data, columnNames);
    }

    /**
     * Gets a JPanel for settings. This is another helper method.
     * @return A JPanel with the settings window.
     */
    private JPanel settingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // User Agent settings:
        JPanel userAgentPanel = new JPanel(new BorderLayout());
        userAgentPanel.setBorder(BorderFactory.createTitledBorder("User Agent"));

        // Setup user agent text fields, labels, and document listeners
        JLabel userAgentLabel = new JLabel("User Agent (this doesn't save):");
        JTextField userAgentField = new JTextField(15);
        userAgentField.setText(userAgent.getUserAgent());
        userAgentField.addActionListener(_ -> userAgent.setUserAgent(userAgentField.getText(), true));

        // Set up the user agent field with labels and document listener.
        JLabel userAgentWebAddressLabel = new JLabel("User Agent Web Address:");
        JTextField userAgentWebAddressField = new JTextField(15);
        userAgentWebAddressField.setText(userAgent.getUserAgentWebAddress());
        userAgentWebAddressField.addActionListener(_ -> {
            userAgent.setUserAgentWebAddress(userAgentWebAddressField.getText(), true);
            ConfigManager.setProperty("userAgentWebAddress", userAgentWebAddressField.getText());
        });

        // Font settings
        JPanel printerPanel = new JPanel(new GridBagLayout());
        printerPanel.setBorder(BorderFactory.createTitledBorder("Printer Settings (Units are in inches)"));

        JLabel fontLabel = new JLabel("Font:");
        JTextField fontField = new JTextField(30);
        fontField.setText(ConfigManager.getProperty("font"));
        fontField.addActionListener(_ -> {
            ConfigManager.setProperty("font", fontField.getText());
            labelGenerator.setFontName(fontField.getText());
        });

        JLabel fontSizeLabel = new JLabel("Font Size:");
        JTextField fontSizeField = new JTextField(30);
        fontSizeField.setText(ConfigManager.getProperty("fontSize"));
        fontSizeField.addActionListener(_ -> validateAndSetDoubleField(
                fontSizeField,
                labelGenerator::setFontSize,
                "fontSize",
                "Font Size"
        ));

        JLabel paperWidthLabel = new JLabel("Paper Width:");
        JTextField paperWidthField = new JTextField(30);
        paperWidthField.setText(ConfigManager.getProperty("paperWidth"));
        paperWidthField.addActionListener(_ -> validateAndSetDoubleField(
                paperWidthField,
                labelGenerator::setPageWidth,
                "paperWidth",
                "Paper Width"
        ));


        JLabel paperHeightLabel = new JLabel("Paper Height:");
        JTextField paperHeightField = new JTextField(30);
        paperHeightField.setText(ConfigManager.getProperty("paperHeight"));
        paperHeightField.addActionListener(_ -> validateAndSetDoubleField(
                paperHeightField,
                labelGenerator::setPageHeight,
                "paperHeight",
                "Paper Height"
        ));

        JLabel labelWidthLabel = new JLabel("Label Width:");
        JTextField labelWidthField = new JTextField(30);
        labelWidthField.setText(ConfigManager.getProperty("labelWidth"));
        labelWidthField.addActionListener(_ -> validateAndSetDoubleField(
                labelWidthField,
                labelGenerator::setLabelWidth,
                "labelWidth",
                "Label Width"
        ));

        JLabel labelMaxHeightLabel = new JLabel("Label Max Height:");
        JTextField labelMaxHeightField = new JTextField(30);
        labelMaxHeightField.setText(ConfigManager.getProperty("labelMaxHeight"));
        labelMaxHeightField.addActionListener(_ -> validateAndSetDoubleField(
                labelMaxHeightField,
                labelGenerator::setLabelMaxHeight,
                "labelHeight",
                "Label Max Height"
        ));
        
        JPanel userAgentInputPanel = new JPanel(new GridBagLayout());
        JPanel fullAgentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Add all components to their panels
        gbc.gridx = 0;
        gbc.gridy = 0;
        userAgentInputPanel.add(userAgentLabel, gbc);
        printerPanel.add(fontLabel, gbc);
        gbc.gridx = 1;
        userAgentInputPanel.add(userAgentField, gbc);
        printerPanel.add(fontField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        userAgentInputPanel.add(userAgentWebAddressLabel, gbc);
        printerPanel.add(fontSizeLabel, gbc);
        gbc.gridx = 1;
        userAgentInputPanel.add(userAgentWebAddressField, gbc);
        printerPanel.add(fontSizeField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        printerPanel.add(paperWidthLabel, gbc);
        gbc.gridx = 1;
        printerPanel.add(paperWidthField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        printerPanel.add(paperHeightLabel, gbc);
        gbc.gridx = 1;
        printerPanel.add(paperHeightField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        printerPanel.add(labelWidthLabel, gbc);
        gbc.gridx = 1;
        printerPanel.add(labelWidthField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        printerPanel.add(labelMaxHeightLabel, gbc);
        gbc.gridx = 1;
        printerPanel.add(labelMaxHeightField, gbc);

        // Add panels to the UA main panel
        userAgentPanel.add(fullAgentPanel, BorderLayout.NORTH);
        userAgentPanel.add(userAgentInputPanel, BorderLayout.CENTER);
        // Add subpanels to the main panel
        panel.add(new JLabel("<html><font color=\"green\">Press enter to save settings anywhere.</font></html>"), BorderLayout.NORTH);
        panel.add(userAgentPanel, BorderLayout.WEST);
        panel.add(printerPanel, BorderLayout.EAST);

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
        String typeOfTable = table.getColumnName(0);
        if (row < 0 || col < 0) return;
        if (typeOfTable.equals("Artist Name")) {return;}

        if (col == 0 || col == 1) {
            setSearchStatus("Fetching Info...", "blue");
        }

        Constants.THREAD_MANAGER.submit(() -> {
            String response;
            DefaultTableModel model;

            try {
                if (col == 0) {
                    response = sendRequest(typeOfTable.equals("Disc Name") ? "tracks" : "release", idList.get(row), true);
                    MusicBrainzJSONReader reader = new MusicBrainzJSONReader(response);
                    MusicBrainzTrack[] tracks = typeOfTable.equals("Disc Name") ? reader.getTracks() : reader.getReleaseTracks();
                    model = reader.getTracksAsTableModel(tracks);
                    String date = typeOfTable.equals("Release Name") ? table.getValueAt(row, 3).toString() : null;
                    createTrackDialog(table.getValueAt(row, 0).toString(), table.getValueAt(row, 1).toString(),
                            Integer.parseInt(table.getValueAt(row, 2).toString()), date, model, tracks);
                } else if (col == 1) {
                    response = sendRequest("artist", table.getValueAt(row, 1).toString(), false);
                    MusicBrainzJSONReader reader = new MusicBrainzJSONReader(response);
                    model = reader.getArtistsAsTableModel(reader.getArtists());
                    createArtistDialog(model);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "An error occurred while processing the request. More info: " + e, "Error", JOptionPane.ERROR_MESSAGE);
                setSearchStatus("Error", "red");
            }
        });
    }

    /**
     * Helper method to create a track dialog. This is what the user uses to make the final labels
     * from a search.
     * @param title The title of the CD / Release.
     * @param artist The artist of the CD / Release.
     * @param trackCount The number of tracks on the CD / Release.
     * @param date The date of the release. CDStubs typically do not have a date.
     * @param model The table model to use for the track list.
     */
    private void createTrackDialog(String title, String artist, int trackCount, String date, DefaultTableModel model, MusicBrainzTrack[] tracks) {
        // Set up panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Add labels to the panel
        panel.add(new JLabel("Title: " + title));
        panel.add(new JLabel("Artist: " + artist));
        panel.add(new JLabel("Track Count: " + trackCount));
        if (date != null) {
            panel.add(new JLabel("Date: " + date));
        }

        // Add the label panel to the main panel
        mainPanel.add(panel, BorderLayout.NORTH);
        // Add the table to the main panel
        JTable trackTable = new JTable(model);
        JScrollPane trackScrollPane = new JScrollPane(trackTable);
        mainPanel.add(trackScrollPane, BorderLayout.CENTER);
        // Add the bottom (question) label to the main panel
        mainPanel.add(new JLabel("Would you like to add this record to your CD label?"), BorderLayout.SOUTH);
        setSearchStatus("All done!", "green");

        // Show dialog
        int result = JOptionPane.showConfirmDialog(null, mainPanel, "Tracks", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            labelGenerator.addRelease(new MusicBrainzFinalizedRelease(title, artist, tracks));
        } else if (result == JOptionPane.NO_OPTION) {
            System.out.println("she rejected you...");
        }
    }

    private void createArtistDialog(DefaultTableModel model) {
        JScrollPane artistScrollPane = new JScrollPane();
        JTable artistTable = new JTable(model);
        resizeColumnWidths(artistTable);
        artistScrollPane.setViewportView(artistTable);
        JOptionPane pane = new JOptionPane(
                artistScrollPane,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION
        );
        JDialog dialog = pane.createDialog("Artists");
        setSearchStatus("All done!", "green");

        dialog.setPreferredSize(artistTable.getPreferredSize());
        dialog.pack();
        dialog.setVisible(true);
    }

    /**
     * A custom table cell renderer that sets the background color of the cell to light blue.
     * Used for columns that support ClickSearch.
     */
    static class LightBlueColumnRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setBackground(new Color(173, 216, 230)); // Light blue color
            return cell;
        }
    }

    /**
     * Helper method to make sure that an int field is valid.
     * @param field The field.
     * @param setter The method in MusicBrainzLabelGenerator that takes an int.
     * @param configKey the JSON config key.
     * @param fieldName The field name.
     */
    private void validateAndSetDoubleField(JTextField field, Consumer<Double> setter, String configKey, String fieldName) {
        String input = field.getText().trim();

        if (input.isEmpty()) {
            showError(fieldName + " cannot be empty.");
            return;
        }

        try {
            double value = Double.parseDouble(input);
            if (value <= 0) {
                showError(fieldName + " must be a positive number.");
                return;
            }
            setter.accept(value);
            ConfigManager.setDoubleProperty(configKey, value);
        } catch (NumberFormatException e) {
            showError("Please enter a valid whole number for " + fieldName + ".");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

}