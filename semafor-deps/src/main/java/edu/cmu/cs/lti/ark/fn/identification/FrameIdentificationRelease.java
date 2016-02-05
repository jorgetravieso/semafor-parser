/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das Language Technologies Institute, Carnegie Mellon University, All Rights Reserved.
 *
 * FrameIdentificationRelease.java is part of SEMAFOR 2.0.
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

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.util.optimization.LDouble;


public class FrameIdentificationRelease {
    public static TObjectDoubleHashMap<String> parseParamFile(String paramsFile) {
        TObjectDoubleHashMap<String> startParamList = new TObjectDoubleHashMap<String>();
        try {
            BufferedReader fis = new BufferedReader(new FileReader(paramsFile));
            String pattern = null;
            int count = 0;
            while ((pattern = fis.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(pattern.trim(), "\t");
                String paramName = st.nextToken().trim();
                String rest = st.nextToken().trim();
                String[] arr = rest.split(",");
                double value = new Double(arr[0].trim());
                boolean sign = new Boolean(arr[1].trim());
                LDouble val = new LDouble(value, sign);
                startParamList.put(paramName, val.exponentiate());
                if (count % 100000 == 0) {
                    System.out.println("Processed param number:" + count);
                }
                count++;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return startParamList;
    }
}
