# Entity Salience

## Train A Model

To train new Entity Salience model, use the following command:


~~~~~~~~~~~~~~~~~~~~~~~~
$ mvn clean package

$ spark-submit \
 --class de.mpg.mpi_inf.ambiversenlu.nlu.entitysaliance.spark.runners.EntitySalienceTrainingSparkRunner \
 --master yarn-cluster \
 --conf "spark.executor.extraJavaOptions=-Daida.conf=de_en_es_zh_20170320_cass" \
 --executor-memory 12G \
 --driver-memory 8g \
 --driver-java-options "-Dspark.akka.timeout=60000 -Dspark.driver.maxResultSize=10G -Dspark.driver.memory=10G -Dspark.yarn.maxAppAttempts=1" \
 --num-executors 10 \
 --executor-cores 6 \
 --files src/main/resources/log4j.properties \
 target/entitylinking-1.0.0-SNAPSHOT-standalone.jar \
 -i hdfs:/user/d5aida/nyt_entity_salience/annotated-seqs/training/ \
 -o hdfs:/user/d5aida/nyt_entity_salience/result/models/ \
 -m RANDOM_FOREST \
 -f 3 \
 -d cassandra \
 -s 2
~~~~~~~~~~~~~~~~~~~~~~~~

 
The above command with the example parameters is found to be the best peforming one for the current setup. 

Some explanation about the parameters:

The parameters that start with "--" are spark specific. Please refer to the [documentation](https://spark.apache.org/docs/1.6.0/submitting-applications.html) for more information.
The parameters after  `target/entitylinking-1.0.0-SNAPSHOT-standalone.jar` are application specifig.

- `-i` is the input file or folder of the training documents. The training documents are stored in sequence file(s). They can be already annotated with entities. If the documents need to be annotated with entities (entity linking need to be run), please set the feature extractor to ANNOTATE_AND_ENTITY_SALIENCE in `src/main/config/default/salience.properties` like `features.extractor=ANNOTATE_AND_ENTITY_SALIENCE`.
- `-o` is the output folder where the result model is saved.
- `-m` is the classification method used to train the model. Possible values are: `LOG_REG`, `GBT`, `RANDOM_FOREST`, `DECISION_TREE`, `MPC`, `ONE_VS_ALL`
- `-f` is the number of folds used to for cross validation
- `-d` is the default database configuration. Possible values are `cassandra` and `db`.
- `-s` is the scaling factor for positive instances. 

### Testing a model
To test an already trained model use the following command:

~~~~~~~~~~~~~~~~~~~~~~~~
spark-submit \
 --class de.mpg.mpi_inf.ambiversenlu.nlu.entitysaliance.spark.runners.EntitySalienceTestingSparkRunner \
 --master yarn-cluster \
 --conf "spark.executor.extraJavaOptions=-Daida.conf=de_en_es_zh_20170320_cass" \
 --executor-memory 12G \
 --driver-memory 10g \
 --driver-java-options "-Dspark.akka.timeout=60000 -Dspark.driver.maxResultSize=10G -Dspark.driver.memory=10G -Dspark.yarn.maxAppAttempts=1" \
 --num-executors 40 \
 --executor-cores 4 \
 --files src/main/resources/log4j.properties \
 target/entitylinking-1.0.0-SNAPSHOT-standalone.jar \
 -i hdfs:/user/d5aida/nyt_entity_salience/annotated-seqs/2007/ \
 -m hdfs:/user/d5aida/nyt_entity_salience/result/models/application_1511363909014_0551/model_RANDOM_FOREST/part-00000 \
 -o hdfs:/user/d5aida/nyt_entity_salience/result/eval/
~~~~~~~~~~~~~~~~~~~~~~~~

Same as the previous command, the parameters before `target/entitylinking-1.0.0-SNAPSHOT-standalone.jar` are spark specific. 
The parameters bellow  `target/entitylinking-1.0.0-SNAPSHOT-standalone.jar` are application parameters specific for the testing.

- `-i` is the input file or folder of the test documents. The training documents are stored in sequence file(s). They can be already annotated with entities. If the documents need to be annotated with entities (entity linking need to be run), please set the feature extractor to ANNOTATE_AND_ENTITY_SALIENCE in `src/main/config/default/salience.properties` like `features.extractor=ANNOTATE_AND_ENTITY_SALIENCE`.
- `-m` is the path to the trained model produced from the training step.
- `-o` is a path where the results of the evaluation is stored

## Convert NYT collection to Sequence files

~~~~~~~~~~~~~~~~~~~~~~~~
./scripts/entitysalience/nyt2seq.sh \
	 -i /GW/ambiverse/work/collections/nytimes_uncompressed/ \
	 -a /GW/ambiverse/work/collections/nyt_entity_salience/nyt-salience-mapping \
	 -m /GW/ambiverse/work/collections/nyt_entity_salience/fb2yagoIds.tsv \
	 -o /GW/ambiverse/work/collections/nyt_entity_salience/seqs/ \
	 -s 2003,2004,2005,2006,2007
~~~~~~~~~~~~~~~~~~~~~~~~

Where:
- `-i` is the input folder of the NYT collection
- `-a` is mapping file of salient entities
- `-m` is a mapping file of freebase ids to yago ids
- `-o` is an output folder where the sequence files will be stored
- `-s` is subfolders of the NYT collection in a case you want to process only certain years.

