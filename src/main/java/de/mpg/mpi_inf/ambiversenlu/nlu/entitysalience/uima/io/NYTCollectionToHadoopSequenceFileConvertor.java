package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.io;

import com.beust.jcommander.Parameter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.NYTCollectionReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.ParameterizedExecutable;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience.uima.SparkUimaUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class NYTCollectionToHadoopSequenceFileConvertor extends ParameterizedExecutable {

    private Logger logger = LoggerFactory.getLogger(NYTCollectionToHadoopSequenceFileConvertor.class);

    @Parameter(names = { "--input", "-i"}, description = "Input directory")
    private String input;

    @Parameter(names = { "--subfolder", "-s"}, description = "Selected Subfolders")
    private List<String> subfolder;

    @Parameter(names = { "--annotated", "-a"}, description = "Annotated File.")
    private String annotatedFile;

    @Parameter(names = { "--mapping", "-m"}, description = "IDs mapping file.")
    private String idsMappingFile;


    @Parameter(names = { "--output", "-o"}, description = "Output Directory.")
    private String output;



    public NYTCollectionToHadoopSequenceFileConvertor(String[] args) throws Exception {
        super(args);
    }

    @Override
    protected int run() throws Exception {
        File imputDir = new File(input);
        File[] yearDirs = imputDir.listFiles(d -> d.isDirectory());

        if(subfolder!= null && !subfolder.isEmpty()) {
            yearDirs = new File[subfolder.size()];
            for(int i=0; i<subfolder.size(); i++) {
                yearDirs[i] = new File(imputDir.getAbsolutePath()+"/"+subfolder.get(i));
            }
        }

        logger.info("Parallel processing the following directories: "+Arrays.toString(yearDirs));

        Arrays.stream(yearDirs).parallel()
                .forEach(dir -> {

                    logger.info("Processing '{}'.", dir);
                    try {
                        Object[] params = {NYTCollectionReader.PARAM_SOURCE_LOCATION, dir,
                            NYTCollectionReader.PARAM_PATTERNS, "[+]/**/*.xml",
                            NYTCollectionReader.PARAM_LANGUAGE, "en",
                            NYTCollectionReader.PARAM_OFFSET, 0,
                            NYTCollectionReader.PARAM_ANNOTATED_FILE, annotatedFile,
                            NYTCollectionReader.PARAM_ID_MAPPING_FILE, idsMappingFile};

                        String outputFileName = dir.getName() + ".seq";
                        File outputFile = new File(output, outputFileName);
                        SparkUimaUtils.createSequenceFile(params, outputFile.getAbsolutePath());
                    } catch (ResourceInitializationException e) {
                        throw new RuntimeException(e);
                    } catch (IOException | URISyntaxException | UIMAException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    } catch (MissingSettingException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } logger.info("Processed '" + dir + "'.");
                });

        return 0;
    }

    public static void main(String[] args) throws Exception {
        new NYTCollectionToHadoopSequenceFileConvertor(args).run();
    }
}
