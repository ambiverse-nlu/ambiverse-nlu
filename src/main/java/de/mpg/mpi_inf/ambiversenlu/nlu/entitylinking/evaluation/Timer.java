package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation;

import java.util.HashMap;
import java.util.Map;

public class Timer {

  public enum STAGE {DOC}

  ;

  private Map<STAGE, Long> time = new HashMap<>();

  private Map<STAGE, Boolean> active = new HashMap<>();

  protected Timer() {
  }

  public void startTimer(STAGE stage) {
    if (active.get(stage != null && active.get(stage))) {
      throw new IllegalStateException("A timer of type " + stage.name() + " has been already initialized");
    }
    active.put(stage, true);
    time.put(stage, System.currentTimeMillis());
  }

  public void endTimer(STAGE stage) {
    if (active.get(stage == null && !active.get(stage))) {
      throw new IllegalStateException("A timer of type " + stage.name() + " has not been initialized");
    }
    active.put(stage, false);
    time.put(stage, System.currentTimeMillis() - time.get(stage));
  }

  public long getTimeinMills(STAGE stage) {
    if (active.get(stage == null)) {
      throw new IllegalStateException("A timer of type " + stage.name() + " has not been initialized");
    } else if (active.get(stage)) {
      throw new IllegalStateException("A timer of type " + stage.name() + " has not been stopped");
    }
    return time.get(stage);
  }

  protected Map<STAGE, Long> getTimes() {
    return time;
  }

  public void setTimes(Map<STAGE, Long> time) {
    this.time = time;
  }

}
