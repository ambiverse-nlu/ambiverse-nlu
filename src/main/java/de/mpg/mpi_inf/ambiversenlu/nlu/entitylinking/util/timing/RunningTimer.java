package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.document.DocumentTimeTracker;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.formatter.HierarchicalTimingInfoFormatter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.formatter.TimingInfoFormatter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.tracker.NoopRunningTimeTracker;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.tracker.RealRunningTimeTracker;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.tracker.RunningTimeTracker;

/**
 * Captures the running time of modules and their stages. The timing is not
 * perfectly accurate due to synchronization, however the contention should
 * be very low. Don't use this class to do high-frequency profiling, this 
 * is useful for high-level performance measuring. 
 *
 * A module is usually a class and a stage is usually one logical
 * step inside the class.
 *
 *
 */
public class RunningTimer {

  private static RunningTimeTracker tracker_ = new NoopRunningTimeTracker();

  private static DocumentTimeTracker docTracker_ = new DocumentTimeTracker();

  private static TimingInfoFormatter timeInfoFormatter = new HierarchicalTimingInfoFormatter();

  public static void clear() {
    tracker_.clearTrackedInfo();
    docTracker_.clearTrackedInfo();
  }
  
  /*
   * Document Timing related methods 
   */

  public static String getTrackedDocumentTime() {
    return docTracker_.getTrackedDocInfo();
  }

  public static String getTrackedDocumentTime(boolean descOrderTotalTime) {
    return docTracker_.getTrackedDocInfo(descOrderTotalTime);
  }

  public static String getDetailedOverview() {
    return timeInfoFormatter.format(tracker_.getTrackedInfo());
  }

  public static void trackDocumentTime(String docid, double totTime) {
    docTracker_.recordDocumentRunTime(docid, totTime);
  }

  /*
   * RunningTime related methods 
   */

  public static Integer recordStartTime(String moduleId) {
    return tracker_.recordStartTime(moduleId);
  }

  public static long recordEndTime(String moduleId, Integer uniqueId) {
    return tracker_.recordEndTime(moduleId, uniqueId);
  }

  public static void enableRealTimeTracker() {
    tracker_ = new RealRunningTimeTracker();
  }
}
