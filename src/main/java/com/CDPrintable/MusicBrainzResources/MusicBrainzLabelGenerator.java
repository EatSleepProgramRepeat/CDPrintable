/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This class renders the final labels for the CD cases.
 */

package com.CDPrintable.MusicBrainzResources;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.util.ArrayList;

public class MusicBrainzLabelGenerator implements Printable {
    private final ArrayList<MusicBrainzFinalizedRelease> finalizedReleaseList;
    public int LABEL_WIDTH;
    public int LABEL_MAX_HEIGHT;
    public int dpiX;
    public int dpiY;
    public int marginTop;
    public int marginBottom;
    public int marginLeft;
    public int marginRight;
    private int fontSize = 10;
    public double pageWidth = 8.5;
    public double pageHeight = 11;

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public MusicBrainzLabelGenerator() {
        double[] dpi = getDPI();
        this.dpiX = (int) dpi[0];
        this.dpiY = (int) dpi[1];

        this.LABEL_WIDTH = 4 * dpiX; // Example: 1 inch width
        this.LABEL_MAX_HEIGHT = 2 * dpiY; // Example: 1 inch height

        finalizedReleaseList = new ArrayList<>();
        System.out.println("DPI: dpiX=" + dpiX + ", dpiY=" + dpiY);
        System.out.println("Label dimensions: " + LABEL_WIDTH + "x" + LABEL_MAX_HEIGHT);

        double[] margins = getMargins();
        this.marginTop = (int) margins[0];
        this.marginBottom = (int) margins[1];
        this.marginLeft = (int) margins[2];
        this.marginRight = (int) margins[3];
        System.out.println("Margins: " + margins[0] + "x" + margins[1] + "x" + margins[2] + "x" + margins[3]);
    }

    public int getLabelWidth() {
        return LABEL_WIDTH;
    }

    public void setLabelWidth(int labelWidth) {
        LABEL_WIDTH = labelWidth * dpiX;
    }

    public int getLabelMaxHeight() {
        return LABEL_MAX_HEIGHT;
    }

    public void setLabelMaxHeight(int labelMaxHeight) {
        LABEL_MAX_HEIGHT = labelMaxHeight * dpiY;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setColor(Color.BLACK);

        Font font = new Font("Arial", Font.PLAIN, fontSize);
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics();

        double lineHeight = fontMetrics.getHeight();
        double maxLinesPerPage = Math.floor(((pageHeight * 72) - marginTop - marginBottom) / lineHeight);

        ArrayList<ArrayList<String>> releasesAsLines = new ArrayList<>();

        for (MusicBrainzFinalizedRelease release : finalizedReleaseList) {
            ArrayList<String> releaseLines = new ArrayList<>();
            releaseLines.add(release.getTitle());
            releaseLines.add(release.getArtist());

            StringBuilder lineBuilder = new StringBuilder();
            for (MusicBrainzTrack track : release.getTracks()) {
                String line = track.getTrackNumber() + ". " + track.getTitle() + " ";
                // Split stuff up IF the line gets too long
                if (fontMetrics.stringWidth(lineBuilder + line) > LABEL_WIDTH) {
                    releaseLines.add(lineBuilder.toString());
                    lineBuilder.delete(0, lineBuilder.length());
                }
                if (releaseLines.size() * fontMetrics.getHeight() >= LABEL_MAX_HEIGHT) {break;}
                lineBuilder.append(line);
            }
            if (!lineBuilder.isEmpty()) {
                releaseLines.add(lineBuilder.toString());
            }

            releaseLines.add(""); // spacer line
            releasesAsLines.add(releaseLines);
        }

        // Group releases into pages
        ArrayList<ArrayList<ArrayList<String>>> pages = new ArrayList<>();
        ArrayList<ArrayList<String>> currentPage = new ArrayList<>();
        int currentLineCount = 0;

        for (ArrayList<String> releaseLines : releasesAsLines) {
            if (currentLineCount + releaseLines.size() > maxLinesPerPage && !currentPage.isEmpty()) {
                pages.add(currentPage);
                currentPage = new ArrayList<>();
                currentLineCount = 0;
            }
            currentPage.add(releaseLines);
            currentLineCount += releaseLines.size();
        }

        if (!currentPage.isEmpty()) {
            pages.add(currentPage);
        }

        // Handle page out of bounds
        if (pageIndex >= pages.size()) {
            return NO_SUCH_PAGE;
        }

        // Draw the releases for this page
        int y = 0;
        for (ArrayList<String> releaseLines : pages.get(pageIndex)) {
            for (String line : releaseLines) {
                g2d.drawString(line, 0, y + fontMetrics.getAscent());
                y += fontMetrics.getHeight();
            }
        }

        return PAGE_EXISTS;
    }


    public void addRelease(MusicBrainzFinalizedRelease release) {
        finalizedReleaseList.add(release);
    }

    public void printLabel() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }

    private double[] getDPI() {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pageFormat = job.defaultPage();
        Paper paper = pageFormat.getPaper();

        // Get the width and height of the paper in points (1 point = 1/72 inch)
        double widthInPoints = paper.getWidth();
        double heightInPoints = paper.getHeight();

        System.out.println("Paper size: " + widthInPoints + "x" + heightInPoints);

        // Dynamically calculate the paper size in inches based on the imageable area
        double widthInInches = widthInPoints / 72;
        double heightInInches = heightInPoints / 72;

        // Calculate DPI
        double dpiX = widthInPoints / widthInInches;
        double dpiY = heightInPoints / heightInInches;

        return new double[]{dpiX, dpiY};
    }

    private double[] getMargins() {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pageFormat = job.defaultPage();
        Paper paper = pageFormat.getPaper();

        // Get paper dimensions
        double paperWidth = paper.getWidth();
        double paperHeight = paper.getHeight();

        // Get imageable area dimensions
        double imageableX = paper.getImageableX();
        double imageableY = paper.getImageableY();
        double imageableWidth = paper.getImageableWidth();
        double imageableHeight = paper.getImageableHeight();

        // Calculate margins
        double rightMargin = paperWidth - (imageableX + imageableWidth);
        double bottomMargin = paperHeight - (imageableY + imageableHeight);

        return new double[]{imageableX, rightMargin, imageableY, bottomMargin};
    }

    public void displayPagesAsImages() {
        try {
            // Create PrinterJob and PageFormat
            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat pageFormat = job.defaultPage();
            Paper paper = pageFormat.getPaper();

            // Define image dimensions based on paper size
            int width = (int) paper.getWidth();
            int height = (int) paper.getHeight();

            // Go through each page index until NO_SUCH_PAGE is returned
            int pageIndex = 0;
            while (true) {
                // Create BufferedImage and draw page content
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = image.createGraphics();

                // White background
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, width, height);

                int result = this.print(g2d, pageFormat, pageIndex);
                g2d.dispose();

                // If there's no such page, break out of the loop
                if (result != Printable.PAGE_EXISTS) {
                    break;
                }

                // Show the image in a JOptionPane
                ImageIcon icon = new ImageIcon(image);
                JOptionPane.showMessageDialog(
                        null,
                        new JLabel(icon),
                        "Page Preview - Page " + (pageIndex + 1),
                        JOptionPane.PLAIN_MESSAGE
                );

                pageIndex++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while generating page images.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}