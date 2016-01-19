package parser;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.maochen.nlp.parser.DNode;
import org.maochen.nlp.parser.DTree;
import org.maochen.nlp.parser.IParser;
import org.maochen.nlp.parser.stanford.nn.StanfordNNDepParser;

import java.util.stream.Collectors;

import stanfordparser.StanfordToMst;

import static org.junit.Assert.assertEquals;

/**
 * Created by Maochen on 1/19/16.
 */
public class StanfordToMstTest {

    private static final IParser PARSER = new StanfordNNDepParser("/Users/ramini/Code/ameliang/ameliang/amelia-nlp/src/main/resources/models/NNDep.model", null, null);


    private void eval(String sentence, String pos, String dep, String rel) {
        DTree tree = PARSER.parse(sentence);

        String[] posArray = pos.split("\\s");
        String[] depArray = dep.split("\\s");
        String[] relArray = rel.split("\\s");

        String actualPos = tree.stream().filter(x -> !x.equals(tree.getPaddingNode())).map(DNode::getPOS)
                .collect(Collectors.joining(StringUtils.SPACE));
        String actualDep = tree.stream().filter(x -> !x.equals(tree.getPaddingNode()))
                .map(x -> x.getDepLabel().toUpperCase()).collect(Collectors.joining(StringUtils.SPACE));
        String actualMstDep = tree.stream().filter(x -> !x.equals(tree.getPaddingNode()))
                .map(x -> StanfordToMst.convert(x).isEmpty() ? x.getDepLabel().toUpperCase() : StanfordToMst.convert(x))
                .collect(Collectors.joining(StringUtils.SPACE));

        for (int i = 1; i < tree.size(); i++) {
            DNode node = tree.get(i);
//            assertEquals("\nsentence: " + sentence + "\nexp: " + pos + "\nact: " + actualpos, posArray[i - 1], node.getPOS());

            String depTag = StanfordToMst.convert(node);
            if (depTag.isEmpty()) {
                depTag = node.getDepLabel().toUpperCase();
            }
            assertEquals("\nsentence: " + sentence + "\nactual pos: " + actualPos + "\nactual dep: " + actualDep + "\nexp: " + dep + "\nact: " + actualMstDep, depArray[i - 1], depTag);
        }

    }

    @Test
    public void test1() {
        String sentence = "I need help with my invoice.";
        String pos = "PRP MD VB IN PRP$ NN .";
        String dep = "SUB ROOT OBJ VMOD NMOD PMOD P";
        String rel = "2 0 2 3 6 4 2";
        eval(sentence, pos, dep, rel);
    }

    @Test
    public void test2() {
        String sentence = "The dog is very big and is scary .";
        String pos = "DT NN VBZ RB JJ CC VBZ JJ .";
        String dep = "NMOD SUB ROOT AMOD PRD VMOD VMOD PRD P";
        String rel = "2 3 0 5 3 3 3 7 3";
        eval(sentence, pos, dep, rel);
    }

    @Test
    public void test3() {
        String sentence = "The luxury auto maker last year sold 1,214 cars in the u.s .";
        String pos = "DT NN NN NN JJ NN VBD CD NNS IN DT NNS .";
        String dep = "NMOD NMOD NMOD SUB NMOD VMOD ROOT NMOD OBJ NMOD NMOD PMOD P";
        String rel = "4 4 4 7 6 7 0 9 7 9 14 12 7";
        eval(sentence, pos, dep, rel);
    }

    @Test
    public void test4() {
        String sentence = "John Smith likes to play soccer during winter .";
        String pos = "NNP NNP VBZ TO VB NN IN NN .";
        String dep = "NMOD SUB ROOT VMOD VMOD OBJ VMOD PMOD P";
        String rel = "2 3 0 5 3 5 5 7 3";
        eval(sentence, pos, dep, rel);
    }

    @Test
    public void test5() {
        String sentence = "John is scared of dogs .";
        String pos = "NNP VBZ VBN IN NNS .";
        String dep = "SUB ROOT VC VMOD PMOD P";
        String rel = "2 0 2 3 4 2";
        eval(sentence, pos, dep, rel);
    }
}