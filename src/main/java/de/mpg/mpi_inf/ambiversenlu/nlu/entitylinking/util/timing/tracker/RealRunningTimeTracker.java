package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.tracker;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.data.Module;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.data.TimingInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class RealRunningTimeTracker extends RunningTimeTracker {

  /**
   * Map is moduleId -> uniqueId -> timestamp.
   *
   * UniqueId is an id assigned each time start(moduleId) is called. The map
   * is only accessed by start() (which is synchronized), no need to be 
   * concurrency safe. 
   *
   */

  private Map<String, Map<Integer, Long>> moduleStart = new ConcurrentHashMap<String, Map<Integer, Long>>();

  private Map<String, Map<Integer, Long>> moduleEnd = new ConcurrentHashMap<String, Map<Integer, Long>>();

  private Map<String, Integer> moduleLevelStart = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());

  private Map<String, Integer> moduleLevelEnd = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());

  private int currentModuleLevel = 0;

  @Override public Integer recordStartTime(String moduleId) {
    synchronized (this) {
      Integer uniqueId = start(moduleId);

      if (!moduleLevelStart.containsKey(moduleId)) {
        moduleLevelStart.put(moduleId, ++currentModuleLevel);
      }
      return uniqueId;
    }
  }

  @Override public Long recordEndTime(String moduleId, Integer uniqueId) {
    synchronized (this) {
      Long tStamp = end(moduleId, uniqueId);
      if (!moduleLevelEnd.containsKey(moduleId)) {
        moduleLevelEnd.put(moduleId, currentModuleLevel--);
      }
      return tStamp;
    }
  }

  /**
   * Starts the time for the given moduleId.
   *
   * @param moduleId
   */
  private Integer start(String moduleId) {
    Long timestamp = System.currentTimeMillis();

    updateOverallStartTime(timestamp);

    Integer uniqueId = 0;
    Map<Integer, Long> starts = moduleStart.get(moduleId);
    if (starts != null) {
      uniqueId = moduleStart.get(moduleId).size();
    } else {
      starts = new HashMap<Integer, Long>();
      moduleStart.put(moduleId, starts);
    }
    starts.put(uniqueId, timestamp);
    return uniqueId;
  }

  /**
   * Halts the timer for the given moduleId, capturing the full time of 
   * the module. The uniqueId has to correspond to what is returned by 
   * start(moduleId).
   *
   * @param moduleId
   */
  public Long end(String moduleId, Integer uniqueId) {
    Long timestamp = System.currentTimeMillis();
    // every call to recordEndTimer will overwrite the overallEndTime
    updateOverallEndTime(timestamp);
    Map<Integer, Long> end = moduleEnd.get(moduleId);
    if (end == null) {
      end = new ConcurrentHashMap<Integer, Long>();
      moduleEnd.put(moduleId, end);
    }
    end.put(uniqueId, timestamp);
    Long startTime = moduleStart.get(moduleId).get(uniqueId);
    return timestamp - startTime;
  }

  @Override public TimingInfo getTrackedInfo() {
    return getTrackedInfoImpl(moduleStart, moduleEnd);
  }

  private TimingInfo getTrackedInfoImpl(Map<String, Map<Integer, Long>> start, Map<String, Map<Integer, Long>> end) {
    TimingInfo timingInfo = new TimingInfo();
    for (Entry<String, Integer> e : moduleLevelStart.entrySet()) {
      String module = e.getKey();
      int level = e.getValue();

      double totalTime = 0.0;
      double maxTime = 0.0;
      for (Integer uniqueId : end.get(module).keySet()) {
        Long finish = end.get(module).get(uniqueId);
        assert start.containsKey(module) : "No start for end.";
        Long begin = start.get(module).get(uniqueId);
        Long dur = finish - begin;
        totalTime += dur;
        if (dur > maxTime) {
          maxTime = dur;
        }
      }
      double avgTime = totalTime / end.get(module).size();
      timingInfo.addModule(new Module(module, level, end.get(module).size(), avgTime, maxTime));
    }

    timingInfo.setTotalExecutionTime(overallEndTime - overallStartTime);
    return timingInfo;
  }

  @Override public void clearTrackedInfo() {
    moduleStart.clear();
    moduleEnd.clear();
    moduleLevelStart.clear();
    moduleLevelEnd.clear();
  }
}