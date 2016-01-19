/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * ParseUtils.java is part of SEMAFOR 2.0.
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
package edu.cmu.cs.lti.ark.fn.evaluation;

import java.util.ArrayList;
import java.util.StringTokenizer;



public class ParseUtils
{
	public static ArrayList<String> getRightInputForFrameIdentification(String segs)
	{
		ArrayList<String> result = new ArrayList<String>();
		String line = segs.trim();
		StringTokenizer st = new StringTokenizer(line,"\t");
		while(st.hasMoreTokens())
		{
			String tok = st.nextToken();
			int lastInd = tok.lastIndexOf("#");
			String rest = tok.substring(lastInd+1);
			if(rest.equals("true"))	// token(s) comprise a gold target
			{
				String ind = tok.substring(0,lastInd);	// the token number(s) for the potential target
				result.add("Null\t"+ind+"\t"+"0");
			}
		}
		return result;
	}
}
