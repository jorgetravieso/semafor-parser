/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das Language Technologies Institute, Carnegie Mellon University, All Rights Reserved.
 *
 * LRIdentificationModelSingleNode.java is part of SEMAFOR 2.0.
 *
 * SEMAFOR 2.0 is free software: you can redistribute it and/or modify  it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * SEMAFOR 2.0 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SEMAFOR 2.0.  If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.cmu.cs.lti.ark.fn.identification;

import gnu.trove.map.hash.THashMap; // THashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.wordnet.WordNetRelations;
import edu.cmu.cs.lti.ark.util.ds.map.IntCounter;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;
import edu.cmu.cs.lti.ark.util.optimization.LDouble;
import edu.cmu.cs.lti.ark.util.optimization.LDouble.IdentityElement;
import edu.cmu.cs.lti.ark.util.optimization.LazyLookupLogFormula;
import edu.cmu.cs.lti.ark.util.optimization.LogFormula;
import edu.cmu.cs.lti.ark.util.optimization.LogModel;

public class LRIdentificationModelSingleNode extends LogModel {
    protected ArrayList<String> mFrameLines;
    protected ArrayList<String> mParseLines;
    protected WordNetRelations mWNR;
    protected int totalNumberOfParams;
    protected String mReg;
    protected double mLambda = 1.0;
    protected String mInitParamFile;
    protected int mNumExamples;
    THashMap<String, THashSet<String>> mFrameMap;
    protected TIntObjectHashMap<LogFormula> mLookupChart;
    private THashMap<String, THashMap<String, Double>> mFeatureCache;
    private String mModelFile;
    protected TObjectIntHashMap<String> localA = null;
    TObjectDoubleHashMap<String> mParamList = null;
    private String mTrainOrTest = "train";

    public LRIdentificationModelSingleNode(TObjectDoubleHashMap<String> paramList, String reg, double l, WordNetRelations mwnr, THashMap<String, THashSet<String>> frameMap) {

    }

    public void initializeParameterIndexes() {
        V = new LDouble[PARAMETER_TABLE_INITIAL_CAPACITY];
        G = new LDouble[PARAMETER_TABLE_INITIAL_CAPACITY];
        savedValues = new TObjectDoubleHashMap<String>(1000);
        m_savedFormulas = new ArrayList<LogFormula>(FORMULA_LIST_INITIAL_CAPACITY);
        m_current = 0;
        m_savedLLFormulas = new ArrayList<LazyLookupLogFormula>(LLFORMULA_LIST_INITIAL_CAPACITY);
        m_llcurrent = 0;
    }

    public void initializeParameters() {
        localA = new TObjectIntHashMap<String>();
        String[] keys = new String[mParamList.size()];
        mParamList.keys(keys);
        int count = 0;
        for (String param : keys) {
            double val = mParamList.get(param);
            LDouble lDoubleVal = LDouble.convertToLogDomain(val);
            int paramIndex = count;
            localA.put(param, paramIndex);
            setValue(paramIndex, lDoubleVal);
            setGradient(paramIndex, new LDouble(LDouble.IdentityElement.PLUS_IDENTITY));
            count++;
        }
    }


    public void setValues(double[] values) {
        String[] keys = new String[mParamList.size()];
        mParamList.keys(keys);
        totalNumberOfParams = keys.length;
        int count = 0;
        for (String param : keys) {
            int paramIndex = localA.get(param);
            setValue(paramIndex, LDouble.convertToLogDomain(values[count]));
            count++;
        }
    }

    public LogFormula checkLookupChart(Integer ind) {
        return mLookupChart.get(ind);
    }

    public LogFormula addToLookUpChart(int ind, String name) {
        LogFormula f = new LazyLookupLogFormula(ind, name);
        mLookupChart.put(ind, f);
        return f;
    }


    public LogFormula getLazyLookupParam(String name, String trainOrTest) {
        if (!localA.contains(name)) {
            if (trainOrTest.equals("train")) {    //	for debugging alphabet creation:
                System.err.println("Problem. Exceeded number of params. Param name:" + name);
                System.exit(0);
            }

            LogFormula f = getFormulaObject(IdentityElement.PLUS_IDENTITY);
            return f;
        }

        int ind = localA.get(name);

        LogFormula f;
        f = checkLookupChart(ind);    // get cached version, if possible
        if (f == null) {
            f = addToLookUpChart(ind, name);    // otherwise, create a new formula node and cache it
        }
        return f;
    }


    protected double classify() {
        // TODO Auto-generated method stub
        return 0;
    }

    protected double classifyTest() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Constructs and returns the formula node for computing the unnormalized score under the model of labeling a particular target
     * with the given frame type. Because there is a latent variable, the top-level node is a summation over possible values of the
     * latent variable (the lexical unit).
     *
     * @return the new node
     * @see #getFormula(int)
     */
    protected LogFormula getFormulaForFrame(String frame, int[] intTokNums, String[][] data) {
        THashSet<String> hiddenUnits = mFrameMap.get(frame);
        LogFormula result = getFormulaObject(LogFormula.Op.PLUS);
        DependencyParse parse = DependencyParse.processFN(data, 0.0);
        for (String unit : hiddenUnits) {
            FeatureExtractor featex = new FeatureExtractor();
            IntCounter<String> valMap = featex.extractFeatures(frame, intTokNums, unit, data, mWNR, mTrainOrTest, null, null, parse);
            Set<String> features = valMap.keySet();
            LogFormula featSum = getFormulaObject(LogFormula.Op.PLUS);

            for (String feat : features) {
                double val = valMap.getT(feat);    // the feature's value (for binary feature, its presence)
                LogFormula prod = getFormulaObject(LogFormula.Op.TIMES);
                LogFormula featVal = getFormulaObject(LDouble.convertToLogDomain(val));
                prod.add_arg(featVal);
                LogFormula paramFormula = getLazyLookupParam(feat, mTrainOrTest);    // the feature's weight
                prod.add_arg(paramFormula);    // feature's value * weight
                featSum.add_arg(prod);    // sum over all features' value * weight
            }
            LogFormula expFormula = getFormulaObject(LogFormula.Op.EXP);
            expFormula.add_arg(featSum);    // exp(featSum)
            result.add_arg(expFormula);    // sum_{hiddenUnits (latent variables)} exp(featSum)
        }
        return result;
    }

    /**
     * Constructs and returns the formula node for computing the normalized (and optionally, regularized) probability under the
     * model of labeling a particular target with its gold frame type in the data. The numerator is computed by calling
     * getFormulaForFrame() with that frame type.
     *
     * @param index Index of the data point
     * @see #getFormulaForFrame(String, int[], String[][])
     */
    protected LogFormula getFormula(int index) {
        // Parse information from the specified line
        m_current = 0;
        m_llcurrent = 0;
        String frameLine = mFrameLines.get(index);
        String[] toks = frameLine.split("\t");
        String frameName = toks[0];
        String[] tokNums = toks[1].split("_");
        int sentNum = new Integer(toks[2]);
        String parseLine = mParseLines.get(sentNum);
        int[] intTokNums = new int[tokNums.length];
        for (int j = 0; j < tokNums.length; j++) {
            intTokNums[j] = new Integer(tokNums[j]);
        }
        Arrays.sort(intTokNums);
        StringTokenizer st = new StringTokenizer(parseLine, "\t");
        int tokensInFirstSent = new Integer(st.nextToken());
        String[][] data = new String[5][tokensInFirstSent];
        for (int k = 0; k < 5; k++) {
            data[k] = new String[tokensInFirstSent];
            for (int j = 0; j < tokensInFirstSent; j++) {
                data[k][j] = "" + st.nextToken().trim();
            }
        }

        // Create the formula subtree
        LogFormula ratio = getFormulaObject(LogFormula.Op.DIVIDE);
        LogFormula numerator = getFormulaForFrame(frameName, intTokNums, data);
        /*
         * building denominator
		 */
        Set<String> frameSet = mFrameMap.keySet();
        LogFormula denominator = getFormulaObject(LogFormula.Op.PLUS);
        for (String frameDashed : frameSet) {
            System.out.println(frameDashed);
            LogFormula denomComponent = getFormulaForFrame(frameDashed, intTokNums, data);
            denominator.add_arg(denomComponent);
        }

        ratio.add_arg(numerator);
        ratio.add_arg(denominator);
        LogFormula ret = getFormulaObject(LogFormula.Op.LOG);
        ret.add_arg(ratio);

        if (mReg.equals("reg"))    // Regularization
        {
            LogFormula ret2 = getFormulaObject(LogFormula.Op.PLUS);
            LogFormula regTerm = getRegularizationTerm();
            ret2.add_arg(ret);
            ret2.add_arg(regTerm);
            return ret2;
        } else {
            return ret;
        }
    }


    protected LogFormula getRegularizationTerm() {
        // (* -0.5 lambda (w . w))
        LogFormula ret = getFormulaObject(LogFormula.Op.TIMES);
        // -0.5
        LogFormula term1 = getFormulaObject(LDouble.convertToLogDomain(-1.0));
        // lambda
        LogFormula term2 = getFormulaObject(LDouble.convertToLogDomain(mLambda));
        // w . w
        LogFormula featweightsum = getFormulaObject(LogFormula.Op.PLUS);
        String[] keys = new String[mParamList.size()];
        mParamList.keys(keys);
        totalNumberOfParams = keys.length;

        for (String param : keys) {
            LogFormula featweight = getFormulaObject(LogFormula.Op.TIMES);
            LogFormula formula = getLazyLookupParam(param, mTrainOrTest);
            featweight.add_arg(formula);
            featweight.add_arg(formula);
            featweightsum.add_arg(featweight);
        }
        ret.add_arg(term1);
        ret.add_arg(term2);
        ret.add_arg(featweightsum);
        return ret;
    }


    protected LogFormula getNextFormula() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getNumTrainingExamples() {
        // TODO Auto-generated method stub
        return mNumExamples;
    }

    public void saveModel(String modelFile) {
        // TODO Auto-generated method stub

    }


}
