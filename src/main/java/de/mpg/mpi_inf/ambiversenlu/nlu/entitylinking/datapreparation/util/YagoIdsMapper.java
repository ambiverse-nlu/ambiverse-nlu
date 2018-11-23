package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util;

public interface YagoIdsMapper {

  public String mapToYagoId(String otherKBId);

  public String mapFromYagoId(String yagoId);

  public int mapToYagoId(int otherKBInternalId);

  public int mapFromYagoId(int yagoInernalId);

}
