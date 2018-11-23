package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.data.TimingInfo;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.formatter.HierarchicalTimingInfoFormatter;

public class RunningTimerTest {

  private void levelOneMethod() {
    Integer id = RunningTimer.recordStartTime("Module1");
    try {
      Thread.sleep(500);
      levelTwoMethod();
      Thread.sleep(500);
    } catch (Exception e) {

    }
    RunningTimer.recordEndTime("Module1", id);
  }

  private void levelTwoMethod() throws Exception {
    Integer id = RunningTimer.recordStartTime("Module2");
    try {
      Thread.sleep(2000);
    } catch (Exception e) {

    }
    RunningTimer.recordEndTime("Module2", id);
  }

  public static void main(String[] args) throws Exception {
    System.out.println("Default No-op Time Tracker");
    new RunningTimerTest().levelOneMethod();
    System.out.println(RunningTimer.getDetailedOverview());
    RunningTimer.clear();

    System.out.println("Enabling Real Time Tracker");
    RunningTimer.enableRealTimeTracker();
    new RunningTimerTest().levelOneMethod();
    // new RunningTimerTest().levelOneMethod();
    String output = RunningTimer.getDetailedOverview();
    System.out.println(output);

    HierarchicalTimingInfoFormatter hFormatter = new HierarchicalTimingInfoFormatter();
    TimingInfo tInf = hFormatter.parse(output);
    System.out.println(tInf.getTotatExecutionTime());
  }
}
