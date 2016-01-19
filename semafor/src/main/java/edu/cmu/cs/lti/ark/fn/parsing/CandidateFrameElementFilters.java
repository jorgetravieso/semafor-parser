/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * CandidateFrameElementFilters.java is part of SEMAFOR 2.0.
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
package edu.cmu.cs.lti.ark.fn.parsing;

import edu.cmu.cs.lti.ark.util.ds.Range;
import edu.cmu.cs.lti.ark.util.ds.Range0Based;

/**
 * Classes defining heuristic approaches to identifying candidate frame element filler spans 
 * in a sentence given its parse. CandidateFrameElementFiller is the interface for these classes.
 * Training data is passed to the constructor, so it can extract counts of patterns used in filtering.
 * The method getCandidateFillerSpanRanges() is used to identify all candidate spans in a sentence; 
 * these are the only ones for which features will be extracted.
 * 
 * @author Nathan Schneider (nschneid)
 * @since 2009-04-07
 * @see FeatureExtractor
 */
public class CandidateFrameElementFilters {
	public static final Range0Based EMPTY_SPAN = new Range0Based(-1,-1, false);	// indicates that a frame element has no overt filler
	
	public static Range0Based createSpanRange(int start, int end) {
		if (start<0 && end<0)
			return EMPTY_SPAN;
		return new Range0Based(start,end,true);
	}
	
	public static boolean isEmptySpan(Range span) {
		return span==EMPTY_SPAN;
	}
}
