/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * DataPointWithElements.java is part of SEMAFOR 2.0.
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.cs.lti.ark.fn.parsing.CandidateFrameElementFilters;
import edu.cmu.cs.lti.ark.util.Interner;
import edu.cmu.cs.lti.ark.util.XmlUtils;
import edu.cmu.cs.lti.ark.util.ds.Pair;
import edu.cmu.cs.lti.ark.util.ds.Range;
import edu.cmu.cs.lti.ark.util.ds.Range0Based;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParses;

public class DataPointWithElements extends DataPoint
{
	private final int numSpans;	// includes the target span and any frame elements
	private String[] frameElementNames;
	private ArrayList<Range0Based> frameElementTokenRanges;
	
	private String target;
	
	private static final String RE_FE = "(\t([^\\t]+)\t(\\d+([:]\\d+)?))";	// \t frame_name \t token_range
	
	public DataPointWithElements(DependencyParses parses, String frameElementsLine, String dataSet) {
		super(parses, 0, dataSet);
		
		Pair<Integer,Pair<String,String>> parts = decomposeFELine(frameElementsLine);
		
		numSpans = parts.getFirst();
		
		String mainFramePortion = parts.getSecond().getFirst();
		this.processFrameLine(mainFramePortion);
				
		String fePortion = parts.getSecond().getSecond();
		processFrameElements(fePortion);
	}
	
	protected static Pair<Integer,Pair<String,String>> decomposeFELine(String frameElementsLine) {
		Pattern r = Pattern.compile("^(\\d+)\t([^\\t]*\t[^\\t]*\t[^\\t]*\t[^\\t]*\t\\d+)(" + RE_FE + "*)\\s*$");
		Matcher m = r.matcher(frameElementsLine);
		try {
			if (!m.find()) {
				throw new Exception("Error processing frame elements line:\n" + frameElementsLine);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		
		int numSpans = Integer.parseInt(m.group(1));
		
		String mainFramePortion = m.group(2);
		String fePortion = m.group(3);
		
		Pair<String,String> portions = new Pair<String,String>(mainFramePortion, fePortion);
		return new Pair<Integer,Pair<String,String>>(numSpans, portions);
	}
	
	public String getTarget() {
		if (target==null) {
			target = getTokens(this.getParses().getSentence(), this.getTokenNums());
		}
		return target;
	}
	
	public void processFrameElements(String frameElementsString)
	{
		// Frame elements
		frameElementNames = new String[numSpans-1];
		frameElementTokenRanges = new ArrayList<Range0Based>(numSpans-1);
		
		int nFE = 0;
		if (!frameElementsString.trim().equals("")) {
			Matcher feM = Pattern.compile(RE_FE).matcher(frameElementsString);
			while (feM.find()) {
				String feName = (String)Interner.globalIntern(feM.group(2));
				String feSpan = feM.group(3);
				int feStart = -1;
				int feEnd = -1;
				if (feSpan.contains(":")) {	// startIndex:endIndex range
					String[] rangeParts = feSpan.split(":");
					feStart = Integer.parseInt(rangeParts[0]);
					feEnd = Integer.parseInt(rangeParts[1]);
				}
				else {	// single token in the span
					feStart = feEnd = Integer.parseInt(feSpan);
				}
				
				frameElementNames[nFE] = feName;
				frameElementTokenRanges.add(CandidateFrameElementFilters.createSpanRange(feStart, feEnd));
				nFE++;
			}
		}
		if (nFE!=numSpans-1) {	// sanity check
			System.err.println("Unable to read correct number of frame elements from string (found " + Integer.toString(nFE) + ", should be " + Integer.toString(numSpans-1) + "):\n" + frameElementsString);
			System.exit(1);
		}
		
	}
	
	/**
	 * @return An array listing, in the order they were annotated in the XML file, the frame element names 
	 * (of this frame) corresponding to annotated filler spans in the sentence. The same element name may be 
	 * listed multiple times. Elements filled by null instantiations are not included.
	 */
	public String[] getOvertFilledFrameElementNames() {
		return frameElementNames;
	}
	
	/**
	 * @return A list of 0-based word token index ranges (startIndex, endIndex) (inclusive) delimiting spans which are 
	 * frame element fillers. This list is parallel to the list of frame element names returned by {@link #getOvertFilledFrameElementNames()}.
	 */
	public ArrayList<Range0Based> getOvertFrameElementFillerSpans() {
		return frameElementTokenRanges;
	}
	
	public Node buildAnnotationSetNode(Document doc, int parentId, int num, String orgLine)
	{
		Node node = super.buildAnnotationSetNode(doc, parentId, num, orgLine);
		
		Node layers = XmlUtils.applyXPath(node, "layers")[0];
		Node feLayer = doc.createElement("layer");
		int setId = parentId*100+num;
		int layerId = setId*100+2;
		XmlUtils.addAttribute(doc,"ID", (Element)feLayer,""+layerId);
		XmlUtils.addAttribute(doc,"name", (Element)feLayer,"FE");
		layers.appendChild(feLayer);
		Node labels = doc.createElement("labels");
		feLayer.appendChild(labels);
		
		List<Range0Based> fillerSpans = getOvertFrameElementFillerSpans();
		String[] feNames = getOvertFilledFrameElementNames();
		for (int i=0; i<feNames.length; i++) {
			String feName = feNames[i];
			Range fillerSpan = fillerSpans.get(i);
			
			int labelId = layerId*100+i+1;
			Node label = doc.createElement("label");
			XmlUtils.addAttribute(doc,"ID", (Element)label,""+labelId);
			XmlUtils.addAttribute(doc,"name", (Element)label,feName);
			
			int startCharIndex = getCharacterIndicesForToken(fillerSpan.getStart()).getStart();
			int endCharIndex = getCharacterIndicesForToken(fillerSpan.getEnd()).getEnd();
			XmlUtils.addAttribute(doc,"start", (Element)label,""+startCharIndex);
			XmlUtils.addAttribute(doc,"end", (Element)label,""+endCharIndex);
			labels.appendChild(label);
		}
		
		return node;
	}

	public static String getTokens(String sentence, int[] intNums)
	{
		StringTokenizer st = new StringTokenizer(sentence, " ", true);
		int count = 0;
		String result="";
		Arrays.sort(intNums);
		while(st.hasMoreTokens())
		{
			String token = (String)Interner.globalIntern(st.nextToken().trim());
			if(token.equals(""))
				continue;
			if(Arrays.binarySearch(intNums, count)>=0)
				result+=token+" ";
			count++;
		}
		return result.trim();
	}
}
