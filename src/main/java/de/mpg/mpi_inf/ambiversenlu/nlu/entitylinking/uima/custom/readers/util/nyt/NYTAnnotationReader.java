package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.nyt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

public class NYTAnnotationReader {

    private Logger logger = LoggerFactory.getLogger(NYTAnnotationReader.class);

    public static Map<Integer, Map<Integer, NYTMention>> getNytAnnotations(String path) throws IOException {

        final Map<Integer, Map<Integer, NYTMention>> annotations = new HashMap<>();
        final Integer[] docId = new Integer[1];
        Files.lines(Paths.get(path)).forEach(l -> {
            String[] split = l.split("\t");
            if (split.length == 2) {
                docId[0] = Integer.parseInt(split[0]);
                annotations.put(docId[0], new TreeMap<>());
            } else if (split.length == 7) { //Extract the entities with annotation
                double label = Double.parseDouble(split[1]); //The label is on position 1
                String entityMention = split[3]; //The entity mention is on position 3
                int begin = Integer.parseInt(split[4]); //The mention begin index
                int end = Integer.parseInt(split[5]); //The mention end index
                String fbId = split[6];

                NYTMention mention = new NYTMention(fbId, label, entityMention, begin, end);

                if (docId[0] != null) {
                    annotations.get(docId[0]).put(begin, mention);
                }
            } else if (split.length == 1) {
                docId[0] = null; //Start for new document
            }
        });
        return annotations;
    }

    public static Set<String> getAllIds(String path) throws IOException {
        final Set<String> ids = new HashSet<>();
        final Integer[] docId = new Integer[1];
        Files.lines(Paths.get(path)).forEach(l -> {
            String[] split = l.split("\t");

            if (split.length == 7) { //Extract the entities with annotation
                String freebaseId = split[6];
                ids.add(freebaseId);
            }
        });
        return ids;
    }

    public static Map<String, String> getFreebase2YagoIds(String path) throws IOException {
        final Map<String, String> ids = new HashMap<>();
        final Integer[] docId = new Integer[1];
        Files.lines(Paths.get(path)).forEach(l -> {
            String[] split = l.split("\t");
            ids.put(split[0], split[1]);

        });
        return ids;
    }

    public static class NYTMention {
        private String id;
        private double label;
        private String entityMention;
        private int begin;
        private int end;

        public NYTMention() {
        }

        public NYTMention(double label, String entityMention, int begin, int end) {
            this.label = label;
            this.entityMention = entityMention;
            this.begin = begin;
            this.end = end;
        }

        public NYTMention(String id, double label, String entityMention, int begin, int end) {
            this.id = id;
            this.label = label;
            this.entityMention = entityMention;
            this.begin = begin;
            this.end = end;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getLabel() {
            return label;
        }

        public void setLabel(double label) {
            this.label = label;
        }

        public String getEntityMention() {
            return entityMention;
        }

        public void setEntityMention(String entityMention) {
            this.entityMention = entityMention;
        }

        public int getBegin() {
            return begin;
        }

        public void setBegin(int begin) {
            this.begin = begin;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        @Override
        public String toString() {
            return "NYTMention{" +
                    "label=" + label +
                    ", entityMention='" + entityMention + '\'' +
                    ", begin=" + begin +
                    ", end=" + end +
                    '}';
        }
    }

    public static void main(String[] args) throws IOException {
        Map<Integer, Map<Integer, NYTMention>> annotations = getNytAnnotations("/Users/corrogg/Downloads/testMap/testMap.txt");
        for(Entry<Integer, NYTMention> m : annotations.get(1529781).entrySet()) {
            System.out.println(m.getValue());
        }
    }
}
