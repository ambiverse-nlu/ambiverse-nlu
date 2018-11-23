/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * This class couples each token to it's class probabilities
 * (PERS, ORG, LOC, MISC)
 *
 */
public class ClassProbabilityDistribution {
	
	private String token;
	private double pers;
	private double org;
	private double loc;
	private double misc;
	
	
	public ClassProbabilityDistribution(String token, double pers, double org, double loc, double misc) {
		super();
		this.token = token;
		this.pers = pers;
		this.org = org;
		this.loc = loc;
		this.misc = misc;
	}


	public String getToken() {
		return token;
	}


	public double getPers() {
		return pers;
	}


	public double getOrg() {
		return org;
	}


	public double getLoc() {
		return loc;
	}


	public double getMisc() {
		return misc;
	}
	
	

}
