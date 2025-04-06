package com.CDPrintable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class ProgramWindow {
    private final UserAgent userAgent;
    private JLabel fullUserAgentLabel = new JLabel();

    ProgramWindow() {
        userAgent = new UserAgent("CDPrintable/" + Constants.VERSION, "example@example.com");

        JFrame frame = new JFrame("CD Printable");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel tablePanel = tablePanel();
        JPanel findCDPanel = findCDPanel();
        JPanel settingsPanel = settingsPanel();

        tabbedPane.addTab("Table", tablePanel);
        tabbedPane.addTab("Find CD", findCDPanel);
        tabbedPane.addTab("Settings", settingsPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);

        // Set the frame to be visible
        frame.setVisible(true);
    }
    public JPanel tablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Set up all the tables for the cd
        String[] columnNames = {"CD Name", "Artist", "Genre", "Year", "Track Count"};
        JTable table = new JTable(new String[][] {new String[] {"None", "", "", "", ""}}, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);

        // Set up the panel
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    public JPanel findCDPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Track List panel set up
        JPanel trackListPanel = new JPanel(new BorderLayout());
        trackListPanel.setBorder(BorderFactory.createTitledBorder("Track List"));

        // Track List table set up
        String[] columnNames = {"Track Number", "Track Name", "Track Length"};
        JTable trackListTable = new JTable(new String[][] {new String[] {"None", "", ""}}, columnNames);
        JScrollPane trackListScrollPane = new JScrollPane(trackListTable);
        trackListPanel.add(trackListScrollPane, BorderLayout.CENTER);

        // Add the Track List panel to the main panel
        panel.add(trackListPanel, BorderLayout.NORTH);

        // CD Search Panel set up
        JPanel cdSearchPanel = new JPanel();
        cdSearchPanel.setBorder(BorderFactory.createTitledBorder("Search for CD"));
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        cdSearchPanel.setLayout(new FlowLayout());
        cdSearchPanel.add(searchField);
        cdSearchPanel.add(searchButton);

        // Add the CD Search panel to the main panel
        panel.add(cdSearchPanel, BorderLayout.SOUTH);

        return panel;
    }
    public JPanel settingsPanel() {
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