/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * IndexedLabeledPath.java is part of SEMAFOR 2.0.
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
package edu.cmu.cs.lti.ark.util.ds.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.cmu.cs.lti.ark.util.Indexer;
import edu.cmu.cs.lti.ark.util.ds.Pair;

/**
 * {@link Path} subclass in which elements in the path are integer indices and in which 
 * edges between them are associated with a label. 
 * 
 * @author Nathan Schneider (nschneid)
 * @since 2010-10-31
 *
 * @param <N> Type of objects in the indexed list
 * @param <L> Label type
 * 
 * @see {@link LabeledPath}
 * @see {@link IndexedPath}
 */

public class IndexedLabeledPath<N,L> extends LabeledPath<Integer,L> implements Comparable<IndexedLabeledPath<N,L>>, Indexer<List<Integer>,List<N>,LabeledPath<N,L>> {
	private static final long serialVersionUID = 1561209734998077623L;
	
	public IndexedLabeledPath() {
		
	}
	
	public IndexedLabeledPath(IndexedLabeledPath<N,L> that) {
		super(that);
	}
	
	public IndexedLabeledPath(int[] indices, Iterable<L> labels) {
		this(indices,false,labels);
	}
	
	public IndexedLabeledPath(int[] indices, boolean includesSource, Iterable<L> labels) {
		this(indices,includesSource,labels,false);
	}
	public IndexedLabeledPath(int[] indices, boolean includesSource, Iterable<L> labels, boolean overrideCycleCheck) {
		super();
		if (includesSource)
			this.add(new Pair<Integer,L>(indices[0],null));
		Iterator<L> labelsI = labels.iterator();
		for (int i=((includesSource) ? 1 : 0); i<indices.length; i++) {
			this.add(new Pair<Integer,L>(indices[i],labelsI.next()), overrideCycleCheck);
		}
		assert !labelsI.hasNext();
	}
	public IndexedLabeledPath(List<Pair<Integer, L>> entries, boolean includesSource, boolean allowCycles, boolean allowSelfLoops) {
		super(entries, allowCycles, allowSelfLoops);
		this._includeSource = includesSource;
	}
	
	public int[] getIndices() {
		int[] items = new int[this.size()];
		for (int i=0; i<items.length; i++)
			items[i] = this.get(i).getFirst();
		return items;
	}
	@Override
	public List<Integer> indices() {
		return getEntries();
	}
	
	@Override
	public IndexedLabeledPath<N,L> getSuffix(int maxLength) {
		assert maxLength>0;
		List<Pair<Integer,L>> pre = new ArrayList<Pair<Integer,L>>(this.subList(Math.max(0, this.size()-maxLength), this.size()));
		if (_includeSource)
			pre.set(0, new Pair<Integer,L>(pre.get(0).getFirst(),null));
		return new IndexedLabeledPath<N,L>(pre, this._includeSource, this._allowCycles, this._allowSelfLoops);
	}
	@Override
	public IndexedLabeledPath<N,L> getPrefix(int maxLength) {
		assert maxLength>0;
		return new IndexedLabeledPath<N,L>(this.subList(0, Math.min(this.size(), this.size()+maxLength)), 
				this._includeSource, this._allowCycles, this._allowSelfLoops);
	}
	
	
	
	
	
	@Override
	public LabeledPath<N,L> apply(List<N> nodes) {
		LabeledPath<N,L> path = new LabeledPath<N,L>();
		path._includeSource = this._includeSource;
		for (Pair<Integer,L> item : this) {
			path.add(new Pair<N,L>(nodes.get(item.getFirst()), item.getSecond()));
		}
		return path;
	}

	@Override
	public int compareTo(IndexedLabeledPath<N,L> that) {
		int i = this.size()-1;
		int j = that.size()-1;
		while (i>=0) {
			if (j<0)	// a is longer; order b first
				return 1;
			
			Pair<Integer, L> a = this.get(i);
			Pair<Integer, L> b = that.get(j);
			
			i--; j--;	// working backwards
			
			if (a.equals(b))
				continue;
			
			// if corresponding indices don't match, order them so the path with the lower index comes first
			if (a.getFirst()<b.getFirst()) return -1;
			else if (a.getFirst()>b.getFirst()) return 1;
			
			// corresponding labels don't match
			assert (a.getSecond()!=b.getSecond());
			if (a.getSecond()==null || a.getSecond().hashCode()<b.getSecond().hashCode()) return -1;
			else if (b.getSecond()==null || a.getSecond().hashCode()>b.getSecond().hashCode()) return 1;
			
			assert false;	// equals() or hashCode() problem?
		}
		if (j>=0)	// b is longer; order a first
			return -1;
		return 0;	// they are equal
	}
	
	@Override
	public Object clone() {
		return new IndexedLabeledPath<N,L>(this);
	}
	

	
}
