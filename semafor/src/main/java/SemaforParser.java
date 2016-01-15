import edu.cmu.cs.lti.ark.fn.parsing.ParserDriver;
import edu.cmu.cs.lti.ark.util.FileUtil;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jtravieso on 7/28/15.
 */



//-Xms4g -Xmx4g \

public class SemaforParser {


    private final String MST_MODE = "server";
    private final String MST_MACHINE = "localhost";
    private final String MST_PORT = "12345";
    private final String DECODING_TYPE= "ad3";



    SemaforConfig config;
    ParserDriver parserDriver;


    public SemaforParser() throws IOException {
        config = SemaforConfig.getInstance();
        initParser();
    }

    public void initParser() throws IOException {

         final String MODEL_DIR = config.getSemaforResource("models").getPath();                          //"/Users/jtravieso/IdeaProjects/Semafor/src/main/resources/fnmfiles/models";
         final String GRAPH_FILE = MODEL_DIR + "/sparsegraph.gz";
         final String STOPWORDS =  config.getSemaforResource("stopwords.txt").getPath();                  //"/Users/jtravieso/IdeaProjects/Semafor/src/main/resources/fnmfiles/stopwords.txt";
         final String WORDNET_CONFIG_FILE = config.getSemaforResource("file_properties.xml").getPath();   //"/Users/jtravieso/IdeaProjects/Semafor/src/main/resources/fnmfiles/file_properties.xml";


        final String tempFolder = "/Users/jtravieso/Desktop";
        //garbage
        // final String ALL_LEMMA_TAGS_FILE = inputFilePath + ".all.lemma.tags";

        String[] FNArgs;
        FNArgs = new String[]{
                "mstmode:" + MST_MODE,
                "mstserver:" + MST_MACHINE,
                "mstport:" + MST_PORT,
                //    "posfile:" + inputFilePath + ".pos.tagged",
                //    "test-parsefile:" + inputFilePath + ".conll.output",
                "stopwords-file:"+STOPWORDS,
                "wordnet-configfile:"+ WORDNET_CONFIG_FILE,
                "fnidreqdatafile:" + MODEL_DIR + "/reqData.jobj",
                "goldsegfile:null",
                "userelaxed:no",
                //    "testtokenizedfile:" + inputFilePath + ".tokenized",
                "idmodelfile:" + MODEL_DIR + "/idmodel.dat",
                "alphabetfile:" + MODEL_DIR + "/parser.conf",
                "framenet-femapfile:" + MODEL_DIR + "/framenet.frame.element.map",
                "eventsfile:" + tempFolder + "fnm.events.bin",
                "spansfile:" + tempFolder + "fnm.spans",
                "model:" + MODEL_DIR + "/argmodel.dat",
                "useGraph:" + GRAPH_FILE,
                //    "frameelementsoutputfile:" + inputFilePath + ".fes",
                //    "alllemmatagsfile:" + ALL_LEMMA_TAGS_FILE,
                "requiresmap:" + MODEL_DIR + "/requires.map",
                "excludesmap:" + MODEL_DIR + "/excludes.map",
                "decoding:" + DECODING_TYPE
        };


        parserDriver = new ParserDriver(FNArgs);
    }

    public List<String> findFrames(String text){

        List<String> frames = new ArrayList<String>();
        if(text == null || text.trim().isEmpty()){
            return Collections.EMPTY_LIST;
        }

        try {

            //creating temp input + output file
            File inputFile = File.createTempFile("fnmodel.input", "temp");
            File outputFile = new File(inputFile.getPath() + ".output");
            System.out.println("Created tempFile " + inputFile.getPath());
            FileUtils.writeStringToFile(inputFile, text.trim());

            runPreprocessor(inputFile.getPath());
            parserDriver.runParser(inputFile.getPath(), outputFile.getPath());
            frames = extractFromOutput(outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }


        return frames;
    }

    private void runPreprocessor(String inputFile) throws IOException {


         //String semaforHome = "/Users/jtravieso/IdeaProjects/Semafor/src/main/resources/fnmfiles";
        //String prepShell = "/Users/jtravieso/IdeaProjects/Semafor/src/main/resources/fnmfiles/preprocess.sh";
        File prepShell = config.getPreprocessorScript();

        ProcessBuilder pb = new ProcessBuilder(prepShell.getPath(), inputFile , config.getSemaforHome());
        Process p = pb.start();

        InputStream is = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);


        //System.out.printf("Output of running %s is:", Arrays.toString(cmd));
        String lineRead;
        while ((lineRead = br.readLine()) != null) {
            System.out.println(lineRead);
        }

    }

    public List<String> extractFromOutput(File outputFile){

        List<String> frames = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(outputFile));
            String line;

            while((line = in.readLine()) != null){
                String[] split = line.split("\t");
                if(split.length > 2){
                    frames.add(split[2].trim().toLowerCase());
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
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
