import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.cmu.cs.lti.ark.fn.parsing.FrameFeatures;
import edu.cmu.cs.lti.ark.fn.parsing.ParserDriver;
import edu.cmu.cs.lti.ark.preprocess.PreprocessedText;
import stanfordparser.StanfordParser;

/**
 * Created by ramini on 1/18/16.
 */

public class SemaforParser {
    private final String MST_MODE = "server";
    private final String MST_MACHINE = "localhost";
    private final String MST_PORT = "12345";
    private final String DECODING_TYPE= "ad3";

    SemaforConfig config;
    ParserDriver parserDriver;
    StanfordParser stanfordParser;

    public SemaforParser() throws IOException {
        config = SemaforConfig.getInstance();
        stanfordParser = StanfordParser.getInstance();
        initParser();
    }

    public void initParser() throws IOException {
        final String MODEL_DIR = config.getSemaforResource("models").getPath();
        final String GRAPH_FILE = MODEL_DIR + "/sparsegraph.gz";
        final String STOPWORDS =  config.getSemaforResource("stopwords.txt").getPath();
        final String WORDNET_CONFIG_FILE = config.getSemaforResource("file_properties.xml").getPath();
        final String tempFolder = "/Users/ramini/Desktop";
        String[] FNArgs;
        FNArgs = new String[]{
                "mstmode:" + MST_MODE,
                "mstserver:" + MST_MACHINE,
                "mstport:" + MST_PORT,
                "stopwords-file:"+STOPWORDS,
                "wordnet-configfile:"+ WORDNET_CONFIG_FILE,
                "fnidreqdatafile:" + MODEL_DIR + "/reqData.jobj",
                "goldsegfile:null",
                "userelaxed:no",
                "idmodelfile:" + MODEL_DIR + "/idmodel.dat",
                "alphabetfile:" + MODEL_DIR + "/parser.conf",
                "framenet-femapfile:" + MODEL_DIR + "/framenet.frame.element.map",
                "eventsfile:" + tempFolder + "fnm.events.bin",
                "spansfile:" + tempFolder + "fnm.spans",
                "model:" + MODEL_DIR + "/argmodel.dat",
                "useGraph:" + GRAPH_FILE,
                "requiresmap:" + MODEL_DIR + "/requires.map",
                "excludesmap:" + MODEL_DIR + "/excludes.map",
                "decoding:" + DECODING_TYPE
        };
        parserDriver = new ParserDriver(FNArgs);
    }

    /**
     * Given an input string, returns a list of FrameNet frames for that input.
     * @param text The given input string.
     * @return The list of FamerNet frames for the given input string.
     */
    public List<String> findFrames(String text){
        // Return an empty list if the input us null or empty
        if (text == null || text.trim().isEmpty()){
            return Lists.newArrayList();
        }

        List<String> frames = new ArrayList<String>();
        // Run the Semafor parser and get the frame features
        Set<FrameFeatures> frameFeatures = parserDriver.runParser(runPreprocessor(text));
        // Get the frame names
        for (FrameFeatures ff: frameFeatures) {
            frames.add(ff.frameName);
        }

        return frames;
    }

    /**
     * Preprocesses the given text and prepare it for frame identification.
     * @param text The given text.
     * @return The preprocessed text.
     */
    private List<PreprocessedText> runPreprocessor(String text) {
        return stanfordParser.parse(text);
    }

    /**
     * Extracts the list of frames from the parser output, which is a list of Strings.
     * @param parserOutput The List of strings returned by the parser as output.
     * @return The list of frames extracted from parser output.
     */
    public List<String> extractFromOutput(List<String> parserOutput){
        List<String> frames = new ArrayList<String>();
        for(String line : parserOutput) {
            String[] split = line.split("\t");
            if (split.length > 2){
                frames.add(split[2].trim().toLowerCase());
            }
        }

        return frames;
    }

    public static void main(String[] args) throws IOException {
        SemaforParser semafor = new SemaforParser();

        System.out.print("\n Frame: ");
        System.out.println(semafor.findFrames("I need help with my invoice."));
        System.out.print("\n Frame: ");
        System.out.println(semafor.findFrames("The dog is very big and is scary."));
    }
}
