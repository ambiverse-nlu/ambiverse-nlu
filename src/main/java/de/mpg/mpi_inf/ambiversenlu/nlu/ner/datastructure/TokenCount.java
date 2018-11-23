/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure;

import java.util.Comparator;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 * 
 * Simple class that maps a token to its count.
 */
public class TokenCount {
	
	String token;
	int count;

	public TokenCount(String token, int count) {
		super();
		this.token = token;
		this.count = count;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
	public String toString() {
		return "TokenCount [token=" + token + ", count=" + count + "]";
	}
	
	public static class Comparators{
		
		public static Comparator<TokenCount> COUNT = new Comparator<TokenCount>() {
			
			@Override
			public int compare(TokenCount o1, TokenCount o2) {
				return Integer.compare(o1.getCount(), o2.getCount());
			}
		};
	}

}
