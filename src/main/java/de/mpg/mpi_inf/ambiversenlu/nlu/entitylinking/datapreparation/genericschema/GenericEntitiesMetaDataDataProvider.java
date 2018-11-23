package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiesmetadata.EntitiesMetaDataEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiesmetadata.EntityMetaData;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class GenericEntitiesMetaDataDataProvider extends EntitiesMetaDataEntriesDataProvider {

	Logger logger = LoggerFactory.getLogger(GenericEntitiesMetaDataDataProvider.class);

	private GenericReader genericReader;

	private String knowledgebasename;

	public GenericEntitiesMetaDataDataProvider(GenericReader reader, String knowledgebasename) {
		this.genericReader = reader;
		this.knowledgebasename = knowledgebasename;
	}

	private Set<EntityMetaData> run() throws IOException, EntityLinkingDataAccessException {
		Iterator<Fact> imageURLs = genericReader.iterator(Relations.IMGURL.getRelation());
		Map<String, String> imageURL = new HashMap<>();
		while (imageURLs.hasNext()) {
			Fact fact = imageURLs.next();
			imageURL.put(fact.getSubject(), fact.getObject());
		}

		Iterator<Fact> urlsFacts = genericReader.iterator(Relations.URL.getRelation());
		Map<String, String> urls = new HashMap<>();
		while (urlsFacts.hasNext()) {
			Fact fact = urlsFacts.next();
			urls.put(fact.getSubject(), fact.getObject());
		}

		Iterator<Fact> titleFacts = genericReader.iterator(Relations.TITLE.getRelation());
		Map<String, String> titles = new HashMap<>();
		while (titleFacts.hasNext()) {
			Fact fact = titleFacts.next();
			titles.put(fact.getSubject(), fact.getObject());
		}

		Set<EntityMetaData> entitiesMetaData = new HashSet<>();

		Entities entities = DataAccess.getAllEntities();
		for (Entity e : entities) {
			String entityWithLC = e.getIdentifierInKb();
			String preferredName = titles.get(entityWithLC) != null ? titles.get(entityWithLC) : "";
			String url = urls.get(entityWithLC) != null ? urls.get(entityWithLC) : "";
			String depictionUrl = imageURL.get(entityWithLC) != null ? imageURL.get(entityWithLC) : "";
			String licenceUrl = "";
			String wikiDataID = e.getIdentifierInKb();
			String description = "";
			entitiesMetaData.add(new EntityMetaData(entityWithLC, preferredName, url, depictionUrl, licenceUrl,
					description, wikiDataID));

		}
		return entitiesMetaData;
	}

	/** returns the preferred name */

	@Override
	public Iterator<EntityMetaData> iterator() {
		try {
			return run().iterator();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getKnowledgebaseName() {
		return knowledgebasename;
	}

}
