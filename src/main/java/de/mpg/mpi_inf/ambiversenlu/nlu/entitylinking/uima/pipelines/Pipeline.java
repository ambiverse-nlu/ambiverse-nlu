package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Pipeline {

  Map<String, String> steps = new HashMap<>();
  public Map<String, String> getSteps() {
    addSteps();
    return steps;
  }

  abstract void addSteps();
  abstract Set<Language> supportedLanguages();
  void addstep(String previous, String next) {
    if(steps.containsKey(previous) && !steps.get(previous).equals(next)) {
      throw new IllegalArgumentException("The next step of a given previous step must be unique. You tried to include next step"
          + next + " to" + previous + " in Pipeline " + ", but it aleady contained " + steps.get(previous));
    }
    steps.put(previous, next);
  }


}
