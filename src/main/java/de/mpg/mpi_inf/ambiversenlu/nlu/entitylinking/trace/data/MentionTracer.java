package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.data;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mention;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Collection;

public class MentionTracer {

  private TIntObjectHashMap<EntityTracer> entities = new TIntObjectHashMap<EntityTracer>();

  private Mention mention;

  public MentionTracer(Mention mention) {
    this.mention = mention;
  }

  public String getName() {
    return mention.getMention();
  }

  public EntityTracer getEntityTracer(int entity) {
    return entities.get(entity);
  }

  public int getOffset() {
    return mention.getCharOffset();
  }

  public void addEntityTracer(int entity, EntityTracer entityTracer) {
    entities.put(entity, entityTracer);
  }

  public Collection<EntityTracer> getEntityTracers() {
    return entities.valueCollection();
  }

  public int getLength() {
    return mention.getCharLength();
  }

  public String getMentionStr() {
    return mention.getCharOffset() + ": " + mention.getMention();
  }
}
