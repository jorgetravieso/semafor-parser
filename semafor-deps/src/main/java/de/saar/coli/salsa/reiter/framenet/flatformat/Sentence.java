/**
 * 
 * Copyright 2007-2009 by Nils Reiter.
 * 
 * This FrameNet API is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3.
 *
 * This FrameNet API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this FrameNet API.  If not, see www.gnu.org/licenses/gpl.html.
 * 
 */
package de.saar.coli.salsa.reiter.framenet.flatformat;

import de.saar.coli.salsa.reiter.framenet.RealizedFrame;

import de.uniheidelberg.cl.reiter.util.Range;

import java.util.List;
import java.util.LinkedList;

/**
 * This class represents a sentence in the flat, plain-text based format.
 * @author Nils Reiter
 * @since 0.2
 *
 */
public class Sentence extends de.saar.coli.salsa.reiter.framenet.Sentence {
	
	/**
	 * A list of realized frames found in this sentence
	 */
	List<RealizedFrame> realizedFrames = new LinkedList<RealizedFrame>();
	
	/**
	 * A constructor taking a sentence id and the text of the sentence
	 * @param id The id
	 * @param text The surface string of the sentence
	 */
	public Sentence(String id, String text) {
		super(id, text);
	}
	
	/**
	 * A constructor that takes an integer as identifier as well as a string as
	 * surface form of the sentence. The identifier is transformed into a string by
	 * the constructor.
	 * @param id The identifier
	 * @param text The surface form of the sentence
	 */
	public Sentence (int id, String text) {
		super(String.valueOf(id), text);
	}
	
	/**
	 * This method adds a RealizedFrame object to the sentence
	 * @param realizedFrame The realized frame
	 */
	protected void addRealizedFrame(RealizedFrame realizedFrame) {
		realizedFrames.add(realizedFrame);
	}
	
	/**
	 * This method returns the list of realized frames in the sentence
	 * @return An ordered list of RealizedFrame objects
	 */
	public List<RealizedFrame> getRealizedFrames() {
		return realizedFrames;
	}
	
	public void addToken(Range range) {
		Token token = new Token(this, range);
		this.tokenList.put(range, token);
		
	}
}
