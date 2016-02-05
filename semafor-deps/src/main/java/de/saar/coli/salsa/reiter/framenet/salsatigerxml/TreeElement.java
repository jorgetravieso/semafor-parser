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

import de.saar.coli.salsa.reiter.framenet.IHasID;
import de.saar.coli.salsa.reiter.framenet.AbstractToken;

import de.uniheidelberg.cl.reiter.util.Range;

/**
 * An abstract class as base class for all classes building up the tree
 * (terminal and non-terminal graph nodes, so to speak)
 * @author Nils Reiter
 * @since 0.2
 *
 */
public abstract class TreeElement extends AbstractToken implements IHasID, Comparable<TreeElement>   {
	String id;
	
	Sentence sentence;
	
	Range range;
	
	/**
	 * The default constructor
	 */
	public TreeElement(Sentence sentence) {
		this.sentence = sentence;
	}
	
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the surface string under this node
	 */
	abstract public String toString();
	
	abstract public String toString2();
	
	/**
	 * Returns true, if the tree node is a terminal node. 
	 * False otherwise
	 * @return true or false
	 */
	abstract public boolean isTerminal();
	
	/**
	 * Retrieves the TreeElement objects by their id and stores them 
	 * internally for each object
	 * @param tree The tree
	 */
	abstract protected void populate(Tree tree);
	
	/**
	 * Returns the length in characters of this subtree
	 * @return The length of this subtree
	 */
	abstract protected int length();

	abstract protected void calculateRange(int start);
	
	public Range getRange() {
		return this.range;
	}
	
	public Sentence getSentence() {
		return sentence;
	}
	
	public int compareTo(TreeElement other) {
		return this.getRange().compareTo(other.getRange());
	}
}
