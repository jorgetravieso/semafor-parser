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

import de.saar.coli.salsa.reiter.framenet.FrameNotFoundException;
import de.saar.coli.salsa.reiter.framenet.FrameElementNotFoundException;
import de.saar.coli.salsa.reiter.framenet.FrameNet;
import de.saar.coli.salsa.reiter.framenet.RealizedFrame;
import de.saar.coli.salsa.reiter.framenet.IToken;

import org.dom4j.Element;

import org.jaxen.dom4j.Dom4jXPath;
import org.jaxen.JaxenException;

import java.util.List;
import java.util.LinkedList;

/**
 * This class is used to analyze the "sem" part of the XML tree.
 * 
 * @author Nils Reiter
 * @since 0.2
 */
public class Semantics {
	/**
	 * The tree of syntactic annotation
	 */
	Tree tree;
	
	/**
	 * The list of realized frames in the semantic part
	 */
	List<RealizedFrame> realizedFrames;
	
	/**
	 * The FrameNet object
	 */
	FrameNet frameNet;
	
	/**
	 * Creates a new Semantics object, based on a XML sem element, 
	 * an already parsed syntactic tree and a FrameNet object
	 * @param element The XML element
	 * @param tree The syntactic tree
	 * @param frameNet The FrameNet object
	 */
	@SuppressWarnings("unchecked")
	public Semantics (Element element, Tree tree, FrameNet frameNet) throws JaxenException {
		this.tree = tree;
		this.realizedFrames = new LinkedList<RealizedFrame>();
		this.frameNet = frameNet;
		List frames = new Dom4jXPath("frames/frame").selectNodes(element);
		for (int i = 0; i < frames.size(); i++) {
			RealizedFrame realizedFrame = getRealizedFrame((Element) frames.get(i));
			if (realizedFrame != null)
				realizedFrames.add(realizedFrame);
		}
	}
	
	/**
	 * Used to access the list of realized frames in this semantic layer
	 * @return The list of realized frames in this semantic layer
	 */
	public List<RealizedFrame> getRealizedFrames() {
		return realizedFrames;
	}
	
	/**
	 * This method is used internally to navigate through the XML hierarchy
	 * @param element The XML element
	 * @return A realized frame
	 */
	@SuppressWarnings("unchecked")
	private RealizedFrame getRealizedFrame(Element element) throws JaxenException {
		String framename = element.attributeValue("name");
		Element targetNode = (Element) new Dom4jXPath("target").selectNodes(element).get(0);
		String frameId = getIdRef(((Element) new Dom4jXPath("fenode").selectNodes(targetNode).get(0)));
		List frameElementNodes = new Dom4jXPath("fe").selectNodes(element);
		
		try {
			RealizedFrame realizedFrame = new RealizedFrame(frameNet.getFrame(framename), 
					getRealization(frameId), 
					frameId);
			for (int k = 0; k < frameElementNodes.size(); k++) {
				Element fenode = (Element) frameElementNodes.get(k);
				
				String frameElementName = fenode.attributeValue("name");
				
				String frameElementId = getIdRef((Element) new Dom4jXPath("fenode").selectNodes(fenode).get(0));
				try {
					realizedFrame.addRealizedFrameElement(frameElementName, 
							getRealization(frameElementId));
				} catch (FrameElementNotFoundException e) {
					e.printStackTrace();
				}
			}
			return realizedFrame;
		} catch (FrameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private IToken getRealization(String id) {
		return this.tree.getTreeElement(id);
	}
	
	private String getIdRef(Element node) {
		if (node != null && node.getName().equalsIgnoreCase("fenode")) {
			Element element = (Element) node;
			return element.attributeValue("idref");
		}
		return "";
	}
	
}
