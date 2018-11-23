**How to add a new language to KnowNER**

KnowNER is a trained model which annotates a text with labels (Person, Location, Organization and Misc).
In order to make KnowNER supporting a new language, one should first provide necessary resources for training and evaluating the model and then to train the models. Most of the provided resources can be generated automatically, but some manual action is also required. There are local resources and Cassandra tables. Additionally, some of the resources are dump-dependent and regenerated with every new dump (these are Cassandra tables with new keyspace and some local resources).

1. Generate file with aida facts from Yago (aidaFacts.tsv), which should also contain the new language. It can be done by running yago with the AIDAExtractorMerger. The aida facts are used in feature extractors such as `YagoLabelsToClassProbabilitiesBmeow`.
2. Create (or modify) a configuration folder in entity-linking project in parent folder _src/main/config_. The configuration name is the name of this folder. All the .properties files with configurations should be here. A good starting point is to copy default configuration and modify it
3. Prepare Aida database (Postgres) to support the new language
    
    1. Update enum `de.mpg.mpi_inf.ambiversenlu.nlu.language.Language.LANGUAGE` with the new language where first argument is identifier and second argument is three-letter representation:
    
        Example: **ab(10, "abc")**
       
    2. Add the new language to `aida.properties`. You should update/add the following properties:
        * add the new language to `languages` property
        
            Example: languages=en,es,de,cs,zh,ru **,ab**
            
        * add `stopwords` property and the corresponding file with stopwords in the new language
        
            Example: **ab**.stopwords=src/main/resources/tokens/stopwords-**abc**.txt
        * add `symbols` property and the corresponding file with symbols (or just use the common one _src/main/resources/tokens/symbols.txt_)
        
            Example: **ab**.symbols=src/main/resources/tokens/symbols.txt
        * add `dummysentence` property which contains a short text in the new language
            
            Example: **ab**.dummysentence=blablablabalaba
    3. Run Aida database creation script which will create a new version of Postgres database. The database name (dump) will be used in the further steps.
4. Prepare KnowNER code for supporting the new language
    1. Set the correct Aida database name (dump) in `database_aida.properties`, which we have obtained at the step 3.3
        
        Example: dataSource.databaseName=**dump_name**
        
    2. Languages need to be preprocessed with a tokenizer, a pos tagger, and optionally a lemmatizer. Make sure the implementations for the required components are added to `de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component`:
    
        Example:
        * **AB**_TOKENIZER(ABTokenizer.class)
        * **AB**_POS(ABPos.class)
        * **AB**_LEMMATIZER(ABLemmatizer.class)
    3. Add the new language to `languages` property in `ner.properties`
                   
       Example: languages=en,es,de,cs,ru **,ab**
    
    4. Note that the local resources will be stored into parent folder defined in `lang_resources_location` property in `ner.properties`
        
        Example: lang_resources_location=/GW/ambiverse/work/ner/knowner/generated_configurations. So the resources for the new language will be stored in folder `/GW/ambiverse/work/ner/knowner/generated_configurations/`**ab**`/`
        
    5. Note that the Cassandra tables for the new language will be stored in a keyspace which includes prefix defined in `keyspace_pattern` property in `ner.properties` and dump name
    
        Example: If one sets the `keyspace_pattern` property as following: keyspace_pattern=ner_models_ and the dump name is **dump**, then the keyspace will have the following name for the new language: **dump_ner_models_ab**
    
    6. In order to generate the resources for the new language run `de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.PrepareData` with arguments `. KNOWNER_PREPARE_RESOURCES` and VM option `-Daida.conf=`**configuration name**. The process will generate resources for all active languages in KnowNER. In particular, for previously existing languages it will add _dump dependent_ resources
        * It is possible to add not _dump dependent_ local resources, e.g. `posDictionary`, manually, then the process will not generate it
        * If one wants to add the _dump dependent_ resources manually (works only for local resources) or not to rewrite previously created resources (works for local resources and Cassandra tables), one should set `createNew` flag in `preparation.properties` to false, otherwise the process will throw exception if some of the resources already exist.
    7. Note that the trained model will be stored in parent folder defined in `trained_models_directory` property in `ner.properties`
    
        Example: If one sets the `trained_models_directory` property as following trained_models_directory=/GW/ambiverse/work/ner/KnowNER_model then the models will be located in directory: /GW/ambiverse/work/ner/KnowNER_model/**dump**/**ab**/
    
    7. In order to train the models (KB and NED) for the new language run `de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.PrepareData` with arguments `. KNOWNER_TRAIN_MODEL` and VM option `-Daida.conf=`**configuration name**. Generation of the resources and training the model can be done at one run with arguments `. KNOWNER_PREPARE_RESOURCES,KNOWNER_TRAIN_MODEL`.