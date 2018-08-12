package com.stationmillenium.android.replay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Soundcloud playlist DTO
 * Created by vincent on 10/04/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaylistDTO implements Serializable {

    private int id;
    private String title;
    private String imageURL;
    private int count;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
