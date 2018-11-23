package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.PipelinesHolder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;

public class EntitySalienceProcessorAnalysisEngineSpark extends SparkSerializableAnalysisEngine {

    private static final long serialVersionUID = 5379348579430029337L;
    private static Logger logger = LoggerFactory.getLogger(EntitySalienceProcessorAnalysisEngineSpark.class);

    public EntitySalienceProcessorAnalysisEngineSpark() throws ResourceInitializationException {
        super();
        this.initialize();
    }

    @Override
    public void initialize() throws ResourceInitializationException{
        try {
            logger.debug("Setting analysis engine to AIDA.");
            this.setAnalysisEngine(PipelinesHolder.getAnalyisEngine(PipelineType.DISAMBIGUATION));
        } catch (InvalidXMLException | IOException | ClassNotFoundException | EntityLinkingDataAccessException | MissingSettingException | NoSuchMethodException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new ResourceInitializationException(e);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Run the default deserialization process
        in.defaultReadObject();

        // Re-initialize the analysis engine after the collection ID has been read
        try {
            initialize();
        } catch (ResourceInitializationException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new IOException(e);
        }
    }
}
