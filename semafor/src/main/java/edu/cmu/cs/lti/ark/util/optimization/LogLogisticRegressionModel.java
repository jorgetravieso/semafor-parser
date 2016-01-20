/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * LogLogisticRegressionModel.java is part of SEMAFOR 2.0.
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
package edu.cmu.cs.lti.ark.util.optimization;

import gnu.trove.TDoubleArrayList;
import gnu.trove.THashMap;
import gnu.trove.TIntArrayList;
import gnu.trove.TObjectDoubleHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import edu.cmu.cs.lti.ark.util.optimization.LDouble.IdentityElement;

/**
 * Defines a logistic regression model for determining whether two sentences from 
 * Wikipedia are parallel or not. 
 * 
 * @author Kevin Gimpel
 * @date 8/17/2007
 */
public class LogLogisticRegressionModel extends LogModel{

	ArrayList<TDoubleArrayList> m_trainingData;
	TIntArrayList m_trainingLabels;
	ArrayList<TDoubleArrayList> m_testData;
	TIntArrayList m_testLabels;
	ArrayList<TDoubleArrayList> m_devData;
	TIntArrayList m_devLabels;
	int m_currentTrainingExample = 0;
	int numParams = 0;
	double lambda = 0.001;
	Map<Integer, LogFormula> mLookupChart;
	boolean mReg = false;

	/*
	 * for LBFGS
	 */
	protected int m_max_its = 2000;
	//not sure how this parameter comes into play
	protected double m_eps = 1.0e-5;
	protected double xtol = 1.0e-10; //estimate of machine precision.  get this right
	//number of corrections, between 3 and 7
	//a higher number means more computation and time, but more accuracy, i guess
	protected int m_num_corrections = 3; 
	protected boolean m_debug = true;
	
	public static void main(String[] args)
	{
		LogLogisticRegressionModel m = new LogLogisticRegressionModel("/Users/dipanjand/work/summer2009/FramenetParsing/BPData/traindata.txt","train");
		try {
			//m.runCustomLBFGS("model/testModel.txt");
			m.runTotallyRandomSGA("/Users/dipanjand/work/summer2009/FramenetParsing/BPData/testModelSGA.txt");
			//m.runBatchSGA("model/testModelSGA.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	protected double classify()
	{
		int numCorrect = 0;
		for (int j = 0; j < m_trainingData.size(); j++)
		{
			TDoubleArrayList currDatum = m_trainingData.get(j);
			// classify using current weights
			double pos = generateTestVal(currDatum);
			pos = new LDouble(pos).exponentiate();
			if ((pos >= 0.5 && m_trainingLabels.get(j) == 1) || (pos < 0.5 && m_trainingLabels.get(j) == 0)) {
				numCorrect++;
			}
		}
		double acc = ((double)numCorrect) / ((double)m_trainingData.size());
		System.out.println("Train: " + numCorrect + " / " + m_trainingData.size() + " = " + acc);
		return acc;
	}	
	


	protected double classifyTest()
	{
		int numCorrect = 0;
		for (int j = 0; j < m_testData.size(); j++) {
			TDoubleArrayList currDatum = m_testData.get(j);
			// classify using current weights
			double pos = generateTestVal(currDatum);
			pos = new LDouble(pos).exponentiate();
			if ((pos >= 0.5 && m_testLabels.get(j) == 1) || (pos < 0.5 && m_testLabels.get(j) == -1)) {
				numCorrect++;
			}
		}
		double acc = ((double)numCorrect) / ((double)m_testData.size());
		System.out.println("Test: " + numCorrect + " / " + m_testData.size() + " = " + acc);
		return acc;
	}	



	protected LogFormula getNextFormula() {
		m_currentTrainingExample++;
		if(m_currentTrainingExample>=m_trainingData.size())
			return null;
		return generateFormulaForTrainingExample(m_trainingData.get(m_currentTrainingExample-1), m_trainingLabels.get(m_currentTrainingExample-1));
	}

	protected LogFormula getFormula(int index) {
		return generateFormulaForTrainingExample(m_trainingData.get(index), m_trainingLabels.get(index));		
	}
	public int getNumTrainingExamples() {
		return m_trainingData.size();
	}

	private LogFormula generateFormulaForTrainingExample(TDoubleArrayList datum, int label)
	{
		m_current = 0;
		m_llcurrent = 0;	
		LogFormula epower = getFormulaObject(LogFormula.Op.EXP);
		LogFormula featweightsum1 = getFormulaObject(LogFormula.Op.PLUS);
		for (int i = 0; i < datum.size(); i++) {
			LogFormula featweight1 = getFormulaObject(LogFormula.Op.TIMES);
			int paramId = A.getInt("param_"+i);
			LogFormula formula = getLazyLookupFormulaObjectCustom(paramId,"param_"+i);
			featweight1.add_arg(formula);
			featweight1.add_arg(getFormulaObject(LDouble.convertToLogDomain(datum.get(i))));
			featweightsum1.add_arg(featweight1);
		}
		epower.add_arg(featweightsum1);
		LogFormula logpart = getFormulaObject(LogFormula.Op.LOG); 
		LogFormula logsum = getFormulaObject(LogFormula.Op.PLUS);
		logsum.add_arg(getFormulaObject(IdentityElement.TIMES_IDENTITY));
		logsum.add_arg(epower);
		logpart.add_arg(logsum);
		if (label == 1)
		{
			LogFormula ret = getFormulaObject(LogFormula.Op.PLUS);
			LogFormula term2 = getFormulaObject(LogFormula.Op.TIMES);
			term2.add_arg(getFormulaObject(LDouble.convertToLogDomain(-1.0)));			
			term2.add_arg(logpart);
			ret.add_arg(featweightsum1);
			ret.add_arg(term2);
			if(mReg)
			{
				LogFormula ret2 = getFormulaObject(LogFormula.Op.PLUS);
				LogFormula regTerm = getRegularizationTerm();
				ret2.add_arg(ret);
				ret2.add_arg(regTerm);
				return ret2;
			}
			else
			{
				return ret;
			}
		} else {
			LogFormula ret = getFormulaObject(LogFormula.Op.TIMES);
			ret.add_arg(getFormulaObject(LDouble.convertToLogDomain(-1.0)));
			ret.add_arg(logpart);
			if(mReg)
			{
				LogFormula ret2 = getFormulaObject(LogFormula.Op.PLUS);
				LogFormula regTerm = getRegularizationTerm();
				ret2.add_arg(ret);
				ret2.add_arg(regTerm);
				return ret2;
			}
			else
			{
				return ret;
			}
		}
	}
	
	private double generateTestVal(TDoubleArrayList datum)
	{
		m_current = 0;
		m_llcurrent = 0;	
		LogFormula epower = getFormulaObject(LogFormula.Op.EXP);
		LogFormula featweightsum1 = getFormulaObject(LogFormula.Op.PLUS);
		for (int i = 0; i < datum.size(); i++) {
			LogFormula featweight1 = getFormulaObject(LogFormula.Op.TIMES);
			int paramId = A.getInt("param_"+i);
			LogFormula formula = getLazyLookupFormulaObjectCustom(paramId,"param_"+i);
			featweight1.add_arg(formula);
			featweight1.add_arg(getFormulaObject(LDouble.convertToLogDomain(datum.get(i))));
			featweightsum1.add_arg(featweight1);
		}
		epower.add_arg(featweightsum1);
		LogFormula logpart = getFormulaObject(LogFormula.Op.LOG); 
		LogFormula logsum = getFormulaObject(LogFormula.Op.PLUS);
		logsum.add_arg(getFormulaObject(IdentityElement.TIMES_IDENTITY));
		logsum.add_arg(epower);
		logpart.add_arg(logsum);
		LogFormula ret = getFormulaObject(LogFormula.Op.PLUS);
		LogFormula term2 = getFormulaObject(LogFormula.Op.TIMES);
		term2.add_arg(getFormulaObject(LDouble.convertToLogDomain(-1.0)));			
		term2.add_arg(logpart);
		ret.add_arg(featweightsum1);
		ret.add_arg(term2);
		return ret.evaluate(this).exponentiate();
	}


	private LogFormula getRegularizationTerm() {
		// (* -0.5 lambda (w . w))
		LogFormula ret = getFormulaObject(LogFormula.Op.TIMES);

		// -0.5
		LogFormula term1 = getFormulaObject(LDouble.convertToLogDomain(-1.0));

		// lambda
		LogFormula term2 = getFormulaObject(LDouble.convertToLogDomain(lambda));

		// w . w
		LogFormula featweightsum = getFormulaObject(LogFormula.Op.PLUS);
		for (int i = 0; i < numParams; i++) {
			LogFormula featweight = getFormulaObject(LogFormula.Op.TIMES);
			int paramId = A.getInt("param_"+i);
			LogFormula formula = getLazyLookupFormulaObjectCustom(paramId,"param_"+i);
			featweight.add_arg(formula);
			featweight.add_arg(formula);
			featweightsum.add_arg(featweight);
		}
		ret.add_arg(term1);
		ret.add_arg(term2);
		ret.add_arg(featweightsum);
		return ret;
	}


	public LogFormula getLazyLookupFormulaObjectCustom(int ind, String name) {
		LogFormula f;
		f = checkLookupChart(ind);
		if(f == null)
			f = addToLookUpChart(ind,name);
		return f;
	}
	
	public LogFormula checkLookupChart(Integer ind)
	{
		return mLookupChart.get(ind);
	}

	public LogFormula addToLookUpChart(int ind, String name)
	{
		LogFormula f = new LazyLookupLogFormula(ind, name);
		mLookupChart.put(ind, f);
		return f;
	}	



	public LogLogisticRegressionModel(String xtestfile, String testOrTrain) {
		initializeParameterIndexes();
		if(testOrTrain.equals("test"))
			loadTrainingData(xtestfile, m_testData, m_testLabels);
		else
			loadTrainingData(xtestfile, m_trainingData, m_trainingLabels);
	}
	
	

	protected void initializeParameterIndexes() {
		A = new Alphabet();
		V = new LDouble[PARAMETER_TABLE_INITIAL_CAPACITY];
		G = new LDouble[PARAMETER_TABLE_INITIAL_CAPACITY];
		m_trainingData = new ArrayList<TDoubleArrayList>(1000);
		m_trainingLabels = new TIntArrayList(1000);
		m_testData = new ArrayList<TDoubleArrayList>(100);
		m_testLabels = new TIntArrayList(100);
		m_devData = new ArrayList<TDoubleArrayList>(100);
		m_devLabels = new TIntArrayList(100);
		savedValues = new TObjectDoubleHashMap<String>(1000);
		m_savedFormulas = new ArrayList<LogFormula>(FORMULA_LIST_INITIAL_CAPACITY);
		m_current = 0;
		m_savedLLFormulas = new ArrayList<LazyLookupLogFormula>(LLFORMULA_LIST_INITIAL_CAPACITY);
		m_llcurrent = 0;
		mLookupChart = new THashMap<Integer,LogFormula>(PARAMETER_TABLE_INITIAL_CAPACITY);
	}
	protected void initializeParameter(int paramIndex)
	{
		setValue(paramIndex, new LDouble(1.0));
		setGradient(paramIndex, new LDouble(LDouble.IdentityElement.PLUS_IDENTITY));		
	}

	private void loadTrainingData(String datafile, ArrayList<TDoubleArrayList> input, TIntArrayList output) {
		BufferedReader dataFileReader;
		String line;
		try {
			dataFileReader = new BufferedReader(new FileReader(datafile));
			TDoubleArrayList lineList;
			String[] toks;
			/*
			 * read in each line of the file and process it
			 */
			while ((line = dataFileReader.readLine()) != null) {
				lineList = new TDoubleArrayList();
				/*
				 * tokenize the line into features
				 */
				toks = line.split("\\s");
				output.add(Integer.parseInt(toks[0].trim()));
				int j = 0;
				for (int i = 1; i < toks.length; i++)
				{
					String token = toks[i].trim();
					lineList.add(Double.parseDouble(token));
					if (j >= numParams) {//maxNumFeatures) {
						int paramId = A.getInt("param_"+j);
						initializeParameter(paramId);
						numParams++;
					}
					j++;
				}
				lineList.add(1.0);
				if (j >= numParams) {//maxNumFeatures) {
					int paramId = A.getInt("param_"+j);
					initializeParameter(paramId);
					numParams++;
				}
				input.add(lineList);				
			}
			if (input.size() != output.size()) {
				System.out.println("Differing numbers of input and output lines.");
				System.out.println("Input: " + input.size() + " lines");
				System.out.println("Output: " + output.size() + " lines");
			}
			System.out.println("Num params: " + numParams);
		} catch (Exception exc) {
			exc.printStackTrace();			
		}
	}

	public void saveModel(String modelFile) {
		PrintWriter outputWriter;
		try {
			outputWriter = new PrintWriter(new FileOutputStream(modelFile));
			for (int i = 1; i <= numParams; i++) {
				outputWriter.println(getValue(i).getValue());
			}
			outputWriter.flush();
			outputWriter.close();
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();			
		}
	}




	
	public double transformGradient(int i,boolean maximize)
	{
		double factor = 1.0;
		if(maximize)
			factor = -1.0;
		return G[i+1].exponentiate()*factor;
	}

	public double[] getValues()
	{
		double[] values = new double[numParams];
		for(int i = 0; i < numParams; i ++)
		{
			values[i] = V[i+1].exponentiate();
		}
		return values;
	}
	
	public void setValues(double[] values)
	{
		for(int i = 0; i < numParams; i ++)
		{
			V[i+1] = LDouble.convertToLogDomain(values[i]);
		}
	}	


	public void saveParameters(String outputFile)
	{
		try
		{
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(outputFile));
			for(int i = 0; i < numParams; i ++)
			{
				String paramName = "param_"+i;
				bWriter.write(paramName+"\t"+V[i+1].getValue()+"\t"+V[i+1].isPositive()+"\n");
			}			
			bWriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	

	public double[] runTotallyRandomSGA(String paramFile)
	{
		double[] m_estimate = new double[numParams];
		int trainingSize = m_trainingData.size();
		int countUpdates = 0;
		int maxUpdates = trainingSize*1000;
		Date d = new Date();
		Random rand = new Random(d.getTime());
		boolean maximize = true;
		LogFormula.Op Type = LogFormula.Op.PLUS;
		RootLogFormula fullLogLike = new RootLogFormula(this,Type,"lazyroot");
		double oldValue = Double.MIN_VALUE;
		double STOPPING_CRITERION=0.001;
		while(countUpdates<maxUpdates)
		{
			if(countUpdates%100==0)
			{
				LDouble l_value = fullLogLike.evaluate(this);
				double m_value = this.extractFunctionValueForLBFGS(l_value, maximize);
				if(Math.abs(m_value-oldValue)<STOPPING_CRITERION)
				{
					break;
				}
				System.out.println("Function value:"+m_value+"\n");
				fullLogLike.changedParamValues();
				saveParameters(paramFile);
				classify();
				oldValue=m_value;
			}
			int index = rand.nextInt(trainingSize);
			for(int i = 0; i < numParams; i ++)
			{
				G[i+1].reset(IdentityElement.PLUS_IDENTITY);
			}
			LogFormula f = this.getFormula(index);
			f.backprop(this, new LDouble(LDouble.IdentityElement.TIMES_IDENTITY));
			double[] g = new double[numParams];
			for(int i = 0; i < numParams; i ++)
			{
				g[i] = transformGradient(i,maximize);
			}
			double[] v = getValues();
			v = SGA.updateGradient(v, g);
			setValues(v);
			countUpdates++;
			m_estimate=v;
		}		
		saveParameters(paramFile);
		classify();
		return m_estimate;
	}
	


}
