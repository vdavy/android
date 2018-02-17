/**
 *
 */
package com.stationmillenium.android.libutils.xml.abstracts;

import android.util.Xml;

import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO.Song;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO.Song.ImageMetadata;
import com.stationmillenium.android.libutils.exceptions.XMLParserException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * Parser for the XML about current title
 * http://developer.android.com/training/basics/network-ops/xml.html for more details
 *
 * @author vincent
 */
public abstract class AbstractXMLParser<T> {

    //vars
    protected XmlPullParser parser;
    private InputStream is;

    /**
     * Create a new {@link AbstractXMLParser}
     *
     * @param is the {@link InputStream} of the XML to parse
     * @throws XMLParserException if any error occurs
     */
    public AbstractXMLParser(InputStream is) throws XMLParserException {
        try {
            //create the new parser
            Timber.d("Create the XML parser");

            if (is != null) { //check the input stream is not null
                this.is = is;
                parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, "ISO-8859-1");
                parser.nextTag();

            } else { //input stream is null : error thrown
                Timber.w("Input stream is null !");
                throw new XMLParserException("XML parsing exception");
            }

        } catch (XmlPullParserException e) { //process errors
            Timber.w(e, "XML parsing exception");
            throw new XMLParserException("XML parsing exception", e);
        } catch (IOException e) {
            Timber.w(e, "XML IO exception");
            throw new XMLParserException("XML parsing exception", e);

        }
    }

    /**
     * Parse the current XML
     *
     * @return the associated data in a {@link CurrentTitleDTO}
     * @throws XMLParserException if any error occurs
     */
    public abstract T parseXML() throws XMLParserException;

    /**
     * Read the artist tag
     *
     * @param dtoToFillIn the {@link Song}
     * @throws IOException            if any IO error occurs
     * @throws XmlPullParserException if any parsing error occurs
     */
    protected void readArtistTag(Song dtoToFillIn) throws IOException, XmlPullParserException {
        String artist = readTagText("artist");
        if ((artist != null) && (!artist.equals(""))) {
            Timber.d("Artist value : %s", artist);
            dtoToFillIn.setArtist(artist);
        }
    }

    /**
     * Read the title tag
     *
     * @param song the {@link Song}
     * @throws XmlPullParserException if any XML error occurs
     * @throws IOException            if any IO error occurs
     */
    protected void readTitleTag(Song song) throws IOException, XmlPullParserException {
        String title = readTagText("title");
        if ((title != null) && (!title.equals(""))) {
            Timber.d("Title value : %s", title);
            song.setTitle(title);
        }
    }

    /**
     * Parse the meta data from XML of an image
     *
     * @param dtoToFillIn the DTO to fill in with data
     * @throws XmlPullParserException if any XML error occurs
     * @throws IOException            if any IO error occurs
     */
    protected void parseImageMetaData(ImageMetadata dtoToFillIn) throws XmlPullParserException, IOException {
        Timber.d("Parse the image meta data");
        parser.require(XmlPullParser.START_TAG, null, "image"); //root tag
        while (parser.next() != XmlPullParser.END_TAG) { //process until the end
            if (parser.getEventType() != XmlPullParser.START_TAG) { //if not the start tag, continue
                continue;
            }
            Timber.d("Image tag found");

            //process the tag
            String name = parser.getName();
            switch (name) {
                case "path":  //path case
                    String path = readTagText("path");
                    if ((path != null) && (!path.equals(""))) {
                        Timber.d("Path value : %s", path);
                        dtoToFillIn.setPath(path);
                    }

                    break;
                case "width":  //width case
                    String width = readTagText("width");
                    if ((width != null) && (!width.equals(""))) {
                        Timber.d("Width value : %s", width);
                        dtoToFillIn.setWidth(width);
                    }

                    break;
                case "height":  //height case
                    String height = readTagText("height");
                    if ((height != null) && (!height.equals(""))) {
                        Timber.d("Height value : %s", height);
                        dtoToFillIn.setHeight(height);
                    }
                    break;
            }
        }
    }

    /**
     * Read a tag text
     *
     * @param tag the tag to read
     * @return the read text on the tag
     * @throws IOException            if any IO error occurs
     * @throws XmlPullParserException if any XML error occurs
     */
    protected String readTagText(String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, tag);

        Timber.d("Read value for tag '" + tag + "' : " + result);
        return result;
    }

    /**
     * Close the {@link InputStream}
     */
    protected void closeInputStream() {
        try {
            Timber.d("Close input stream");
            if (is != null)
                is.close();
        } catch (IOException e) {
            Timber.w("Error while closing XML input stream");
        }
    }
}
