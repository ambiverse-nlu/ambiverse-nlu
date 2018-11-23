 #!/bin/bash

export MAVEN_OPTS="-Xmx44G"

args=(${@// /\\ })
mvn exec:java -Dexec.mainClass="de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.similarity.TrainPipeline" -Dorg.postgresql.forcebinary=true -Dexec.args="${args[*]}"

# ./scripts/training/estimate_mention_entity_similarity_settings.sh -s src/main/resources/similarity/conll/SwitchedUnit.properties -tmp /tmp -o out -d src/test/resources/collections/en/conll/docs/ -z src/test/resources/collections/en/conll/settings/collection.properties -i AIDA -b 1 -f 947 -m SMO -l en