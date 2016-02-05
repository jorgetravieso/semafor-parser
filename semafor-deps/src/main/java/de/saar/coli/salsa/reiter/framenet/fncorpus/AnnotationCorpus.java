package de.saar.coli.salsa.reiter.framenet.fncorpus;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;

import de.saar.coli.salsa.reiter.framenet.*;

/**
 * This class can be used to access the luXML files, i.e., 
 * the examples in the FrameNet annotation. It's heavily inter-
 * weaved with and based on the FrameNetCorpus class.
 * 
 * 
 * @author reiter
 * @since 0.4
 *
 */
public class AnnotationCorpus extends CorpusReader {

	/**
	 * An index of AnnotatedLexicalUnits
	 */
	Map<LexicalUnit, AnnotatedLexicalUnit> annotations;
	
	/**
	 * A constructor, just as in FrameNetCorpus.
	 * @param frameNet
	 * @param logger
	 */
	public AnnotationCorpus(FrameNet frameNet, Logger logger) {
		super(frameNet, logger);
		this.annotations = new HashMap<LexicalUnit, AnnotatedLexicalUnit>();
	}

	/**
	 * Returns the annotated examples with the given lexical unit.
	 * @param lu The lexical unit
	 * @return An object of the class AnnotatedLexicalUnit
	 */
	public AnnotatedLexicalUnit getAnnotation(LexicalUnit lu) {
		return this.annotations.get(lu);
	}
	
	@Override
	public void parse(File directory)  {
		parse(directory, ".*");
	}
	
	/**
	 * An extension of {@link #parse(File)}. Allows the specification 
	 * of a pattern, which must be contained in the filename of the files
	 * to be loaded. 
	 * 
	 * Example: If you specify "2442" as pattern string, only lu-files with 2442
	 * in their names will be loaded.
	 * 
	 * @param directory The luXML directory
	 * @param pattern A pattern
	 */
	public void parse(File directory, String pattern) {
		if (directory.isDirectory() && directory.canRead()) {
			for (File file : directory.listFiles()) {
				if (file.getName().contains(pattern))
					this.parseFile(file);
			}
		}
	}
	
	private void parseFile(File file) {
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
		this.getLogger().info("XML Document ("+ file.getAbsolutePath() + ") has been read.");

		try {
			Element luanno = (Element) new Dom4jXPath("/lexunit-annotation").selectSingleNode(document);
			
			LexicalUnit lu = frameNet.getLexicalUnit(luanno.attributeValue("ID"));

			annotations.put(lu, new AnnotatedLexicalUnit(this, luanno, lu));
						
		} catch (JaxenException e) {
			e.printStackTrace();
		} catch (FrameNotFoundException e) {
			e.printStackTrace();
		}
	}

}
