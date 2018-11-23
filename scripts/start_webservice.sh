#! /bin/bash
export MAVEN_OPTS="-Djetty.port=8080 -Dorg.postgresql.forcebinary=true -Xmx32G"
echo "Starting Entity Linking Web Service..."
mvn clean compile
mvn jetty:run


