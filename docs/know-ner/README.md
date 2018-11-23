**KnowNER**

KnowNER is a UIMA annotator, which annotates the provided document with NamedEntity annotations. This class is used in DISAMBIGUATION_PIPELINE and EVALUATION_PIPELINE.

The execution of KnowNER depends on the configuration in _ner.properties_ file:

* models_path - the path to the directory containing models which are used for NER
* keyspace_pattern - KnowNER will use keyspace \<dump\>\_\<keyspace_pattern\>\_\<language\> for `WikipediaLinkProbability` and `MentionTokenWeights` feature extractors
* lang_resources_location - KnowNER will use the local resources from \<lang_resources_location\>/\<language\> in annotators and feature extractors
* languages - list of languages which are currently supported by KnowNER