package de.mpg.mpi_inf.ambiversenlu.nlu.language;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;

import java.io.Serializable;
import java.util.*;

/**
 * <p>
 * The idea of this class is to encapsulate the languages so that different runs support different languages.
 * No language which is not supported can be instantiated.
 * The issue here is that the rest of the code relies in ENUMS for languages so we are mimiking a dynamic initialization of enums.
 * As getLanguageForString() is static and cannot be overriden, we need a way to forbid instantiations of non-supported languages.
 */
public class Language implements Serializable {

	private enum LANGUAGE {
		en(0, "eng"),
		de(1, "deu"),
		es(2, "spa"),
		zh(3, "zho", "zh-cn"),
		cs(4, "ces"),
		ru(5, "rus"),
		fr(6, "fra", "fre"),
		it(7, "ita"),
		pt(8, "por"),
		ar(9, "ara");
		private final int id;
		private final String[] abbreviations;

		LANGUAGE(int id, String... abbreviations) {
			this.id = id;
			this.abbreviations = abbreviations;
		}
	}

	private static final Map<LANGUAGE, Language> mapLanguages = new HashMap<>();

	private final LANGUAGE language;
	private final String pathStopWords;
	private final String pathSymbols;
	private final String dummySentence;

	private static final Map<String, LANGUAGE> stringToAidaLanguage;
	private static final Map<String, String> stringTo2Letter;
	private static final Map<String, String> stringTo3Letter;

	static {
		stringToAidaLanguage = new HashMap<>();
		stringTo2Letter = new HashMap<>();
		stringTo3Letter = new HashMap<>();

		for (LANGUAGE l : LANGUAGE.values()) {
			for (String abbreviation : l.abbreviations) {
				stringTo2Letter.put(abbreviation, l.name());
				stringTo3Letter.put(abbreviation, l.abbreviations[0]);
				stringToAidaLanguage.put(abbreviation, l);
			}
			stringToAidaLanguage.put(l.name(), l);
		}

		init();
	}


	private Language(LANGUAGE language, String pathStopWords, String pathSymbols, String dummySentence) throws AidaUnsupportedLanguageException {
		this.language = language;
		this.pathStopWords = pathStopWords;
		this.pathSymbols = pathSymbols;
		this.dummySentence = dummySentence;
	}

	private static void init() throws AidaUnsupportedLanguageException {
		Set<String> dblanguages = EntityLinkingConfig.getLanguages();
		for (String l : dblanguages) {
			Language language = new Language(LANGUAGE.valueOf(l),
					EntityLinkingConfig.get(l + ".stopwords"),
					EntityLinkingConfig.get(l + ".symbols"),
					EntityLinkingConfig.get(l + ".dummysentence"));
			mapLanguages.put(language.language, language);
		}
	}

	public String getDummySentence() {
		return dummySentence;
	}

	public int getID() {
		return language.id;
	}

	/**
	 * returns Language object for the given language. Supports two- and three- letter language code
	 * @param language
	 * @return
	 * @throws AidaUnsupportedLanguageException if the language is not supported or not active
	 */
	public static Language getLanguageForString(String language) throws AidaUnsupportedLanguageException {
		LANGUAGE l = stringToAidaLanguage.get(language);
		if (l == null || !mapLanguages.containsKey(l)) {
			throw new AidaUnsupportedLanguageException("Language " + language + " "
					+ "is not supported or not recognized. Please provide 2 or 3 letter encoding");
		}
		return mapLanguages.get(l);
	}

	/**
	 * returns Language object for the given language, and if it is not active, returns default. Supports two- and three- letter language code
	 * @param language
	 * @param deflt
	 * @return
	 * @throws AidaUnsupportedLanguageException
	 */
	public static Language getLanguageForString(String language, String deflt) throws AidaUnsupportedLanguageException {
		try {
			return getLanguageForString(language);
		} catch (AidaUnsupportedLanguageException e) {
			return getLanguageForString(deflt);
		}
	}

	public static Collection<Language> activeLanguages() {
		return Collections.unmodifiableCollection(mapLanguages.values());
	}

	public static boolean isSupportedLanguage(String languageString) {
		return stringToAidaLanguage.containsKey(languageString);
	}

	public static boolean isActiveLanguage(String languageString) {
		return isSupportedLanguage(languageString) && mapLanguages.containsKey(stringToAidaLanguage.get(languageString));
	}

	public static String get3letterLanguage(String language) {
		String result = stringTo3Letter.get(language);
		if(result == null) {
			throw new IllegalArgumentException("Language " + language + " is not supported!");
		}
		return result;
	}

	public static String get2letterLanguage(String language) {
		String result = stringTo2Letter.get(language);
		if(result == null) {
			throw new IllegalArgumentException("Language " + language + " is not supported!");
		}
		return result;
	}

	@Override
	public String toString() {
		return language.toString();
	}

	public String name() {
		return language.name();
	}

	public String getPathStopWords() {
		return pathStopWords;
	}

	public String getPathSymbols() {
		return pathSymbols;
	}

	public boolean isArabic() {
		return language.equals(LANGUAGE.ar);
	}

	public boolean isGerman() {
		return language.equals(LANGUAGE.de);
	}

	public boolean isEnglish() {
		return language.equals(LANGUAGE.en);
	}

	public boolean isSpanish() {
		return language.equals(LANGUAGE.es);
	}

	public boolean isFrench() {
		return language.equals(LANGUAGE.fr);
	}

	public boolean isItalian() {
		return language.equals(LANGUAGE.it);
	}

	public boolean isPortuguese() {
		return language.equals(LANGUAGE.pt);
	}

	public boolean isRussian() {
		return language.equals(LANGUAGE.ru);
	}

	public boolean isChinese() {
		return language.equals(LANGUAGE.zh);
	}

	public boolean isCzech() {
		return language.equals(LANGUAGE.cs);
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Language language1 = (Language) o;

		if (language != language1.language) return false;
		if (pathStopWords != null ? !pathStopWords.equals(language1.pathStopWords) : language1.pathStopWords != null)
			return false;
		return pathSymbols != null ? pathSymbols.equals(language1.pathSymbols) : language1.pathSymbols == null;

	}

	@Override
	public int hashCode() {
		int result = language != null ? language.hashCode() : 0;
		result = 31 * result + (pathStopWords != null ? pathStopWords.hashCode() : 0);
		result = 31 * result + (pathSymbols != null ? pathSymbols.hashCode() : 0);
		return result;
	}
}


