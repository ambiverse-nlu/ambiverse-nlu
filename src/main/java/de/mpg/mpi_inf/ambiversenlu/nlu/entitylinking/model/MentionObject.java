package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure.NerType;

public class MentionObject {
	private String mention;
	private Double prior;
	private NerType.Label label;
	private double milneSimilarity;

	public MentionObject(String mention, Double prior) {
		this.mention = mention;
		this.prior = prior;
	}


	public String getMention() {
		return mention;
	}

	public void setMention(String mention) {
		this.mention = mention;
	}

	public Double getPrior() {
		return prior;
	}

	public void setPrior(Double prior) {
		this.prior = prior;
	}

	public NerType.Label getLabel() {
		return label;
	}

	public void setLabel(NerType.Label label) {
		this.label = label;
	}

	public MentionObject copy() {
		return new MentionObject(mention, prior);
	}

	public double getMilneSimilarity() {
		return milneSimilarity;
	}

	public void setMilneSimilarity(double milneSimilarity) {
		this.milneSimilarity = milneSimilarity;
	}
}
