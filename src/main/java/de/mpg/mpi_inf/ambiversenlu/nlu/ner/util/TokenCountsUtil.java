/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.MentionTokenCounts;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.MentionTokenFrequencyCounts;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.TokenCount;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * Helper class to construct top-k and top-percentile lists
 * from a token counts list.
 * 
 * See main() method for usage examples.
 */
public class TokenCountsUtil {
	
	private List<TokenCount> tokenCounts;
	
	private Logger logger;
	
	public TokenCountsUtil(MentionTokenCounts counts) {
		tokenCounts = new ArrayList<>();
		
		logger = Logger.getLogger(getClass());
		
		logger.debug("Building sorted count list.");
		for(String token : counts.getKeys()){
			tokenCounts.add(new TokenCount(token, counts.getCount(token)));
		}
		tokenCounts.sort(TokenCount.Comparators.COUNT);
		Collections.reverse(tokenCounts);
	}
	
	/**
	 * @param k number of mentions to retrieve from the top list
	 * @return  all mention tokens from the highest rank to k
	 */
	public Set<String> getTopK(int k){
		if(!evaluateK(k)){
			return null;
		}
		return tokenCounts.subList(0, k-1).stream()
				.map(TokenCount::getToken).collect(Collectors.toSet());
	}
	
	/**
	 * @param percentile 0.2 for top 20%
	 * @return  all mention tokens that fall into the upper percentile
	 */
	public Set<String> getTopPercentile(double percentile){
		if(!evaluatePercentile(percentile)){
			return null;
		}
		int numberOfTokensToRetrieve = (int) Math.floor(percentile * tokenCounts.size());
		return getTopK(numberOfTokensToRetrieve);
	}
	
	/**
	 * @param k number of mentions to retrieve from the top list
	 * @return A map that maps each token to its assigned weight
	 */
	public Map<String, Double> getMentionWeightsForTopK(int k){
		if(!evaluateK(k)){
			return null;
		}
		
		List<TokenCount> topk = new ArrayList<>(tokenCounts.subList(0, k-1));
		
		int sumCounts = topk.stream().mapToInt(TokenCount::getCount).sum();
		
		Map<String, Double> mentionWeights = new HashMap<>();
		
		topk.stream().forEach(tc -> mentionWeights
				.put(tc.getToken(), tc.getCount()/(double)sumCounts));
		
		return mentionWeights;
	}
	
	/**
	 * @param percentile 0.2 for top 20%
	 * @return A map that maps each token to its assigned weight
	 */
	public Map<String, Double> getMentionWeightsTopPercentile(double percentile){
		if(!evaluatePercentile(percentile)){
			return null;
		}
		int numberOfTokensToRetrieve = (int) Math.floor(percentile * tokenCounts.size());
		return getMentionWeightsForTopK(numberOfTokensToRetrieve);
	}
	
	/**
	 * Returns all frequent mention tokens with their associated weights
	 * @return A map that maps each token to its assigned weight
	 */
	public Map<String, Double> getMentionWeights(){
		return getMentionWeightsForTopK(tokenCounts.size());
	}
	
	public List<TokenCount> getTokenCountList(){
		return tokenCounts;
	}
	
	/**
	 * check whether k is within the legal limits
	 * @return true if k is within limits, otherwise false
	 */
	private boolean evaluateK(int k){
		if(k > tokenCounts.size()){
			logger.error("K is larger than the number of token mentions. "
					+ "k: " + k + ", # token mentions: " + tokenCounts.size());
			return false;
		} else if(k == 0) {
			logger.error("K equals 0");
			return false;
		} else if(k < 0){
			logger.error("K is smaller 0, k: " + k);
			return false;
		}
		return true;
	}
	
	/**
	 * check whether the passed percentile is within the legal limits
	 * @return true if percentage is wihtin limits, otherwise false
	 */
	private boolean evaluatePercentile(double percentile){
		if(percentile > 1.0){
			logger.error("Percentile is larger than 1. "
					+ "Cannot retrieve more than 100% of the items. percentile: " + percentile);
			return false;
		} else if(percentile == 0.0){
			logger.error("Percentile is zero");
			return false;
		} else if(percentile < 0.0){
			logger.error("Percentage is negative.");
			return false;
		}
		
		return true;
	}
}
