package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.YagoIdsMapper;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.Yago3Util;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class GenericDictionaryEntriesDataProvider extends DictionaryEntriesDataProvider {

	private static final Logger logger = LoggerFactory.getLogger(GenericDictionaryEntriesDataProvider.class);

	private boolean mapYagoIdsToOtherKBIds = false;

	private YagoIdsMapper yagoIdsMapper;

	private String knowledgebaseName;

	private GenericReader genericReader;

	public GenericDictionaryEntriesDataProvider(GenericReader genericReader, YagoIdsMapper yagoIdsMapper,
			String knowledgebaseName) {
		this(genericReader, knowledgebaseName);
		this.yagoIdsMapper = yagoIdsMapper;
		mapYagoIdsToOtherKBIds = true;
	}

	public GenericDictionaryEntriesDataProvider(GenericReader genericReader, String knowledgebaseName) {
		this.genericReader = genericReader;
		this.knowledgebaseName = knowledgebaseName;
	}

	private Map<String, List<DictionaryEntity>> getDictionaryEntries() throws IOException {

		Map<String, List<DictionaryEntity>> dictionaryEntries = new HashMap<>();

		List<Fact> isNamedEntityAll = genericReader.getFacts(Relations.IS_NAMEDENTITY.getRelation());
		Map<String,String> isNamedEntity = new HashMap<>();
		for(Fact fact: isNamedEntityAll) {
			isNamedEntity.put(fact.getSubject(), fact.getObject());
		}

		int count = 0;

		Relations r = Relations.LABEL;
		List<Fact> relationPairsSet = genericReader.getFacts(r.getRelation());
		for (Fact entry : relationPairsSet) {
			String target = entry.getSubject();
			if (!Yago3Util.isNamedEntity(target)) {
				continue;
			}

			String valueIsNE = isNamedEntity.get(target);
			if(valueIsNE == null) {
				valueIsNE = EntityType.UNKNOWN.name();
			}
			EntityType isNE = EntityType.find(valueIsNE);

			if (mapYagoIdsToOtherKBIds) {
				target = yagoIdsMapper.mapFromYagoId(target);
				if (target == null)
					continue;
			}

			String mentionLanguage = FactComponent.getLanguageOfString(entry.getObject());

			Language language;
			if (mentionLanguage == null || "".equals(mentionLanguage)) {
				language = Language.getLanguageForString(mentionLanguage, "en");
			} else if (Language.isActiveLanguage(mentionLanguage)) {
				language = Language.getLanguageForString(mentionLanguage);
			} else {
				continue;
			}
			String mention = entry.getObjectAsJavaString();
			// a hack to fix some bug in YAGO extraction, should be removed
			// later
			mention = mention.replace("_", " ");

			List<DictionaryEntity> candidates = dictionaryEntries.get(mention);
			if (candidates == null) {
				candidates = new LinkedList<>();
				dictionaryEntries.put(mention, candidates);
			}

			String source = r.toString();

			DictionaryEntity dictionaryEntity = DictionaryEntity.getDictionaryEntity(target, knowledgebaseName, source, language, isNE);

			candidates.add(dictionaryEntity);

			if (++count % 1000000 == 0) {
				logger.info(count + " means read");
			}
		}
		return dictionaryEntries;
	}

	@Override
	public Iterator<Entry<String, List<DictionaryEntity>>> iterator() {
		try {
			return getDictionaryEntries().entrySet().iterator();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
