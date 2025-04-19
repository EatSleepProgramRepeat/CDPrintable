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

import java.awt.*;
import java.awt.print.*;
import java.util.ArrayList;

public class MusicBrainzLabelGenerator implements Printable {
    private ArrayList<MusicBrainzFinalizedRelease> finalizedReleaseList;

    @Override
    public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(Color.BLACK);
        g2d.drawString("CD Labels", 0, 0);

        return PAGE_EXISTS;
    }

    public MusicBrainzLabelGenerator() {
        finalizedReleaseList = new ArrayList<>();
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

        // Assume standard paper size (e.g., 8.5 x 11 inches for Letter)
        double widthInInches = 8.5;
        double heightInInches = 11.0;

        // Calculate DPI
        double dpiX = widthInPoints / widthInInches;
        double dpiY = heightInPoints / heightInInches;

        return new double[]{dpiX, dpiY};
    }
}
