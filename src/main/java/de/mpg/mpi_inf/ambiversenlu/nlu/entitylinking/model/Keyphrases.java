package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Holds all the keyphrase data describing a set of entities.
 *
 *
 */
public class Keyphrases {

  private TIntObjectHashMap<int[]> entityKeyphrases;

  private TIntObjectHashMap<TIntIntHashMap> entityKeyphraseSources;

  private TIntDoubleHashMap keyphraseSourceWeights;

  private TObjectIntHashMap<String> keyphraseSource2id;

  private TIntObjectHashMap<int[]> keyphraseTokens;

  private TIntObjectHashMap<TIntDoubleHashMap> entity2keyphrase2mi;

  private TIntObjectHashMap<TIntDoubleHashMap> entity2keyword2mi;

  public void setEntityKeyphrases(TIntObjectHashMap<int[]> entityKeyphrases) {
    this.entityKeyphrases = entityKeyphrases;
  }

  public void setKeyphraseTokens(TIntObjectHashMap<int[]> keyphraseTokens) {
    this.keyphraseTokens = keyphraseTokens;
  }

  public void setEntityKeyphraseWeights(TIntObjectHashMap<TIntDoubleHashMap> entity2keyphrase2mi) {
    this.entity2keyphrase2mi = entity2keyphrase2mi;
  }

  public void setEntityKeywordWeights(TIntObjectHashMap<TIntDoubleHashMap> entity2keyword2mi) {
    this.entity2keyword2mi = entity2keyword2mi;
  }

  public TIntObjectHashMap<int[]> getEntityKeyphrases() {
    return entityKeyphrases;
  }

  public TIntObjectHashMap<int[]> getKeyphraseTokens() {
    return keyphraseTokens;
  }

  public TIntObjectHashMap<TIntDoubleHashMap> getEntityKeywordWeights() {
    return entity2keyword2mi;
  }

  public TIntObjectHashMap<TIntDoubleHashMap> getEntityKeyphraseWeights() {
    return entity2keyphrase2mi;
  }

  public TIntObjectHashMap<TIntIntHashMap> getEntityKeyphraseSources() {
    return entityKeyphraseSources;
  }

  public void setEntityKeyphraseSources(TIntObjectHashMap<TIntIntHashMap> entityKeyphraseSources) {
    this.entityKeyphraseSources = entityKeyphraseSources;
  }

  public TIntDoubleHashMap getKeyphraseSourceWeights() {
    return keyphraseSourceWeights;
  }

  public void setKeyphraseSourceWeights(TIntDoubleHashMap keyphraseSourceWeights) {
    this.keyphraseSourceWeights = keyphraseSourceWeights;
  }

  public TObjectIntHashMap<String> getKeyphraseSource2id() {
    return keyphraseSource2id;
  }

  public void setKeyphraseSource2id(TObjectIntHashMap<String> keyphraseSource2id) {
    this.keyphraseSource2id = keyphraseSource2id;
  }
}
