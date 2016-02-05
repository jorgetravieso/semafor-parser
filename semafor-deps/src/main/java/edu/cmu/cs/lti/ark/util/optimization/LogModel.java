/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das Language Technologies Institute, Carnegie Mellon University, All Rights Reserved.
 *
 * LogModel.java is part of SEMAFOR 2.0.
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
package edu.cmu.cs.lti.ark.util.optimization;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.ArrayList;

import edu.cmu.cs.lti.ark.util.optimization.LDouble.IdentityElement;

/**
 * A model manages a couple of things. It creates formulas that can be used in evaluating a function that depends on model
 * parameters. The formula could just be a model parameter (e.g., a multinomial probability), or it could be a formula whose leaves
 * are model parameters.
 *
 * The model also stores those parameters and provides methods for accessing and changing them, and does the same for their
 * gradients.
 *
 * The model also contains methods for doing training. This abstract class contains methods for running LBFGS and stochastic
 * gradient ascent to maximize functions represented as Formula objects. For optimizing large formulas, LazyBigFormula should be
 * used.
 *
 * @author Kevin Gimpel
 * @date 3/2007
 */
public abstract class LogModel {
    /**
     * Mappings from indices to parameter values and gradients
     */
    protected LDouble[] V, G;
    protected Alphabet A;
    protected TObjectDoubleHashMap<String> savedValues;
    /**
     * The following ArrayLists store Formula and LazyLookupFormula objects for re-use to avoid the need to constantly create and
     * destroy Java objects, which can be costly.
     */
    protected ArrayList<LogFormula> m_savedFormulas;
    /**
     * The index of the next Formula in m_savedFormulas that can be used.
     */
    public int m_current;
    public ArrayList<LazyLookupLogFormula> m_savedLLFormulas;
    /**
     * The index of the next LazyLookupFormula in m_savedLLFormulas that can be used.
     */
    public int m_llcurrent;

    protected static int PARAMETER_TABLE_INITIAL_CAPACITY = 2000000;
    protected static int FORMULA_LIST_INITIAL_CAPACITY = 500000;
    protected static int LLFORMULA_LIST_INITIAL_CAPACITY = 500000;

    /**
     * Gets parameter value corresponding to given index, or null if none could be found.
     *
     * @param index key to find parameter value in HashMap
     * @return the value if it could be found, or null otherwise
     */
    public LDouble getValue(int index) {
        return V[index];
    }

    public void setValue(int index, LDouble newValue) {
        if (index >= V.length) {
            V = doubleArray(V);
        }
        V[index] = newValue;
    }

    /**
     * Gets gradient value corresponding to given index, or null if none could be found.
     *
     * @param index key to find gradient value in HashMap
     * @return the gradient if it could be found, or null otherwise
     */
    public LDouble getGradient(int index) {
        return G[index];
    }

    /**
     * Uses given LDouble as entry in HashMap.
     */
    public void setGradient(int index, LDouble newGradient) {
        if (index >= G.length) {
            G = doubleArray(G);
        }
        G[index] = newGradient;
    }

    /**
     * Should be called when we run out of space in a[]. Creates a new array of size 2*a.length and copies all elements in a to the
     * new array. Then returns the reference to the new array.
     *
     * @param a LDouble[] to be expanded
     */
    private LDouble[] doubleArray(LDouble[] a) {
        LDouble[] b = new LDouble[a.length * 2];
        System.arraycopy(a, 0, b, 0, a.length);
        return b;
    }

    protected abstract LogFormula getNextFormula();

    protected abstract LogFormula getFormula(int index);

    public abstract int getNumTrainingExamples();

    protected String getParamString(int paramIndex) {
        return A.getString(paramIndex) + "; " + paramIndex + "; " + getValue(paramIndex) + "; " + getGradient(paramIndex);
    }

    public LogFormula getFormulaObject(LogFormula.Op o) {
        LogFormula f;
        if (m_current == m_savedFormulas.size()) {
            // create a new one
            f = new LogFormula(o);
            m_savedFormulas.add(f);
            m_current++;
            return f;
        } else {
            f = m_savedFormulas.get(m_current);
            f.reset(o);
            m_current++;
            return f;
        }
    }

    public LogFormula getFormulaObject(LDouble v) {
        LogFormula f;
        if (m_current == m_savedFormulas.size()) {
            // create a new one
            f = new LogFormula(v);
            m_savedFormulas.add(f);
            m_current++;
            return f;
        } else {
            f = m_savedFormulas.get(m_current);
            f.reset(v);
            m_current++;
            return f;
        }
    }

    public LogFormula getFormulaObject(IdentityElement ie) {
        LogFormula f;
        if (m_current == m_savedFormulas.size()) {
            // create a new one
            f = new LogFormula(ie);
            m_savedFormulas.add(f);
            m_current++;
            return f;
        } else {
            f = m_savedFormulas.get(m_current);
            f.reset(ie);
            m_current++;
            return f;
        }
    }

}
