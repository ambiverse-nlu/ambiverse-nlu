#!/bin/bash

export MAVEN_OPTS="-Xmx44G"

args=(${@// /\\ })
mvn exec:java -Dexec.mainClass="de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.optimization.SGDParameterOptimizer" -Dexec.args="${args[*]}"