package com.stationmillenium.android.libutils.xml;

import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO.Song;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO.Song.ImageMetadata;
import com.stationmillenium.android.libutils.exceptions.XMLParserException;
import com.stationmillenium.android.libutils.xml.abstracts.AbstractXMLParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * Parser for the XML about current title
 *
 * @author vincent
 *         http://developer.android.com/training/basics/network-ops/xml.html for more details
 */
public class XMLCurrentTitleParser extends AbstractXMLParser<CurrentTitleDTO> {

    /**
     * Create a new {@link XMLCurrentTitleParser}
     *
     * @param is the {@link InputStream} of the XML to parse
     * @throws XMLParserException if any error occurs
     */
    public XMLCurrentTitleParser(InputStream is) throws XMLParserException {
        super(is);
    }

    /**
     * Parse the current XML
     *
     * @return the associated data in a {@link CurrentTitleDTO}
     * @throws XMLParserException if any error occurs
     */
    public CurrentTitleDTO parseXML() throws XMLParserException {
        CurrentTitleDTO currentTitleDTO = new CurrentTitleDTO();

        try { //parse the start tag : androidCurrentSongs
            parser.require(XmlPullParser.START_TAG, null, "androidCurrentSongs");
            while (parser.next() != XmlPullParser.END_TAG) { //process until the end
                if (parser.getEventType() != XmlPullParser.START_TAG) //if not the start tag, continue
                    continue;

                //process the tag
                String name = parser.getName();
                if (name.equals("currentSong"))
                    parseCurrentSong(currentTitleDTO);
                else if (name.equals("last5Songs"))
                    parseLast5Songs(currentTitleDTO);
            }

        } catch (XmlPullParserException e) { //process errors
            Timber.w(e, "XML parsing exception");
            throw new XMLParserException("XML parsing exception", e);
        } catch (IOException e) {
            Timber.w(e, "XML IO exception");
            throw new XMLParserException("XML parsing exception", e);
        } finally { //close the input stream
            closeInputStream();
        }

        return currentTitleDTO;
    }

    /**
     * Parse the current song
     *
     * @param dtoToFillIn the DTO to fill in with data
     * @throws XmlPullParserException if any XML error occurs
     * @throws IOException            if any IO error occurs
     */
    private void parseCurrentSong(CurrentTitleDTO dtoToFillIn) throws XmlPullParserException, IOException {
        Timber.d("Parse the current song part");
        String availableValue = parser.getAttributeValue(null, "available");
        parser.require(XmlPullParser.START_TAG, null, "currentSong"); //
        while (parser.next() != XmlPullParser.END_TAG) { //process until the end
            if (parser.getEventType() != XmlPullParser.START_TAG) //if not the start tag, continue
                continue;

            //check if current song available
            if (Boolean.valueOf(availableValue)) {
                //process the tag
                String name = parser.getName();
                switch (name) {
                    case "artist":  //artist case
                        if (dtoToFillIn.getCurrentSong() == null)
                            dtoToFillIn.setCurrentSong(new Song());
                        readArtistTag(dtoToFillIn.getCurrentSong());

                        break;
                    case "title":  //title case
                        if (dtoToFillIn.getCurrentSong() == null)
                            dtoToFillIn.setCurrentSong(new Song());
                        readTitleTag(dtoToFillIn.getCurrentSong());

                        break;
                    case "image":  //image case
                        if (dtoToFillIn.getCurrentSong() == null)
                            dtoToFillIn.setCurrentSong(new Song());
                        if (dtoToFillIn.getCurrentSong().getMetadata() == null)
                            dtoToFillIn.getCurrentSong().setMetadata(new ImageMetadata());

                        parseImageMetaData(dtoToFillIn.getCurrentSong().getMetadata());
                        break;
                }
            }
        }
    }

    /**
     * Parse the history list
     *
     * @param dtoToFillIn the DTO to fill in with data
     * @throws XmlPullParserException if any XML error occurs
     * @throws IOException            if any IO error occurs
     */
    private void parseLast5Songs(CurrentTitleDTO dtoToFillIn) throws XmlPullParserException, IOException {
        Timber.d("Parse the last 5 songs");
        parser.require(XmlPullParser.START_TAG, null, "last5Songs"); //root tag
        while (parser.next() != XmlPullParser.END_TAG) { //process until the end
            if (parser.getEventType() != XmlPullParser.START_TAG) { //if not the start tag, continue
                continue;
            }
            Timber.d("Last 5 songs list found");

            Song song = new Song();
            parser.require(XmlPullParser.START_TAG, null, "song"); //song tag
            while (parser.next() != XmlPullParser.END_TAG) { //process until the end
                if (parser.getEventType() != XmlPullParser.START_TAG) { //if not the start tag, continue
                    continue;
                }
                Timber.d("Song tag found");

                //process the tag
                String name = parser.getName();
                switch (name) {
                    case "artist":
                         readArtistTag(song);
                         break;
                    case "title":
                        readTitleTag(song);
                        break;
                    case "image":  //image case
                        song.setMetadata(new ImageMetadata());
                        parseImageMetaData(song.getMetadata());

                }
            }

            //add to history list
            Timber.d("Song to add to history list : %s", song);
            dtoToFillIn.getHistory().add(song);
        }
    }

}
