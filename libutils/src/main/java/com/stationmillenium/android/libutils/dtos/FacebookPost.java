package com.stationmillenium.android.libutils.dtos;

import java.io.Serializable;

/**
 * A facebook item DTO
 *
 * @author vincent
 */
public class FacebookPost implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8684823554663528375L;
    private String postText;
    private String postURL;

    /**
     * Create a {@link FacebookPost}
     *
     * @param postText the post text
     * @param postURL  the post associated URL
     */
    public FacebookPost(String postText, String postURL) {
        super();
        this.postText = postText;
        this.postURL = postURL;
    }

    /**
     * @return the post URL
     */
    public String getPostURL() {
        return postURL;
    }

    @Override
    public String toString() {
        return postText;
    }

    public String getPostText() {
        return postText;
    }

}