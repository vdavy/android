/**
 *
 */
package com.stationmillenium.android.libutils.exceptions;

/**
 * Exception for XMLCurrentTitleParser
 *
 * @author vincent
 */
public class XMLParserException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 4695621213210863042L;


    /**
     * Create a new {@link XMLParserException}
     *
     * @param message the message of the exception
     * @param cause   the cause (as {@link Exception}) of the exception
     */
    public XMLParserException(String message, Exception cause) {
        super(message, cause);
    }

    /**
     * Create a new {@link XMLParserException}
     *
     * @param message the message of the exception
     */
    public XMLParserException(String message) {
        super(message);
    }

}
