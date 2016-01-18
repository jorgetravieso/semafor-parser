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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectDoubleHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

    public static final String SERVER_FLAG = "server";
    public static final int BATCH_SIZE = 50;

    BufferedReader goldSegReader = null;
    int segmentationMode;
    SmoothedGraph sg = null;
    String decodingType;
    Decoding decoding = null;
    THashSet<String> allRelatedWords;
    FastFrameIdentifier idModel;
    FNModelOptions options;
    WordNetRelations wnr;

    /*
	 *  Required flags for creating a ParserDriver:
	 *  mstmode
	 * 	mstserver
	 * 	mstport
	 *  posfile
	 *  test-parsefile
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
        String mstServerMode = options.mstServerMode.get();
        String mstServer = null;
        int mstPort = -1;

		// Initialize connection to the MST server, if it exists
        if (mstServerMode.equals(SERVER_FLAG)) {
            mstServer = options.mstServerName.get();
            mstPort = options.mstServerPort.get();
        }
		// Initialize WordNet config file
        String stopWordsFile = options.stopWordsFile.get();
        String wnConfigFile = options.wnConfigFile.get();
        wnr = new WordNetRelations(stopWordsFile, wnConfigFile);

        initModel(wnr, options);
    }

    /**
     * Runs the parser on a given preprocessed input text and returns a String
     * @param preprocessedTexts The given list of preprocessed texts.
     * @return The list os output strings including the frames.
     */
    public Set<FrameFeatures> runParser(List<PreprocessedText> preprocessedTexts){
        final Set<FrameFeatures> frames = Sets.newHashSet();

        for (PreprocessedText preprocessedText: preprocessedTexts) {

            ArrayList<String> idResult = new ArrayList<String>();

            // 1. getting segments
            RoteSegmenter segmenter = new RoteSegmenter();
            String segmenterInput = preprocessedText.toString();
            String segment = segmenter.findSegmentationForTest(segmenterInput, allRelatedWords);
            ArrayList<String> inputForFrameId = ParseUtils.getRightInputForFrameIdentification(segment);

            // 2. frame identification
            for (String input : inputForFrameId) {
                String[] toks = input.split("\t");
                // offset of the sentence within the loaded data (relative to options.startIndex)
                int sentNum = new Integer(toks[2]);
                String bestFrame = null;
                if (sg == null) {
                    bestFrame = idModel.getBestFrame(input, segmenterInput);
                } else {
                    bestFrame = idModel.getBestFrame(input, segmenterInput, sg);
                }
                String tokenRepresentation = FrameIdentificationRelease.getTokenRepresentation(toks[1], segmenterInput);
                String[] split = tokenRepresentation.trim().split("\t");
                // BestFrame\tTargetTokenNum(s)\tSentenceOffset
                idResult.add(1 + "\t" + bestFrame + "\t" + split[0] + "\t" + toks[1] + "\t" + split[1] + "\t" + sentNum);
            }
            // 3. argument identification
            CreateAlphabet.run(false, Lists.newArrayList(segmenterInput), idResult, wnr);
            LocalFeatureReading lfr = new LocalFeatureReading(options.eventsFile.get(), options.spansFile.get(), idResult);
            try {
                lfr.readLocalFeatures();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Could not read local features. Exiting.");
                System.exit(-1);
            }

            for (FrameFeatures ff: lfr.getMFrameFeaturesList()) {
                frames.add(ff);
            }
        }

        return frames;
    }

    private void initModel(WordNetRelations wnr, FNModelOptions options){

        RequiredDataForFrameIdentification r =
                (RequiredDataForFrameIdentification)
                        SerializedObjects.readSerializedObject(options.fnIdReqDataFile.get());
        allRelatedWords = r.getAllRelatedWords();
        Map<String, Set<String>> relatedWordsForWord = r.getRelatedWordsForWord();
        Map<String, THashMap<String, Set<String>>> wordNetMap = r.getWordNetMap();
        THashMap<String, THashSet<String>> frameMap = r.getFrameMap();
        THashMap<String, THashSet<String>> cMap = r.getcMap();
        Map<String, Map<String, Set<String>>> revisedRelationsMap =
                r.getRevisedRelMap();
        wnr.setRelatedWordsForWord(relatedWordsForWord);
        wnr.setWordNetMap(wordNetMap);
        Map<String, String> hvLemmas = r.getHvLemmaCache();
        TObjectDoubleHashMap<String> paramList =
                FrameIdentificationRelease.parseParamFile(options.idParamFile.get());
        System.out.println("Initializing frame identification model...");
        idModel = new FastFrameIdentifier(
                paramList,
                "reg",
                0.0,
                frameMap,
                null,
                cMap,
                relatedWordsForWord,
                revisedRelationsMap,
                hvLemmas);
        boolean usegraph = !options.useGraph.get().equals("null");
        sg = null;
        if (usegraph) {
            sg = (SmoothedGraph) SerializedObjects.readSerializedObject(options.useGraph.get());
            System.out.println("Read graph successfully from: " + options.useGraph.get());
        }
        // initializing argument identification
        // reading requires and excludes map
        String requiresMapFile = options.requiresMapFile.get();
        String excludesMapFile = options.excludesMapFile.get();
        System.out.println("Initializing alphabet for argument identification..");
        CreateAlphabet.setDataFileNames(options.alphabetFile.get(),
                options.frameNetElementsMapFile.get(),
                options.eventsFile.get(),
                options.spansFile.get());
        decodingType = options.decodingType.get();
        decoding = null;
        if (decodingType.equals("beam")) {
            decoding = new Decoding();
            decoding.init(options.modelFile.get(),
                    options.alphabetFile.get());
        } else {
            decoding = new JointDecoding(true); // exact decoding
            decoding.init(options.modelFile.get(),
                    options.alphabetFile.get());
            ((JointDecoding) decoding).setMaps(requiresMapFile, excludesMapFile);
        }

        String goldSegFile = options.goldSegFile.get();
        goldSegReader = null;
        // 0 == gold, 1 == strict, 2 == relaxed
        segmentationMode = -1;
        if (goldSegFile == null || goldSegFile.equals("null") || goldSegFile.equals("")) {
            if (options.useRelaxedSegmentation.get().equals("yes")) {
                segmentationMode = 2;
                System.err.println("Using relaxed auto target identification.");
            } else {
                segmentationMode = 1;
                System.err.println("Using strict auto target identification.");
            }

        } else {
            segmentationMode = 0;
            System.err.println("Using gold targets from: " + goldSegFile);
            try {
                goldSegReader = new BufferedReader(new FileReader(goldSegFile));
            } catch (IOException e) {
                System.err.println("Could not open gold segmentation file:" + goldSegFile);
                System.exit(-1);
            }
        }
    }
}
