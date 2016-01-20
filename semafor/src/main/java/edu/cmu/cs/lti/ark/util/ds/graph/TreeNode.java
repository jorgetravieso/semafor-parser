/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * TreeNode.java is part of SEMAFOR 2.0.
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
package edu.cmu.cs.lti.ark.util.ds.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Node in a tree structure. 
 * The root node should have index 0 and no parent; all other nodes have index > 0 and one parent.
 * 
 * @param T Node type; same as the name of the subclass being defined (proxy for SELF_TYPE)
 * @author Nathan Schneider (nschneid)
 * @since 2009-04-04
 */

public abstract class TreeNode<T extends TreeNode<T>> extends RootedDAGNode<T> {
	
	private static final long serialVersionUID = -5923423841213967909L;


	public TreeNode() {
		
	}
	
	public void setParentIndex(int i) {
		List<Integer> pIndices = new ArrayList<Integer>(1);
		pIndices.add(i);
		try {
			setParentIndices(pIndices);
		}
		catch (GraphException gex) {
			;
		}
	}

	public int getParentIndex() {
		if (parentIndices==null || parentIndices.size()==0)
			return -1;
		return getParentIndices().get(0);
	}

	public T getParent() {
		if (parents==null || parents.size()==0)
			return null;
		return parents.get(0);
	}

	public void setParent(T p) {
		List<T> plist = new ArrayList<T>(1);
		if (p!=null)
			plist.add(p);
		
		try {
			setParents(plist);
		}
		catch (GraphException gex) {
			;
		}
	}
	
	/**
	 * @throws GraphException if attempting to add multiple parents
	 */
	public void setParents(List<T> p) throws GraphException {
		if (p!=null && p.size()>1)
			throw new GraphException("Multiple parents are not allowed in TreeNode");
		super.setParents(p);
	}
	
	/**
	 * @throws GraphException if attempting to add indices for multiple parents
	 */
	public void setParentIndices(List<Integer> p) throws GraphException {
		if (p!=null && p.size()>1)
			throw new GraphException("Multiple parents are not allowed in TreeNode");
		super.setParentIndices(p);
	}
	
	public void setChildren(List<T> c) {
		try {
			super.setChildren(c);
		}
		catch (GraphException gex) {
			;
		}
	}

	/**
	 * @param includeSelf If true, the current node will be included in the list, along with its descendants
	 * @return A list of all descendants, in no particular order
	 */
	@SuppressWarnings("unchecked")
	public List<T> getDescendants(boolean includeSelf) {
		List<T> nodeList = new ArrayList<T>();
		if (includeSelf)
			nodeList.add((T)this);
		
		if (!this.hasChildren())
			return nodeList;
		
		List<T> plist = this.getChildren();
		for (T node : plist)
			nodeList.addAll(node.getDescendants(true));
		
		return nodeList;
	}
}
