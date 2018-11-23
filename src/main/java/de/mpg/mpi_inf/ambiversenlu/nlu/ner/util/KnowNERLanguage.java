package de.mpg.mpi_inf.ambiversenlu.nlu.ner.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.LanguageNotSupportedException;

import java.util.*;
import java.util.stream.Collectors;

public class KnowNERLanguage {

	private enum LANGUAGE {
		en(false),
		de(true),
		es(true),
		cs(false),
		ru(true),
		zh(false);

		private final boolean lemmatize;

		LANGUAGE(boolean lemmatize) {
			this.lemmatize = lemmatize;
		}
	}

	private static final Map<LANGUAGE, KnowNERLanguage> activeLanguages = Collections.unmodifiableMap(Arrays
			.stream(KnowNERSettings.getLanguages().split(","))
			.map(l-> new KnowNERLanguage(LANGUAGE.valueOf(l)))
			.filter(l -> Language.isActiveLanguage(l.name()))
			.collect(Collectors.toMap(l-> l.language, l-> l, (u, v) -> {
				throw new IllegalStateException(String.format("Duplicate key %s", u));
			}, LinkedHashMap::new)));

	private final LANGUAGE language;

	private KnowNERLanguage(LANGUAGE language) {
		this.language = language;
	}

	public String name() {
		return language.name();
	}

	@Override
	public String toString() {
		return "KnowNERLanguage{" +
				"language=" + language +
				'}';
	}

	public static KnowNERLanguage valueOf(String language) {
		return activeLanguages.get(LANGUAGE.valueOf(language));
	}

	public static Set<Language> supportedLanguages() {
		return activeLanguages.keySet()
						.stream().map(l -> Language.getLanguageForString(l.name()))
						.collect(Collectors.toSet());
	}

	public static Collection<KnowNERLanguage> activeLanguages() {
		return activeLanguages.values();
	}

	public static boolean supports(Language language) {
		return activeLanguages.containsKey(LANGUAGE.valueOf(language.name()));
	}

	public static boolean requiresLemma(Language language) {
		if (!supports(language)) {
			throw new LanguageNotSupportedException("KnowNER does not supported language " + language.name());
		}
		return LANGUAGE.valueOf(language.name()).lemmatize;
	}

	public static boolean requiresLemma(String documentLanguage) {
		return requiresLemma(Language.getLanguageForString(documentLanguage));
	}
}
