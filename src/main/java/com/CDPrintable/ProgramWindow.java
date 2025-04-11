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

import com.CDPrintable.MusicBrainzResources.MusicBrainzCDStub;
import com.CDPrintable.MusicBrainzResources.MusicBrainzJSONReader;
import com.CDPrintable.MusicBrainzResources.MusicBrainzRelease;
import com.CDPrintable.MusicBrainzResources.MusicBrainzRequest;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProgramWindow {
    private final UserAgent userAgent;
    private JLabel fullUserAgentLabel = new JLabel();

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
    private JPanel searchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Track List panel set up
        JPanel trackListPanel = new JPanel(new BorderLayout());
        trackListPanel.setBorder(BorderFactory.createTitledBorder("Search Results"));

        // Search table set up
        JTable searchTable = new JTable(getCDStubModel());
        JScrollPane trackListScrollPane = new JScrollPane(searchTable);
        trackListPanel.add(trackListScrollPane, BorderLayout.CENTER);

        // Add the Track List panel to the main panel
        panel.add(trackListPanel, BorderLayout.CENTER);

        // CD Search Panel set up
        JPanel cdSearchPanel = new JPanel();
        cdSearchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        JTextField searchField = new JTextField(15);
        JComboBox<String> searchTypeComboBox = new JComboBox<>(new String[] {"CDStub", "Artist", "Release"});
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            if (searchTypeComboBox.getSelectedItem() == null) {
                return;
            }
            if (searchTypeComboBox.getSelectedItem().equals("CDStub")) {
                searchTable.setModel(getCDStubModel());
                MusicBrainzJSONReader reader = sendRequest("cdstub", searchField.getText());


                MusicBrainzCDStub[] cdStubs = reader.getCDStubs();
                searchTable.setModel(reader.getCDStubsAsTableModel(cdStubs));
            } else if (searchTypeComboBox.getSelectedItem().equals("Artist")) {
                searchTable.setModel(getArtistModel());
            } else if (searchTypeComboBox.getSelectedItem().equals("Release")) {
                searchTable.setModel(getReleaseModel());
                MusicBrainzJSONReader reader = sendRequest("release", searchField.getText());

                MusicBrainzRelease[] releases = reader.getReleases();
                searchTable.setModel(reader.getReleasesAsTableModel(releases));
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a search type.");
            }
        });
        cdSearchPanel.setLayout(new FlowLayout());
        cdSearchPanel.add(searchTypeComboBox);
        cdSearchPanel.add(searchField);
        cdSearchPanel.add(searchButton);

        // Add the CD Search panel to the main panel
        panel.add(cdSearchPanel, BorderLayout.SOUTH);

        return panel;
    }

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
                    • Remove any special characters from your query.""");
        }

        MusicBrainzJSONReader reader = new MusicBrainzJSONReader(response);
        return reader;
    }

    private DefaultTableModel getCDStubModel() {
        String[] columnNames = {"Disc Name", "Artist", "Track Count", ""};
        String[][] data = {{"", "", "", ""}};
        return new javax.swing.table.DefaultTableModel(data, columnNames);
    }

    private DefaultTableModel getArtistModel() {
        String[] columnNames = {"Artist Name", "Date Organised", ""};
        String[][] data = {{"", "", ""}};
        return new javax.swing.table.DefaultTableModel(data, columnNames);
    }

    private DefaultTableModel getReleaseModel() {
        String[] columnNames = {"Release Name", "Artist", "Track Count", "Date", ""};
        String[][] data = {{"", "", "", ""}};
        return new javax.swing.table.DefaultTableModel(data, columnNames);
    }

    private JPanel settingsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // User Agent settings:
        JPanel userAgentPanel = new JPanel(new BorderLayout());
        userAgentPanel.setBorder(BorderFactory.createTitledBorder("User Agent"));

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

        // Add sub panels to main panel
        panel.add(userAgentPanel);
        panel.add(fontPanel);

        return panel;
    }
}