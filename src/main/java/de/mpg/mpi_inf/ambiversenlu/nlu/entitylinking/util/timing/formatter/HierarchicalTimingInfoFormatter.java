package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.formatter;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.NiceTime;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.data.Module;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.data.TimingInfo;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class HierarchicalTimingInfoFormatter implements TimingInfoFormatter {

  private static final String NEW_LINE = "\n";

  private static final String CUSTOM_TAB_SPACE = "\t\t\t";

  private static final String TAB_SPACE = "\t";

  private NumberFormat nf;

  public HierarchicalTimingInfoFormatter() {
    nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
    nf.setMaximumFractionDigits(2);
  }

  @Override public String format(TimingInfo timingInfo) {
    StringBuilder sb = new StringBuilder();
    if (timingInfo.getAllModules().size() > 0) {
      //sb.append("Module Overview\n").append("===============\n");
      sb.append("Level").append(CUSTOM_TAB_SPACE).append("Module").append(CUSTOM_TAB_SPACE).append("#Calls").append(CUSTOM_TAB_SPACE)
          .append("Avg. Time (ms)").append(CUSTOM_TAB_SPACE).append("Max. Time (ms)").append(CUSTOM_TAB_SPACE).append("Avg. Time (readable)")
          .append(CUSTOM_TAB_SPACE).append("Max. Time (readable)").append(NEW_LINE);

      for (Module m : timingInfo.getAllModules()) {
        sb.append(m.getExecutionLevel()).append(CUSTOM_TAB_SPACE);
        for (int sp = 1; sp < m.getExecutionLevel(); sp++) {
          sb.append("\t");
        }

        sb.append(m.getId()).append(CUSTOM_TAB_SPACE);
        sb.append(m.getNumberOfCalls()).append(CUSTOM_TAB_SPACE);
        sb.append(nf.format(m.getAverageExecutionTime())).append(CUSTOM_TAB_SPACE);
        sb.append(nf.format(m.getMaximumExecutionTime())).append(CUSTOM_TAB_SPACE);
        sb.append(NiceTime.convert(m.getAverageExecutionTime())).append(CUSTOM_TAB_SPACE);
        sb.append(NiceTime.convert(m.getMaximumExecutionTime())).append(NEW_LINE);
      }
    }
    sb.append("Total Time Taken").append(TAB_SPACE).append(NiceTime.convert(timingInfo.getTotatExecutionTime())).append(NEW_LINE);
    sb.append("Total Time Taken (ms)").append(TAB_SPACE).append(timingInfo.getTotatExecutionTime()).append(NEW_LINE);
    return sb.toString();
  }

  @Override public TimingInfo parse(String content) throws ParseException {
    return constructTimingInfo(content);
  }

  private TimingInfo constructTimingInfo(String content) throws ParseException {
    String[] lines;
    int totLines = 0;
    TimingInfo retInfo = new TimingInfo();
    try {
      lines = content.split("\n");
      totLines = lines.length;
      // ignoring the first line(header) and last 2 lines(Total time info)      
      for (int i = 1; i < totLines - 2; i++) {
        String[] module = lines[i].split(CUSTOM_TAB_SPACE);
        retInfo.addModule(new Module(module[1], Integer.parseInt(module[0]), Integer.parseInt(module[2]), Double.parseDouble(module[3]),
            Double.parseDouble(module[4])));
      }
      // also append the total time (last line containing time in ms)
      retInfo.setTotalExecutionTime(Long.parseLong(lines[totLines - 1].split(TAB_SPACE)[1]));
    } catch (Exception e) {
      e.printStackTrace();
      throw new ParseException("Invalid Format encountered", 0);
    }
    return retInfo;
  }
}