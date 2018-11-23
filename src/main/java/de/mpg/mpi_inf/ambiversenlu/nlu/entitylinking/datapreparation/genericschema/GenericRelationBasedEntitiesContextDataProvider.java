package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntitiesContextEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiescontext.EntityContextEntry;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.YagoIdsMapper;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.Utils;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;

public class GenericRelationBasedEntitiesContextDataProvider extends EntitiesContextEntriesDataProvider {

	private static final Logger logger = LoggerFactory.getLogger(GenericRelationBasedEntitiesContextDataProvider.class);

	private String relationName;

	private String contextSourceName;

	private boolean mapYagoIdsToOtherKBIds = false;

	private YagoIdsMapper yagoIdsMapper;

	private String knowledgebaseName;

	private GenericReader genericReader;

	public GenericRelationBasedEntitiesContextDataProvider(GenericReader reader, String relationName,
			String contextSourceName, YagoIdsMapper yagoIdsMapper, String knowledgebaseName) throws IOException {
		this(reader, relationName, contextSourceName, knowledgebaseName);
		this.knowledgebaseName = knowledgebaseName;
		mapYagoIdsToOtherKBIds = true;
	}

	public GenericRelationBasedEntitiesContextDataProvider(GenericReader reader, String relationName,
			String contextSourceName, String knowledgebaseName) throws IOException {
		this.genericReader = reader;
		this.relationName = relationName;
		this.contextSourceName = contextSourceName;
		this.knowledgebaseName = knowledgebaseName;
	}

	private Map<String, Set<EntityContextEntry>> getEntitiesContexts() throws IOException {
		Map<String, Set<EntityContextEntry>> entitiesContext = new HashMap<>();

		List<Fact> facts = genericReader.getFacts(relationName);

		if (facts == null) {
			logger.error("No facts found for relation: " + relationName);
			throw new IllegalStateException("No facts found for relation: " + relationName);
		} else {
			logger.info("Extracting Entities context from relation: " + relationName);
		}

		for (Fact fact : facts) {
			String entity = fact.getSubject();

			if (mapYagoIdsToOtherKBIds) {
				entity = yagoIdsMapper.mapFromYagoId(entity);
				if (entity == null)
					continue;
			}
			String keyphrase;

			keyphrase = fact.getObjectAsJavaString();

			Set<EntityContextEntry> entityContext = entitiesContext.get(entity);
			if (entityContext == null) {
				entityContext = new HashSet<>();
				entitiesContext.put(entity, entityContext);
			}
			Language language = Language.getLanguageForString(FactComponent.getLanguageOfString(fact.getObject()), "en");
			Matcher m = Utils.cleaner.matcher(keyphrase);
			keyphrase = m.replaceAll(" ");


			EntityContextEntry entityContextEntry = new EntityContextEntry(keyphrase, contextSourceName, language);
			entityContext.add(entityContextEntry);

		}
		logger.info("Done");
		return entitiesContext;
	}


	@Override
	public Iterator<Entry<String, Set<EntityContextEntry>>> iterator() {
		try {
			return getEntitiesContexts().entrySet().iterator();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getKnowledgebaseName() {
		return knowledgebaseName;
	}
}
