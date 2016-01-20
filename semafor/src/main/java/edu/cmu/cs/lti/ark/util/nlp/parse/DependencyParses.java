/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * DependencyParses.java is part of SEMAFOR 2.0.
 * 
 * SEMAFOR 2.0 is free software: you can redistribute it and/or modify  it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * SEMAFOR 2.0 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along
 * with SEMAFOR 2.0.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.cmu.cs.lti.ark.util.nlp.parse;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Represents a collection of predicted dependency parses for a single sentence.
 * 
 * @author Nathan Schneider (nschneid)
 * @since 2009-06-19
 */
public class DependencyParses implements Iterable<DependencyParse>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2181832066796195517L;
	protected DependencyParse[] parses;
	protected DependencyParse[][] nodes;
	
	public DependencyParses(DependencyParse[] parses) {
		this.parses = parses;
		this.nodes = new DependencyParse[parses.length][];
	}

	public DependencyParse get(int i) {
		return this.parses[i];
	}
	public int size() {
		return this.parses.length;
	}
	
	public Iterator<DependencyParse> iterator() {
		return Arrays.asList(parses).iterator();
	}
	
	public String getSentence() {
		return getBestParse().getSentence();
	}
	
	/**
	 * @return The first parse in the list
	 */
	public DependencyParse getBestParse() {
		if (this.parses.length<1 || this.parses[0]==null)
			return null;
		return this.parses[0];
	}
	
}
