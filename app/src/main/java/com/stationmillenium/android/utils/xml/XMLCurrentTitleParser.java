/**
 * 
 */
package com.stationmillenium.android.utils.xml;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.dto.CurrentTitleDTO;
import com.stationmillenium.android.dto.CurrentTitleDTO.Song;
import com.stationmillenium.android.dto.CurrentTitleDTO.Song.ImageMetadata;
import com.stationmillenium.android.exceptions.XMLParserException;
import com.stationmillenium.android.utils.xml.abstracts.AbstractXMLParser;

/**
 * Parser for the XML about current title
 * @see http://developer.android.com/training/basics/network-ops/xml.html for more details
 * @author vincent
 *
 */
public class XMLCurrentTitleParser extends AbstractXMLParser<CurrentTitleDTO>{

	private static final String TAG = "XMLCurrentTitleParser";

	/**
	 * Create a new {@link XMLCurrentTitleParser} 
	 * @param is the {@link InputStream} of the XML to parse
	 * @throws XMLParserException if any error occurs
	 */
	public XMLCurrentTitleParser(InputStream is) throws XMLParserException {
		super(is);
	}

	/**
	 * Parse the current XML 
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
			Log.w(TAG, "XML parsing exception", e);
			throw new XMLParserException("XML parsing exception", e);
		} catch (IOException e) {
			Log.w(TAG, "XML IO exception", e);
			throw new XMLParserException("XML parsing exception", e);
		} finally { //close the input stream
			closeInputStream();
		}

		return currentTitleDTO;
	}

	/**
	 * Parse the current song
	 * @param dtoToFillIn the DTO to fill in with data
	 * @throws XmlPullParserException if any XML error occurs
	 * @throws IOException if any IO error occurs
	 */
	private void parseCurrentSong(CurrentTitleDTO dtoToFillIn) throws XmlPullParserException, IOException {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Pase the current song part");
		String availableValue = parser.getAttributeValue(null, "available"); 
		parser.require(XmlPullParser.START_TAG, null, "currentSong"); //
		while (parser.next() != XmlPullParser.END_TAG) { //process until the end
			if (parser.getEventType() != XmlPullParser.START_TAG) //if not the start tag, continue
				continue;

			//check if current song available
			if (Boolean.valueOf(availableValue)) {	        	
				//process the tag
				String name = parser.getName();
				if (name.equals("artist")) { //artist case
					if (dtoToFillIn.getCurrentSong() == null)
						dtoToFillIn.setCurrentSong(new Song());
					readArtistTag(dtoToFillIn.getCurrentSong());

				} else if (name.equals("title")) { //title case   
					if (dtoToFillIn.getCurrentSong() == null)
						dtoToFillIn.setCurrentSong(new Song());
					readTitleTag(dtoToFillIn.getCurrentSong());

				} else if (name.equals("image")) { //image case
					if (dtoToFillIn.getCurrentSong() == null) 
						dtoToFillIn.setCurrentSong(new Song());
					if (dtoToFillIn.getCurrentSong().getMetadata() == null) 
						dtoToFillIn.getCurrentSong().setMetadata(new ImageMetadata());

					parseImageMetaData(dtoToFillIn.getCurrentSong().getMetadata());
				}
			}
		}  
	}

	/**
	 * Parse the history list
	 * @param dtoToFillIn the DTO to fill in with data
	 * @throws XmlPullParserException if any XML error occurs
	 * @throws IOException if any IO error occurs
	 */
	private void parseLast5Songs(CurrentTitleDTO dtoToFillIn) throws XmlPullParserException, IOException {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Parse the last 5 songs");
		parser.require(XmlPullParser.START_TAG, null, "last5Songs"); //root tag
		while (parser.next() != XmlPullParser.END_TAG) { //process until the end
			if (parser.getEventType() != XmlPullParser.START_TAG) { //if not the start tag, continue
				continue;
			}
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Last 5 songs list found");

			Song song = new Song();
			parser.require(XmlPullParser.START_TAG, null, "song"); //song tag
			while (parser.next() != XmlPullParser.END_TAG) { //process until the end
				if (parser.getEventType() != XmlPullParser.START_TAG) { //if not the start tag, continue
					continue;
				}
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Song tag found");

				//process the tag
				String name = parser.getName();
				if (name.equals("artist")) { //artist case
					readArtistTag(song);

				} else if (name.equals("title")) { //title case      	
					readTitleTag(song);
				}
			}

			//add to history list
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Song to add to history list : " + song);
			dtoToFillIn.getHistory().add(song);
		}
	}

}
