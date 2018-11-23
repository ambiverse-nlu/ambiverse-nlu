package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers;

/**
 *     //    defines positions for the annotations
 //    0    1     2   3        4       5      6
 //    WORD LEMMA POS POSITION MENTION ENTITY TYPE

  */
public enum OrderType {
	WORD_LEMMA_POS_POSITION_MENTION_ENTITY_TYPE(0,1,2,3,4,5,6),
	WORD_POS_POSITION_MENTION_ENTITY_TYPE(0,null,1,2,3,4,5),
	WORD_POSITION_MENTION_ENTITY_TYPE(0,null,null,1,2,3,4),
	WORD_POSITION_TYPE(0,null,null,1,null,null,2),
	WORD_POS_POSITION_TYPE(0,null,1,2,null,null,3),
	WORD_LEMMA_POS_POSITION_TYPE(0,1,2,3,null,null,4),
	DEFAULT(0,null,null,1,2,3,4);
	private final Integer[] indices;
	private final long nonEntityLength;

	OrderType(Integer... indices) {

		this.indices = indices;
		nonEntityLength = (indices[0] == null? 0 : 1) +
				(indices[1] == null? 0 : 1) +
				(indices[2] == null? 0 : 1);
	}

	public String getWord(String[] word) {
		return word[indices[0]];
	}

	public boolean hasLemma() {
		return indices[1] != null;
	}

	public String getLemma(String[] word) {
		return word[indices[1]];
	}

	public boolean hasPOS() {
		return indices[2] != null;
	}

	public String getPOS(String[] word) {
		return word[indices[2]];
	}

	public boolean hasPosition() {
		return indices[3] != null;
	}

	public String getPosition(String[] word) {
		return word[indices[3]];
	}

	public boolean hasMention() {
		return indices[4] != null;
	}

	public String getMention(String[] word) {
		return word[indices[4]];
	}

	public boolean hasEntity() {
		return indices[5] != null;
	}

	public String getEntity(String[] word) {
		return word[indices[5]];
	}

	public boolean hasType() {
		return indices[6] != null;
	}

	public String getType(String[] word) {
		return word[indices[6]];
	}

	public boolean isEntity(String[] word) {
		return word.length > nonEntityLength;
	}
}