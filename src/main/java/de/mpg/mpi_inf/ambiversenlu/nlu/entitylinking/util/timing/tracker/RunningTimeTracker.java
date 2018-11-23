package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.tracker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.data.TimingInfo;

public abstract class RunningTimeTracker {

  protected long overallStartTime = 0;

  protected long overallEndTime = 0;

  protected void updateOverallStartTime(long timestamp) {
    //set the overall start time
    if (overallStartTime == 0) {
      overallStartTime = timestamp;
    }
  }

  protected void updateOverallEndTime(long timestamp) {
    overallEndTime = timestamp;
  }

  public abstract Integer recordStartTime(String moduleId);

  public abstract Long recordEndTime(String moduleId, Integer uniqueId);

  public abstract TimingInfo getTrackedInfo();

  public abstract void clearTrackedInfo();
}
