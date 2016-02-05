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


import java.io.Serializable;
import java.util.List;
import java.util.Iterator;

/**
 * Represents a lexical unit of FrameNet.
 * 
 * @author Nils Reiter
 *
 */
public abstract class LexicalUnit implements Serializable, IHasCDate, IHasName, IHasDefinition, IHasID {
	/**
	 * The XML id of the lexical unit.
	 */
	String id;
	
	/**
	 * The XML field "name".
	 */
	String name;

	/**
	 * The XML field "status".
	 */
	String status;
	
	/**
	 * The XML field "cDate".
	 */
	String cDate;
	
	/**
	 * The XML field "definition".
	 */	
	String definition;

	/**
	 * The XML field "lemmaID".
	 */
	String lemmaID;

	/**
	 * The lemma of the lexical unit.
	 */
	String lemma;
	
	/**
	 * A string representation of the part of speech. 
	 */
	String pos;
	
	/**
	 * The frame to which this lexical unit belongs
	 */
	Frame frame;
		
	/**
	 * 
	 */
	List<Lexeme> lexemes = null;
	
	private static final long serialVersionUID = 8L;
		
	protected LexicalUnit() {}
	
	/**
	 * Returns the part of speech of this lemma. The method returns the 
	 * complete text that is given in the FrameNet database, i.e. "prep",
	 * "adv", "adj", ... in lower case.
	 * @return A string representing the part of speech
	 */
	public String getPartOfSpeechAbbreviation() {
		return pos;
	}
	
	/*
	 * Returns the part of speech of this lemma using {@link PartOfSpeech}
	 * found in the package edu.mit.jwi.item. The method returns null, if the 
	 * part of speech of the lexical unit can not be represented with 
	 * {@link edu.mit.jwi.item.PartOfSpeech}.
	 * @return The part of speech, or null if it can not be represented.
	 */
	/*public PartOfSpeech getPartOfSpeech() {
		if (pos.equals("adv"))
			return PartOfSpeech.ADVERB;
		if (pos.equals("v"))
			return PartOfSpeech.VERB;
		if (pos.equals("n"))
			return PartOfSpeech.NOUN;
		if (pos.equals("a"))
			return PartOfSpeech.ADJECTIVE;
		if (frameNet.isDebug()) 
			System.err.println("LexicalUnit.getPartOfSpeech(): Part of Speech " + pos + " convertable in object. Returning null.");
		return null;
	}*/

	public String getCDate() {
		return cDate;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDefinition() {
		return definition;
	}

	/**
	 * Returns the annotation status as string.
	 * 
	 * @return The status of the annotation.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * The lemma id is a ongoing integer value for the different lemmas. 
	 * 
	 * @return the lemmaID
	 */
	public String getLemmaID() {
		return lemmaID;
	}

	/**
	 * The lemma is the word without its part of speech. The lemma of the lexical unit
	 * "walk.v", for instance, is "walk".
	 * 
	 * This method is deprecated since 0.4 and 0.3.5.
	 * 
	 * @deprecated See http://www.cl.uni-heidelberg.de/trac/FrameNetAPI/ticket/31 for details.
	 * @return the lemma
	 */
	public String getLemma() {
		return lemma;
	}	
	
	/**
	 * Returns the name of the lexical unit.
	 */
	public String toString() {
		return name;
	}

	/**
	 * Returns the frame in which this lexical unit occurs
	 * 
	 * @return the frame
	 */
	public Frame getFrame() {
		return frame;
	}

	/**
	 * Returns a collection of lexemes in this lexical unit
	 * @return The lexemes in this lexical unit
	 */
	public List<Lexeme> getLexemes() {
		return lexemes;
	}
	
	public String getLexemeString() {
		StringBuffer buf = new StringBuffer();
		Iterator<Lexeme> lexit = this.getLexemes().iterator();
		do {
			buf.append(lexit.next().getValue());
			if (lexit.hasNext())
				buf.append(" ");
		} while (lexit.hasNext());
		return buf.toString();
	}
}