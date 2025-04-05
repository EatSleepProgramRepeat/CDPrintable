package com.CDPrintable;

public class UserAgent {
    private String userAgent;
    private String userAgentEmail;

    /**
     * Constructor for UserAgent.
     * @param userAgent The user agent string.
     * @param userAgentEmail The user agent email.
     */
    public UserAgent(String userAgent, String userAgentEmail) {
        this.userAgent = userAgent;
        this.userAgentEmail = userAgentEmail;
    }

    /**
     * Gets the full user agent string. This is the user agent string with the user agent email appended to it.
     * @return The full user agent string.
     */
    public String getFullUserAgent() {
        return userAgent + " (" + userAgentEmail + ")";
    }

    /**
     * Gets the user agent string. This is the string towards the beginning of the full user agent (e.g. CDPrintable/1.0.0).
     * @return The user agent string.
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets the user agent email.
     * @return The user agent email.
     */
    public String getUserAgentEmail() {
        return userAgentEmail;
    }

    /**
     * Sets the user agent string.
     * @param userAgent The user agent string to set.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Sets the user agent email.
     * @param userAgentEmail The user agent email to set.
     */
    public void setUserAgentEmail(String userAgentEmail) {
        this.userAgentEmail = userAgentEmail;
    }
}
