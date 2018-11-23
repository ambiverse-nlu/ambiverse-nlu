package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.roaringbitmap.RoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MilneWittenEntityEntitySimilarity extends EntityEntitySimilarity {

  private static final Logger logger = LoggerFactory.getLogger(MilneWittenEntityEntitySimilarity.class);

  private TIntObjectHashMap<RoaringBitmap> entity2vector;

  private int collectionSize;

  public MilneWittenEntityEntitySimilarity(EntityEntitySimilarityMeasure similarityMeasure, EntitiesContext entityContext) throws Exception {
    // not needed - uses entites directly
    super(similarityMeasure, entityContext);

    setupEntities(entityContext.getEntities());
  }

  private void setupEntities(Entities entities) throws Exception {
    logger.debug("Initializing MilneWittenEntityEntitySimilarity for " + entities.size() + " entities");

    collectionSize = DataAccess.getCollectionSize();

    TIntObjectHashMap<int[]> entityInlinks = DataAccess.getInlinkNeighbors(entities);

    // inlinks are assumed to be pre-sorted.
    entity2vector = new TIntObjectHashMap<>();

    for (TIntObjectIterator<int[]> itr = entityInlinks.iterator(); itr.hasNext(); ) {
      itr.advance();
      int entity = itr.key();
      int[] inLinks = itr.value();

      RoaringBitmap bs = new RoaringBitmap();
      for (int l : inLinks) {
        bs.add(l);
      }
      entity2vector.put(entity, bs);
    }

    logger.debug("Done initializing MilneWittenEntityEntitySimilarity for " + entities.size() + " entities");
  }

  @Override public double calcSimilarity(Entity a, Entity b) throws Exception {
    RoaringBitmap bsA = entity2vector.get(a.getId());
    RoaringBitmap bsB = entity2vector.get(b.getId());

    int sizeA = bsA.getCardinality();
    int sizeB = bsB.getCardinality();

    int max = -1;
    int min = -1;

    if (sizeA >= sizeB) {
      max = sizeA;
      min = sizeB;
    } else {
      max = sizeB;
      min = sizeA;
    }

    double sim = 0.0; // default is no sim

    int overlap = RoaringBitmap.and(bsA, bsB).getCardinality();

    if (overlap > 0) {
      // now calc the real similarity
      double distance = (Math.log(max) - Math.log((double) overlap)) / (Math.log(collectionSize) - Math.log(min));

      sim = 1 - distance;

      if (distance > 1.0) {
        // really far apart ...
        sim = 0.0;
      }
    }
    
//    System.out.println("EE-Milne: " + a.getIdentifierInKb() + " " + b.getIdentifierInKb() + " " + sim);
    return sim;
  }
}
