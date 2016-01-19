package edu.cmu.cs.lti.ark.preprocess;

import java.util.List;

/**
 * Represents a preprocessed text to be used for the frame identification.
 * Created by ramini on 1/18/16.
 */
public class PreprocessedText {
    private String text;
    private List<String> tokens;
    private List<String> posTags;
    private List<String> lemmas;
    private List<String> namedEntities;
    private List<String> depTreeTags;
    private List<Integer> parentIds;

    public PreprocessedText() {
    }

    public PreprocessedText(String text, List<String> tokens, List<String> posTags, List<String> lemmas, List<String> namedEntities, List<String> depTreeTags, List<Integer> parentIds) {
        this.text = text;
        this.tokens = tokens;
        this.posTags = posTags;
        this.lemmas = lemmas;
        this.namedEntities = namedEntities;
        this.depTreeTags = depTreeTags;
        this.parentIds = parentIds;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<String> getPosTags() {
        return posTags;
    }

    public void setPosTags(List<String> posTags) {
        this.posTags = posTags;
    }

    public List<String> getLemmas() {
        return lemmas;
    }

    public void setLemmas(List<String> lemmas) {
        this.lemmas = lemmas;
    }

    public List<String> getNamedEntities() {
        return namedEntities;
    }

    public void setNamedEntities(List<String> namedEntities) {
        this.namedEntities = namedEntities;
    }

    public List<String> getDepTreeTags() {
        return depTreeTags;
    }

    public void setDepTreeTags(List<String> depTreeTags) {
        this.depTreeTags = depTreeTags;
    }

    public List<Integer> getParentIds() {
        return parentIds;
    }

    public void setParentIds(List<Integer> parentIds) {
        this.parentIds = parentIds;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append(tokens.size()).append("\t");
        for (String token: tokens) {
            output.append(token).append("\t");
        }

        for (String posTag: posTags) {
            output.append(posTag).append("\t");
        }

        for (String depTreeTag: depTreeTags) {
            output.append(depTreeTag).append("\t");
        }

        for (Integer parentId: parentIds) {
            output.append(parentId).append("\t");
        }

        for (String namedEntity: namedEntities) {
            output.append(namedEntity).append("\t");
        }

        for (String lemma: lemmas) {
            output.append(lemma).append("\t");
        }

        return output.toString().trim();
    }

    public String[][] toArrayPresentation() {
        int numTokens = tokens.size();
        String[][] data = new String[6][numTokens];
        for(int k = 0; k < 6; k ++) {
            data[k] = new String[numTokens];
            for(int j = 0; j < numTokens; j ++)
            {
                data[0][j] = tokens.get(j);
                data[1][j] = posTags.get(j);
                data[2][j] = depTreeTags.get(j);
                data[3][j] = parentIds.get(j).toString();
                data[4][j] = namedEntities.get(j);
                data[5][j] = lemmas.get(j);
            }
        }

        return data;
    }
}
