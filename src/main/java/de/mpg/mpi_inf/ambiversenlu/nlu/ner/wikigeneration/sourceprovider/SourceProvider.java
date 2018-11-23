package de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration.sourceprovider;

import java.io.IOException;

public class SourceProvider {
	public static Source getSource(String typeStr, Integer articlesNumber, String language) throws IOException {
		Type type = Type.valueOf(typeStr);
		switch (type) {
			case CONLL_ENTITIES:
				return new ConllEntitiesSource(articlesNumber);
			case ENTITY_RANK:
				return new EntityRankSource(articlesNumber, language);
			case MANUAL:
				return new ManualSource(language);
			default:
				throw new UnsupportedOperationException("type " + type + " is not supported!");
		}
	}

	public enum Type {CONLL_ENTITIES, ENTITY_RANK, MANUAL}
}
