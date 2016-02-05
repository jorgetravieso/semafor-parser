package de.saar.coli.salsa.reiter.framenet;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;


/**
 * This class imports FrameNet data from a SalsaTigerXML file. 
 * In Salto, frames and frame elements can be defined ad-hoc. 
 * This class can be used to extract them from the file and merge 
 * them with another (original) FrameNet database.
 * 
 * @author Nils Reiter
 * @since 0.4
 *
 */
public class STXDatabaseReader extends DatabaseReader {

	/**
	 * The XML document with the SalsaTigerXML information
	 */
	Document document;
	
	/**
	 * The used namespaces
	 */
	Map<String, String> namespaces = null;
	
	/**
	 * Creates the object by parsing the given file
	 * @param salsaTigerFile
	 */
	public STXDatabaseReader(File salsaTigerFile) {
		super();
		try {
			SAXReader reader = new SAXReader();
			this.document = reader.read(salsaTigerFile);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		init();
	}
	
	/**
	 * If the file has already been parsed, this constructor
	 * can be used
	 * @param document The parsed document
	 */
	public STXDatabaseReader(Document document) {
		super();
		this.document = document;
		
		init();
	}
	
	private void init() {
		namespaces = new HashMap<String, String>();
		namespaces.put("clt", "http://www.clt-st.de/framenet/frame-database");
	}
	
	/**
	 * This method reads the document and adds the frames to the
	 * given FrameNet database.
	 */
	@SuppressWarnings("unchecked")
	public boolean read(FrameNet fn) {
		
		try {
			XPath xpath = new Dom4jXPath("/corpus/head/clt:frames/clt:frame");
			xpath.setNamespaceContext(new SimpleNamespaceContext(namespaces));
			List frames = xpath.selectNodes(document);
			
			for (Object frameNode : frames) {
				if (frameNode != null) {
					Element frameX = (Element) frameNode;
					Frame frame = new STXFrame(fn, frameX, namespaces);
					frame.linkFrameNet(fn);

				}
			}
			return true;
		} catch (JaxenException e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
