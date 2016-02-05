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
package de.saar.coli.salsa.reiter.framenet.salsatigerxml;

import org.dom4j.Element;
import de.uniheidelberg.cl.reiter.util.*;

/**
 * Represents a terminal node in the syntactic tree
 * @author Nils Reiter
 * @since 0.2
 *
 */
public class TreeTerminal extends TreeElement {
	String lemma;
	String morph;
	String pos;
	String word;
	
	/**
	 * Creates the terminal node and stores its attributes
	 * @param sentence The Sentence, in which this tree occurres
	 * @param element The XML element
	 */
	public TreeTerminal(Sentence sentence, Element element) {
		super(sentence);
		this.id = element.attributeValue("id");
		this.lemma = element.attributeValue("lemma");
		this.morph = element.attributeValue("pos");
		this.pos = element.attributeValue("pos");
		this.word = element.attributeValue("word");
		
		if (this.pos == null)
			this.pos = "";
	}

	public String toString2() {
		if (pos.startsWith("PUNC"))
			return word;
		return " " + word;
	}

	public String toString() {
		return word;
	}

	
	public boolean isTerminal() {
		return true;
	}
	
	protected void populate(Tree tree) {};
		
	protected int length() {
		if (this.pos.startsWith("PUNC"))
			return this.word.length();
		return this.word.length()+1;
	}
	
	protected void calculateRange(int start) {
		this.range = new Range(start, start+this.length());
	}
}
