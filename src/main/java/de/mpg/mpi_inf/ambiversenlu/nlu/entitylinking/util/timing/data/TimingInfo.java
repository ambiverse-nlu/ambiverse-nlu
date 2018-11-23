package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TimingInfo {

  private List<Module> modules = new LinkedList<Module>();

  private long totalExecutionTime;

  public void addModule(Module module) {
    modules.add(module);
  }

  public void setTotalExecutionTime(long timeInMs) {
    totalExecutionTime = timeInMs;
  }

  public long getTotatExecutionTime() {
    return totalExecutionTime;
  }

  /**
   * Returns a module with given Id.
   * Returns null if no match.
   * @param moduleId
   * @return
   */
  public Module getModule(String moduleId) {
    for (Module m : modules) {
      if (m.getId().equals(moduleId)) {
        return m;
      }
    }
    return null;
  }

  /**
   * Returns all modules
   * @return
   */
  public List<Module> getAllModules() {
    return modules;
  }

  /**
   * Returns a list of modules at the given level.
   * Returns null if no modules are found.
   *
   * @param moduleLevel
   * @return
   */
  public List<Module> getModules(int moduleLevel) {
    List<Module> retList = null;
    for (Module m : modules) {
      if (m.getExecutionLevel() == moduleLevel) {
        if (retList == null) {
          retList = new ArrayList<Module>();
        }
        retList.add(m);
      }
    }
    return retList;
  }
}