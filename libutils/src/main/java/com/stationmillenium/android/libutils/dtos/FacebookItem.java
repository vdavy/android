package com.stationmillenium.android.libutils.dtos;

import java.io.Serializable;

/**
 * A facebook item DTO
 *
 * @author vincent
 */
public class FacebookItem implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8684823554663528375L;
    private String tweetText;
    private String tweetURL;

    /**
     * Create a {@link FacebookItem}
     *
     * @param tweetText the tweet text
     * @param tweetURL  the tweet associated URL
     */
    public FacebookItem(String tweetText, String tweetURL) {
        super();
        this.tweetText = tweetText;
        this.tweetURL = tweetURL;
    }

    /**
     * @return the tweetURL
     */
    public String getTweetURL() {
        return tweetURL;
    }

    @Override
    public String toString() {
        return tweetText;
    }

    public String getTweetText() {
        return tweetText;
    }

}