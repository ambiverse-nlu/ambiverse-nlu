package de.mpg.mpi_inf.ambiversenlu.nlu.ner.wikigeneration.sourceprovider;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManualSource extends Source {

	private final String language;
	private static final Map<String, LinkedHashMap<Integer, String>> LANGUAGE_SPECIFIC_ENTITY_IDS_MAP;

	static {
		Map<String, LinkedHashMap<Integer, String>> originalNamedEntityIDsMap_ = new HashMap<>();

		LinkedHashMap<Integer, String> originalNamedEntityIDs = new LinkedHashMap<>();
				originalNamedEntityIDs.put(269059, "<Philadelphia_Phillies>");
				originalNamedEntityIDs.put(1834903, "<Near_South_Side,_Chicago>");
				originalNamedEntityIDs.put(235302, "<Leonardo_da_Vinci>");
				originalNamedEntityIDs.put(1491592, "<Gail_Godwin>");
				originalNamedEntityIDs.put(3207851, "<William_Stanley_(inventor)>");
				originalNamedEntityIDs.put(16105, "<Delhi>");
				originalNamedEntityIDs.put(507198, "<Sea_of_Japan_naming_dispute>");
				originalNamedEntityIDs.put(1676931, "<The_Ritz_Hotel,_London>");
				originalNamedEntityIDs.put(2432299, "<Little_Catawissa_Creek>");
				originalNamedEntityIDs.put(599892, "<Karl_Marx>");
				originalNamedEntityIDs.put(338513, "<West_Liberty_Foods>");
				originalNamedEntityIDs.put(3988333, "<Museum_of_Socialist_Art,_Sofia>");
				originalNamedEntityIDs.put(2155059, "<Frederic_M._Richards>");
		originalNamedEntityIDsMap_.put("en", originalNamedEntityIDs);

		originalNamedEntityIDs = new LinkedHashMap<>();
				originalNamedEntityIDs.put(438757, "<ABBA>");
				originalNamedEntityIDs.put(269059, "<Philadelphia_Phillies>");
				originalNamedEntityIDs.put(623532, "<Amazon_River>");
				originalNamedEntityIDs.put(431381, "<Sagrada_Família>");
				originalNamedEntityIDs.put(41373, "<Russia>");
				originalNamedEntityIDs.put(480303, "<Chicago>");
				originalNamedEntityIDs.put(235302, "<Leonardo_da_Vinci>");
				originalNamedEntityIDs.put(16105, "<Delhi>");
				originalNamedEntityIDs.put(851998, "<William_Shakespeare>");
				originalNamedEntityIDs.put(795102, "<Albert_Einstein>");
				originalNamedEntityIDs.put(64297, "<Mao_Zedong>");
				originalNamedEntityIDs.put(539275, "<Plato>");
				originalNamedEntityIDs.put(463236, "<Napoleon>");
		originalNamedEntityIDsMap_.put("de", originalNamedEntityIDs);
		originalNamedEntityIDs = new LinkedHashMap<>();
				originalNamedEntityIDs.put(3073104, "<cs/Lenka_Honzáková>");
				originalNamedEntityIDs.put(441823, "<Munich_Agreement>");
		originalNamedEntityIDsMap_.put("cs", originalNamedEntityIDs);
		LANGUAGE_SPECIFIC_ENTITY_IDS_MAP = Collections.unmodifiableMap(originalNamedEntityIDsMap_);
	}

	public ManualSource(String language) {
		super(LANGUAGE_SPECIFIC_ENTITY_IDS_MAP.get(language).size());
		this.language = language;
	}

	@Override
	protected LinkedHashMap<Integer, String> getNamedEntitiesMap(int begin, int amount) {
		if (begin != 0 || amount != LANGUAGE_SPECIFIC_ENTITY_IDS_MAP.get(language).size()) {
			throw new IllegalStateException("Request for entities does not match the current state of the manual source");
		}
		return LANGUAGE_SPECIFIC_ENTITY_IDS_MAP.get(language);
	}

	@Override
	public SourceProvider.Type getType() {
		return SourceProvider.Type.MANUAL;
	}
}
