package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResourceLocator;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class HdfsResourceLoaderLocator extends Resource_ImplBase implements ExternalResourceLocator {
    public static final String PARAM_FILESYSTEM = "fileSystem";
    @ConfigurationParameter(
        name = "fileSystem",
        mandatory = false
    )
    private String fileSystem;
    private ResourcePatternResolver resolverInstance;

    public HdfsResourceLoaderLocator() {
    }

    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams) throws ResourceInitializationException {
        super.initialize(aSpecifier, aAdditionalParams);

        try {
            if (this.fileSystem == null) {
                new HdfsResourceLoader(new Configuration(true));
            } else {
                this.resolverInstance = new HdfsResourceLoader(new Configuration(), new URI(this.fileSystem));
            }

            return true;
        } catch (URISyntaxException var4) {
            throw new ResourceInitializationException(var4);
        }
    }

    public Object getResource() {
        return this.resolverInstance;
    }
}