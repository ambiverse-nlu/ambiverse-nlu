package de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration.sourceprovider;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Source {
	private static final Logger logger = LoggerFactory.getLogger(Source.class);
	protected final Integer totalNumber;
	protected int start = 0;
	protected final Set<Integer> missingEntities;
	protected boolean init = true;
	protected boolean exhausted = false;
	private AtomicInteger processed = new AtomicInteger(0);

	public Source(Integer totalNumber) {
		this.totalNumber = totalNumber;
		missingEntities = new ConcurrentSkipListSet<>();
	}

	public int getTotalNumber() {
		return totalNumber;
	}

	public void addMissingEntity(int entityId) {
		missingEntities.add(entityId);
	}

	public boolean notEnoughEntities() {
		return (init || !missingEntities.isEmpty()) && !exhausted;
	}

//	accessed only from main thread, not threadsafe
	public LinkedHashMap<Integer, String> retrieveOriginalNamedEntityIDs() throws EntityLinkingDataAccessException {
		int missingNumber;
		if (init) {
			missingNumber = totalNumber;
			init = false;
		} else {
			missingNumber = missingEntities.size();
		}
		if (missingNumber == 0) {
			throw new IllegalStateException("There are no missing entities");
		}
		logger.info("retrieveOriginalNamedEntityIDs: start=" + start + ", missing=" + missingNumber);
		LinkedHashMap<Integer, String> entities =  getNamedEntitiesMap(start, missingNumber);
		logger.info("found entities: " + entities.size());
		if (entities.size() > missingNumber) {
			throw new EntityLinkingDataAccessException("Retrieved more entities (" + entities.size() + ") than required (" + missingNumber + ")!");
		} else if (entities.size() < missingNumber) {
			if (entities.size() == 0) {
				exhausted = true;
			}
			logger.warn("The source contains fewer entities than requested: " + entities.size() + " < " + missingNumber);
		}
		start += missingNumber;
		missingEntities.clear();
		return entities;
	}

	abstract LinkedHashMap<Integer, String> getNamedEntitiesMap(int begin, int amount) throws EntityLinkingDataAccessException;

	public abstract SourceProvider.Type getType();

	public int processedDocumentsCounterIncrement() {
		return processed.incrementAndGet();
	}

	public int getProcessedDocumentsNumber() {
		return processed.get();
	}
}
