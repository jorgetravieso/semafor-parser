package stanfordparser;

import com.google.common.collect.Lists;
import edu.cmu.cs.lti.ark.preprocess.PreprocessedText;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by ramini on 1/18/16.
 */
public class StanfordParser {

    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
    private final Properties props = new Properties();
    private static StanfordCoreNLP pipeline;
    private static StanfordParser instance = null;

    public static StanfordParser getInstance() {
        if (instance == null) {
            instance = new StanfordParser();
        }

        return instance;
    }

    private StanfordParser() {
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    class Node {
        protected int index;
        protected String label;
        protected int parentIndex;
    }

    public List<PreprocessedText> parse(String text) {
        List<PreprocessedText> output = Lists.newArrayList();
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            List<String> tokens = Lists.newArrayList();
            List<String> posTags = Lists.newArrayList();
            List<String> lemmas = Lists.newArrayList();
            List<String> namedEntities = Lists.newArrayList();
            List<String> depTreeTags = Lists.newArrayList();
            List<Integer> parentIds = Lists.newArrayList();

            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                tokens.add(token.get(CoreAnnotations.TextAnnotation.class));
                posTags.add(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
                namedEntities.add(token.get(CoreAnnotations.NamedEntityTagAnnotation.class));
            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            List<Tree> children = tree.getChildrenAsList();
            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

            dependencies.typedDependencies().stream()
                    .map(td -> {
                        Node node = new Node();
                        node.index = td.dep().index();
                        node.label = td.reln().getShortName();
                        node.parentIndex = td.gov().index();
                        return node;
                    }).sorted((a, b) -> a.index - b.index)
                    .forEach(node -> {
                                depTreeTags.add(node.label);
                                parentIds.add(node.parentIndex);
                            }
                    );


            // TODO: populate the depTreeTags
            // TODO: populate the parentIndexes

            PreprocessedText ppt = new PreprocessedText();
            ppt.setText(sentence.get(CoreAnnotations.TextAnnotation.class));
            ppt.setTokens(tokens);
            ppt.setPosTags(posTags);
            ppt.setLemmas(lemmas);
            ppt.setNamedEntities(namedEntities);
            ppt.setDepTreeTags(depTreeTags);
            ppt.setParentIds(parentIds);

            output.add(ppt);
        }

        return output;
    }
}
