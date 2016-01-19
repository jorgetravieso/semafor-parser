/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * LazyLookupLogFormula.java is part of SEMAFOR 2.0.
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

public class LazyLookupLogFormula extends LogFormula {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1695010089287337618L;
	protected int m_index;
	private LDouble m_tempLDouble3 = new LDouble();
	
	public String toString(LogModel m) {
		return "name=" + m_name + "\tindex=" + m_index + "\tval=" + m.getValue(m_index) + "\tgrad=" + m.getGradient(m_index); 
	}
	
	public LazyLookupLogFormula(int i, String name) {
		super(LogFormula.Op.LOOKUP, name);
		//addParametertoSet(i);
		m_index = i;
	}
	public LazyLookupLogFormula(int i) {
		super(LogFormula.Op.LOOKUP);
		//addParametertoSet(i);
		m_index = i;
	}

	/**
	 * DON'T reset m_value or m_gradient since these may be references to elements in the Model's table.
	 * Instead, we'll just replace the references in lookup_evaluate and lookup_backprop below.
	 * @param i
	 * @param name
	 */
	public void reset(int i, String name) {
		reset(LogFormula.Op.LOOKUP, name);
		m_index = i;
	}
	public void reset(int i) {
		reset(LogFormula.Op.LOOKUP);
		m_index = i;
	}
	
	LDouble lookup_evaluate(LogModel m) {
		// get a reference to the parameter value
		LDouble ret = m.getValue(m_index); 
		// if it was found,
		if (ret != null) {
			// set m_value to the same value as the one we found
			m_value.reset(ret);
			// return m_value
			return m_value;
		} else {
			System.out.println("Null LDouble value encountered for param " + m_index + ", " + m.A.getString(m_index));
			// reset m_value and return it
			m_value.reset();
			return m_value;
		}
	}
}
