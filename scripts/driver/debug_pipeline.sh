#!/bin/bash

export MAVEN_OPTS="-Xmx14G -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=y"
args=(${@// /\\ })
mvn exec:java -Dexec.mainClass='de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.driver.UimaCommandLineProcessor' -Dorg.postgresql.forcebinary=true -Dexec.args="${args[*]}"

