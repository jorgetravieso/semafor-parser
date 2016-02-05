package de.saar.coli.salsa.reiter.framenet.flatformat;

import de.saar.coli.salsa.reiter.framenet.AbstractToken;
import de.saar.coli.salsa.reiter.framenet.Sentence;
import de.uniheidelberg.cl.reiter.util.Range;

public class Token extends AbstractToken {
	Sentence sentence = null;
	Range range = null;
	
	protected Token(Sentence sentence, Range range) {
		this.sentence = sentence;
		this.range = range;
	}
	
	public Range getRange() {
		return this.range;
	}

	public Sentence getSentence() {
		return this.sentence;
	}

	public String toString() {
		//return this.range.toString();
		return this.sentence.getSurface(this.range);
	}
}
