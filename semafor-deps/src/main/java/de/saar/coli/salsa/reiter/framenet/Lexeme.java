package de.saar.coli.salsa.reiter.framenet;

import java.io.Serializable;

/**
 * This class represents a lexeme in FrameNet.
 * @author Nils Reiter
 * @since 0.4
 *
 */
public class Lexeme implements Serializable, IHasID {

	private final static long serialVersionUID = 1l;
	
	/**
	 * The XML id of the lexeme
	 */
	String id;
	
	String pos;
	
	boolean breakBefore;
	
	boolean headword;
	
	String value;
	
	/**
	 * Returns the part of speech tag of the lexeme
	 * @return A string: N, V or A
	 */
	public String getPos() {
		return pos;
	}

	/**
	 * Returns true, if the lexeme is marked with breakBefore
	 * @return true or false
	 */
	public boolean isBreakBefore() {
		return breakBefore;
	}

	/**
	 * Returns true, if the lexeme is marked as a headword
	 * @return true or false
	 */
	public boolean isHeadword() {
		return headword;
	}

	/**
	 * Returns the string value of the lexeme
	 * @return A string
	 */
	public String getValue() {
		return value;
	}


	public String getId() {
		return id;
	}
	
	
	public String toString() {
		return this.getValue();
	}

}
