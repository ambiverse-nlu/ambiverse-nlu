#! /bin/bash
export AIDA_CONF="default"
echo "Starting Entity Linking Web Service..."
export MAVEN_OPTS="-Xmx32G -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4008,server=y,suspend=n"
mvn clean compile
mvn jetty:run
