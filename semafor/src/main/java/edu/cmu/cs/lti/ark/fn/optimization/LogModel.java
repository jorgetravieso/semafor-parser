/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * LogModel.java is part of SEMAFOR 2.0.
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
package edu.cmu.cs.lti.ark.fn.optimization;

import gnu.trove.TObjectDoubleHashMap;

/**
 * A model manages a couple of things.
 * It creates formulas that can be used in evaluating a function that depends on model parameters.
 * The formula could just be a model parameter (e.g., a multinomial probability), or it could be
 * a formula whose leaves are model parameters.
 * 
 * The model also stores those parameters and provides methods for accessing and changing them, 
 * and does the same for their gradients.
 * 
 * The model also contains methods for doing training. This abstract class contains methods for 
 * running LBFGS and stochastic gradient ascent to maximize functions represented as Formula objects.
 * For optimizing large formulas, LazyBigFormula should be used.
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
	 * Gets parameter value corresponding to given index, or null if none could be found. 
	 * @param index key to find parameter value in HashMap 
	 * @return the value if it could be found, or null otherwise
	 */
	public LDouble getValue(int index) {
		return V[index];
	}

	/**
	 * Gets gradient value corresponding to given index, or null if none could be found.
	 * @param index key to find gradient value in HashMap
	 * @return the gradient if it could be found, or null otherwise
	 */
	public LDouble getGradient(int index) {
		return G[index];
	}
	protected abstract double classify();
}
