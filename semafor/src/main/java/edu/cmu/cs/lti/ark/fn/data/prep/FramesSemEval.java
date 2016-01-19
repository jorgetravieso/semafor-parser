/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * FramesSemEval.java is part of SEMAFOR 2.0.
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import edu.cmu.cs.lti.ark.util.XmlUtils;



public class FramesSemEval
{
	public static final String[] devSet = {
		"/mal2/dipanjan/experiments/FramenetParsing/semeval-2007-task19/train/ANC/HistoryOfLasVegas.xml",
		"/mal2/dipanjan/experiments/FramenetParsing/semeval-2007-task19/train/ANC/StephanopoulosCrimes.xml",
		"/mal2/dipanjan/experiments/FramenetParsing/semeval-2007-task19/train/NTI/Iran_Biological.xml",
		"/mal2/dipanjan/experiments/FramenetParsing/semeval-2007-task19/train/NTI/NorthKorea_Introduction.xml",
		"/mal2/dipanjan/experiments/FramenetParsing/semeval-2007-task19/train/NTI/WMDNews_042106.xml"
	};
	
	public static void test()
	{
		ArrayList<String> sentences1 = FixTokenization.readSentencesFromFile("/mal2/dipanjan/experiments/FramenetParsing/semeval-2007-task19/test/ANC/IntroOfDublin.txt");
		sentences1.addAll(FixTokenization.readSentencesFromFile("/mal2/dipanjan/experiments/FramenetParsing/semeval-2007-task19/test/NTI/ChinaOverview.txt"));
		sentences1.addAll(FixTokenization.readSentencesFromFile("/mal2/dipanjan/experiments/FramenetParsing/semeval-2007-task19/test/NTI/workAdvances.txt"));
		FixTokenization.writeSentencesToTempFile("/mal2/dipanjan/experiments/FramenetParsing/framenet_1.3/ddData/semeval.fulltest.sentences", sentences1);		
	}
	
	public static void train()
	{
		Arrays.sort(devSet);
		try
		{
			BufferedWriter bWriter = new BufferedWriter(new FileWriter("/mal2/dipanjan/experiments/FramenetParsing/framenet_1.3/ddData/semeval.fulltrain.sentences"));
			String[] sets = {"ANC","NTI","PropBank"};
			for(int i = 0; i < sets.length; i ++)
			{
				String directory = "/mal2/dipanjan/experiments/FramenetParsing/semeval-2007-task19/train/"+sets[i]; 
				File dir = new File(directory);
				FilenameFilter filter = new FilenameFilter(){
					public boolean accept(File dir, String name) {
						return name.endsWith(".xml");
					}
				};
				String[] fileNames = dir.list(filter);
				int len = fileNames.length;
				for(int j = 0; j < len; j ++)
				{
					String fileName = dir.getAbsolutePath()+"/"+fileNames[j];
					if(Arrays.binarySearch(devSet, fileName)>=0)
						continue;
					getLUStatistics(fileName,bWriter);
				}
			}
			bWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void getLUStatistics(String fileName, BufferedWriter bWriter) throws Exception
	{
		int totalNumberOfSentences = 0;
		Document d = XmlUtils.parseXmlFile(fileName, false);
		Element[] luList = XmlUtils.applyXPath(d, "/corpus/documents/document/paragraphs/paragraph/sentences/sentence/text");
		int listLen = luList.length;
		System.out.println(listLen);
		for(int j = 0; j < listLen; j++)
		{
			String text = luList[j].getTextContent().trim();
			bWriter.write(text+"\n");
			totalNumberOfSentences++;
		}
		System.out.println("Total number of sentences:"+totalNumberOfSentences);
	}
}


















