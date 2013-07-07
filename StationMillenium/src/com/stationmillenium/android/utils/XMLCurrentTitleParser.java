/**
 * 
 */
package com.stationmillenium.android.utils;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import com.stationmillenium.android.dto.CurrentTitleDTO;
import com.stationmillenium.android.dto.CurrentTitleDTO.Song;
import com.stationmillenium.android.dto.CurrentTitleDTO.Song.ImageMetadata;
import com.stationmillenium.android.exceptions.XMLParserException;

/**
 * Parser for the XML about current title
 * @see http://developer.android.com/training/basics/network-ops/xml.html for more details
 * @author vincent
 *
 */
public class XMLCurrentTitleParser {

	private static final String TAG = "XMLCurrentTitleParser";
	
	//xml namespace
	private static final String ns = null;//"http://www.station-millenium.com/AndroidCurrentSongs";
	
	//vars
	private XmlPullParser parser;
	
	/**
	 * Create a new {@link XMLCurrentTitleParser} 
	 * @param is the {@link InputStream} of the XML to parse
	 * @throws XMLParserException in case of error
	 */
	public XMLCurrentTitleParser(InputStream is) throws XMLParserException {
		try {
			//create the new parser
			Log.d(TAG, "Create the XML parser");
			parser = Xml.newPullParser();
	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	        parser.setInput(is, "ISO-8859-1");
	        parser.nextTag();
	        
		} catch(XmlPullParserException e) { //process errors
			Log.w(TAG, "XML parsing exception", e);
			throw new XMLParserException("XML parsing exception", e);
		} catch (IOException e) {
			Log.w(TAG, "XML IO exception", e);
			throw new XMLParserException("XML parsing exception", e);

		} finally { //close the input stream
			try {
				Log.d(TAG, "Close input stream");
				is.close();
			} catch (IOException e) {
				Log.w(TAG, "Error while closing XML input stream");
			}
		}
	}
	
	/**
	 * Parse the current XML 
	 * @return the associated data in a {@link CurrentTitleDTO}
	 * @throws XMLParserException if any error occurs
	 */
	public CurrentTitleDTO parseXML() throws XMLParserException {
		CurrentTitleDTO currentTitleDTO = new CurrentTitleDTO();
				
		try { //parse the start tag : androidCurrentSongs
			parser.require(XmlPullParser.START_TAG, ns, "androidCurrentSongs");
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
		Log.d(TAG, "Pase the current song part");
		String availableValue = parser.getAttributeValue(ns, "available"); 
		parser.require(XmlPullParser.START_TAG, ns, "currentSong"); //
		while (parser.next() != XmlPullParser.END_TAG) { //process until the end
	        if (parser.getEventType() != XmlPullParser.START_TAG) //if not the start tag, continue
	            continue;

	        //check if current song available
	        
	        if (Boolean.valueOf(availableValue)) {	        	
	        	//process the tag
		        String name = parser.getName();
		        if (name.equals("artist")) { //artist case
		        	String artist = readTagText("artist");
		        	if ((artist != null) && (!artist.equals(""))) {
		        		Log.d(TAG, "Artist value : " + artist);
		        		if (dtoToFillIn.getCurrentSong() == null)
		        			dtoToFillIn.setCurrentSong(new Song());
		        		dtoToFillIn.getCurrentSong().setArtist(artist);
		        	}
		        
		        } else if (name.equals("title")) { //title case      	
		        	String title = readTagText("title");
		        	if ((title != null) && (!title.equals(""))) {
		        		Log.d(TAG, "Title value : " + title);
		        		if (dtoToFillIn.getCurrentSong() == null)
		        			dtoToFillIn.setCurrentSong(new Song());
		        		dtoToFillIn.getCurrentSong().setTitle(title);
		        	}
		        	
		        } else if (name.equals("image")) { //image case
		        	if (dtoToFillIn.getCurrentSong() == null) 
		        		dtoToFillIn.setCurrentSong(new Song());
		        	if (dtoToFillIn.getCurrentSong().getMetadata() == null) 
		        		dtoToFillIn.getCurrentSong().setMetadata(new ImageMetadata());
		        	
		        	parseImageMetaData(dtoToFillIn);
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
		Log.d(TAG, "Parse the last 5 songs");
		parser.require(XmlPullParser.START_TAG, ns, "last5Songs"); //root tag
		while (parser.next() != XmlPullParser.END_TAG) { //process until the end
	        if (parser.getEventType() != XmlPullParser.START_TAG) { //if not the start tag, continue
	            continue;
	        }
	        Log.d(TAG, "Last 5 songs list found");

	        Song song = new Song();
	        parser.require(XmlPullParser.START_TAG, ns, "song"); //song tag
			while (parser.next() != XmlPullParser.END_TAG) { //process until the end
		        if (parser.getEventType() != XmlPullParser.START_TAG) { //if not the start tag, continue
		            continue;
		        }
		        Log.d(TAG, "Song tag found");
		        
			    
	        	//process the tag
		        String name = parser.getName();
		        if (name.equals("artist")) { //artist case
		        	String artist = readTagText("artist");
		        	if ((artist != null) && (!artist.equals(""))) {
		        		Log.d(TAG, "Artist value : " + artist);
		        		song.setArtist(artist);
		        	}
		        
		        } else if (name.equals("title")) { //title case      	
		        	String title = readTagText("title");
		        	if ((title != null) && (!title.equals(""))) {
		        		Log.d(TAG, "Title value : " + title);
		        		song.setTitle(title);
		        	}
		        }
			}
			
			//add to history list
	        Log.d(TAG, "Song to add to history list : " + song);
	        dtoToFillIn.getHistory().add(song);
		}
	}
	
	/**
	 * Parse the meta data from XML of an image
	 * @param dtoToFillIn the DTO to fill in with data
	 * @throws XmlPullParserException if any XML error occurs
	 * @throws IOException if any IO error occurs
	 */
	private void parseImageMetaData(CurrentTitleDTO dtoToFillIn) throws XmlPullParserException, IOException {
		Log.d(TAG, "Parse the image meta data");
		parser.require(XmlPullParser.START_TAG, ns, "image"); //root tag
		while (parser.next() != XmlPullParser.END_TAG) { //process until the end
	        if (parser.getEventType() != XmlPullParser.START_TAG) { //if not the start tag, continue
	            continue;
	        }
	        Log.d(TAG, "Image tag found");
	    
        	//process the tag
	        String name = parser.getName();
	        if (name.equals("path")) { //path case
	        	String path = readTagText("path");
	        	if ((path != null) && (!path.equals(""))) {
	        		Log.d(TAG, "Path value : " + path);
	        		dtoToFillIn.getCurrentSong().getMetadata().setPath(path);
	        	}
	        
	        } else if (name.equals("width")) { //width case      	
	        	String width = readTagText("width");
	        	if ((width != null) && (!width.equals(""))) {
	        		Log.d(TAG, "Width value : " + width);
	        		dtoToFillIn.getCurrentSong().getMetadata().setWidth(width);
	        	}
	        
	        } else if (name.equals("height")) { //height case      	
	        	String height = readTagText("height");
	        	if ((height != null) && (!height.equals(""))) {
	        		Log.d(TAG, "Height value : " + height);
	        		dtoToFillIn.getCurrentSong().getMetadata().setHeight(height);
	        	}
	        }
		}
	}
	
	/**
	 * Read a tag text
	 * @param tag the tag to read
	 * @return the read text on the tag
	 * @throws IOException if any IO error occurs
	 * @throws XmlPullParserException if any XML error occurs
	 */
	private String readTagText(String tag) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, tag);
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    parser.require(XmlPullParser.END_TAG, ns, tag);
	    
	    Log.d(TAG, "Read value for tag '" + tag + "' : " + result);
	    return result;
	}
}
