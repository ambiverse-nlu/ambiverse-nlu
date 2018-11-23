/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure;

import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * Class that represents a pair of BMEOW (also BILOU) and
 * named entity type tag, e.g. begin person (B-PERS), 
 * middle location (M-LOC), etc.
 */
public class BmeowTypePair {

	private BmeowTag bmeowTag;
	private NerType.Label nerType;
	
	/**
	 * @param bmeowTag - The bmeow Tag
	 * @param nerType - The NER class
	 */
	public BmeowTypePair(BmeowTag bmeowTag, NerType.Label nerType) {
		super();
		this.bmeowTag = bmeowTag;
		this.nerType = nerType;
	}
	
	/**
	 * @return the nerType
	 */
	public NerType.Label getNerType() {
		return nerType;
	}
	/**
	 * @return the bmeowTag
	 */
	public BmeowTag getBmeowTag() {
		return bmeowTag;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		} else if (this == obj){
			return true;
		} else if (obj.getClass().equals(this.getClass())){
			BmeowTypePair other = (BmeowTypePair) obj;
			return new EqualsBuilder()
					.append(this.nerType, other.nerType)
					.append(this.bmeowTag, other.bmeowTag)
					.isEquals();
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int hash = this.nerType.hashCode() + this.bmeowTag.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return "BmeowTypePair [nerType=" + nerType + ", bmeowTag=" + bmeowTag + "]";
	}
	
	
	public String getFeatureString(){
		return this.bmeowTag + "_" + this.nerType;
	}
	
	
	public static Set<BmeowTypePair> getAllPossiblePairCombinations(){
		Set<BmeowTypePair> pairs = new HashSet<>();
		
		for(BmeowTag tag : BmeowTag.values()){
			for(NerType.Label type : NerType.Label.values()){
				pairs.add(new BmeowTypePair(tag, type));
			}
		}
		
		return pairs;
	}
}
