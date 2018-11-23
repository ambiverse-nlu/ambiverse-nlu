package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.conf;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.Yago3PrepConf;

import java.io.IOException;

public class DataPrepConfFactory {

  public static DataPrepConf getConf(DataPrepConfName confName) throws IOException {
    switch (confName) {
      case GENERIC:
        return new GenericPrepConf();
      case YAGO3:
        return new Yago3PrepConf();
      case TEST:
        return new TestDataPrepConf();
      default:
        return new DummyDataPrepConf();
    }
  }
}
