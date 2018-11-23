package de.mpg.mpi_inf.ambiversenlu.nlu.ner.util;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.jcas.JCas;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

public class NamedEntities {

    private Map<String, String> namedEntityMap = new HashMap<>();

    public void put(String word, String classLabel) {
        namedEntityMap.put(word, classLabel);
    }

    public Map<String, String> getWordLabelsMap() {
        return Collections.unmodifiableMap(namedEntityMap);
    }

    public static NamedEntities getNamedEntitiesFromJCas(JCas jCas) {
        NamedEntities nes = new NamedEntities();
        for (Sentence sentence : select(jCas, Sentence.class)) {
            List<NamedEntity> dknes = selectCovered(jCas, NamedEntity.class, sentence);
            for (NamedEntity ne : dknes) {
                nes.put(ne.getCoveredText(), ne.getValue());
            }
        }
        return nes;
    }
}
