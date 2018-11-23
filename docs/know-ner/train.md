**NerTrainer**

The purpose of this class is to train a KnowNER-model on a given language and training corpus. There are 4 training schemes available, which correspond to cumulatively aggregating stages of features.

* **Agnostic** scheme contains only agnostic features, local to the training corpus.
    * `CoveredFeatureExtractor`
    * `Prefixes`
    * `Suffixes`
    * `org.dkpro.tc.features.window.POSExtractor`
    * `WordShapeExtractor`
    * `PresenceInLeftWindow`
    * `PresenceInRightWindow`
    * `BeginOfSentence`
    
* **Name** scheme additionally to agnostic features contains features which hold information about the Named Entity mentions

    * `MentionTokenWeights`
    * `PosSequenceMatch`
    
* **KB** scheme additionally to name and agnostic features contains features which hold information about Named Entities from different knowledge bases

    * `WikipediaLinkProbability`
    * `ClassTypeProbabilityBmeow`
    * `CountryLowercaseMatch`
    * `DictionariesExtractor`
    
* **NED** scheme additionally contains feature which links the mentions to named entities

    * `BmeowTag`
    
NerTrainer uses configuration file _ner.properties_. In particular, the execution depends on the following properties:
* trained_models_directory - directory where the newly trained models are stored
* trained_models_evaluation_directory - directory where evaluation files are stored, if option -e is passed
* keyspace_pattern - NerTrainer will use keyspace \<dump\>\_\<keyspace_pattern\>\_\<language\> for `WikipediaLinkProbability` and `MentionTokenWeights` feature extractors
* lang_resources_location - NerTrainer will use the local resources from \<lang_resources_location\>/\<language\> in annotators and feature extractors
* dkpro_home - location of dkpro resources
* conll_eval_path - location of conlleval script, which is used to evaluate the model by conll standard


The class has the following options:

* -l - language of the model
* -c - corpus name, which should be found under path _\<generated_configurations\>/\<language\>/corpus/\<corpus_name\>_
* -f - properties file corresponding to a scheme of training (i.e. _ner/new/NED.properties_)
* -t - tag-scheme. There are 2 options: BMEOW or DEFAULT, where DEFAULT means IOB  scheme
* -m - training mode. There are 2 options: TEST or DEV, where in case of TEST we train on training corpus + development test corpus (range traina). In case of DEV we train only on training corpus (range train). The ranges are specified in _src/main/resources/ner/dataset_ranges.properties_.
* -s - source of named entities used in `BmeowTag`. There are 2 options: AIDA or GOLD, where AIDA means that the named entities should be infered by entity-linking disambiguation engine (`AidaAnalysisEngine`). GOLD means that they are provided in the training set.
* -e - evaluation flag, optional. If it is set, then `KnowNEREvaluation` evaluates the model on the testing part of the corpus. Under the hood, it uses the conll evaluation script
* -p - prefix for the model name, optional
* -d - parent directory, optional. If set, then first the parent directory is created and the model is stored in it