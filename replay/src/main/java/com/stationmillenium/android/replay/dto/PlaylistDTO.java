package com.stationmillenium.android.replay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Soundcloud playlist DTO
 * Created by vincent on 10/04/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaylistDTO implements Serializable {

    private int id;
    private String title;
    @JsonProperty("imageURL")
    private String artworkURL;
    @JsonProperty("count")
    private int trackCount;

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

    public String getArtworkURL() {
        return artworkURL;
    }

    public void setArtworkURL(String artworkURL) {
        this.artworkURL = artworkURL;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }
}
