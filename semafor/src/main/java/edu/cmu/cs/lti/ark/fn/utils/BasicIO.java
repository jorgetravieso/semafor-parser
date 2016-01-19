/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * BasicIO.java is part of SEMAFOR 2.0.
 * 
 * SEMAFOR 2.0 is free software: you can redistribute it and/or modify  it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * SEMAFOR 2.0 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along
 * with SEMAFOR 2.0.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.cmu.cs.lti.ark.fn.utils;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import edu.cmu.cs.lti.ark.fn.wordnet.WordNetRelations;



public class BasicIO
{
	static final TokenizerFactory TOKENIZER_FACTORY = new IndoEuropeanTokenizerFactory();
    static final SentenceModel SENTENCE_MODEL  = new IndoEuropeanSentenceModel();
	
    public static void main(String[] args) {
    	WordNetRelations wnr = new WordNetRelations("lrdata/stopwords.txt", "fnmfiles/file_properties.xml");
    	String haveLemma = wnr.getLemmaForWord("be", "VB");
    	System.out.println(haveLemma);
    }
    
	
}
