package de.saar.coli.salsa.reiter.framenet.fncorpus;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import de.saar.coli.salsa.reiter.framenet.LexicalUnit;
import de.saar.coli.salsa.reiter.framenet.Lexeme;
import de.saar.coli.salsa.reiter.framenet.Frame;
import de.saar.coli.salsa.reiter.framenet.FrameNotFoundException;
import de.saar.coli.salsa.reiter.framenet.FrameElementNotFoundException;

import org.dom4j.Element;
import org.jaxen.dom4j.Dom4jXPath;
import org.jaxen.JaxenException;

/**
 * This class represents one luXML-file, i.e., a lexical unit with
 * its example annotations.
 * 
 * @author reiter
 * @since 0.4
 *
 */
public class AnnotatedLexicalUnit {
	/**
	 * The lexical unit in the FrameNet database
	 */
	LexicalUnit lexicalUnit = null;
	
	/**
	 * The definition string
	 */
	String definition = null;
	
	/**
	 * A list of incorporated FEs
	 */
	String incorporatedFE = null;
	
	/**
	 * The corpus
	 */
	AnnotationCorpus annotationCorpus = null;
	
	/**
	 * A list of sentences containing this lexical unit
	 */
	List<Sentence> sentences = null;
	
	/**
	 * The frame in which this lexical unit is listed
	 */
	Frame frame = null;
	
	@SuppressWarnings("unchecked")
	/**
	 * The constructor processes the XML node(s).
	 */
	public AnnotatedLexicalUnit(AnnotationCorpus annotationCorpus, Element element, LexicalUnit lu) 
	throws JaxenException, FrameNotFoundException {
		this.lexicalUnit = lu;
		this.annotationCorpus = annotationCorpus;
		this.incorporatedFE = element.attributeValue("incorporatedFE");
		this.definition = element.valueOf("definition");
		this.sentences = new LinkedList<Sentence>();
		this.frame = annotationCorpus.getFrameNet().getFrame(element.attributeValue("frame"));
		
		Iterator sc_iter = new Dom4jXPath("subcorpus").selectNodes(element).iterator();
		while (sc_iter.hasNext()) {
			Element subcorpus = (Element) sc_iter.next();
			
			Iterator as_iter = new Dom4jXPath("annotationSet").selectNodes(subcorpus).iterator();
			while(as_iter.hasNext()) {
				Element annotationSet_element = (Element) as_iter.next();
				Sentence sentence = new Sentence(this.annotationCorpus, annotationSet_element.element("sentence"));
				sentences.add(sentence);
				try {
					new AnnotationSet(sentence, annotationSet_element, frame, lexicalUnit);
				} catch (FrameElementNotFoundException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

	/**
	 * Returns the definition of the lexical unit
	 * @return a string
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * Returns the incorporared frame element.
	 * @return A string
	 */
	public String getIncorporatedFE() {
		return incorporatedFE;
	}
	
	/**
	 * Returns a collection of lexemes in this lexical unit
	 * @return The lexemes of this lexical unit
	 */
	public List<Lexeme> getLexemes() {
		return this.lexicalUnit.getLexemes();
	}

	/**
	 * Returns a list of sentences containing this lexical unit
	 * @return A list of sentences
	 */
	public List<Sentence> getSentences() {
		return sentences;
	}

	/**
	 * Returns the frame 
	 * @return a frame
	 */
	public Frame getFrame() {
		return frame;
	}
}
