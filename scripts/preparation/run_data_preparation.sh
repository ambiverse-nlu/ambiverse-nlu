#!/bin/bash

# example call
# ./scipts/preparation/run_data_preparation.sh en_20100817v11_yago3_db <PATH/TO/TEMP>
export MAVEN_OPTS="-Xmx500G"

args=(${@// /\\ })
mvn -U clean compile exec:java -Dexec.mainClass="de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.PrepareData" -Daida.conf=${args[0]} -Dexec.args="${args[*]:1}" -Dmaven.test.skip=true
