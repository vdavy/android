/**
 * 
 */
package com.stationmillenium.android.exceptions;

import com.stationmillenium.android.utils.xml.XMLCurrentTitleParser;

/**
 * Exception for {@link XMLCurrentTitleParser}
 * @author vincent
 *
 */
public class XMLParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4695621213210863042L;


	/**
	 * Crate a new {@link XMLParserException} 
	 * @param message the message of the exception
	 * @param cause the cause (as {@link Exception}) of the exception
	 */
	public XMLParserException(String message, Exception cause) {
		super(message, cause);
	}
	
}
