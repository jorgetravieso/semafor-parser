package de.saar.coli.salsa.reiter.framenet;

import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.jaxen.XPath;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;

/**
 * This class represents a frame read from a SalsaTigerXML file.
 * @author Nils Reiter
 * @since 0.4 
 */
public class STXFrame extends Frame {
	
	
	static final private long serialVersionUID = 1l;
	
	@SuppressWarnings("unchecked")
	protected STXFrame(FrameNet fn, Element element, Map<String, String> namespaces) {
		this.name = element.attributeValue("name");
		this.cDate = "";
		this.definition = "";
		this.id = "";
		try {
			XPath xpath =  new Dom4jXPath("clt:element");
			xpath.setNamespaceContext(new SimpleNamespaceContext(namespaces));

			List fe_nodelist = xpath.selectNodes(element);
			for (int i = 0; i < fe_nodelist.size(); i++) {
				Element feX = (Element) fe_nodelist.get(i);
				FrameElement fe = new STXFrameElement(feX);
				this.frameElements.put(fe.getName(), fe);
			}
		} catch (JaxenException e) {
			e.printStackTrace();
		}
		
		
	}

}
