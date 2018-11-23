package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactSource.FileFactSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GenericFileReader implements GenericReader {

	private Logger logger = LoggerFactory.getLogger(GenericFileReader.class);

	private String fileLocation;

	private Map<String, List<Fact>> relations2SubjectObjectPairs;

	private boolean loaded = false;

	public GenericFileReader(String fileLocation) {
		this.fileLocation = fileLocation;
		relations2SubjectObjectPairs = new HashMap<>();
	}

	private void load() {
		FileFactSource fileFactSource = new FileFactSource(new File(fileLocation));

		int count = 0;
		for (Fact fact : fileFactSource) {
			// Remove facts on which we cannot build the index
			if (fact.getObject().length() > 1000) {
				continue;
			}
			String relation = fact.getRelation();
			List<Fact> list = relations2SubjectObjectPairs.get(relation);
			if (list == null) {
				list = new ArrayList<Fact>();
				relations2SubjectObjectPairs.put(relation, list);
			}
			list.add(fact);
			if (++count % 10000000 == 0) {
				logger.info("Finished reading " + (count / 1000000) + " million facts!");
			}
		}
	}

	public synchronized List<Fact> getFacts(String relation) {
		if (!loaded) {
			load();
			loaded = true;
		}
		return relations2SubjectObjectPairs.get(relation);
	}

	@Override public Iterator<Fact> iterator(String relation) throws IOException {
		return getFacts(relation).iterator();
	}
}
