#!/bin/bash

mvn compile

export MAVEN_OPTS="-Xmx14G"
args=(${@// /\\ })
mvn exec:java -Dexec.mainClass='de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.run.UimaCommandLineProcessor' -Dorg.postgresql.forcebinary=true -Dexec.args="${args[*]}"
