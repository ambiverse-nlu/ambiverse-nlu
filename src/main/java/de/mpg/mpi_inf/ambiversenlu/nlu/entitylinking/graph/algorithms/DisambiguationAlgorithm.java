package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.algorithms;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ExternalEntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.PreparedInputChunk;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;

import java.util.List;
import java.util.Map;

public abstract class DisambiguationAlgorithm {

  protected PreparedInputChunk input_;

  protected ExternalEntitiesContext externalContext_;

  protected DisambiguationSettings settings_;

  protected Tracer tracer_;

  public DisambiguationAlgorithm(PreparedInputChunk input, ExternalEntitiesContext externalContext, DisambiguationSettings settings, Tracer tracer) {
    input_ = input;
    externalContext_ = externalContext;
    settings_ = settings;
    tracer_ = tracer;
  }

  public abstract Map<ResultMention, List<ResultEntity>> disambiguate() throws Exception;

}
