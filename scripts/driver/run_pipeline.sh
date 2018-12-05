#!/bin/bash

mvn compile

export MAVEN_OPTS="-Xmx8G"
args=(${@// /\\ })
mvn exec:java -Dexec.mainClass='de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.run.UimaCommandLineDisambiguator' -Dorg.postgresql.forcebinary=true -Dexec.args="${args[*]}"
