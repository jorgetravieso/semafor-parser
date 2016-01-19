package stanfordparser;

import org.maochen.nlp.parser.DTree;
import org.maochen.nlp.parser.IParser;
import org.maochen.nlp.parser.stanford.nn.StanfordNNDepParser;

import java.util.ArrayList;

/**
 * @author jtravieso
 * @since 1/19/16.
 */
public class StanfordParserII {
    private IParser parser=new StanfordNNDepParser(null,null,new ArrayList<>());


    public DTree parse(final String sentence){
      return   parser.parse(sentence);
    }

    public static void main(String[] args) {
        String i="I have a car.";
        StanfordParserII spii=new StanfordParserII();

        DTree tree=spii.parse(i);



        System.out.println(tree);
    }

}
