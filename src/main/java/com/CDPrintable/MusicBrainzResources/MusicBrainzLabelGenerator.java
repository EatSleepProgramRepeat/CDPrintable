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

import java.util.ArrayList;

public class MusicBrainzLabelGenerator {
    private ArrayList<MusicBrainzFinalizedRelease> finalizedReleaseList;

    public MusicBrainzLabelGenerator() {
        finalizedReleaseList = new ArrayList<>();
    }

    public void addRelease(MusicBrainzFinalizedRelease release) {
        finalizedReleaseList.add(release);
    }
}
