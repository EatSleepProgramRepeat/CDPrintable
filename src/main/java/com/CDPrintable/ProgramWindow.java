package com.CDPrintable;

import javax.swing.*;
import java.awt.*;

public class ProgramWindow {
    ProgramWindow() {
        JFrame frame = new JFrame("CD Printable");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel tablePanel = tablePanel();
        JPanel findCDPanel = findCDPanel();

        tabbedPane.addTab("Table", tablePanel);
        tabbedPane.addTab("Find CD", findCDPanel);

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
}
