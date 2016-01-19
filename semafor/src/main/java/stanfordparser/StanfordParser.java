package stanfordparser;

import com.google.common.collect.Lists;

import org.maochen.nlp.parser.DTree;
import org.maochen.nlp.parser.IParser;
import org.maochen.nlp.parser.stanford.nn.StanfordNNDepParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.cmu.cs.lti.ark.preprocess.PreprocessedText;

/**
 * Created by ramini on 1/18/16.
 */
public class StanfordParser {

    private IParser dependencyParser;
    private static StanfordParser instance = null;

    public static StanfordParser getInstance() {
        if (instance == null) {
            instance = new StanfordParser();
        }

        return instance;
    }

    private StanfordParser() {
        dependencyParser = new StanfordNNDepParser(null,null,new ArrayList<>());
    }

    public List<PreprocessedText> parse(String text) {
        List<PreprocessedText> output = Lists.newArrayList();

        DTree depTree = dependencyParser.parse(text);

        List<String> tokens = depTree.stream().filter(x->!x.equals(depTree.getPaddingNode()))
                .map(x -> x.getForm()).collect(Collectors.toList());
        List<String> posTags = depTree.stream().filter(x->!x.equals(depTree.getPaddingNode()))
                .map(x -> x.getPOS()).collect(Collectors.toList());
        List<String> lemmas = depTree.stream().filter(x->!x.equals(depTree.getPaddingNode()))
                .map(x->x.getLemma()).collect(Collectors.toList());
        List<String> namedEntities = depTree.stream().filter(x->!x.equals(depTree.getPaddingNode()))
                .map(x->x.getNamedEntity()).collect(Collectors.toList());
        List<Integer> parentIds = depTree.stream().filter(x->!x.equals(depTree.getPaddingNode()))
                .map(x -> x.getHead().getId()).collect(Collectors.toList());
        List<String> depTreeTags = depTree.stream().filter(x -> !x.equals(depTree.getPaddingNode()))
                .map(x -> StanfordToMst.convert(x)).collect(Collectors.toList());

        PreprocessedText ppt = new PreprocessedText();
        ppt.setText(text);
        ppt.setTokens(tokens);
        ppt.setPosTags(posTags);
        ppt.setLemmas(lemmas);
        ppt.setNamedEntities(namedEntities);
        ppt.setDepTreeTags(depTreeTags);
        ppt.setParentIds(parentIds);
        output.add(ppt);

        return output;
    }
}
