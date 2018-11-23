package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.test;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TestDictionaryEntriesDataProvider extends DictionaryEntriesDataProvider {

  Map<String, List<DictionaryEntity>> data;

  public TestDictionaryEntriesDataProvider(boolean isPrior) {
    data = new HashMap<>();
    List<DictionaryEntity> entries = new ArrayList<>();
    data.put("Page", entries);
    String kb = "TEST";
    String s = "test_source";
    Language en = Language.getLanguageForString("en");

    DictionaryEntity jp = DictionaryEntity.getDictionaryEntity("Jimmy_Page", kb, s, en, EntityType.NAMED_ENTITY);
    DictionaryEntity lp = DictionaryEntity.getDictionaryEntity("Larry_Page", kb, s, en, EntityType.NAMED_ENTITY);
    DictionaryEntity pb = DictionaryEntity.getDictionaryEntity("Page_(book)", kb, s, en, EntityType.CONCEPT);
    DictionaryEntity pa = DictionaryEntity.getDictionaryEntity("Page_(attendant)", kb, s, en, EntityType.CONCEPT);
    DictionaryEntity bp = DictionaryEntity.getDictionaryEntity("Betty_Page", kb, s, en, EntityType.BOTH);

    entries.add(jp);
    if (isPrior) {
      entries.add(jp);
      entries.add(jp);
    }

    entries.add(lp);
    if (isPrior) {
      entries.add(lp);
      entries.add(lp);
      entries.add(lp);
      entries.add(lp);
    }

    entries.add(pb);
    if (isPrior) {
      entries.add(pb);
      entries.add(pb);
    }

    entries.add(pa);

    entries.add(bp);
    if (isPrior) {
      entries.add(bp);
    }

    DictionaryEntity prob = DictionaryEntity.getDictionaryEntity("PAGE_PROBLEM", kb, s, en, EntityType.BOTH);
    List<DictionaryEntity> probs = new ArrayList<>();
    probs.add(prob);
    data.put("Page (Problem)", probs);

    List<DictionaryEntity> entries123 = new ArrayList<>();
    data.put("123", entries123);

    DictionaryEntity a1 = DictionaryEntity.getDictionaryEntity("123", kb, s, en, EntityType.NAMED_ENTITY);
    DictionaryEntity a2 = DictionaryEntity.getDictionaryEntity("einszweidrei", kb, s, en, EntityType.CONCEPT);

    entries123.add(a1);
    entries123.add(a2);
    if (isPrior) {
      entries123.add(a1);
    }
  }

  @NotNull
  @Override
  public Iterator<Map.Entry<String, List<DictionaryEntity>>> iterator() {
    return data.entrySet().iterator();
  }
}
