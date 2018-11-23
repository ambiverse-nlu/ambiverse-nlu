package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.similarity.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EnsembleMentionEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileLines;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AverageSimilarityScoresCalculator {

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    String arffTrainingFile = args[0];

    String settingsFileDir = null;

    if (args.length > 1) {
      settingsFileDir = args[1];
    }

    Map<Integer, String> pos2attribute = new HashMap<Integer, String>();
    Map<String, Integer> attribute2pos = new HashMap<String, Integer>();

    Map<Integer, Double> attrPos2total = new HashMap<Integer, Double>();
    Map<Integer, Double> attrPos2max = new HashMap<Integer, Double>();

    Map<Integer, Double> attrPos2trueTotal = new HashMap<Integer, Double>();
    Map<Integer, Double> attrPos2trueMax = new HashMap<Integer, Double>();

    int attr = 0;
    int inst = 0;
    int trueInst = 0;
    int trueInstWithPrior = 0;

    for (String line : new FileLines(arffTrainingFile, "Reading scores from ARFF training file: " + arffTrainingFile)) {
      if (line.matches("\\s")) {
        continue;
      } else if (line.startsWith("@attribute")) {
        String[] data = line.split(" ");

        if (data[2].equals("NUMERIC")) {
          String attrName = data[1].replace("'", "");
          pos2attribute.put(attr, attrName);
          attribute2pos.put(attrName, attr);

          attrPos2total.put(attr, 0.0);
          attrPos2trueTotal.put(attr, 0.0);
          attrPos2max.put(attr, 0.0);
          attrPos2trueMax.put(attr, 0.0);

          attr++;
        }
      } else { // instance line
        inst++;
        String[] data = line.split(",");

        boolean correct = false;

        if (data[data.length - 1].equals("TRUE")) {
          trueInst++;
          correct = true;

          int bestPriorId = attribute2pos.get("bestPrior");
          double bestPrior = Double.parseDouble(data[bestPriorId]);

          if (bestPrior > 0.9) {
            trueInstWithPrior++;
          }
        }

        for (int i = 0; i < data.length - 1; i++) { // ignore class
          double current = Double.parseDouble(data[i]);

          Double total = attrPos2total.get(i);
          attrPos2total.put(i, total + current);

          Double max = attrPos2max.get(i);

          if (current > max) {
            attrPos2max.put(i, current);
          }

          if (correct) {
            Double trueTotal = attrPos2trueTotal.get(i);
            attrPos2trueTotal.put(i, trueTotal + current);

            Double trueMax = attrPos2trueMax.get(i);

            if (current > trueMax) {
              attrPos2trueMax.put(i, current);
            }
          }
        }
      }
    }

    System.out.println("Stats:\n----------");
    System.out.println("Total instances: " + inst);
    System.out.println("True instances: " + trueInst);

    System.out.println("\nScores:\n----------");

    for (Integer key : pos2attribute.keySet()) {
      double average = attrPos2total.get(key) / (double) inst;
      double trueAverage = attrPos2trueTotal.get(key) / (double) trueInst;
      double falseAverage = (attrPos2total.get(key) - attrPos2trueTotal.get(key)) / (double) (inst - trueInst);
      double max = attrPos2max.get(key);
      double trueMax = attrPos2trueMax.get(key);

      System.out.println(
          pos2attribute.get(key) + "\n\tavg: " + average + "\n\tmax: " + max + "\n\t\ttrueAvg: " + trueAverage + "\n\t\tfalseAvg: " + falseAverage
              + "\n\t\ttrueMax: " + trueMax);
    }

    System.out.println("\nSetting averages:\n----------");
    if (settingsFileDir != null) {
      BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFileDir + File.separator + "averages.properties"));

      File settingsDir = new File(settingsFileDir);

      File[] settingsFiles = settingsDir.listFiles(new FileFilter() {

        @Override public boolean accept(File pathname) {
          return pathname.getAbsolutePath().endsWith(".properties");
        }
      });

      for (File settingsFile : settingsFiles) {
        double total = 0.0;
        double max = 0.0;

        Properties prop = new Properties();
        prop.load(new FileReader(settingsFile));

        if (prop.containsKey("priorThreshold")) {
          Map<String, Double> mesWeights = new HashMap<String, Double>();

          String[] mes = prop.getProperty("mentionEntitySimilarities").split(" ");

          double[] nonPriorWeights = new double[mes.length / 2];

          for (int i = 0; i < mes.length / 2; i++) {
            String[] conf = mes[i].split(":");
            double weight = Double.parseDouble(conf[2]);

            nonPriorWeights[i] = weight;
          }

          double[] normNonPriorWeights = EnsembleMentionEntitySimilarity.rescaleArray(nonPriorWeights);

          for (int i = 0; i < mes.length / 2; i++) {
            String[] conf = mes[i].split(":");
            String name = conf[0] + ":" + conf[1];

            mesWeights.put(name, normNonPriorWeights[i]);
          }

          double[] withPriorWeights = new double[mes.length / 2 + 1];

          for (int i = mes.length / 2; i < mes.length; i++) {
            String[] conf = mes[i].split(":");
            double weight = Double.parseDouble(conf[2]);

            withPriorWeights[i - mes.length / 2] = weight;
          }

          double priorWeight = Double.parseDouble(prop.getProperty("priorWeight"));
          withPriorWeights[withPriorWeights.length - 1] = priorWeight;

          double[] normWithPriorWeights = EnsembleMentionEntitySimilarity.rescaleArray(withPriorWeights);

          for (int i = mes.length / 2; i < mes.length; i++) {
            String[] conf = mes[i].split(":");
            String name = conf[0] + ":" + conf[1];

            mesWeights.put(name, normWithPriorWeights[i - mes.length / 2]);
          }

          mesWeights.put("prior", normWithPriorWeights[normNonPriorWeights.length - 1]);

          for (String m : mesWeights.keySet()) {
            double weight = mesWeights.get(m);

            Integer id = attribute2pos.get(m);

            double trueAverage = attrPos2trueTotal.get(id) / (double) trueInstWithPrior;
            double trueMax = attrPos2trueMax.get(id);

            total += (trueAverage * weight);
            max += (trueMax * weight);
          }

          Integer priorId = attribute2pos.get("prior");
          double trueAverage = attrPos2trueTotal.get(priorId) / (double) trueInst;
          double trueMax = attrPos2trueMax.get(priorId);

          total += (trueAverage * mesWeights.get("prior"));
          max += (trueMax * mesWeights.get("prior"));
        } else {
          for (String line : new FileLines(settingsFile)) {
            String[] data = line.split(" ?= ?");

            String identifier = data[0];

            if (identifier.equals("mentionEntitySimilarities")) {
              String[] sims = data[1].split(" ");

              for (String sim : sims) {
                String[] conf = sim.split(":");
                String name = conf[0] + ":" + conf[1];
                double weight = Double.parseDouble(conf[2]);

                Integer id = attribute2pos.get(name);

                if (id != null) {
                  double trueAverage = attrPos2trueTotal.get(id) / (double) trueInst;
                  double trueMax = attrPos2trueMax.get(id);

                  total += (trueAverage * weight);
                  max += (trueMax * weight);
                }
              }
            } else if (identifier.equals("priorWeight")) {
              double weight = Double.parseDouble(data[1]);
              Integer priorId = attribute2pos.get("prior");
              double trueAverage = attrPos2trueTotal.get(priorId) / (double) trueInst;
              double trueMax = attrPos2trueMax.get(priorId);

              total += (trueAverage * weight);
              max += (trueMax * weight);
            }
          }
        }

        writer.write(settingsFile.getName().replace(".properties", "") + " = " + total + ":" + max);
        writer.newLine();
      }

      writer.flush();
      writer.close();
    }
  }
}
