/**
 *
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.consts;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.YAGO;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *         <p>
 *         Classes with AIDA type ids for faster lookup of persons,
 *         organizations and locations
 */
public class TypeIds {

	private static final Logger logger = LoggerFactory.getLogger(TypeIds.class);

	public static final TIntSet PERSON_IDS;

	public static final TIntSet ORGANIZATION_IDS;

	public static final TIntSet LOCATION_IDS;

	public static final int PERSON_ID;

	public static final int ORGANIZATION_ID;

	public static final int LOCATION_ID;

	static {
		int einsteinEntityId = 2267938;
		int barackObamaEntityId = 59659;

		int germanyEntityId = 5774;
		int moscowEntityId = 64773;

		int googleEntityId = 30132;
		int unEntityId = 36422;
		try {
			int[] typeIdsForPersonId = DataAccess.getTypeIdsForEntityId(einsteinEntityId);
			PERSON_IDS = new TIntHashSet(typeIdsForPersonId);
			PERSON_IDS.addAll(DataAccess.getTypeIdsForEntityId(barackObamaEntityId));

			int[] typeIdsForLocationId = DataAccess.getTypeIdsForEntityId(germanyEntityId);
			LOCATION_IDS = new TIntHashSet(typeIdsForLocationId);
			LOCATION_IDS.addAll(DataAccess.getTypeIdsForEntityId(moscowEntityId));

			int[] typeIdsForOrganizationId = DataAccess.getTypeIdsForEntityId(googleEntityId);
			ORGANIZATION_IDS = new TIntHashSet(typeIdsForOrganizationId);
			ORGANIZATION_IDS.addAll(DataAccess.getTypeIdsForEntityId(unEntityId));

			PERSON_ID = DataAccess.getIdForTypeName(YAGO.person);
			ORGANIZATION_ID = DataAccess.getIdForTypeName(YAGO.organization);
			LOCATION_ID = DataAccess.getIdForTypeName(YAGO.location);
		} catch (EntityLinkingDataAccessException e) {
			logger.error("Could not retrieve type ids for Einstein entity " + einsteinEntityId);
			throw new RuntimeException(e);
		}
	}

}
