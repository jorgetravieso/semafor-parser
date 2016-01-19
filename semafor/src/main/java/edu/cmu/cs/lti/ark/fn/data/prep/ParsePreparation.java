/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * ParsePreparation.java is part of SEMAFOR 2.0.
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
package edu.cmu.cs.lti.ark.fn.data.prep;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;




public class ParsePreparation
{
	/**
	 * @param file Path to the file
	 * @return List of all lines from the given file
	 */
	public static ArrayList<String> readSentencesFromFile(String file) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			return readLinesFromFile(reader, 0, true);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Reads up to a certain number of lines of a file, starting at the current position of the {@link BufferedReader}
	 * @param reader
	 * @param n Nonnegative number of lines to read ({@code 0} to read all lines).
	 * @param closeAtEOF If {@code true} and either {@code n==0} or fewer than {@code n} line could be fetched 
	 * due to the end of the file, the stream will be closed. Otherwise, closing the stream is the caller's responsibility.
	 * @return List of the lines in the range
	 */
	public static ArrayList<String> readLinesFromFile(BufferedReader reader, int n, boolean closeAtEOF) {
		int j = 0;
		ArrayList<String> result = new ArrayList<String>();
		try
		{
			String line = null;
			while((n==0 || j<n) && (line=reader.readLine())!=null)
			{
				result.add(line.trim());
				j++;
			}
			if (closeAtEOF && (n==0 || j<n))
				reader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}


}
