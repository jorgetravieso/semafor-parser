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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.jaxen.dom4j.Dom4jXPath;
import org.jaxen.JaxenException;

/*import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;*/


import de.saar.coli.salsa.reiter.framenet.FrameElementNotFoundException;
import de.saar.coli.salsa.reiter.framenet.FrameNet;
import de.saar.coli.salsa.reiter.framenet.FrameNotFoundException;
import de.saar.coli.salsa.reiter.framenet.CorpusReader;
import de.saar.coli.salsa.reiter.framenet.RealizedFrame;
import de.saar.coli.salsa.reiter.framenet.STXDatabaseReader;

import java.io.File;

import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class can be used to "parse" SalsaTigerXML, 
 * a format describing FN-annotated text (see Erk and Pado, 2004).
 * SalsaTigerXML contains both syntactic and semantic features. This
 * class only extracts the semantic features using shallow XPath 
 * techniques. It does not try to do a complete parse. 
 * 
 * The contents of the file is converted to a set of {@link RealizedFrame} 
 * objects. 
 * 
 * This class assumes, that the sentence ids are unique,
 * i.e. that each sentence id occurs only once.
 * 
 * @author Nils Reiter
 * @version 0.3
 * @since 0.2
 *
 */
public class SalsaTigerXML extends CorpusReader {
	
	/**
	 * Creates a new SalsaTigerXML object
	 * 
	 * @param frameNet The FrameNet object for this annotation. For now, it should 
	 * be of the same FrameNet version. Theoretically, there should be some mapping
	 * possible using framesDiff.xml, but this has not been tested.
	 */	
	public SalsaTigerXML(FrameNet frameNet, Logger logger) {
		super(frameNet, logger);
		
	}
	
	/**
	 * This method takes a File argument, parses the file and appends the found 
	 * sentences to the list of sentences found after the creation of the object.
	 * 
	 * In version 0.2, no validation is done.
	 * 
	 * @param file The file containing SalsaTigerXML
	 */
	@SuppressWarnings("unchecked")
	public void parse(File file) throws FrameNotFoundException, FrameElementNotFoundException {
		

		
		// Make document
		Document document = null;
		try {
			SAXReader reader = new SAXReader();
			document = reader.read(file);
		} catch (DocumentException e) {
			this.getLogger().severe(e.getMessage());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (this.getLogger() != null)
			this.getLogger().info("XML Document ("+ file.getAbsolutePath() + ") has been read.");
		
		// interpret frames found in the file
		this.frameNet.readData(new STXDatabaseReader(document));

		
		try {
			List sentences = new Dom4jXPath("/corpus/body/s").selectNodes(document);

			for (Object sent : sentences) {		
				if (sent != null) {
					Element sentence = (Element) sent;
					Sentence s = new Sentence(this.getFrameNet(), sentence);
					getSentences().add(s);
					getSentenceIndex().put(s.getId(), s);
				}
			}
		} catch (JaxenException e) {
			e.printStackTrace();
		}
	}	

}
