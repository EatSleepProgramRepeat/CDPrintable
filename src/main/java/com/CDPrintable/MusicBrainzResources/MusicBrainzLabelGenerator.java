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

        // Set font and metrics
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics();

        // Determine how many lines a sheet of paper can hold
        double paperMaxLines = Math.floor(((pageHeight * 72) - marginTop - marginBottom) / fontSize);
        System.out.println("paperMaxLines=" + paperMaxLines);

        // Find label max height in lines
        double labelLineMaxHeight = Math.floor((double) LABEL_MAX_HEIGHT / fontSize);
        System.out.println("labelLineMaxHeight=" + labelLineMaxHeight);

        // Make an array of the lines for the releases
        ArrayList<String> lines = new ArrayList<>();
        for (MusicBrainzFinalizedRelease release : finalizedReleaseList) {
            lines.add(release.getTitle());
            lines.add(release.getArtist());
            StringBuilder finalLine = new StringBuilder();
            StringBuilder lineBuilder = new StringBuilder();
            for (MusicBrainzTrack track : release.getTracks()) {
                String line = track.getTrackNumber() + ". " + track.getTitle() + " ";
                if (fontMetrics.stringWidth(line + lineBuilder) > LABEL_WIDTH) {
                    lineBuilder.append("\n");
                    lines.add(lineBuilder.toString());
                    lineBuilder.delete(0, lineBuilder.length());
                }
                lineBuilder.append(line);
            }
            lines.add(lineBuilder.toString());
            lines.add("");
            System.out.println(lines.toString());
        }

        // Get empty lines to find where releases start/end
        ArrayList<Integer> emptyLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).isEmpty()) {
                emptyLines.add(i);
            }
        }

        System.out.println("emptyLines=" + emptyLines);

        int startLine = (int) (pageIndex * paperMaxLines);
        double endLine = Math.min(startLine + paperMaxLines, lines.size());

        if (startLine > lines.size()) {
            return NO_SUCH_PAGE;
        }

        int y = 0;

        // Draw. Every. Dang. Line
        // ALERT! The code below stops you from pulling girls. Wait, that's the whole codebase!
        for (int i = startLine; i < endLine; i++) {
            String line = lines.get(i);
            g2d.drawString(line, 0, y + fontMetrics.getAscent());
            y += fontMetrics.getHeight();
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
        double leftMargin = imageableX;
        double rightMargin = paperWidth - (imageableX + imageableWidth);
        double topMargin = imageableY;
        double bottomMargin = paperHeight - (imageableY + imageableHeight);

        return new double[]{leftMargin, rightMargin, topMargin, bottomMargin};
    }

    public void displayPageAsImage() {
        try {
            // Create a PrinterJob and PageFormat
            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat pageFormat = job.defaultPage();

            // Define the image dimensions based on the paper size
            Paper paper = pageFormat.getPaper();
            int width = (int) paper.getWidth();
            int height = (int) paper.getHeight();

            // Create a BufferedImage
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            // Set up the Graphics2D object
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // Render the page content
            this.print(g2d, pageFormat, 0);
            g2d.dispose();

            // Create an ImageIcon from the BufferedImage
            ImageIcon pageIcon = new ImageIcon(image);

            // Display the image in a JOptionPane
            JOptionPane.showMessageDialog(null, new JLabel(pageIcon), "Page Preview", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while generating the page image.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}