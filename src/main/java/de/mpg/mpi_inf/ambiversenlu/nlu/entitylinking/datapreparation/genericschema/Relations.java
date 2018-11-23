package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Relations {

	LABEL		("rdfs:label"),
	COOCURS		("<cooccursWith>"),
	TYPE		("rdf:type", true),
	DESCRIPTION	("<hasDescription>", true),
	CONTEXT		("<occursInContext>", true),
	CONTAINS	("<containsEntity>"),
	PROBABILITY	("<hasEntityProbability>"),
	RANK		("<hasRank>"),
	IMGURL		("<hasImageUrl>", true),
	URL			("<hasUrl>", true),
	TITLE		("<hasTitle>", true),
	IS_A		("<isA>"),
	IS_NAMEDENTITY("<isNamedEntity>", true),
	LINK_TO("<hasLinkTo>", true),
	HAS_KEYPHRASE("<hasKeyphrase>", true),
	SAME_AS("owl:sameAs", true);

	public static Map<String, Relations> names2Relations = new HashMap<>();

	static {
		for(Relations rel: Relations.values()) {
			names2Relations.put(rel.getRelation(), rel);
		}
	}


	String relation;
	boolean optional = false;

	Relations(String relation) {
		this.relation = relation;
	}

	Relations(String relation, boolean optional) {
		this(relation);
		this.optional = optional;
	}

	public String getRelation() {
		return relation;
	}


	
	public static String[] getRelations() {
	    return Arrays.stream(Relations.class.getEnumConstants()).map(t -> t.getRelation()).toArray(String[]::new);
	}
}
