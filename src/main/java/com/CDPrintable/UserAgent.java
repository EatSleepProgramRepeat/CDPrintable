/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This class store User Agent information.
 */

package com.CDPrintable;

import javax.swing.JLabel;

public class UserAgent {
    private String userAgent;
    private String userAgentWebAddress;

    /**
     * Constructor for UserAgent.
     * @param userAgent The user agent string.
     * @param userAgentEmail The user agent email.
     */
    public UserAgent(String userAgent, String userAgentEmail) {
        this.userAgent = userAgent;
        this.userAgentWebAddress = userAgentEmail;
    }

    /**
     * Gets the full user agent string. This is the user agent string with the user agent email appended to it.
     * @return The full user agent string.
     */
    @Override
    public String toString() {
        return userAgent + " (" + userAgentWebAddress + ")";
    }

    /**
     * Gets the user agent string. This is the string towards the beginning of the full user agent (e.g. CDPrintable/1.0.0).
     * @return The user agent string.
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the user agent web link
     * @return The user agent web link.
     */
    public String getUserAgentWebAddress() {
        return userAgentWebAddress;
    }

    /**
     * Sets the user agent string.
     * @param userAgent The user agent string to set.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Sets the user agent string and updates the full user agent label.
     * @param userAgent The user agent string to set.
     * @param fullUserAgentLabel The label to update.
     */
    public void setUserAgent(String userAgent, JLabel fullUserAgentLabel) {
        this.userAgent = userAgent;
        fullUserAgentLabel.setText(toString());
    }

    /**
     * Sets the user agent web address and does some tomfoolery IDK man.
     * @param address The user agent web address to set.
     */
    public void setUserAgentWebAddress(String address, JLabel fullUserAgentLabel) {
        this.userAgentWebAddress = address;
        fullUserAgentLabel.setText(toString());
    }
}
