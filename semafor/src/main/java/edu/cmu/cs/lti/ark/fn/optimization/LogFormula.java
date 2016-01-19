/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * LogFormula.java is part of SEMAFOR 2.0.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.lti.ark.fn.optimization.LDouble.IdentityElement;

/**
 * Class to represent a function. Format is a tree structure, in which leaf nodes are 
 * table look-ups for parameter values and non-leaf nodes are operations, such as 
 * TIMES, PLUS, DIVIDE, etc. Every node of this "formula tree" is an instance of a 
 * Formula Object. 
 *  
 * @author Kevin Gimpel, Noah Smith
 * 2/21/07
 */
public class LogFormula implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3967927293400355614L;
	public enum Op { PLUS, MINUS, TIMES, DIVIDE, NEG, EXP, LOG, POWER, LOOKUP, CONSTANT }

	protected String m_name;
	/**
	 * The operation to be performed at this node
	 */
	protected Op m_operation;
	/**
	 * The terms (children) in the formula
	 */
	protected List<LogFormula> m_args;
	/**
	 * The computed value, if m_valueComputed == true
	 */
	protected LDouble m_value;
	/**
	 * Indicates whether the value of this formula has been computed
	 */
	protected boolean m_valueComputed;
	/**
	 * The gradient of this formula's parent with respect to this formula 
	 */
	protected LDouble m_gradient;
	/**
	 * Indicates whether the gradient of this formula has been computed
	 */
	protected boolean m_gradientComputed;

	protected LDouble m_tempLDouble;

		
	protected LDouble accumulatedGradient;

	 protected int inDegrees;
	
	
	/** Constructors **/
	public LogFormula(Op o, String name) {
		m_name = name;
		m_operation = o;
		m_valueComputed = false;
		m_value = new LDouble(LDouble.IdentityElement.PLUS_IDENTITY);	// initialize as plus-identity element
		initCommon();
	}
	public LogFormula(Op o) {
		m_name = null;
		m_operation = o;
		m_valueComputed = false;
		m_value = new LDouble(LDouble.IdentityElement.PLUS_IDENTITY);	// initialize as plus-identity element
		initCommon();
	}
	public LogFormula(LDouble v, String name) {
		m_name = name;
		m_operation = Op.CONSTANT;
		m_valueComputed = true;
		m_value = new LDouble(v);
		initCommon();
	}
	public LogFormula(LDouble v) {
		m_name = null;
		m_operation = Op.CONSTANT;
		m_valueComputed = true;
		m_value = new LDouble(v);
		initCommon();
	}
	public LogFormula(IdentityElement ie) {
		m_name = null;
		m_operation = Op.CONSTANT;
		m_valueComputed = true;
		m_value = new LDouble(ie);
		initCommon();
	}
	public LogFormula(double v) {
		m_name = null;
		m_operation = Op.CONSTANT;
		m_valueComputed = true;
		m_value = new LDouble(v);
		initCommon();
	}
	private void initCommon() {
		m_args = new ArrayList<LogFormula>();
		m_gradientComputed = false;
		m_gradient = new LDouble(LDouble.IdentityElement.PLUS_IDENTITY); // initialize as plus-identity element
		m_tempLDouble = new LDouble();
		accumulatedGradient = new LDouble(LDouble.IdentityElement.PLUS_IDENTITY);
		inDegrees = 0;
	}

	/** Reset functions **/
	public void reset(Op o, String name) {
		reset(o);
		m_name = name;
	}
	public void reset(Op o) {
		m_name = null;
		m_operation = o;
		m_valueComputed = false;
		m_value.reset();	// reset to plus-identity element
		resetCommon();
	}
	public void reset(LDouble v, String name) {
		reset(v);
		m_name = name;
	}
	public void reset(LDouble v) {
		m_name = null;
		m_operation = Op.CONSTANT;
		m_valueComputed = true;
		m_value.reset(v);	// reset to given value
		resetCommon();
	}
	public void reset(IdentityElement ie) {
		m_name = null;
		m_operation = Op.CONSTANT;
		m_valueComputed = true;
		m_value.reset(ie);	// reset to given value
		resetCommon();
	}
	public void reset(double v) {
		m_name = null;
		m_operation = Op.CONSTANT;
		m_valueComputed = true;
		m_value.reset(v);	// reset to given value
		resetCommon();
	}
	private void resetCommon() {
		m_gradientComputed = false;
		m_gradient.reset(); // initialize as plus-identity element
		m_args.clear();
		accumulatedGradient = new LDouble(LDouble.IdentityElement.PLUS_IDENTITY);
		inDegrees = 0;
	}

	public void finalize() {
		for (int i = 0; i < m_args.size(); i++) {
			m_args.get(i).finalize();
		}
	}

	/**
	 *  For lookup formulas, goes into the appropriate data structure and returns the value. 
	 */
	LDouble lookup_evaluate(LogModel m) {
		System.out.println("can't call lookup_evaluate on a non-lookup formula! Returning null.");
		return null;
	}

	/**
	 * Computes the value of the formula with given values for the parameters.
	 * @return computed value 
	 */
	public LDouble evaluate(LogModel m) {
		if (m_valueComputed) return m_value;
		int n = m_args.size(), i;
		switch (m_operation) {
		case POWER:
			LogMath.logpower(m_args.get(0).evaluate(m), m_args.get(1).evaluate(m), m_value);
			m_valueComputed = true;
			break;
		case TIMES:
			// initialize m_value to the multiplicative identity element
			m_value.reset(LDouble.IdentityElement.TIMES_IDENTITY);
			// evaluate each operand and log-multiply onto m_value, placing the result into m_value
			for (i = 0; i < n; i++) {
				LogMath.logtimes(m_value, m_args.get(i).evaluate(m), m_value);
			}
			m_valueComputed = true;
			break;
		case PLUS:
			// if there are more than k terms in the sum and this Formula node has a name, check 
			// the table of saved values
			if (m_name != null) {
				if (m.savedValues.containsKey(m_name)) {
					double v = m.savedValues.get(m_name); 
					m_value.reset(v);
				} else {
					// initialize m_value to the plus identity element
					m_value.reset(LDouble.IdentityElement.PLUS_IDENTITY);
					// evaluate each operand and log-add to m_value
					for (i = 0; i < n; i++) {
						LogMath.logplus(m_value, m_args.get(i).evaluate(m), m_value);
					}
					// save the result under the given name
					m.savedValues.put(m_name, m_value.value);
				}
			} else {
				// reset m_value to the plus identity element
				m_value.reset(LDouble.IdentityElement.PLUS_IDENTITY);
				// evaluate each operand and log-add to m_value
				for (i = 0; i < n; i++) {
					LogMath.logplus(m_value, m_args.get(i).evaluate(m), m_value);
				}
			}
			m_valueComputed = true;
			break;
		case DIVIDE:
			// log-divide the first operand by the second, placing result into m_value
			// if m_args.size() > 2, ignores remaining operands
			LogMath.logdivide(m_args.get(0).evaluate(m), m_args.get(1).evaluate(m), m_value);
			m_valueComputed = true;
			break;
		case EXP:
			// exponentiate the argument (assumed to be only one), placing result into m_value
			LogMath.logexp(m_args.get(0).evaluate(m), m_value);
			m_valueComputed = true;
			break;
		case LOG:
			// take the log of the argument (assumed to be only one, but not checked)
			// if the argument is negative, prints a warning and changes it to positive.. does this make sense?
			m_tempLDouble = m_args.get(0).evaluate(m);
			if (!m_tempLDouble.isPositive()) {
				System.out.println("Tried to take the log of a negative number");
			}
			LDouble.convertToLogDomain(m_tempLDouble.value, m_value);
			m_valueComputed = true;
			break;
		case NEG:
			// todo: implement negation
			break;
		case LOOKUP:
			try {
				// look up the value in a global table for the model (see LazyLookupFormula)
				m_value = lookup_evaluate(m);
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
			break;
		case CONSTANT:
			// do nothing; i.e., simply return m_value
			break;
		}
		return m_value;
	}

	public String toString() {
		return "name=" + m_name + "\tval=" + m_value + "\tgrad=" + m_gradient; 
	}
}

