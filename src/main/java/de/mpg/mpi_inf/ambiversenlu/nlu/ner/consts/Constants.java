package de.mpg.mpi_inf.ambiversenlu.nlu.ner.consts;

import edu.stanford.nlp.process.WordShapeClassifier;

import java.time.format.DateTimeFormatter;

/**
 *
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * A class with constants.
 *
 * REQUIREMENTS: Before running anything you need to set the path of
 * DKPRO_HOME_LOCAL or DKPRO_HOME to the home directory of
 * DkPro core.
 *
 */
public class Constants {

	/**
	 * id of word shape classifier from Stanford CoreNLP package
	 * see: http://nlp.stanford.edu/nlp/javadoc/javanlp-3.5.0/index.html?edu/stanford/nlp/process/WordShapeClassifier.html
	 */
	public static final int WORD_SHAPE_CLASSIFIER = WordShapeClassifier.WORDSHAPECHRIS4;


	public static final String MENTION_LINKLIKELIHOOD_CLEAN_PATTERN = "[^\\p{L}\\p{N} ]";

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSS");


	public static final String FEATURE_NULL = "_NULL";


	public static final String YAGO_KB_IDENTIFIER = "YAGO3";
}
