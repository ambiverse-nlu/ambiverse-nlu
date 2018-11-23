#!/bin/bash

export MAVEN_OPTS="-Xmx8G"
args=(${@// /\\ })
mvn exec:java -Dexec.mainClass='de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.driver.UimaCommandLineDisambiguator' -Dorg.postgresql.forcebinary=true -Dexec.args="${args[*]}"
