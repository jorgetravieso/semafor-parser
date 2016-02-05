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

import org.jaxen.dom4j.Dom4jXPath;
import org.jaxen.JaxenException;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

/**
 * Represents the syntactic tree
 * @author Nils Reiter
 * @since 0.2
 *
 */
public class Tree {
	String root;
	
	Map<String, TreeElement> treeElements;
	
	Map<String, TreeTerminal> terminalNodes;
	
	TreeElement rootNode;
	
	/**
	 * Creates a new Tree object.
	 * Takes a "graph" XML element as argument
	 * @param node The XML element
	 */
	@SuppressWarnings("unchecked")
	public Tree(Sentence sentence, Element node) throws JaxenException {
		this.root = node.attributeValue("root");
		this.treeElements = new HashMap<String, TreeElement>();
		this.terminalNodes = new HashMap<String, TreeTerminal>();

		List nl = new Dom4jXPath("terminals/t").selectNodes(node);
		if (nl != null) {
			for (int i = 0; i<nl.size(); i++) {
				Element term = (Element) nl.get(i);
				TreeTerminal terminal = new TreeTerminal(sentence, term);
				treeElements.put(terminal.getId(), terminal);
				terminalNodes.put(terminal.word, terminal);
			}
		}
		
		nl = new Dom4jXPath("nonterminals/nt").selectNodes(node);
		if (nl != null) {
			for (int i = 0; i<nl.size(); i++) {
				Element nterm = (Element) nl.get(i);
				TreeNonTerminal terminal = new TreeNonTerminal(sentence, nterm);
				treeElements.put(terminal.getId(), terminal);
			}
		}
		
		for (TreeElement te : treeElements.values()) {
			te.populate(this);
		}
		
		this.rootNode = treeElements.get(this.root);
		
		this.rootNode.calculateRange(0);
		
		for (TreeElement te : this.terminalNodes.values()) {
			sentence.addToken(te.getRange());
		}
	}
	
	/**
	 * Returns the surface string of the tree
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for(TreeElement te : treeElements.values()) {
			buf.append(te.toString());
			buf.append("\n");
		}
		return buf.toString();
	};
	
	/**
	 * Returns an element of the tree by its id
	 * @param id The id of the element
	 * @return the TreeElement
	 */
	protected TreeElement getTreeElement(String id) {
		return this.treeElements.get(id);
	}
	
}
