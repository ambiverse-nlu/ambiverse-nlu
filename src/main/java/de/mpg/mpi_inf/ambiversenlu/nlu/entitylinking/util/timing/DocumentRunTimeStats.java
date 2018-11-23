package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.NiceTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class DocumentRunTimeStats {

  // need to store map sorted by value.
  private Map<String, Double> documentsCompletionTime;

  //additional criteria
  private boolean findMedian;

  private boolean findPercentile;

  private double percentile;

  private boolean excludeDocumentDetails;

  // default percentile
  private final double DEFAULT_PERCENTILE_VALUE = 0.95;

  /**
   * Default Constructor, returns summary along with list of individual document running times.
   */
  public DocumentRunTimeStats(Map<String, Double> documentsCompletionTime) {
    this(documentsCompletionTime, false, false);
  }

  public DocumentRunTimeStats(Map<String, Double> documentsCompletionTime, boolean descOrderSort) {
    this(documentsCompletionTime, false, descOrderSort);
  }

  /**
   *
   * @param documentsCompletionTime
   * @param excludeDocTime Set to true to skip adding individual document execution time to stats summary.
   */
  public DocumentRunTimeStats(Map<String, Double> documentsCompletionTime, boolean excludeDocTime, boolean descOrderSort) {
    this.documentsCompletionTime = CollectionUtils.sortMapByValue(documentsCompletionTime, descOrderSort);
    this.excludeDocumentDetails = excludeDocTime;
  }

  /**
   * Compute stats from already existing file. Suitable for command line execution
   * @param file
   */
  public DocumentRunTimeStats(File file) {
    this(file, false);
  }

  public DocumentRunTimeStats(File file, boolean descOrder) {
    // no need to return already known individual timings!
    this.excludeDocumentDetails = true;

    Map<String, Double> tempHash = new HashMap<String, Double>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));
      String line = null;
      while (true) {
        line = br.readLine();
        if (line == null) {
          break;
        }
        StringTokenizer st = new StringTokenizer(line);
        int count = st.countTokens();

        if (count != 2) {
          // exit!
          System.err.println("File Doesnt seem to be in right format : <DOCID><SPACE><TOTALTIME>");
          System.exit(1);
        }

        String docid = st.nextToken();
        String runTimeStr = st.nextToken();
        // file can contain time info in  a,xyz.sdf format
        runTimeStr = runTimeStr.replaceAll(",", "");
        double runTime = Double.parseDouble(runTimeStr.substring(0, runTimeStr.length() - 1));
        tempHash.put(docid, runTime);
      }

      // sort the map
      this.documentsCompletionTime = CollectionUtils.sortMapByValue(tempHash, descOrder);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      try {
        br.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public DocumentRunTimeStats computeMedian() {
    this.findMedian = true;
    return this;
  }

  /**
   * A default 95 percentile is used.
   *
   * @return
   */
  public DocumentRunTimeStats computePercentile() {
    this.findPercentile = true;
    this.percentile = DEFAULT_PERCENTILE_VALUE;
    return this;
  }

  public DocumentRunTimeStats computePercentile(double percentile) {
    this.findPercentile = true;
    this.percentile = percentile;
    return this;
  }

  public String generateStats() {
    StringBuilder completeInfo = new StringBuilder();
    StringBuilder specificInfo = new StringBuilder();
    int totDocuments = documentsCompletionTime.size();
    int medianPos = -1;
    int percentilPos = -1;

    specificInfo.append("Total Documents : " + totDocuments).append("\n");

    if (findMedian) {
      medianPos = totDocuments / 2;
    }

    if (findPercentile) {
      percentilPos = (int) (percentile * totDocuments);
    }

    int tmpCnt = 0;
    double overallTime = 0;
    NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
    nf.setMaximumFractionDigits(2);
    for (Entry<String, Double> e : documentsCompletionTime.entrySet()) {
      tmpCnt++;
      overallTime += e.getValue();
      completeInfo.append(e.getKey()).append("\t").append(nf.format(e.getValue()) + "ms").append("\n");
      if (medianPos != -1 && tmpCnt == medianPos) {
        specificInfo.append("Median : ").append(nf.format(e.getValue()) + "ms").append(" ( Document : ").append(e.getKey()).append(")").append("\n");
      }

      if (percentilPos != -1 && tmpCnt == percentilPos) {
        specificInfo.append(percentile * 100).append(" Percentile : ").append(nf.format(e.getValue()) + "ms").append(" ( Document : ")
            .append(e.getKey()).append(")").append("\n");
      }
    }

    if (!excludeDocumentDetails) {
      specificInfo.append("\n").append(completeInfo.toString());
    }

    specificInfo.append("\nOverall Execution Time : ").append(NiceTime.convert(overallTime)).append("\n");
    return specificInfo.toString();
  }

  public static void main(String args[]) {
    if (args.length == 0) {
      System.err.println("Provide one command line arg representing the file location");
      System.exit(1);
    }

    File f = new File(args[0]);
    String content = new DocumentRunTimeStats(f).computeMedian().computePercentile(0.95).generateStats();
    System.out.println(content);
  }
}
