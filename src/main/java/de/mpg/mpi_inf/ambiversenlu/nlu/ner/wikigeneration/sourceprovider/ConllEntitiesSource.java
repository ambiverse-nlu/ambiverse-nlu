package de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration.sourceprovider;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ConllEntitiesSource extends Source {
	private static final String CONLL_ENTITIES_SOURCE_PATH = "src/test/resources/collections/en/conll/docs/corpus.tsv";

	private final List<KBIdentifiedEntity> originalNamedEntities;
	private int articlesNumber;
	private int start = 0;

	public ConllEntitiesSource(int articlesNumber) throws IOException {
		super(articlesNumber);
		originalNamedEntities = new ArrayList<>(extractEntitiesFromAidaDataset(CONLL_ENTITIES_SOURCE_PATH, 0, 1392));
	}

	@Override
	protected LinkedHashMap<Integer, String> getNamedEntitiesMap(int begin, int amount) throws EntityLinkingDataAccessException {
		if (begin > originalNamedEntities.size() - 1) {
			return new LinkedHashMap<>();
		}
		List<KBIdentifiedEntity> kbIdentifiedEntities = originalNamedEntities.subList(begin, Math.min(begin + amount, originalNamedEntities.size()));
		TObjectIntHashMap<KBIdentifiedEntity> articleEntityIDRepresentationMap =
				DataAccess.getInternalIdsForKBEntities(kbIdentifiedEntities);
		return kbIdentifiedEntities.stream()
				.collect(Collectors.toMap(articleEntityIDRepresentationMap::get, KBIdentifiedEntity::getIdentifier,
						(u, v) -> {
							throw new IllegalStateException(String.format("Duplicate key %s", u));
						},
						LinkedHashMap::new));
	}

	@Override
	public SourceProvider.Type getType() {
		return SourceProvider.Type.CONLL_ENTITIES;
	}

	private static Set<KBIdentifiedEntity> extractEntitiesFromAidaDataset(String datasetPath, int begin, int end) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(datasetPath));
		int counter = -1;
		Set<KBIdentifiedEntity> entities = new HashSet<>();
		for (String s : lines) {
			if (s.contains("-DOCSTART-")) {
				counter++;
			}
			if (begin <= counter && counter <= end) {
				String[] split = s.split("\t");
				if (split.length == 5) {
					entities.add(KBIdentifiedEntity.getKBIdentifiedEntity(split[3].replace("'", "''"), "YAGO3"));
				}
			}
		}
		return entities;
	}

}
