package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.inlinks.InlinksEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.YagoIdsMapper;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * This providers iterates over all cooccurrences in Pubmed
 * 
 */
public class GenericInlinksDataProvider extends InlinksEntriesDataProvider {

	private boolean mapYagoIdsToOtherKBIds = false;

	private YagoIdsMapper yagoIdsMapper;

	private String knowledgebaseName;

	private GenericReader genericReader;

	public GenericInlinksDataProvider(GenericReader genericReader, YagoIdsMapper yagoIdsMapper,
			String knowledgebaseName) {
		this(genericReader, knowledgebaseName);
		this.yagoIdsMapper = yagoIdsMapper;
		mapYagoIdsToOtherKBIds = true;
	}

	public GenericInlinksDataProvider(GenericReader genericReader, String knowledgebaseName) {
		this.genericReader = genericReader;
		this.knowledgebaseName = knowledgebaseName;
	}

	@Override
	public Iterator<Entry<String, String>> iterator() {
		try {
			return run().iterator();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private List<Entry<String, String>> run() throws IOException {
		List<Entry<String, String>> inlinksList = new LinkedList<Entry<String, String>>();

		List<Fact> inlinksSet = genericReader.getFacts(Relations.LINK_TO.getRelation());

		for (Fact entry : inlinksSet) {
			String inlinkName = entry.getSubject();
			if (mapYagoIdsToOtherKBIds) {
				inlinkName = yagoIdsMapper.mapFromYagoId(inlinkName);
				if (inlinkName == null)
					continue;
			}

			String entityName = entry.getObject();
			if (mapYagoIdsToOtherKBIds) {
				entityName = yagoIdsMapper.mapFromYagoId(entityName);
				if (entityName == null)
					continue;
			}

			inlinksList.add(new AbstractMap.SimpleEntry<>(inlinkName, entityName));
		}

		return inlinksList;

	}

	@Override
	public String getKnowledgebaseName() {
		return knowledgebaseName;
	}

}
