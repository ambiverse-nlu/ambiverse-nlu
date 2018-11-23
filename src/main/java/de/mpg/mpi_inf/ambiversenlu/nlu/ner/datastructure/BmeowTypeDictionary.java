package de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.util.*;


/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 * 
 * Helper class that maps a String + Type pair to BMEOW dictionaries
 *
 *	BMEOW Type encoding:
 *	- Beginning
 *  - Middle
 *  - End
 *  - Outside
 *  - single Word
 *
 */
public class BmeowTypeDictionary {
	
	private Map<String, Map<BmeowTag, Set<NerType.Label>>> tokenToBmeowType;
	
	public BmeowTypeDictionary(){
		tokenToBmeowType = new HashMap<>();
	}
	
	public Set<BmeowTypePair> getBmeowTypes(String token, String language){
		String conflatedToken = EntityLinkingManager.conflateToken(token, Language.getLanguageForString(language), true);
		
		if(!tokenToBmeowType.containsKey(conflatedToken)){
			return new HashSet<>();
		}
		
		HashSet<BmeowTypePair> typePairs = new HashSet<>();
		
		Map<BmeowTag, Set<NerType.Label>> map = tokenToBmeowType.get(conflatedToken);
		Set<BmeowTag> bmeowTags = map.keySet();
		for(BmeowTag tag : bmeowTags){
			for(NerType.Label type : map.get(tag)){
				typePairs.add(new BmeowTypePair(tag, type));
			}
		}
		
		return typePairs;
	}
	
	public void put(String token, NerType.Label type){
		String[] split = token.split(" ");
		if(split.length == 0){
			return;
		} else if(split.length == 1){
			this.put(split[0], BmeowTag.WORD, type);
		} else {
			for (int i = 0; i < split.length; i++) {
				if(i == 0){
					this.put(split[i], BmeowTag.BEGIN, type);
				} else if (i < split.length -1){
					this.put(split[i], BmeowTag.MIDDLE, type);
				} else {
					this.put(split[i], BmeowTag.END, type);
				}
			}
		}
	}
	
	private void put(String token, BmeowTag tag, NerType.Label type){
		Map<BmeowTag, Set<NerType.Label>> map = tokenToBmeowType.get(token);
		if(map == null){
			map = new HashMap<>();
		}
		
		Set<NerType.Label> types = map.get(tag);
		if(types == null){
			types = new HashSet<>(Arrays.asList(type));
		} else {
			types.add(type);
		}
		map.put(tag, types);
		tokenToBmeowType.put(token, map);
	}
}
