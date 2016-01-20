/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * RootLogFormula.java is part of SEMAFOR 2.0.
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

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Root node of the formula graph.
 * 
 * 2010-11-08 (nschneid): Added support for multithreading wherein each training example gets its own thread.
 * Only {@link LazyLookupLogFormula#add_arg(LogFormula)} nodes may be shared by multiple training examples.
 * @see RootLogFormula#RootLogFormula(LogModel, Op, String, boolean)
 */
public class RootLogFormula extends LogFormula {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1983416993175438308L;
	/**
	 * Holds shuffled indices of phrase pairs, for use with stochastic gradient descent
	 */
	TIntArrayList m_shuffledIndices = new TIntArrayList();
	/**
	 * Saves index of last data item used in training, for use with stochastic gradient descent.
	 */
	protected int m_lastDatumUsed = -1;
	
	protected LogModel m_owner;
	protected TIntHashSet m_paramIndices;
	protected int[] m_paramIndicesArray; 
	protected boolean m_onceThrough;
	protected int m_numNull = 0;
	protected LDouble[] m_savedFunctionValues;
	
	protected boolean _multithreaded = false;
	
	protected static int INITIAL_CAPACITY = 10000;
	
	public RootLogFormula(LogModel m, Op o, String name) {
		this(m,o,name,false);
	}
	public RootLogFormula(LogModel m, Op o, String name, boolean isMultithreaded) {
		super(o, name);
		m_owner = m;
		_multithreaded = isMultithreaded;
		initCommon(INITIAL_CAPACITY);		
	}

	private void initCommon(int capacity) {
		m_paramIndices = new TIntHashSet();
		m_onceThrough = false;
	}
		
	/**
	 * Subclasses of RootFormula should override this method to supply methods like evaluate() and 
	 * backprop() with a formula for the next training example. The returned Formula should be 
	 * null if and only if there are no more training examples for the current training iteration. 
	 */
	
	/**
	 * Computes the value of the formula with given values for the parameters.
	 * @return computed value 
	 */
	public LDouble evaluate(LogModel m) {
		if (m_valueComputed) return m_value;
		switch (m_operation) {
		case TIMES:
			m_value = new LDouble(LDouble.IdentityElement.TIMES_IDENTITY);
			/*int num = m.getNumTrainingExamples();
			for(int k = 0; k < num; k ++)
			{
				LogFormula f = m.getFormula(k);
				LogMath.logtimes(m_value, f.evaluate(m_owner), m_value);
			}*/
			_evaluate(m,true);
			break;
		case PLUS:
			m_value = new LDouble(LDouble.IdentityElement.PLUS_IDENTITY);
			/*num = m.getNumTrainingExamples();
			for(int k = 0; k < num; k ++)
			{
				LogFormula f = m.getFormula(k);
				LogMath.logplus(m_value, f.evaluate(m_owner), m_value);
			}*/
			_evaluate(m,true);
			break;
		case DIVIDE:
			System.out.println("Can't use division with a RootFormula.");
			break;
		case MINUS:
			System.out.println("Can't use minus with a RootFormula.");
			break;
		case NEG:
			System.out.println("Can't use negation with a RootFormula.");
			break;
		case EXP:
			System.out.println("Can't use exp() with a RootFormula.");
			break;
		case LOG:
			System.out.println("Can't use log with a RootFormula.");
			break;			
		case LOOKUP:
			System.out.println("Can't use lookup with a RootFormula.");
			break;
		case CONSTANT:
			System.out.println("Can't use a constant as a RootFormula.");
			break;
		}
		m_valueComputed = true;		
		return m_value;
	}
	
	protected synchronized void _updateValue(LDouble v) {
		switch (m_operation) {
		case TIMES: LogMath.logtimes(m_value, v, m_value); break;
		case PLUS: LogMath.logplus(m_value, v, m_value); break;
		default: System.out.println(String.format("Can't use operation %d with a RootFormula.", m_operation)); break;
		}
	}

	protected void _evaluate(final LogModel m, final boolean andBackprop) {
		int num = m.getNumTrainingExamples();
		List<Thread> threads = new ArrayList<Thread>();
		for (int k=0; k<num; k++) {	// iterate over training examples
			final LogFormula f = m.getFormula(k);
			if (!_multithreaded)
				_updateValue(f.evaluate(m_owner));
			else {	// create a thread for each training example
				//Thread thread = new ThreadForEvaluateAndBackprop(f, m, andBackprop);
				Thread thread = new Thread() {
					@Override
					public void run() {
						LDouble v = f.evaluate(m_owner);
						_updateValue(v);  // synchronized
						if (andBackprop && m_operation==Op.PLUS)
							f.backprop(m, new LDouble(LDouble.IdentityElement.TIMES_IDENTITY));	// synchronized for LazyLookupLogFormula instances (shared across training examples)
					}
				};
				thread.start();
                threads.add(thread);
			}
		}
		
		if (_multithreaded) {	// wait for all evaluate threads to finish
			for (Thread t : threads) {
	        	try {
	        		t.join();	// Block until the thread dies
	        	}
	        	catch (InterruptedException e) {
	        		e.printStackTrace();
	        	}
	        }
	        threads.clear();
		}
        
        // backprop for TIMES
        if (andBackprop && m_operation==Op.TIMES && m_value.notEqualsZero()) {
        	for(int k=0; k<num; k++) {	// iterate over training examples again
        		final LogFormula f = m.getFormula(k);
        		final LDouble temp = new LDouble();
        		LogMath.logdivide(m_value, f.evaluate(m), temp);	// value should have been computed, so evalute() is simply a lookup
        		if (!_multithreaded)
        			f.backprop(m, temp);
        		else {
        			// create a thread for each training example
    				Thread thread = new Thread() {
    					@Override
    					public void run() {
    						LDouble v = f.evaluate(m_owner);
    						_updateValue(v);  // synchronized
    						if (andBackprop && m_operation==Op.PLUS)
    							f.backprop(m, new LDouble(LDouble.IdentityElement.TIMES_IDENTITY));	// synchronized for LazyLookupLogFormula instances (shared across training examples)
    					}
    				};
    				thread.start();
                    threads.add(thread);
        		}
        	}
        	
        	if (_multithreaded) {	// wait for all backprop threads to finish
        		for (Thread t : threads) {
                	try {
                		t.join();	// Block until the thread dies
                	}
                	catch (InterruptedException e) {
                		e.printStackTrace();
                	}
                }
                threads.clear();
        	}
        }
	}
	
	/**
	 * Propagates gradient down the formula.
	 * @param inc_val 
	 */
	public void backprop(LogModel m, LDouble inc_val) {
		m_gradient = LogMath.logplus(m_gradient, inc_val);
		int i = 0;
		switch (m_operation) {
		case TIMES:
			try {
				// compute/get the product of all the arguments/operands
				evaluate(m); // the product will now be in m_value
				// only proceed if none of the operands equalled zero
				if (m_value.notEqualsZero()) {
					// go through the full set of training data
					LogFormula f = m.getNextFormula();					
					while (f != null) {
						// back-propagate each read-in Formula
						// call backprop on each operand, log-dividing the total product in m_value by the operand
						// divide total product by the current operand's value
						LogMath.logdivide(m_value, f.evaluate(m_owner), m_tempLDouble);
						// multiply the result by the increment value
						LogMath.logtimes(m_tempLDouble, inc_val, m_tempLDouble);
						// call backprop, passing the result
						f.backprop(m_owner, m_tempLDouble);
						if (i % 5000 == 0) System.out.print(i + " ");
						i++;
						f = m.getNextFormula();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e);
			}								
			break;
		case PLUS:
			// for log-plus-nodes, simply call backprop on each operand
			try {
				// go through full set of training data
				LogFormula f = m.getNextFormula();					
				while (f != null) {
					// back-propagate each phrase pair
					f.backprop(m_owner, inc_val);
					if (i % 5000 == 0) System.out.print(i + " ");
					i++;
					f = m.getNextFormula();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e);
			}								
			break;
		case DIVIDE:
			System.out.println("Can't use division with a LazyBigFormula.");
			break;
		case NEG:
			System.out.println("Can't use negation with a LazyBigFormula.");
			break;
		case LOOKUP:
			System.out.println("Can't use lookup with a LazyBigFormula.");
			break;
		}
		m_gradientComputed = true;
	}
}
