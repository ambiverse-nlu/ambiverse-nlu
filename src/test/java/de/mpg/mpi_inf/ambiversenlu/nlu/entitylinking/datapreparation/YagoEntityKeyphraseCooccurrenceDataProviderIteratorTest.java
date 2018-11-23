package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.common.YagoEntityKeyphraseCooccurrenceDataProviderIterator;
import org.junit.Assert;
import org.junit.Test;

public class YagoEntityKeyphraseCooccurrenceDataProviderIteratorTest {

  @Test public void test() {
    TIntObjectHashMap<TIntIntHashMap> map = createHashMap();

    YagoEntityKeyphraseCooccurrenceDataProviderIterator iterator = new YagoEntityKeyphraseCooccurrenceDataProviderIterator(map);
    int count = 0;
    while (iterator.hasNext()) {
      iterator.next();
      count++;
    }
    Assert.assertEquals(count, 9);

  }

  private TIntObjectHashMap<TIntIntHashMap> createHashMap() {
    TIntObjectHashMap<TIntIntHashMap> overallMap = new TIntObjectHashMap<TIntIntHashMap>();
    TIntIntHashMap map1 = new TIntIntHashMap();
    overallMap.put(1, map1);

    TIntIntHashMap map2 = new TIntIntHashMap();
    map2.put(21, 100);
    map2.put(22, 101);
    map2.put(23, 102);
    overallMap.put(2, map2);

    TIntIntHashMap map3 = new TIntIntHashMap();
    map3.put(31, 103);
    map3.put(32, 104);
    map3.put(33, 105);
    overallMap.put(3, map3);

    TIntIntHashMap map4 = new TIntIntHashMap();
    overallMap.put(4, map4);

    TIntIntHashMap map5 = new TIntIntHashMap();
    map5.put(51, 106);
    map5.put(52, 107);
    map5.put(53, 108);
    overallMap.put(5, map5);

    TIntIntHashMap map6 = new TIntIntHashMap();
    overallMap.put(6, map6);

    TIntIntHashMap map7 = new TIntIntHashMap();
    overallMap.put(7, map7);

    return overallMap;
  }

}
