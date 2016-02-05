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

import de.saar.coli.salsa.reiter.framenet.FrameNet;
import de.saar.coli.salsa.reiter.framenet.IToken;
import de.saar.coli.salsa.reiter.framenet.RealizedFrame;

import org.dom4j.Element;

import org.jaxen.dom4j.Dom4jXPath;
import org.jaxen.JaxenException;

import de.uniheidelberg.cl.reiter.util.*;

import java.util.*;

/**
 * This class represents a sentence in the SalsaTigerXML package
 * @author Nils Reiter
 * @since 0.2
 *
 */
public class Sentence extends de.saar.coli.salsa.reiter.framenet.Sentence {
	Tree tree;
	Semantics semantics;
	
	/**
	 * Creates a new Sentence object, based on the FrameNet object and 
	 * an XML element representing the sentence
	 * @param frameNet The FrameNet object
	 * @param element The XML element
	 */
	public Sentence(FrameNet frameNet, Element element) throws JaxenException {
		super(element.attributeValue("id"));
		this.tree = new Tree(this, (Element) new Dom4jXPath("graph").selectNodes(element).get(0));
		this.semantics = new Semantics((Element) new Dom4jXPath("sem").selectNodes(element).get(0), this.tree, frameNet);
		this.setText(this.tree.toString());
	}
	
	public List<RealizedFrame> getRealizedFrames() {
		return this.getSemantics().getRealizedFrames();
	}

	/**
	 * This method returns the Semantics object for this sentence
	 * @return The Semantics object
	 */
	public Semantics getSemantics() {
		return semantics;
	}
	
	public void addToken(Range range) { return; };
	
	public IToken getTokenForString(String s) {
		IToken token = this.tree.terminalNodes.get(s);
		if (token != null)
			return token;
		return null;
	}
	
	public IToken getToken(Range range) {
		for (TreeElement te : tree.treeElements.values()) {
			if (range.compareTo(te.getRange()) == 0)
				return te;
		}
		return null;
	}
	
	public List<IToken> getTokens() {
		SortedSet<IToken> tokenList = new TreeSet<IToken>();
		tokenList.addAll(this.tree.terminalNodes.values());
		List<IToken> ret = new LinkedList<IToken>(tokenList);
		return ret;
	}
}
