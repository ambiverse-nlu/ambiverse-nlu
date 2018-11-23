package de.mpg.mpi_inf.ambiversenlu.nlu.ner;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.CassandraConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CassandraConnectionHolder;

import java.io.IOException;

public class NERManager {
    private static NERManager nerManager = null;

    private static synchronized NERManager getTasksInstance() {
        if (nerManager == null) {
            nerManager = new NERManager();
        }
        return nerManager;
    }

    private static CassandraConnectionHolder cassandraConnectionHolder;

    /**
     * Gets a Cassandra connection holder
     *
     * @param read indicates that the application is reading from the Database, this should be true when running AIDA and false when building the repository
     */
    public static synchronized CassandraConnectionHolder getCassandraConnectionHolder(boolean read) throws IOException {
        if (cassandraConnectionHolder == null) {
            cassandraConnectionHolder = new CassandraConnectionHolder(read, CassandraConfig.NER_FILE_NAME);
        }
        return cassandraConnectionHolder;
    }

    public static void shutDown() throws Throwable {
        nerManager = null;
        EntityLinkingConfig.shutdown();
        if (cassandraConnectionHolder != null) {
            cassandraConnectionHolder.finalize();
        }
    }
}
