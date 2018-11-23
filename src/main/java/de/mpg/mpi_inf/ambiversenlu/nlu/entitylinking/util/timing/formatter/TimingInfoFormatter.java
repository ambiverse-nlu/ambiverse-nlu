package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.formatter;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.data.TimingInfo;

import java.text.ParseException;

public interface TimingInfoFormatter {

  public String format(TimingInfo timingInfo);

  public TimingInfo parse(String content) throws ParseException;
}
