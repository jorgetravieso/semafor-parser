package de.saar.coli.salsa.reiter.framenet;

import org.dom4j.Element;
import org.jaxen.dom4j.Dom4jXPath;
import org.jaxen.JaxenException;


import java.util.LinkedList;
import java.util.List;

/**
 * This class provides the Lexical Unit data from the original FrameNet XML files
 * @author Nils Reiter
 * @since 0.4
 *
 */
public class FNLexicalUnit extends LexicalUnit {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new lexical unit from the corresponding XML node.
	 * The node should provide the following attributes: ID, name, status, cDate, lemmaId.
	 * A definition node should be a direct sub node of the node.
	 * <br/>
	 * <code>
	 * &lt;lexunit ID="3" name="cause.n" pos="N" status="Finished_Initial" cDate="Thu Feb 08 13:27:25 PST 2001" lemmaId="5802"&gt;
	 * 
	 * &lt;definition&gt;COD: a person or thing that gives rise to an action, phenomenon, or condition.&lt;/definition&gt;
	 * 
	 * &lt;/lexunit&gt;</code><br/>
	 * The above, for instance, is a complete node that can be processed by the constructor.
	 * 
	 * @param node The node for the lexical unit.
	 */
	@SuppressWarnings("unchecked")
	protected FNLexicalUnit(Frame frame, Element node) throws JaxenException {
		this.frame = frame;
		id = node.attributeValue("ID");
		name = node.attributeValue("name");
		status = node.attributeValue("status");
		cDate = node.attributeValue("cDate");
		lemmaID = node.attributeValue("lemmaId");
		lemma = name.substring(0, name.lastIndexOf('.'));
		definition = node.element("definition").getText().replace('\n', ' ');
		
		pos = node.attributeValue("pos").toLowerCase();
		
		lexemes = new LinkedList<Lexeme>();
		
		List lexeme_nodelist = new Dom4jXPath("lexemes/lexeme").selectNodes(node);
		for (int i = 0; i < lexeme_nodelist.size(); i++) {
			Element lexeme_element = (Element) lexeme_nodelist.get(i);
			Lexeme lexeme = new FNLexeme(lexeme_element);
			lexemes.add(lexeme);
		}
		
		this.frame.getFramenet().addLexicalUnit(this);
	}
}
