package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.keyphrasecooccurrence.EntityKeyphraseCooccurrenceEntry;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Iterator;

public class YagoEntityKeyphraseCooccurrenceDataProviderIterator implements Iterator<EntityKeyphraseCooccurrenceEntry> {

  private TIntObjectIterator<TIntIntHashMap> entitiesIterator;

  private TIntIntIterator currentEntityKeyphrasesIterator;

  public YagoEntityKeyphraseCooccurrenceDataProviderIterator(TIntObjectHashMap<TIntIntHashMap> superdocKeyphraseCounts) {
    entitiesIterator = superdocKeyphraseCounts.iterator();
    if (entitiesIterator.hasNext()) {
      entitiesIterator.advance();
      currentEntityKeyphrasesIterator = entitiesIterator.value().iterator();
    } else {
      currentEntityKeyphrasesIterator = null;
    }

  }

  @Override public boolean hasNext() {
    if (currentEntityKeyphrasesIterator == null) {
      return false;
    }
    if (currentEntityKeyphrasesIterator.hasNext()) {
      return true;
    } else {
      if (entitiesIterator.hasNext()) {
        do {
          entitiesIterator.advance();
          currentEntityKeyphrasesIterator = entitiesIterator.value().iterator();
        } while (entitiesIterator.hasNext() && !currentEntityKeyphrasesIterator.hasNext());

        return currentEntityKeyphrasesIterator.hasNext();
      } else {
        return false;
      }
    }
  }

  @Override public EntityKeyphraseCooccurrenceEntry next() {
    while (entitiesIterator.hasNext() && !currentEntityKeyphrasesIterator.hasNext()) {
      entitiesIterator.advance();
      currentEntityKeyphrasesIterator = entitiesIterator.value().iterator();
    }
    if (!currentEntityKeyphrasesIterator.hasNext()) {
      return null;
    }

    currentEntityKeyphrasesIterator.advance();

    int entity = entitiesIterator.key();
    int keyphrase = currentEntityKeyphrasesIterator.key();
    int count = currentEntityKeyphrasesIterator.value();

    EntityKeyphraseCooccurrenceEntry entry = new EntityKeyphraseCooccurrenceEntry(entity, keyphrase, count);

    return entry;
  }

  @Override public void remove() {
    // Auto-generated method stub

  }
}

