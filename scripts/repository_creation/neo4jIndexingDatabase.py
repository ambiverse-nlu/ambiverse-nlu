#!/usr/bin/env python
# encoding: utf-8

"""
Create indexes on neo4j graph database. The indexes are on wikidata instances and spatial indexes.

Usage:
  neo4jIndexingDatabase.py -h HOST_ADDRESS --neo4j-username=NEO4J_USER --neo4j-password=NEO4J_PASS
  
Options:
  -h HOST_ADDRESS --host=HOST_ADDRESS                           Neo4j Server Host Address
  --neo4j-username=NEO4J_USER                           Neo4j Username
  --neo4j-password=NEO4J_PASS                           Neo4j Password
"""
import sys
from docopt import docopt
from neo4j.v1 import GraphDatabase, basic_auth, ServiceUnavailable


def main(argv=None):
  print("Connect to server")
  global driver
  driver = GraphDatabase.driver(neo4j_uri, auth=basic_auth(neo4j_username, neo4j_password), connection_timeout=60)
  createWikidataIndex()
  createSpatialIndex()
  driver.close()
   
def createWikidataIndex():
  global driver
  print('Start creating index on WikidataInstances')
  try:
    with driver.session() as session:
      session.run("CREATE INDEX ON :WikidataInstance(url)")
    return True
  except ServiceUnavailable:
    return False
   
def createSpatialIndex():
  global driver
  print('Start Spatial indexing')
  try:
    with driver.session() as session:
      # neo4j-3.4:
      session.run("CREATE INDEX ON :Location(location)");
      session.run("CALL apoc.periodic.commit(\"MATCH (l:Location) where not exists(l.location) with l limit 10000 SET l.location =  point({latitude: l.latitude, longitude: l.longitude, crs: 'WGS-84'}) return count(l)\", {})");    

      #neo4j-3.3 using spatial library:
      #ession.run("CALL spatial.addPointLayer('geom')")
      #session.run("CALL apoc.periodic.commit(\"MATCH (l:Location) WHERE NOT (l)<-[:RTREE_REFERENCE]-() WITH l LIMIT 10000 WITH collect(l) as locs call spatial.addNodes('geom', locs) YIELD count return count\", {})")

    return True

  except ServiceUnavailable:
    return False
  
if __name__ == "__main__":
  # parse options
  options = docopt(__doc__)
  
  neo4j_uri = options['--host']
  neo4j_username = options['--neo4j-username']
  neo4j_password = options['--neo4j-password']
  
  print("Neo4j server: " + neo4j_uri)
  
  sys.exit(main())