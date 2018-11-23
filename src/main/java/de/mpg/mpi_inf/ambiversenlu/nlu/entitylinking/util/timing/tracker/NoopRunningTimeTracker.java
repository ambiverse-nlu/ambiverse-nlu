package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.tracker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.data.TimingInfo;

public class NoopRunningTimeTracker extends RunningTimeTracker {

  @Override public Integer recordStartTime(String moduleId) {
    //    updateOverallStartTime(System.currentTimeMillis());
    return 0;
  }

  @Override public Long recordEndTime(String moduleId, Integer uniqueId) {
    //    updateOverallEndTime(System.currentTimeMillis());
    return 0l;
  }

  @Override public TimingInfo getTrackedInfo() {
    TimingInfo tInfo = new TimingInfo();
    //    tInfo.setTotalExecutionTime(overallEndTime - overallStartTime);
    return tInfo;
  }

  @Override public void clearTrackedInfo() {
    return;
  }
}