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

import edu.cmu.cs.lti.ark.fn.data.prep.AllAnnotationsMergingWithoutNE;
import edu.cmu.cs.lti.ark.fn.data.prep.OneLineDataCreation;
import edu.cmu.cs.lti.ark.fn.evaluation.ParseUtils;
import edu.cmu.cs.lti.ark.fn.identification.FastFrameIdentifier;
import edu.cmu.cs.lti.ark.fn.identification.FrameIdentificationRelease;
import edu.cmu.cs.lti.ark.fn.identification.RequiredDataForFrameIdentification;
import edu.cmu.cs.lti.ark.fn.identification.SmoothedGraph;
import edu.cmu.cs.lti.ark.fn.segmentation.MoreRelaxedSegmenter;
import edu.cmu.cs.lti.ark.fn.segmentation.RoteSegmenter;
import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.fn.wordnet.WordNetRelations;
import edu.cmu.cs.lti.ark.util.CommandLineOptions;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectDoubleHashMap;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ParserDriver {

    public static final String SERVER_FLAG = "server";
    public static final int BATCH_SIZE = 50;
    /*
	 *  required flags:
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
	 *  decodingtype
	 */


    BufferedReader goldSegReader = null;
    int segmentationMode;
    SmoothedGraph sg = null;
    String decodingType;
    Decoding decoding = null;
    THashSet<String> allRelatedWords;
    FastFrameIdentifier idModel;
    FNModelOptions options;
    WordNetRelations wnr;


    public ParserDriver (String[] args){
        init(args);
    }


    public void runParser(String inputFilePath, String outputFile){

       // options.posTaggedFile = new CommandLineOptions.StringOption();
        /* Opening POS tagged file */
        String posFile = inputFilePath + ".pos.tagged";
        String tokenizedFile = inputFilePath + ".tokenized";
        // = inputFilePath + ".fes";
        BufferedReader posReader = null;
        BufferedReader tokenizedReader = null;

        try {
            posReader = new BufferedReader(new FileReader(posFile));
        } catch (IOException e) {
            System.err.println("Could not open POS tagged file: " + posFile + ". Exiting.");
            System.exit(-1);
        }
        try {
            tokenizedReader = new BufferedReader(new FileReader(tokenizedFile));
        } catch (IOException e) {
            System.err.println("Could not open tokenized file: " + tokenizedFile + ". Exiting.");
            System.exit(-1);
        }
        runParser(posReader, tokenizedReader, outputFile);

        if (posReader != null) {
            try {
                posReader.close();
            } catch (IOException e) {
                System.err.println("Could not close POS input stream. Exiting.");
                System.exit(-1);
            }
        }
        if (tokenizedReader != null) {
            try {
                tokenizedReader.close();
            } catch (IOException e) {
                System.err.println("Could not close tokenizedReader input stream. Exiting.");
                System.exit(-1);
            }
        }
    }

    private void runParser(BufferedReader posReader, BufferedReader tokenizedReader, String outputFile) {

        String serverName = options.mstServerName.get();
        int serverPort = options.mstServerPort.get();

        // String outputFile = options.frameElementsOutputFile.get();
        BufferedWriter bWriter = null;
        try {
            bWriter = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            System.err.println("Could not open file to write: " + outputFile);
        }
        String allLemmaTagsOutputFile = outputFile + "all.lemma.tags";
        BufferedWriter bWriter1 = null;
        try {
            bWriter1 = new BufferedWriter(new FileWriter(allLemmaTagsOutputFile));
        } catch (IOException e) {
            System.err.println("Could not open file to write: " + allLemmaTagsOutputFile);
        }

        try {
            String posLine = null;
            String tokenizedLine = null;
            int count = 0;
            ArrayList<String> posLines = new ArrayList<String>();
            ArrayList<String> tokenizedLines = new ArrayList<String>();
            ArrayList<String> segLines = new ArrayList<String>();
            ArrayList<ArrayList<String>> parseSets = new ArrayList<ArrayList<String>>();
            ArrayList<String> tokenNums = new ArrayList<String>();
            ArrayList<String> segs = new ArrayList<String>();
            ArrayList<String> inputForFrameId = new ArrayList<String>();
            ArrayList<String> originalIndices = new ArrayList<String>();
            ArrayList<String> idResult = new ArrayList<String>();
            ArrayList<FrameFeatures> frList = new ArrayList<FrameFeatures>();
            ArrayList<String> argResult = new ArrayList<String>();

//
//            BufferedReader parseReader = null;
//            if (serverName == null) {
//                parseReader = new BufferedReader(new FileReader(options.testParseFile.get()));
//            }



            do {
                int index = 0;
                posLines.clear();
                segLines.clear();
                tokenizedLines.clear();
                tokenNums.clear();
                segs.clear();
                originalIndices.clear();
                idResult.clear();
                frList.clear();
                int size = parseSets.size();
                for (int i = 0; i < size; i++) {
                    ArrayList<String> set = parseSets.get(0);
                    set.clear();
                    parseSets.remove(0);
                }
                parseSets.clear();
                System.out.println("Processing batch of size:" + BATCH_SIZE + " starting from: " + count);
                for (index = 0; index < BATCH_SIZE; index++) {
                    posLine = posReader.readLine();
                    if (posLine == null) {
                        break;
                    }
                    posLines.add(posLine);
                    tokenizedLine = tokenizedReader.readLine();
                    tokenizedLines.add(tokenizedLine);
                    if (goldSegReader != null) {
                        segLines.add(goldSegReader.readLine().trim());
                    }
                    if (serverName == null) {
   //                     ArrayList<String> parse = readCoNLLParse(parseReader);
   //                     parseSets.add(parse);
                    }
                    tokenNums.add("" + index);
                    originalIndices.add("" + (count + index));
                }
                // breaking if the size of posLines is zero
                if (posLines.size() == 0) {
                    break;
                }
                if (serverName != null) {
                    parseSets = getParsesFromServer(serverName,
                            serverPort,
                            posLines);
                }
                ArrayList<String> allLemmaTagsSentences =
                        getAllLemmaTagsSentences(tokenizedLines, parseSets, wnr);
                for (String outSent : allLemmaTagsSentences) {
                    bWriter1.write(outSent + "\n");
                }
				/* actual parsing */
                // 1. getting segments
                if (segmentationMode == 0) {
                    int j = 0;
                    for (String seg : segLines) {
                        String[] toks = seg.trim().split("\\s");
                        String outSeg = "";
                        for (String tok : toks) {
                            outSeg += tok + "#true\t";
                        }
                        outSeg += tokenNums.get(j);
                        segs.add(outSeg.trim());
                        j++;
                    }
                } else if (segmentationMode == 1) {
                    RoteSegmenter seg = new RoteSegmenter();
                    segs = seg.findSegmentationForTest(tokenNums, allLemmaTagsSentences, allRelatedWords);
                } else if (segmentationMode == 2) {
                    MoreRelaxedSegmenter seg = new MoreRelaxedSegmenter();
                    segs = seg.findSegmentationForTest(tokenNums, allLemmaTagsSentences, allRelatedWords);
                }
                inputForFrameId.clear();
                inputForFrameId =
                        ParseUtils.getRightInputForFrameIdentification(segs);

                // 2. frame identification
                for (String input : inputForFrameId) {
                    String[] toks = input.split("\t");
                    int sentNum = new Integer(toks[2]);    // offset of the sentence within the loaded data (relative to options.startIndex)
                    String bestFrame = null;
                    if (sg == null) {
                        bestFrame = idModel.getBestFrame(input, allLemmaTagsSentences.get(sentNum));
                    } else {
                        bestFrame = idModel.getBestFrame(input, allLemmaTagsSentences.get(sentNum), sg);
                    }
                    String tokenRepresentation = FrameIdentificationRelease.getTokenRepresentation(toks[1], allLemmaTagsSentences.get(sentNum));
                    String[] split = tokenRepresentation.trim().split("\t");
                    idResult.add(1 + "\t" + bestFrame + "\t" + split[0] + "\t" + toks[1] + "\t" + split[1] + "\t" + sentNum);    // BestFrame\tTargetTokenNum(s)\tSentenceOffset
                }
                // 3. argument identification
                CreateAlphabet.run(false, allLemmaTagsSentences, idResult, wnr);
                LocalFeatureReading lfr =
                        new LocalFeatureReading(options.eventsFile.get(),
                                options.spansFile.get(),
                                idResult);
                try {
                    lfr.readLocalFeatures();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Could not read local features. Exiting.");
                    bWriter.close();
                    bWriter1.close();
                    System.exit(-1);
                }
                frList = lfr.getMFrameFeaturesList();
                decoding.setData(null, frList, idResult);
                argResult = decoding.decodeAll("overlapcheck", count);
                for (String result : argResult) {
                    bWriter.write(result + "\n");
                }
                count += index;
            } while (posLine != null);
//            if (parseReader != null) {
//                parseReader.close();
//            }
            if (bWriter != null) {
                bWriter.close();
            }
            if (bWriter1 != null) {
                bWriter1.close();
            }
        } catch (IOException e) {
            System.err.println("Could not read line from pos file. Exiting.");
            System.exit(-1);
        }
        // wrapping up joint decoding
        if (!decodingType.equals("beam")) {
            ((JointDecoding) decoding).wrapUp();
        }
    }

    private   void init(String[] args) {
        options = new FNModelOptions(args);
        String mstServerMode = options.mstServerMode.get();
        String mstServer = null;
        int mstPort = -1;

		/* Initializing connection to the MST server, if it exists */
        if (mstServerMode.equals(SERVER_FLAG)) {
            mstServer = options.mstServerName.get();
            mstPort = options.mstServerPort.get();
        }
		/* Initializing WordNet config file */
        String stopWordsFile = options.stopWordsFile.get();
        String wnConfigFile = options.wnConfigFile.get();
        wnr = new WordNetRelations(stopWordsFile, wnConfigFile);


        initModel(wnr,options);






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

    private static ArrayList<String> getAllLemmaTagsSentences(
            ArrayList<String> tokenizedLines,
            ArrayList<ArrayList<String>> parses,
            WordNetRelations wnr) {
        ArrayList<String> neSentences =
                AllAnnotationsMergingWithoutNE.findDummyNESentences(tokenizedLines);
        ArrayList<String> perSentenceParses =
                OneLineDataCreation.getPerSentenceParses(parses, tokenizedLines, neSentences);
        ArrayList<String> res = new ArrayList<String>();
        for (String line : perSentenceParses) {
            String outLine = line + "\t";
            String[] toks = line.trim().split("\\s");
            int sentLen = Integer.parseInt(toks[0]);
            for (int i = 0; i < sentLen; i++) {
                String lemma = wnr.getLemmaForWord(toks[i + 1].toLowerCase(), toks[i + 1 + sentLen]);
                outLine += lemma + "\t";
            }
            outLine = outLine.trim();
            res.add(outLine);
        }
        return res;
    }

    public static ArrayList<ArrayList<String>>
    getParsesFromServer(String server,
                        int port,
                        ArrayList<String> posLines) {
        Socket kkSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            kkSocket = new Socket(server, port);
            out = new PrintWriter(kkSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + server);
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + server);
            System.exit(-1);
        }
        for (String posLine : posLines) {
            out.println(posLine);
        }
        out.println("*");
        ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
        String fromServer = "";
        try {

            while ((fromServer = in.readLine()) != null) {
                fromServer = fromServer.trim();
                String[] toks = fromServer.split("\t");
                ArrayList<String> list = new ArrayList<String>();
                for (int t = 0; t < toks.length; t += 10) {
                    String outLine = "";
                    for (int s = 0; s < 10; s++) {
                        outLine += toks[t + s] + "\t";
                    }
                    outLine = outLine.trim();
                    list.add(outLine);
                }
                ret.add(list);
            }
        } catch (IOException e) {
            System.out.println("Could not read parses from server. Exiting");
            System.exit(-1);
        }
        out.close();
        try {
            in.close();
            kkSocket.close();
        } catch (IOException e) {
            System.err.println("Could not close input channel from server. Exiting.");
            System.exit(-1);
        }
        return ret;
    }


    public static ArrayList<String> readCoNLLParse(BufferedReader bReader) {
        ArrayList<String> thisParse = new ArrayList<String>();
        try {
            String line = null;
            while ((line = bReader.readLine()) != null) {
                line = line.trim();
                if (line.equals("")) {
                    break;
                } else {
                    thisParse.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not read CoNLL parse reader. Exiting.");
            System.exit(-1);
        }
        return thisParse;
    }
}
