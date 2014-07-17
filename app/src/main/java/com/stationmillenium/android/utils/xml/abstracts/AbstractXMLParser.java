/**
 * 
 */
package com.stationmillenium.android.utils.xml.abstracts;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import com.stationmillenium.android.BuildConfig;
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
public abstract class AbstractXMLParser<T> {

	private static final String TAG = "AbstractXMLParser";
	
	//vars
	protected XmlPullParser parser;
	private InputStream is;
	
	/**
	 * Create a new {@link AbstractXMLParser} 
	 * @param is the {@link InputStream} of the XML to parse
	 * @throws XMLParserException if any error occurs
	 */
	public AbstractXMLParser(InputStream is) throws XMLParserException {
		try {
			//create the new parser
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Create the XML parser");
			 
			if (is != null) { //check the input stream is not null
				this.is = is;
				parser = Xml.newPullParser();
		        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		        parser.setInput(is, "ISO-8859-1");
		        parser.nextTag();
		        
			} else { //input stream is null : error thrown
				Log.w(TAG, "Input stream is null !");
				throw new XMLParserException("XML parsing exception");
			}
 	        
		} catch(XmlPullParserException e) { //process errors
			Log.w(TAG, "XML parsing exception", e);
			throw new XMLParserException("XML parsing exception", e);
		} catch (IOException e) {
			Log.w(TAG, "XML IO exception", e);
			throw new XMLParserException("XML parsing exception", e);

		} 
	}
	
	/**
	 * Parse the current XML 
	 * @return the associated data in a {@link CurrentTitleDTO}
	 * @throws XMLParserException if any error occurs
	 */
	public abstract T parseXML() throws XMLParserException;

	/**
	 * Read the artist tag
	 * @param dtoToFillIn the {@link Song}
	 * @throws IOException if any IO error occurs
	 * @throws XmlPullParserException if any parsing error occurs
	 */
	protected void readArtistTag(Song dtoToFillIn) throws IOException, XmlPullParserException {
		String artist = readTagText("artist");
		if ((artist != null) && (!artist.equals(""))) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Artist value : " + artist);
			
			dtoToFillIn.setArtist(artist);
		}
	}
	
	/**
	 * Read the title tag
 	 * @param song the {@link Song}
	 * @throws XmlPullParserException if any XML error occurs
	 * @throws IOException if any IO error occurs
	 */
	protected void readTitleTag(Song song) throws IOException, XmlPullParserException {
		String title = readTagText("title");
		if ((title != null) && (!title.equals(""))) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Title value : " + title);
			song.setTitle(title);
		}
	}
	
	/**
	 * Parse the meta data from XML of an image
	 * @param dtoToFillIn the DTO to fill in with data
	 * @throws XmlPullParserException if any XML error occurs
	 * @throws IOException if any IO error occurs
	 */
	protected void parseImageMetaData(ImageMetadata dtoToFillIn) throws XmlPullParserException, IOException {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Parse the image meta data");
		parser.require(XmlPullParser.START_TAG, null, "image"); //root tag
		while (parser.next() != XmlPullParser.END_TAG) { //process until the end
	        if (parser.getEventType() != XmlPullParser.START_TAG) { //if not the start tag, continue
	            continue;
	        }
	        if (BuildConfig.DEBUG)
	        	Log.d(TAG, "Image tag found");
	    
        	//process the tag
	        String name = parser.getName();
	        if (name.equals("path")) { //path case
	        	String path = readTagText("path");
	        	if ((path != null) && (!path.equals(""))) {
	        		if (BuildConfig.DEBUG)
	        			Log.d(TAG, "Path value : " + path);
	        		dtoToFillIn.setPath(path);
	        	}
	        
	        } else if (name.equals("width")) { //width case      	
	        	String width = readTagText("width");
	        	if ((width != null) && (!width.equals(""))) {
	        		if (BuildConfig.DEBUG)
	        			Log.d(TAG, "Width value : " + width);
	        		dtoToFillIn.setWidth(width);
	        	}
	        
	        } else if (name.equals("height")) { //height case      	
	        	String height = readTagText("height");
	        	if ((height != null) && (!height.equals(""))) {
	        		if (BuildConfig.DEBUG)
	        			Log.d(TAG, "Height value : " + height);
	        		dtoToFillIn.setHeight(height);
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
	protected String readTagText(String tag) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, tag);
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    parser.require(XmlPullParser.END_TAG, null, tag);
	    
	    if (BuildConfig.DEBUG)
	    	Log.d(TAG, "Read value for tag '" + tag + "' : " + result);
	    return result;
	}
	
	/**
	 * Close the {@link InputStream}
	 */
	protected void closeInputStream() {
		try {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Close input stream");
			if (is != null)
				is.close();
		} catch (IOException e) {
			Log.w(TAG, "Error while closing XML input stream");
		}
	}
	
}
