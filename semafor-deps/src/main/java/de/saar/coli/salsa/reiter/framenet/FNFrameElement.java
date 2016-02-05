package de.saar.coli.salsa.reiter.framenet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.dom4j.Element;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;

/**
 * This class provides the Frame Element data from the original FrameNet XML files
 * @author Nils Reiter
 * @since 0.4
 */
public class FNFrameElement extends FrameElement {
	
	private static final long serialVersionUID = 1L;

	
	/**
	 * Creates a new FrameElement object. 
	 * The XML node should provide the following attributes: name, abbrev, cDate, coreType and ID.
	 * There should be a definition element present as a sub node of the given node.
	 * 
	 * @param frame The frame to which this frame element belongs.
	 * @param node The node in the XML document
	 */
	@SuppressWarnings("unchecked")
	protected FNFrameElement(Frame frame, Element node) throws JaxenException {
		super();
		name = node.attributeValue("name");
		abbreviation = node.attributeValue("abbrev");
		cDate = node.attributeValue("cDate");
		coreType = node.attributeValue("coreType");
		id = node.attributeValue("ID");
		definition = node.element("definition").getText();
		
		this.frame = frame;
		superFrameElements = new HashMap<Frame, FrameElement>();
		subFrameElements = new HashMap<Frame, FrameElement>();		
		superFrameElementsDistance = new TreeMap<Integer, Set<FrameElement>>();
		semanticTypes = new HashSet<SemanticType>();
		
		relationsAsSuper = new HashMap<FrameNetRelation, Map<Frame, FrameElementRelation>>();
		relationsAsSub = new HashMap<FrameNetRelation, Map<Frame, FrameElementRelation>>();
		
		// May cause problems when there is no semTypes element. 
		// In 1.3, there are semTypes-elements, even when there is no semType-element.
		List st_nodelist = (new Dom4jXPath("semTypes/semType")).selectNodes(node); //((Element) node.getElementsByTagName("semTypes").item(0)).getElementsByTagName("semType");
		for (int i = 0; i < st_nodelist.size(); i++) {
			Element stnode = (Element) st_nodelist.get(i);
			SemanticType st = frame.framenet.getSemanticType(stnode.attributeValue("name"), true);
			this.semanticTypes.add(st);
			st.registerFrameElement(this);
		}
	}
}
