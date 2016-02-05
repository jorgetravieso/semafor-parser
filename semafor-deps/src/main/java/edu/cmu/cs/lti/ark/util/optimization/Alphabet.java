/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das Language Technologies Institute, Carnegie Mellon University, All Rights Reserved.
 *
 * Alphabet.java is part of SEMAFOR 2.0.
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

import gnu.trove.TObjectIdentityHashingStrategy;
import gnu.trove.TObjectIntHashMap;

import java.io.Serializable;
import java.util.ArrayList;

import edu.cmu.cs.lti.ark.util.Interner;

public class Alphabet implements Serializable {
    /**
     * Maps each unique string to a unique index.
     *
     * @author dipanjan
     */
    private static final long serialVersionUID = -3475498139713667452L;
    private ArrayList<String> m_decode;
    private TObjectIntHashMap<String> m_encode;
    private Interner<String> m_interner = new Interner<String>();
    private static int ALPHABET_INITIAL_CAPACITY = 2000000;

    public Alphabet() {
        m_decode = new ArrayList<String>(ALPHABET_INITIAL_CAPACITY);
        m_encode = new TObjectIntHashMap<String>(ALPHABET_INITIAL_CAPACITY, new TObjectIdentityHashingStrategy<String>());
    }

    /**
     * Returns String corresponding to given integer i, or "<UNK>" if i is out of range.
     */
    public String getString(int i) {
        if (i <= m_decode.size() && i >= 1) {
            return m_decode.get(i - 1);
        } else {
            return "<UNK>";
        }
    }
}

