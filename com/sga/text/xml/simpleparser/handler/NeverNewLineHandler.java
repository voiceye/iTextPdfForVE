/**
 *
 */
package com.sga.text.xml.simpleparser.handler;

import com.sga.text.xml.simpleparser.NewLineHandler;

/**
 * Always returns false.
 * @author Balder
 * @since 5.0.6
 *
 */
public class NeverNewLineHandler implements NewLineHandler {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.sga.text.xml.simpleparser.NewLineHandler#isNewLineTag(java.lang
	 * .String)
	 */
	public boolean isNewLineTag(final String tag) {
		return false;
	}

}
