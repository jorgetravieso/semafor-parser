/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * FixTokenization.java is part of SEMAFOR 2.0.
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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;


public class FixTokenization
{
	public static ArrayList<String> readSentencesFromFile(String file)
	{
		ArrayList<String> result = new ArrayList<String>();
		try
		{
			BufferedReader bReader = new BufferedReader(new FileReader(file));
			String line = null;
			while((line=bReader.readLine())!=null)
			{
				result.add(line.trim());
			}
			bReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}	
	
	public static void writeSentencesToTempFile(String file, ArrayList<String> sentences)
	{
		try
		{
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(file));
			int size = sentences.size();
			System.out.println("Size of sentences:"+size);
			for(int i = 0; i < size; i ++)
			{
				bWriter.write(sentences.get(i).trim()+"\n");
			}
			bWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}	
}

