/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure;


import de.mpg.mpi_inf.ambiversenlu.nlu.ner.consts.TypeIds;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * This is a class that holds the NER type enum and a helper method
 * that maps Aida typeIds to the NER types PERSON, LOCATION, ORGANIZATION
 * and MISC.
 */
public class NerType {
	
	public enum Label {
		PERSON, LOCATION, ORGANIZATION, MISC
	}
	
	
	public static Set<Label> getNerTypesForTypeIds(int[] typeIds) {
		return Collections.singleton(getNerTypeForTypeIds(typeIds));
	}


	public static Label getNerTypeForTypeIds(int[] typeIds) {
		Set<Integer> typeIDSet = Arrays.stream(typeIds).mapToObj(i->i).collect(Collectors.toSet());
		if (typeIDSet.contains(TypeIds.PERSON_ID)) {
			return Label.PERSON;
		}
		if (typeIDSet.contains(TypeIds.ORGANIZATION_ID)) {
			return Label.ORGANIZATION;
		}
		if (typeIDSet.contains(TypeIds.LOCATION_ID)) {
			return Label.LOCATION;
		}
		return Label.MISC;
	}

}
