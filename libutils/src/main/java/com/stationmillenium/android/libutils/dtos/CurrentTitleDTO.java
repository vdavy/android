/**
 *
 */
package com.stationmillenium.android.libutils.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DTO containing current title
 *
 * @author vincent
 */
public class CurrentTitleDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5018371879251293790L;

    /**
     * Class mapping song attributes
     *
     * @author vincent
     */
    public static class Song implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = -2324748087959128986L;

        /**
         * Image meta data from XML
         *
         * @author vincent
         */
        public static class ImageMetadata implements Serializable {

            /**
             *
             */
            private static final long serialVersionUID = -5751557038331104221L;

            private String path;
            private String width;
            private String height;

            /**
             * @return the path
             */
            public String getPath() {
                return path;
            }

            /**
             * @param path the path to set
             */
            public void setPath(String path) {
                this.path = path;
            }

            /**
             * @return the width
             */
            public String getWidth() {
                return width;
            }

            /**
             * @param width the width to set
             */
            public void setWidth(String width) {
                this.width = width;
            }

            /**
             * @return the height
             */
            public String getHeight() {
                return height;
            }

            /**
             * @param height the height to set
             */
            public void setHeight(String height) {
                this.height = height;
            }

            /* (non-Javadoc)
             * @see java.lang.Object#toString()
             */
            @Override
            public String toString() {
                return "ImageMetadata [path=" + path + ", width=" + width
                        + ", height=" + height + "]";
            }

        }

        private String artist;
        private String title;
        private Date playedDate;
        private String imageURL;
        private ImageMetadata metadata;

        /**
         * @return the artist
         */
        public String getArtist() {
            return artist;
        }

        /**
         * @param artist the artist to set
         */
        public void setArtist(String artist) {
            this.artist = artist;
        }

        /**
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * @param title the title to set
         */
        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * @return the image
         */
        public String getImageURL() {
            return imageURL;
        }

        /**
         * @param imageURL the image to set
         */
        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        /**
         * @return the metadata
         */
        public ImageMetadata getMetadata() {
            return metadata;
        }

        /**
         * @param metadata the metadata to set
         */
        public void setMetadata(ImageMetadata metadata) {
            this.metadata = metadata;
        }

        /**
         * @return the playedDate
         */
        public Date getPlayedDate() {
            return playedDate;
        }

        /**
         * @param playedDate the playedDate to set
         */
        public void setPlayedDate(Date playedDate) {
            this.playedDate = playedDate;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Song [artist=" + artist + ", title=" + title
                    + ", playedDate=" + playedDate + ", imageURL=" + imageURL
                    + ", metadata=" + metadata + "]";
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((artist == null) ? 0 : artist.hashCode());
            result = prime * result + ((title == null) ? 0 : title.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof CurrentTitleDTO)) {
                return false;
            }
            Song other = (Song) obj;
            if (artist == null) {
                if (other.artist != null)
                    return false;
            } else if (!artist.equals(other.artist))
                return false;
            if (title == null) {
                if (other.title != null)
                    return false;
            } else if (!title.equals(other.title))
                return false;
            return true;
        }

    }

    private Song currentSong = new Song();
    private List<Song> history = new ArrayList<>();

    /**
     * @return the currentSong
     */
    public Song getCurrentSong() {
        return currentSong;
    }

    /**
     * @param currentSong the currentSong to set
     */
    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    /**
     * @return the history
     */
    public List<Song> getHistory() {
        return history;
    }

    /**
     * @param history the history to set
     */
    public void setHistory(List<Song> history) {
        this.history = history;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CurrentTitleDTO [currentSong=" + currentSong + ", history="
                + history + "]";
    }

}
