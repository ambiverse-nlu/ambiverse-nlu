package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiestypes.EntitiesTypesEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class GenericEntitiesTypesDataProvider extends EntitiesTypesEntriesDataProvider {
	private GenericReader genericReader;
	private String knowledgebase;

	public GenericEntitiesTypesDataProvider(GenericReader genericReader, String knowledgebase) {
		this.genericReader = genericReader;
		this.knowledgebase = knowledgebase;
	}

	private Multimap<String, String> run() throws IOException {
		List<Fact> typesRelationSet = genericReader.getFacts(Relations.TYPE.getRelation());
		Multimap<String, String> entityTypeMap = ArrayListMultimap.create();

		for (Fact entry : typesRelationSet) {
			entityTypeMap.put(entry.getSubject(), entry.getObject());
		}

		return entityTypeMap;
	}

	@Override
	public Iterator<Entry<String, String>> iterator() {
		try {
			return run().entries().iterator();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getKnowledgebaseName() {
		return knowledgebase;
	}
}