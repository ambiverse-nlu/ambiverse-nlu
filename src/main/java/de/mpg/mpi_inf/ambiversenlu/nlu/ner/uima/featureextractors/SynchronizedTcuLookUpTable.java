package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors;

/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Provides speedy access to the TextClassificationUnits (TCU) covered by a TextClassificationSequence.
 * Enables faster access to the previous/next TCU
 * The look-up tables provided here are build for each new sequence.
 */
public class SynchronizedTcuLookUpTable
        extends FeatureExtractorResource_ImplBase
        implements FeatureExtractor
{
    private  static final ThreadLocal<String> lastSeenDocumentIdTL = new ThreadLocal<>();

    protected static final ThreadLocal<HashMap<Integer, Boolean>> idx2SequenceBeginTL = ThreadLocal.withInitial(HashMap::new);
    protected static final ThreadLocal<HashMap<Integer, Boolean>> idx2SequenceEndTL = ThreadLocal.withInitial(HashMap::new);

    protected static final ThreadLocal<HashMap<Integer, TextClassificationTarget>> begin2UnitTL = ThreadLocal.withInitial(HashMap::new);
    protected static final ThreadLocal<HashMap<Integer, Integer>> unitBegin2IdxTL = ThreadLocal.withInitial(HashMap::new);
    protected static final ThreadLocal<HashMap<Integer, Integer>> unitEnd2IdxTL = ThreadLocal.withInitial(HashMap::new);
    protected static final ThreadLocal<List<TextClassificationTarget>> unitsTL = ThreadLocal.withInitial(ArrayList::new);

    public Set<Feature> extract(JCas aView,
                                TextClassificationTarget aClassificationUnit)
            throws TextClassificationException {
        if (isTheSameDocument(aView)) {

            HashMap<Integer, Integer> unitBegin2Idx = unitBegin2IdxTL.get();
            Integer idx = unitBegin2Idx.get(aClassificationUnit.getBegin());
            if (idx == unitBegin2Idx.size() - 1) {
                lastSeenDocumentIdTL.remove();
            }
            return null;
        }

        begin2UnitTL.set(new HashMap<>());
        unitBegin2IdxTL.set(new HashMap<>());
        unitEnd2IdxTL.set(new HashMap<>());
        idx2SequenceBeginTL.set(new HashMap<>());
        idx2SequenceEndTL.set(new HashMap<>());
        unitsTL.set(new ArrayList<>());
        idx2SequenceBeginTL.set(new HashMap<>());
        idx2SequenceEndTL.set(new HashMap<>());

        HashMap<Integer, TextClassificationTarget> begin2Unit = begin2UnitTL.get();
        HashMap<Integer, Integer> unitBegin2Idx = unitBegin2IdxTL.get();
        HashMap<Integer, Integer> unitEnd2Idx = unitEnd2IdxTL.get();
        List<TextClassificationTarget> units = unitsTL.get();
        HashMap<Integer, Boolean> idx2SequenceBegin = idx2SequenceBeginTL.get();
        HashMap<Integer, Boolean> idx2SequenceEnd = idx2SequenceEndTL.get();

        int i = 0;
        for (TextClassificationTarget t : JCasUtil.select(aView, TextClassificationTarget.class)) {
            Integer begin = t.getBegin();
            Integer end = t.getEnd();
            begin2Unit.put(begin, t);
            unitBegin2Idx.put(begin, i);
            unitEnd2Idx.put(end, i);
            units.add(t);
            i++;
        }
        for (TextClassificationSequence sequence : JCasUtil.select(aView, TextClassificationSequence.class)) {
            Integer begin = sequence.getBegin();
            Integer end = sequence.getEnd();
            Integer idxStartUnit = unitBegin2Idx.get(begin);
            Integer idxEndUnit = unitEnd2Idx.get(end);
            idx2SequenceBegin.put(idxStartUnit, true);
            idx2SequenceEnd.put(idxEndUnit, true);
        }
        return null;
    }

    private boolean isTheSameDocument(JCas aView) {
        DocumentMetaData meta = JCasUtil.selectSingle(aView,
                DocumentMetaData.class);
        String currentId = meta.getDocumentId();
        boolean isSame = currentId.equals(lastSeenDocumentIdTL.get());
        lastSeenDocumentIdTL.set(currentId);
        return isSame;
    }

}

