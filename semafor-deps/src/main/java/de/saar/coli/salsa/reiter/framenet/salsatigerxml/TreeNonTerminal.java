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

import java.util.List;
import java.util.LinkedList;

import de.uniheidelberg.cl.reiter.util.*;

/**
 * Represents non terminal nodes in the tree
 * @author Nils Reiter
 * @since 0.2
 *
 */
public class TreeNonTerminal extends TreeElement {
	List<String> daughterIds;
	List<TreeElement> daughters;
	
	/**
	 * Creates a new non-terminal node by providing the XML
	 * element
	 */
	@SuppressWarnings("unchecked")
	public TreeNonTerminal (Sentence sentence, Element element) throws JaxenException {
		super(sentence);
		this.id = element.attributeValue("id");
		this.daughterIds = new LinkedList<String>();
		this.daughters = new LinkedList<TreeElement>();
		List edges = new Dom4jXPath("edge").selectNodes(element);
		if (edges != null) {
			for (int i = 0; i < edges.size(); i++) {
				Element edge = (Element) edges.get(i);
				daughterIds.add(edge.attributeValue("idref"));
			}
		}
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (TreeElement te : daughters) {
			buf.append(te.toString());
			buf.append(" ");
		}
		return buf.toString();
	}
	
	public String toString2() {
		StringBuffer buf = new StringBuffer();
		for (TreeElement te : daughters) {
			buf.append(te.toString2());
			//buf.append(" ");
		}
		return buf.toString();
	}
	
	protected void populate(Tree tree) {
		for (String id : daughterIds) {
			daughters.add(tree.getTreeElement(id));
		}
	}
	
	public boolean isTerminal() {
		return false;
	}
		
	protected int length() {
		int n = 0;
		for (TreeElement dtr : this.daughters) {
			n += dtr.length();
		}
		return n;
	}
	
	protected void calculateRange(int start) {
		this.range = new Range(start, start + this.length());
		int position = start;
		for (TreeElement dtr : this.daughters) {
			dtr.calculateRange(position);
			position += dtr.length();
		}
	}
}
