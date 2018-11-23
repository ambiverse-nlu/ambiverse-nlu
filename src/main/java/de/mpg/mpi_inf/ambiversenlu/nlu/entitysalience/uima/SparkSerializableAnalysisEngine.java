package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.hadoop.HdfsResourceLoaderLocator;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class SparkSerializableAnalysisEngine implements Serializable {
    private static final long serialVersionUID = -3361419085894614298L;

    private static Logger logger = LoggerFactory.getLogger(SparkSerializableAnalysisEngine.class);

    private transient AnalysisEngine ae;

    public SparkSerializableAnalysisEngine() throws ResourceInitializationException {
    }

    public abstract void initialize() throws ResourceInitializationException;

    public SCAS process(SCAS scas) throws CASException, AnalysisEngineProcessException {
        logger.debug("Processing scas.");
        this.ae.process(scas.getJCas());
        return scas;
    }

    public AnalysisEngine getAnalysisEngine() {
        return this.ae;
    }

    public void setAnalysisEngine(AnalysisEngine analysisEngine) {
        this.ae = analysisEngine;
    }

    protected ExternalResourceDescription getLocator() {
        return ExternalResourceFactory.createExternalResourceDescription(HdfsResourceLoaderLocator.class, new Object[0]);
    }
}