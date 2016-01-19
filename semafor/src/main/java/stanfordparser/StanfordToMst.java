package stanfordparser;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.StringUtils;
import org.maochen.nlp.parser.DNode;
import org.maochen.nlp.parser.LangLib;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Maochen on 1/19/16.
 */
public class StanfordToMst {


    // Stanford, MST
    private static final Map<String, String> CONVERTION_DATA = new HashMap<String, String>() {
        {
            put("nsubj", "sub");
            put("punct", "p");
            put("det", "nmod");
            put("advmod", "amod");
            put("compound", "nmod");
            put("nn", "nmod");
        }
    };

    private static final Set<String> DET_BY_HEAD_DEP = ImmutableSet.of(LangLib.DEP_AUX, LangLib.DEP_AUXPASS,
            LangLib.DEP_POSS, LangLib.DEP_POSSESSIVE, LangLib.DEP_CC, LangLib.DEP_PREP, LangLib.DEP_NUM, LangLib.DEP_NUMBER,
            LangLib.DEP_POBJ, LangLib.DEP_XCOMP, "case");

    private static String generateByHeadPos(final DNode dnode) {
        String headPos = dnode.getHead().getPOS();
        if (LangLib.POS_IN.equals(headPos)) {
            headPos = "p";
        }
        return headPos.substring(0, 1) + "mod";
    }

    public static String convert(final DNode dnode) {
        String rectifiedStanfordLabel = dnode.getDepLabel().split(":")[0].toLowerCase();
        rectifiedStanfordLabel = rectifiedStanfordLabel.replaceAll("pass$", StringUtils.EMPTY);
        String tag = StringUtils.EMPTY;

        if (rectifiedStanfordLabel.endsWith("mod") && dnode.getHead().getPOS().equals(LangLib.POS_IN)) {
            tag = LangLib.DEP_PMOD;
        } else if (rectifiedStanfordLabel.equals(LangLib.DEP_DOBJ)) {
            tag = "obj";
        } else if (DET_BY_HEAD_DEP.contains(rectifiedStanfordLabel)) {
            tag = generateByHeadPos(dnode);
        } else if (rectifiedStanfordLabel.equals(LangLib.DEP_CONJ)) { // Cannot tell from DEP, use POS
            tag = "prd";
        } else if (rectifiedStanfordLabel.equals(LangLib.DEP_ACOMP)) {
            String headTag = convert(dnode.getHead());
            if (headTag.equalsIgnoreCase("prd")) {
                tag = generateByHeadPos(dnode);
            } else {
                tag = "prd";
            }
        } else if (CONVERTION_DATA.containsKey(rectifiedStanfordLabel)) {
            tag = CONVERTION_DATA.get(rectifiedStanfordLabel);
        }
        return tag.toUpperCase();
    }
}