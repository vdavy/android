package com.stationmillenium.android.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class CurrentTrack implements Serializable {

    private String time;
    @JsonProperty("is_track")
    private boolean isTrack;
    @JsonProperty("is_image")
    private boolean isImage;
    private String artist;
    private String title;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isTrack() {
        return isTrack;
    }

    public void setTrack(boolean track) {
        isTrack = track;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
