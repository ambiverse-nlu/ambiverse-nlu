package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.FlowControllerFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class PipelinesHolder {

  private static AnalysisEngine[] ae = new AnalysisEngine[PipelineType.values().length];

  private static PipelinesHolder instance = new PipelinesHolder();

  public static AnalysisEngine getAnalyisEngine(PipelineType type)
      throws InvalidXMLException, IOException, ClassNotFoundException, EntityLinkingDataAccessException, MissingSettingException,
      NoSuchMethodException, ResourceInitializationException {
    if (ae[type.ordinal()] == null) {
      ae[type.ordinal()] = instance.createAnalysisEngine(type);
    }
    return ae[type.ordinal()];
  }

  private AnalysisEngine createAnalysisEngine(PipelineType type)
      throws EntityLinkingDataAccessException, ResourceInitializationException, IOException, InvalidXMLException, NoSuchMethodException,
      MissingSettingException, ClassNotFoundException {
    Map<String, String> nextEngine = type.getSteps();
    AggregateBuilder builder = new AggregateBuilder();
    if (!nextEngine
        .isEmpty()) { //We need to allow for a pipeline to be trivilly empty because we separate the process of reading and annotating (UIMA does not allow parallel reading and a custom flow controller)
      Set<String> engines = new HashSet<>(nextEngine.values());
      for (String engine : engines) {
        builder.add(createAnalysisEngineDescription(Component.valueOf(engine)));
      }
      StringJoiner steps = new StringJoiner(",");
      for (Map.Entry<String, String> entry : nextEngine.entrySet()) {
        steps.add(entry.getKey());
        steps.add(entry.getValue());
      }
      builder.setFlowControllerDescription(FlowControllerFactory
          .createFlowControllerDescription(DocumentProcessorFlowController.class, DocumentProcessorFlowController.PARAM_STEPS, steps.toString()));
    }
    return createEngine(builder.createAggregateDescription());
  }

  private AnalysisEngineDescription createAnalysisEngineDescription(Component component)
      throws ResourceInitializationException, IOException, InvalidXMLException, NoSuchMethodException, MissingSettingException,
      ClassNotFoundException {
    AnalysisEngineDescription aed = createEngineDescription(component.component, component.params);
    aed.getAnalysisEngineMetaData().setName(component.name());
    return aed;
  }
}