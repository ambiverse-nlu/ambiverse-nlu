/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure;

import java.util.Set;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 */
public interface MentionTokenCounts {

	public Set<String> getKeys();
	public int getCount(String s);
	
}
