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
package de.saar.coli.salsa.reiter.framenet;

import java.util.*;

import de.uniheidelberg.cl.reiter.util.*;

/**
 * This abstract class is the base class for any sentence reading class
 * for a specific corpus format. It basically provides the fields id and text, 
 * representing a sentence identifier and the surface string of the sentence 
 * itself.
 * 
 * @author Nils Reiter
 * @since 0.2
 */
public abstract class Sentence implements IHasID {
	/**
	 * An identifier of the sentence
	 */
	String id;
	
	/**
	 * The surface string of the sentence
	 */
	String text;
	
	/**
	 * A list of tokens in this sentence
	 */
	protected SortedMap<Range, IToken> tokenList;
	
	/**
	 * A constructor taking only the identifier. Optimally used
	 * in combination with {@link Sentence#setText(String)}.
	 * @param id The identifier
	 */
	public Sentence(String id) {
		this.id = id;
		this.text = "";
		
		this.init();
	}
	
	/**
	 * A constructor setting both the id and the text
	 * @param id The identifier
	 * @param text The surface string of the sentence
	 */
	public Sentence(String id, String text) {
		this.id = id;
		this.text = text;
		
		this.init();
	}
	
	private void init() {
		this.tokenList = new TreeMap<Range, IToken>();
	}
	
	/**
	 * This abstract method returns a (ordered) list of  realized frames
	 * which occur in this sentence. Has to be implemented in inheriting
	 * classes, since it heavily depends on the actual representation of the 
	 * frames.
	 * @return A list of RealizedFrame objects
	 */
	public abstract List<RealizedFrame> getRealizedFrames();
	
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the identifier
	 * @param id the identifier
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * 
	 * @return the text of the sentence
 	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Sets the surface string of the sentence
	 * @param text the sentence
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	public Collection<IToken> getTokens() {
		return this.tokenList.values();
	}
	
	public abstract void addToken(Range range);
	
	public IToken getTokenForString(String s) {
		Range r = this.getRangeForString(s);
		if (r != null)
			return this.getToken(r);
		return null;
	}
	
	public IToken getToken(Range range) {
		if (! this.tokenList.containsKey(range)) {
			addToken(range);
		}
		return this.tokenList.get(range);
	}
	
	public String getSurface(Range range) {
		return this.getText().substring(range.getElement1(), range.getElement2());
	}
	
	public Range getRangeForString(String s) {
		int begin = this.getText().indexOf(s);
		if (begin == -1) {
			return null;
		}
		Range r = new Range(begin, begin + s.length());
		return r;
	}
	
	public String toString() {
		return this.getText();
	}
}
