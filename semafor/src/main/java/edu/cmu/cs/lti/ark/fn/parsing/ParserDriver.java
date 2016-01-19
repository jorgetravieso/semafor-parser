/*******************************************************************************
 * Copyright (c) 2012 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 *
 * CommandLineOptions.java is part of SEMAFOR 2.1.
 *
 * SEMAFOR 2.1 is free software: you can redistribute it and/or modify  it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * SEMAFOR 2.1 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. 
 *
 * You should have received a copy of the GNU General Public License along
 * with SEMAFOR 2.1.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.cmu.cs.lti.ark.fn.parsing;

import com.google.common.collect.Sets;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectDoubleHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.lti.ark.fn.evaluation.ParseUtils;
import edu.cmu.cs.lti.ark.fn.identification.FastFrameIdentifier;
import edu.cmu.cs.lti.ark.fn.identification.FrameIdentificationRelease;
import edu.cmu.cs.lti.ark.fn.identification.RequiredDataForFrameIdentification;
import edu.cmu.cs.lti.ark.fn.identification.SmoothedGraph;
import edu.cmu.cs.lti.ark.fn.segmentation.RoteSegmenter;
import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.fn.wordnet.WordNetRelations;
import edu.cmu.cs.lti.ark.preprocess.PreprocessedText;
import edu.cmu.cs.lti.ark.util.SerializedObjects;

public class ParserDriver {

    SmoothedGraph sg = null;
    THashSet<String> allRelatedWords;
    FastFrameIdentifier idModel;
    FNModelOptions options;
    WordNetRelations wnr;

    /*
	 *  Required flags for creating a ParserDriver:
	 *  stopwords-file
	 *  wordnet-configfile
	 *  fnidreqdatafile
	 *  goldsegfile
	 *  userelaxed
	 *  testtokenizedfile
	 *  idmodelfile
	 *  alphabetfile
	 *  framenet-femapfile
	 *  eventsfile
	 *  spansfile
	 *  model
	 *  useGraph
	 *  frameelementsoutputfile
	 *  alllemmatagsfile
	 *  decodingtype.
	 */
    public ParserDriver (String[] args){
        init(args);
    }

    /**
     * Initializes the parser driver.
     * @param args List of required arguments for generating a ParserDriver.
     */
    private void init(String[] args) {
        options = new FNModelOptions(args);

		// Initialize WordNet config file
        String stopWordsFile = options.stopWordsFile.get();
        String wnConfigFile = options.wnConfigFile.get();
        wnr = new WordNetRelations(stopWordsFile, wnConfigFile);

        initModel(wnr, options);
    }

    /**
     * Initializes the model for classification.
     * @param wnr The given WordNetRelations.
     * @param options Options for initializing the models.
     */
    private void initModel(WordNetRelations wnr, FNModelOptions options){
        RequiredDataForFrameIdentification r = (RequiredDataForFrameIdentification)
                SerializedObjects.readSerializedObject(options.fnIdReqDataFile.get());
        allRelatedWords = r.getAllRelatedWords();
        Map<String, Set<String>> relatedWordsForWord = r.getRelatedWordsForWord();
        Map<String, THashMap<String, Set<String>>> wordNetMap = r.getWordNetMap();
        THashMap<String, THashSet<String>> frameMap = r.getFrameMap();
        THashMap<String, THashSet<String>> cMap = r.getcMap();
        Map<String, Map<String, Set<String>>> revisedRelationsMap = r.getRevisedRelMap();
        wnr.setRelatedWordsForWord(relatedWordsForWord);
        wnr.setWordNetMap(wordNetMap);
        Map<String, String> hvLemmas = r.getHvLemmaCache();
        TObjectDoubleHashMap<String> paramList = FrameIdentificationRelease.parseParamFile(options.idParamFile.get());
        System.out.println("Initializing frame identification model...");
        idModel = new FastFrameIdentifier(paramList, "reg", 0.0, frameMap, null, cMap, relatedWordsForWord,
                revisedRelationsMap, hvLemmas);
        boolean usegraph = !options.useGraph.get().equals("null");
        sg = null;
        if (usegraph) {
            sg = (SmoothedGraph) SerializedObjects.readSerializedObject(options.useGraph.get());
            System.out.println("Read graph successfully from: " + options.useGraph.get());
        }
    }

    /**
     * Runs the parser on a given preprocessed input text and returns a String
     * @param preprocessedTexts The given list of preprocessed texts.
     * @return The list os output strings including the frames.
     */
    public Set<String> runParser(List<PreprocessedText> preprocessedTexts){
        final Set<String> frames = Sets.newHashSet();

        for (PreprocessedText preprocessedText: preprocessedTexts) {

            // 1. getting segments
            RoteSegmenter segmenter = new RoteSegmenter();
            String[][] data = preprocessedText.toArrayPresentation();
            String segment = segmenter.findSegmentationForTest(data, allRelatedWords);
            ArrayList<String> inputForFrameId = ParseUtils.getRightInputForFrameIdentification(segment);

            // 2. frame identification
            for (String input : inputForFrameId) {
                // offset of the sentence within the loaded data (relative to options.startIndex)
                String bestFrame = null;
                if (sg == null) {
                    bestFrame = idModel.getBestFrame(input, data);
                } else {
                    bestFrame = idModel.getBestFrame(input, data, sg);
                }
                frames.add(bestFrame);
            }

        }

        return frames;
    }
}
