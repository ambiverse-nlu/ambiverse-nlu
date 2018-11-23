package de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration.sourceprovider;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;

/** buffers the entities
  */
public abstract class BufferedSource extends Source {

	private static final Logger logger = LoggerFactory.getLogger(BufferedSource.class);
	private static final int REQUEST_MINIMUM = 500;
	private final LinkedHashMap<Integer, String> buffer = new LinkedHashMap<>();
	private final String language;
	private final Integer totalNumber;


	public BufferedSource(Integer totalNumber, String language) {
		super(totalNumber);
		this.totalNumber = totalNumber;
		this.language = language;
	}

	@Override
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

		if (buffer.isEmpty() || buffer.size() < missingNumber) {
			buffer.putAll(getNamedEntitiesMap(start, Math.max(REQUEST_MINIMUM, missingNumber)));
		}
		logger.info("buffer contains entities: " + buffer.size());

		LinkedHashMap<Integer, String> entities;
		if (missingNumber >= REQUEST_MINIMUM) {
			entities = new LinkedHashMap<>(buffer);
			buffer.clear();

		} else {
			entities = new LinkedHashMap<>();
			Iterator<Integer> bufferIterator = buffer.keySet().iterator();
			int count = 0;
			while(bufferIterator.hasNext() && count++ < missingNumber) {
				Integer entityID = bufferIterator.next();
				entities.put(entityID, buffer.get(entityID));
				bufferIterator.remove();
			}
		}
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
}
