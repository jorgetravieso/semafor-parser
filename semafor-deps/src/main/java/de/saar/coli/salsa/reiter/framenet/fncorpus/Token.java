package de.saar.coli.salsa.reiter.framenet.fncorpus;

import de.saar.coli.salsa.reiter.framenet.*;

import de.uniheidelberg.cl.reiter.util.Range;

public class Token extends AbstractToken {
	
	Range range;
	
	Sentence sentence;
	
	protected Token(Sentence sentence, Range range) {
		this.sentence = sentence;
		this.range = range;
	}
	
	public Range getRange() {
		return this.range;
	}
	
	public String toString() {
		return sentence.getText().substring(range.getElement1(), range.getElement2());
	}

	public Sentence getSentence() {
		return sentence;
	}
	
	
}
