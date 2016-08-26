/**
 *
 */
package com.stationmillenium.android.libutils.xml;

import android.annotation.SuppressLint;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.libutils.dto.CurrentTitleDTO.Song;
import com.stationmillenium.android.libutils.dto.CurrentTitleDTO.Song.ImageMetadata;
import com.stationmillenium.android.libutils.exceptions.XMLParserException;
import com.stationmillenium.android.libutils.xml.abstracts.AbstractXMLParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Parser for the XML about song history
 *
 * @author vincent
 *         http://developer.android.com/training/basics/network-ops/xml.html for more details
 */
public class XMLSongHistoryParser extends AbstractXMLParser<List<Song>> {

    private static final String TAG = "XMLSongHistoryParser";
    private static final String DATE_FORMAT = "yyyyMMdd-hhmm";

    /**
     * Create a new {@link XMLSongHistoryParser}
     *
     * @param is the {@link InputStream} of the XML to parse
     * @throws XMLParserException if any error occurs
     */
    public XMLSongHistoryParser(InputStream is) throws XMLParserException {
        super(is);
    }

    /**
     * Parse the current XML
     *
     * @return the associated data in a {@link List<Song>}
     * @throws XMLParserException if any error occurs
     */
    public List<Song> parseXML() throws XMLParserException {
        List<Song> songHistoryList = new ArrayList<>(); //return

        try { //parse the start tag : androidSearchSongsHistory
            parser.require(XmlPullParser.START_TAG, null, "androidSearchSongsHistory");
            while (parser.next() != XmlPullParser.END_TAG) { //process until the end
                if (parser.getEventType() != XmlPullParser.START_TAG) //if not the start tag, continue
                    continue;

                //process the tag
                String name = parser.getName();
                if (name.equals("historySong")) {
                    Song parsedSong = parseCurrentSong(); //parse the current song
                    songHistoryList.add(parsedSong); //add the parsed song to the history list
                }
            }

        } catch (XmlPullParserException e) { //process errors
            Log.w(TAG, "XML parsing exception", e);
            throw new XMLParserException("XML parsing exception", e);
        } catch (IOException e) {
            Log.w(TAG, "XML IO exception", e);
            throw new XMLParserException("XML parsing exception", e);
        } finally { //close the input stream
            closeInputStream();
        }

        return songHistoryList;
    }

    /**
     * Parse the current song
     *
     * @return the parsed {@link Song}
     * @throws XmlPullParserException in any XML parsing error occurs
     * @throws IOException            in any IO error occurs
     */
    private Song parseCurrentSong() throws XmlPullParserException, IOException {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Pase the current song part");
        parser.require(XmlPullParser.START_TAG, null, "historySong"); //check tag
        Song returnSong = new Song();
        while (parser.next() != XmlPullParser.END_TAG) { //process until the end
            if (parser.getEventType() != XmlPullParser.START_TAG) //if not the start tag, continue
                continue;

            //process the tag
            String name = parser.getName();
            switch (name) {
                case "artist":
//artist case
                    readArtistTag(returnSong);
                    break;
                case "title":
//title case
                    readTitleTag(returnSong);
                    break;
                case "playedDate":
//date case
                    readPlayedDateTag(returnSong);
                    break;
                case "image":  //image case
                    if (returnSong.getMetadata() == null)
                        returnSong.setMetadata(new ImageMetadata());
                    parseImageMetaData(returnSong.getMetadata());
                    break;
            }
        }

        return returnSong;
    }

    /**
     * Read the played date tag
     *
     * @param song the {@link Song}
     * @throws XmlPullParserException if any XML error occurs
     * @throws IOException            if any IO error occurs
     */
    @SuppressLint("SimpleDateFormat")
    private void readPlayedDateTag(Song song) throws IOException, XmlPullParserException {
        String dateString = readTagText("playedDate");
        if ((dateString != null) && (!dateString.equals(""))) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Date value : " + dateString);

            try { //parse the date
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                Date date = sdf.parse(dateString);
                song.setPlayedDate(date);
            } catch (ParseException e) { //if any parsing error
                Log.w(TAG, "Can't parse the date : " + dateString);
                song.setPlayedDate(null);
            }
        }
    }

}
