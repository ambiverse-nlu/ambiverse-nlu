package de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration.sourceprovider;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

public class EntityRankSource extends BufferedSource {
	public static final Logger logger = LoggerFactory.getLogger(EntityRankSource.class);

	public EntityRankSource(int totalNumber, String language) {
		super(totalNumber, language);
	}

	protected LinkedHashMap<Integer, String> getNamedEntitiesMap(int begin, int amount) throws EntityLinkingDataAccessException {
		return DataAccess.getTopEntitiesByRank(begin, amount, null);
	}

	@Override
	public SourceProvider.Type getType() {
		return SourceProvider.Type.ENTITY_RANK;
	}
}
