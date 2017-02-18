package com.stationmillenium.android.libutils.dtos;

import java.io.Serializable;

/**
 * A tweet item DTO
 *
 * @author vincent
 */
public class TweetItem implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8684823554663528375L;
    private String tweetText;
    private String tweetURL;

    /**
     * Create a {@link TweetItem}
     *
     * @param tweetText the tweet text
     * @param tweetURL  the tweet associated URL
     */
    public TweetItem(String tweetText, String tweetURL) {
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