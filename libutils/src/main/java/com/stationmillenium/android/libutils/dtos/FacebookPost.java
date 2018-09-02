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
    private String text;
    private String url;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}