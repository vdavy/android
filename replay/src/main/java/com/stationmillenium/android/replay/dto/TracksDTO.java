package com.stationmillenium.android.replay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * Soundcloud track DTO
 * Created by vincent on 28/08/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TracksDTO implements Serializable {

    private int id;
    private int duration;
    private String title;
    private String description;
    @JsonProperty("last_modified")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss ZZZZZ")
    private Date lastModified;
    @JsonProperty("tag_list")
    private String tagList;
    private String genre;
    @JsonProperty("stream_url")
    private String streamURL;
    @JsonProperty("waveform_url")
    private String waveformURL;
    @JsonProperty("artwork_url")
    private String artworkURL;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getTagList() {
        return tagList;
    }

    public void setTagList(String tagList) {
        this.tagList = tagList;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getStreamURL() {
        return streamURL;
    }

    public void setStreamURL(String streamURL) {
        this.streamURL = streamURL;
    }

    public String getWaveformURL() {
        return waveformURL;
    }

    public void setWaveformURL(String waveformURL) {
        this.waveformURL = waveformURL;
    }

    public String getArtworkURL() {
        return artworkURL;
    }

    public void setArtworkURL(String artworkURL) {
        this.artworkURL = artworkURL;
    }
}
