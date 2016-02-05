/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das Language Technologies Institute, Carnegie Mellon University, All Rights Reserved.
 *
 * RootedDAGNode.java is part of SEMAFOR 2.0.
 *
 * SEMAFOR 2.0 is free software: you can redistribute it and/or modify  it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * SEMAFOR 2.0 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SEMAFOR 2.0.  If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.cmu.cs.lti.ark.util.ds.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Node in a rooted DAG (directed acyclic graph). There should be a single root node, with index 0. NOTE: This class does not check
 * for cycles!
 *
 * @param N Node type; same as the name of the subclass being defined (proxy for SELF_TYPE)
 * @author Nathan Schneider (nschneid)
 * @since 2009-04-04
 */
public class RootedDAGNode<N extends RootedDAGNode<N>> extends Node<N> implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7679835927268870379L;
    /**
     * indices of the node's parents
     */
    protected List<Integer> parentIndices;
    /**
     * depth (level) of the node in the tree, with the root node having depth 0
     */
    protected int depth;
    protected List<N> children;

    protected List<N> parents;

    public RootedDAGNode() {

    }

    public List<N> getChildren() {
        return children;
    }

    public void setChildren(List<N> c) throws GraphException {
        children = c;
    }

    /**
     * Adds a child of this node, unless the specified child already exists.
     *
     * @return null if the child was added, otherwise the previously-added equivalent child node
     */
    public N addChild(N c) throws GraphException {
        if (children == null) {
            children = new ArrayList<N>();
        } else {
            int j = children.indexOf(c);
            if (j > -1) {
                return children.get(j);
            }
        }
        children.add(c);
        return null;
    }

    /**
     * Equivalent to calling {@link #addChild(RootedDAGNode)} on the first argument and {@link #addParent(RootedDAGNode)} on the
     * second.
     */
    public static <G extends RootedDAGNode<G>> void link(G parent, G child) throws GraphException {
        parent.addChild(child);
        child.addParent(parent);
    }


    public void setParentIndices(List<Integer> indices) throws GraphException {
        parentIndices = indices;
    }

    public List<Integer> getParentIndices() {
        return parentIndices;
    }

    public void setParents(List<N> p) throws GraphException {
        parents = p;
    }

    /**
     * Adds a parent of this node, unless the specified parent already exists.
     *
     * @return null if the parent was added, otherwise the previously-added equivalent parent node
     */
    public N addParent(N p) throws GraphException {
        if (parents == null) {
            parents = new ArrayList<N>();
        } else {
            int j = parents.indexOf(p);
            if (j > -1) {
                return parents.get(j);
            }
        }
        parents.add(p);
        return null;
    }


    public void setDepth(int d) {
        depth = d;
    }

    public int getDepth() {
        return depth;
    }

    public boolean hasChildren() {
        return (this.children != null && this.children.size() > 0);
    }

    /**
     * @param includeSelf If true, the current node will be included in the list, along with its descendants
     * @return A list of all descendants, obtained by breadth-first traversal of the graph
     */
    @SuppressWarnings("unchecked")
    public List<N> getDescendants(boolean includeSelf) {
        // Add all descendants to the queue (breadth-first traversal)
        List<N> queued = new ArrayList<N>();
        queued.add((N) this);
        int i = 0;
        while (i < queued.size()) {
            N n = queued.get(i);
            for (N c : n.getChildren()) {
                if (!queued.contains(c)) {
                    queued.add(c);
                }
            }
            i++;
        }
        if (!includeSelf) {
            queued.remove(0);
        }
        return queued;
    }


}
