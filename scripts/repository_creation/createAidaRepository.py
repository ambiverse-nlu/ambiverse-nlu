#!/usr/bin/env python
# encoding: utf-8

"""
Create an AmbiverseNLU repository for the most recent Wikipedia dump versions for the specified languages

-----

# Requires:
* Java 8
* Maven 3
* Python 3
* Postgres 9 running
* (optionally Cassandra 3 running for building)

-----

# How to run it: Check out yago3 next to ambiverse-nlu
* git clone https://github.com/yago-naga/yago3.git

# Copy over the yago_aida.ini from the this repository to yago3
* cp $WORKSPACE/ambiverse-nlu/scripts/repository_creation/yago_aida.ini $WORKSPACE/yago3/configuration/yago_aida.ini

# Run the script
* python3 -u ambiverse-nlu/scripts/repository_creation/createAidaRepository.py -s 20181109 -d /workspace/entity_linking_repository_creation/tmp_dumps/ -b /workspace/entity_linking_repository_creation/basics3/ -y /workspace/entity_linking_repository_creation/yago3/ -i /workspace/entity_linking_repository_creation/tmp_yago/ -a /workspace/entity_linking_repository_creation/ambiverse-nlu/ -t /workspace/entity_linking_repository_creation/tmp  -l en -l de --db-server postgres.local --db-username **** --db-password **** -c cassandra-1.local:9042 -c cassanda-2.local:9042 --yago-ini=yago_aida.ini -o /repository/dumps --reuse-yago --include-concepts --run-neo4j --neo4j-file-dir=/workspace/entity_linking_repository_creation/tmp_neo4j/

-----

Usage:
  createAidaRepository.py -d TARGET_DIR -b BASICS_DIST_DIR -y YAGO_DIST_DIR -i YAGO_INDEX_DIR -a AIDA_DIST_DIR -t AIDA_TEMP_DIR (-l LANGUAGE ...) [(--date=DATE ...)] [--wikidata-date=WIKIDATA_DATE] [-s START_DATE] [--reuse-yago] --db-server=DB_SERVER --db-username=DB_USERNAME --db-password=DB_PASSWORD -o DUMPS_OUTPUT_DIR (-c CASSANDRA_HOST ...) [--yago-ini=YAGO_INI] [--skip-aida] [--include-concepts] [--run-neo4j] [--neo4j-file-dir=NEO4J_FILE_DIR] [--neo4j-server=NEO4J_SERVER] [--neo4j-ssh-username=SSH_USERNAME] [--neo4j-ssh-password=SSH_PASSWORD] [--neo4j-destination-dir=NEO4J_DESTINATION_DIR] [--neo4j-import-server-dir=NEO4j_IMPORT_SERVER_DIR] [--stages=STAGES] [--subgraph-entities=SUBGRAPH_ENTITIES] [--subgraph-classes=SUBGRAPH_CLASSES]
  
Options:
  -d TARGET_DIR --target-dir=TARGET_DIR                     directory to store the Wikipedia and Wikidata dumps
  -b BASICS_DIST_DIR --basics-dist-dir=BASICS_DIST_DIR      directory of the BASICS3 distribution
  -y YAGO_DIST_DIR --yago-dist-dir=YAGO_DIST_DIR            directory of the YAGO3 distribution
  -i YAGO_INDEX_DIR --yago-index-dir=YAGO_INDEX_DIR         directory where to store the generated YAGO3 index
  -a AIDA_DIST_DIR --aida-dist-dir=AIDA_DIST_DIR            directory of the AIDA distribution
  -t AIDA_TEMP_DIR --aida-temp-dir=AIDA_TEMP_DIR            directory that holds some temporary AIDA output files
  -l LANGUAGE --language=LANGUAGE                           Wikipedia dump language
  -o DUMPS_OUTPUT_DIR --dumps-output-dir=DUMPS_OUTPUT_DIR   the output folder for persistently stored dumps
  --date=DATE                                               Date of the Wikipedia dump
  --wikidata-date=WIKIDATA_DATE                             Date of the Wikidata dump
  -s START_DATE --start-date=START_DATE                     Date from where the search for dumps starts backwards in time (default: today())
  --db-server=DB_SERVER                                     database server that holds the AIDA database
  --db-username=DB_USERNAME                                 username to use with the AIDA database
  --db-password=DB_PASSWORD                                 password to use with the AIDA database
  -c CASSANDRA_HOST --cassandra-host=CASSANDRA_HOST         Cassandra host that the data will be populated to
  --reuse-yago                                              Flag to set reuse yago output to true
  --yago-ini=YAGO_INI                                       the YAGO configuration file to load [default: yago_aida.ini]
  --skip-aida                                               does not run the AIDA process
  --include-concepts                                        include concepts in the YAGO
  --run-neo4j                                               run neo4j extractor and import
  --neo4j-file-dir=NEO4J_FILE_DIR                           directory where to store the generated Neo4j import files
  --neo4j-server=NEO4J_SERVER                               neo4j database server name (default=hard)
  --neo4j-ssh-username=SSH_USERNAME                         username to use for ssh copy
  --neo4j-ssh-password=SSH_PASSWORD                         password to use for ssh copy
  --neo4j-destination-dir=NEO4J_DESTINATION_DIR             neo4j destination path on the server
  --neo4j-import-server-dir=NEO4j_IMPORT_SERVER_DIR         neo4j server path that is used for import script
  --stages=STAGES                                           stages to run (override preconfigured stages)
  --subgraph-entities=SUBGRAPH_ENTITIES                     sets subgraphEntities in the yago.ini, restricting the entities in YAGO
  --subgraph-classes=SUBGRAPH_CLASSES                       sets subgraphClasses in the yago.ini, restricting the entities in YAGO
"""
import fileinput
import hashlib
import os
import paramiko
import re
import shutil
import subprocess
import sys
from datetime import datetime
from git import Repo
from paramiko import SSHClient
from scp import SCPClient
from subprocess import PIPE, STDOUT

from docopt import docopt

# Constants
YAGO3_DOWNLOADDUMPS_SCRIPT = 'scripts/dumps/downloadDumps.py'
AIDA_RUN_DATA_PREPARATION_SCRIPT = 'scripts/preparation/run_data_preparation.sh'
AIDA_PERSISTENTLY_STORE_DUMPS_SCRIPT = 'scripts/repository_creation/persistentlyStoreDumps.py'

YAGO3_CONFIGURATION_SUBDIR = 'configuration'
YAGO3_CONFIGURATION_TEMPLATE = 'yago_aida.ini'
YAGO3_TMP_CONFIGURATION = 'yago_aida_createAidaRepositoryConfiguration.ini'
YAGO3_ADAPTED_CONFIGURATION_EXTENSION = '.adapted.ini'

YAGO3_LANGUAGES_PROPERTY = 'languages'
YAGO3_YAGOFOLDER_PROPERTY = 'yagoFolder'
YAGO3_NEO4JFOLDER_PROPERTY = 'neo4jFolder'
YAGO3_REUSE_YAGO_PROPERTY = 'reuse'
YAGO3_DUMPSFOLDER_PROPERTY = 'dumpsFolder'
YAGO3_WIKIPEDIAS_PROPERTY = 'wikipedias'
YAGO3_INCLUCE_CONCEPTS_PROPERTY = 'includeConcepts'
YAGO3_EXTRACTORS_PROPERTY = 'extractors'
YAGO3_NEO4J_EXTRACTOR = 'deduplicators.Neo4jThemeTransformer,'
YAGO3_SUBGRAPH_ENTITIES = 'subgraphEntities'
YAGO3_SUBGRAPH_CLASSES = 'subgraphClasses'

NEO4J_IMPORT_SCRIPT_FILE_NAME = 'import_script.txt'
NEO4J_SCRIPT_NEO4J_FOLDER_PLACEHOLDER = 'YAGOOUTPUTPATH'

NEO4J_SOURCE_SERVER_PATH = '/local_san2/tmp/neo4j-enterprise-3.4.0/' #This is hard coded and is on badr. Should it be inputed?
NEO4J_DESTINATION_DIR_PATH = '/var/tmp/neo4j/'
NEO4J_DESTINATION_SERVER_NAME = 'hard'
NEO4J_GRAPH_NAME_FILE = 'graphNameFile'

AIDA_CONFIGURATION_DIR = 'src/main/config'
AIDA_CONFIGURATION_TEMPLATE_DIR = 'default'
AIDA_CONFIGURATION_TMP_DIR = 'default_tmp'
AIDA_CONFIGURATION_STAGES = 'MINIMAL,CASSANDRA_CREATION'
AIDA_CONCEPT_CONFIGURATION_STAGES = 'MINIMAL,CONCEPT_CATEGORIES,CASSANDRA_CREATION'
AIDA_DATABASE_NAME_PREFIX = 'aida_'
AIDA_DATABASE_SCHEMA_VERSION = '_v18'
AIDA_PROPERTIES_FILE = 'aida.properties'
AIDA_DATABASE_AIDA_PROPERTIES_FILE = 'database_aida.properties'
AIDA_CASSANDRA_PROPERTIES_FILE = 'cassandra.properties'
AIDA_PREPARATION_PROPERTIES_FILE = 'preparation.properties'
AIDA_NER_PROPERTIES_FILE = 'ner.properties'

AIDA_SERVER_NAME_PROPERTY = 'dataSource.serverName'
AIDA_DATABASE_NAME_PROPERTY = 'dataSource.databaseName'
AIDA_USER_PROPERTY = 'dataSource.user'
AIDA_PASSWORD_PROPERTY = 'dataSource.password'
AIDA_LANGUAGES_PROPERTY = 'languages'
AIDA_NER_LANGUAGES_PROPERTY = 'languages'
AIDA_TARGET_LANGUAGES_PROPERTY = 'target.languages'
AIDA_HOST_PROPERTY = 'host'
AIDA_KEYSPACE_PROPERTY = 'keyspace'
AIDA_REPLICATION_FACTOR_PROPERTY = 'replication.factor'
AIDA_LOAD_MODELS_FORM_CLASS_PATH = 'load_models_from_classpath'

AIDA_PREPARATION_CONFIG_YAGO3_FILE = 'yago3.file'

class Usage(Exception):
    def __init__(self, msg):
        self.msg = msg


def main(argv=None):
  try:
    print("Adapting the YAGO3 configuration...")
    adaptYagoConfiguration()        # Initial adaptation of YAGO configuration (languages, dumpsFolder)

    print("Downloading dump(s)...")
    downloadDumps()                 # Adaptation of YAGO configuration (dumps locations)

    print("Loading YAGO output folder from configuration file...")
    yagoFolder, neo4jFolder = loadYagoFolderFromAdaptedConfiguration()       # Adaptation of YAGO configuration (yagoFolder, neo4jFolder)

    print("Persistently storing dumps...")
    persistentlyStoreDumps()

    print("Running YAGO3...")
    runYago()

    if skipAida is False:
        print("Adapting the AIDA configuration...")
        dbName = adaptAidaConfiguration(yagoFolder)

        print("Running AIDA...")
        runAida()

        print("Persisting the AIDA configuration...")
        aidaConfigTargetDir = persistAidaConfiguration(dbName)

        print("Repository creation finished. The AIDA config has been added to the Git repository as '{}'".format(os.path.basename(aidaConfigTargetDir)))

    #if run_neo4j:
    #  graphDBname = importYagoToNeo4j(neo4jFolder)
    #  copyGraphToDestination(graphDBname)

  except:
    raise


def execute(cmd, customEnv=None):
    process = subprocess.Popen(cmd, stdout=PIPE, stderr=STDOUT, universal_newlines=True, env=customEnv)

    for line in iter(process.stdout.readline, ""):
        print(line,)

    process.stdout.close()
    return_code = process.wait()

    if return_code:
        raise subprocess.CalledProcessError(return_code, cmd)


"""
Invokes the external shell script for downloading and extracting the Wikipedia dumps
"""


def downloadDumps():
    os.chdir(os.path.abspath(yagoDistDir))

    processCall = ['python3', '-u', os.path.join(os.path.abspath(yagoDistDir), YAGO3_DOWNLOADDUMPS_SCRIPT),
                   '-y', os.path.join(os.path.abspath(yagoDistDir), YAGO3_CONFIGURATION_SUBDIR, YAGO3_TMP_CONFIGURATION),
                   '-s', startDate.strftime("%Y%m%d")]

    if dates:
        for date in dates:
            processCall.append("--date=" + date)

    if options['--wikidata-date']:
        processCall.append("--wikidata-date=" + options['--wikidata-date'])

    print(processCall)

    execute(processCall)


"""
Duplicates the YAGO3 template ini file and adapts the properties as necessary
"""


def adaptYagoConfiguration():
    configDir = os.path.abspath(yagoDistDir)
    iniFile = os.path.join(configDir, YAGO3_CONFIGURATION_SUBDIR, YAGO3_TMP_CONFIGURATION)

    print ("Using YAGO3 configuration template:", yagoConfigurationTemplate)

    shutil.copy(
        os.path.join(configDir, YAGO3_CONFIGURATION_SUBDIR, yagoConfigurationTemplate),
        iniFile)

    dumpsFolderDone = False
    includeConceptsDone = False

    for line in fileinput.input(iniFile, inplace=1):
      if re.match('^' + YAGO3_LANGUAGES_PROPERTY + '\s*=', line):
        line = YAGO3_LANGUAGES_PROPERTY + ' = ' + ','.join(languages) + '\n'
      elif re.match('^' + YAGO3_REUSE_YAGO_PROPERTY + '\s*=', line):
        line = YAGO3_REUSE_YAGO_PROPERTY + ' = ' + str(reuse_yago) + '\n'
      elif re.match('^' + YAGO3_DUMPSFOLDER_PROPERTY + '\s*=', line):
        line = YAGO3_DUMPSFOLDER_PROPERTY + ' = ' + targetDir + '\n'
        dumpsFolderDone = True
      elif re.match('^' + YAGO3_INCLUCE_CONCEPTS_PROPERTY + '\s*=', line):
        line = YAGO3_INCLUCE_CONCEPTS_PROPERTY + ' = ' + str(include_concepts) + '\n'
        includeConceptsDone = True
      elif re.match('^' + YAGO3_EXTRACTORS_PROPERTY + '\s*=', line):
        if run_neo4j is True:
          temp = re.sub(r'\s', '', line).split("=")
          line = temp[0] + "=" + YAGO3_NEO4J_EXTRACTOR + '\n' + temp[1] + '\n'

      # Write the (possibly modified) line back to the configuration file
      sys.stdout.write(line)

    # If the values couldn't be replaced because the property wasn't in the configuration yet, add it.
    with open(iniFile, "a") as configFile:
        if dumpsFolderDone == False:
          configFile.write('\n' + YAGO3_DUMPSFOLDER_PROPERTY + ' = ' + targetDir + '\n')
        if includeConceptsDone == False and include_concepts == True:
          configFile.write('\n' + YAGO3_INCLUCE_CONCEPTS_PROPERTY + ' = ' + str(include_concepts) + '\n')

        # Append new subgraph config if needed
        if yagoSubgraphEntities:
          configFile.write(YAGO3_SUBGRAPH_ENTITIES + "=" + yagoSubgraphEntities)

        if yagoSubgraphClasses:
          configFile.write(YAGO3_SUBGRAPH_CLASSES + "=" + yagoSubgraphClasses)

def loadYagoFolderFromAdaptedConfiguration():
    yagoAdaptedConfigurationFile = os.path.join(os.path.abspath(yagoDistDir), YAGO3_CONFIGURATION_SUBDIR, YAGO3_TMP_CONFIGURATION + YAGO3_ADAPTED_CONFIGURATION_EXTENSION)

    for line in fileinput.input(yagoAdaptedConfigurationFile, inplace=1):
        if re.match('^' + YAGO3_WIKIPEDIAS_PROPERTY + '\s*=', line):
          wikipedias = re.sub(r'\s', '', line).split("=")[1].split(",")
            
          wikipediasFriendly = []
            
          for wikipedia in wikipedias:
            wikipediaLanguage = wikipedia[-34:-32]
            wikipediaDate = wikipedia[-27:-19]
            
            wikipediasFriendly.append(wikipediaLanguage + str(wikipediaDate))
            
          yagoFolder = os.path.join(yagoIndexDir, "yago_aida_" + "_".join(wikipediasFriendly))

          # If YAGO is built as a subgraph, make sure that the dir is unique for that.
          yagoDirSuffix = ""

          if yagoSubgraphEntities:
            yagoDirSuffix += "_" + hashlib.md5(yagoSubgraphEntities.encode('utf-8')).hexdigest()

          if yagoSubgraphClasses:
            yagoDirSuffix += "_" + hashlib.md5(yagoSubgraphClasses.encode('utf-8')).hexdigest()

          if yagoDirSuffix:
              yagoFolder = yagoFolder + yagoDirSuffix

          
          neo4jFolder = ''
          if run_neo4j:
            neo4jFolder = os.path.join(neo4jFileDir, "yago_aida_" + "_".join(wikipediasFriendly))
            if yagoDirSuffix:
                neo4jFolder = neo4jFolder + yagoDirSuffix
            if not os.path.exists(neo4jFolder):
              os.makedirs(neo4jFolder)
          
          # Make sure the yagoFolder directory is there
          print("Creating YAGO in: ", yagoFolder)

          if not os.path.exists(yagoFolder):
            os.makedirs(yagoFolder)

          sys.stdout.write(line)
        elif not (re.match('^' + YAGO3_YAGOFOLDER_PROPERTY + '\s*=', line) or re.match('^' + YAGO3_NEO4JFOLDER_PROPERTY + '\s*=', line)):
          sys.stdout.write(line)   # that way, remove YAGO folder from configuration file for now
        
          
    with open(yagoAdaptedConfigurationFile, "a") as configFile:
      configFile.write('\n' + YAGO3_YAGOFOLDER_PROPERTY + ' = ' + yagoFolder + '\n')
      if run_neo4j:
        configFile.write('\n' + YAGO3_NEO4JFOLDER_PROPERTY + ' = ' + neo4jFolder + '\n')

    return yagoFolder, neo4jFolder


"""
Runs YAGO3 with the adapted configuration file
"""


def runYago():
    # Install most recent version of BASICS
    os.chdir(basicsDistDir)
    execute(['mvn', '-U', 'clean', 'verify', 'install'])

    # Build and run YAGO

    # Switch to YAGO dir
    os.chdir(yagoDistDir)

    # Give enough memory
    myEnv = os.environ.copy()
    myEnv['MAVEN_OPTS'] = '-Xmx400G'

    execute(
        ['mvn', '-U', 'clean', 'verify', 'exec:java',
         '-Dexec.args=' + YAGO3_CONFIGURATION_SUBDIR + '/' + YAGO3_TMP_CONFIGURATION + '.adapted.ini'],
        myEnv)


"""
Duplicates the AIDA template config folder and adapts the properties as necessary
"""


def adaptAidaConfiguration(yagoFolder):
    aidaConfigDir = os.path.join(aidaDistDir, AIDA_CONFIGURATION_DIR)
    aidaConfigTemplateDir = os.path.join(aidaConfigDir, AIDA_CONFIGURATION_TEMPLATE_DIR)
    aidaConfigTmpDir = os.path.join(aidaConfigDir, AIDA_CONFIGURATION_TMP_DIR)

    # Make sure the new configuration folder doesn't exist yet. It it does, delete it.
    if os.path.exists(aidaConfigTmpDir):
        shutil.rmtree(aidaConfigTmpDir)

    # Copy over the template
    shutil.copytree(aidaConfigTemplateDir, aidaConfigTmpDir)

    # Adapt the database properties
    dbName = AIDA_DATABASE_NAME_PREFIX + getDatabaseId(yagoFolder) + AIDA_DATABASE_SCHEMA_VERSION

    dbPropertiesFile = os.path.join(aidaConfigTmpDir, AIDA_DATABASE_AIDA_PROPERTIES_FILE)
    for line in fileinput.input(dbPropertiesFile, inplace=1):
        if re.match('^' + AIDA_SERVER_NAME_PROPERTY + '\s*=', line):
            line = AIDA_SERVER_NAME_PROPERTY + ' = ' + dbServer + '\n'
        elif re.match('^' + AIDA_DATABASE_NAME_PROPERTY + '\s*=', line):
            line = AIDA_DATABASE_NAME_PROPERTY + ' = ' + dbName + '\n'
        elif re.match('^' + AIDA_USER_PROPERTY + '\s*=', line):
            line = AIDA_USER_PROPERTY + ' = ' + dbUsername + '\n'
        elif re.match('^' + AIDA_PASSWORD_PROPERTY + '\s*=', line):
            line = AIDA_PASSWORD_PROPERTY + ' = ' + dbPassword + '\n'
        sys.stdout.write(line)

    # Create a list of both three- and two-letter language codes
    doubleLangMappings = []
    for language in languages:
        doubleLangMappings.extend([getThreeLetterLanguageCode(language), language])

    # Adapt the aida properties
    aidaPropertiesFile = os.path.join(aidaConfigTmpDir, AIDA_PROPERTIES_FILE)
    for line in fileinput.input(aidaPropertiesFile, inplace=1):
        if re.match('^' + AIDA_LANGUAGES_PROPERTY + '\s*=', line):
            line = AIDA_LANGUAGES_PROPERTY + ' = ' + ','.join(languages) + '\n'
        sys.stdout.write(line)

    # Adapt the preparation properties
    prepPropertiesFile = os.path.join(aidaConfigTmpDir, AIDA_PREPARATION_PROPERTIES_FILE)
    for line in fileinput.input(prepPropertiesFile, inplace=1):
        if re.match('^#?\s?' + AIDA_PREPARATION_CONFIG_YAGO3_FILE + '\s*=', line):
            line = AIDA_PREPARATION_CONFIG_YAGO3_FILE + ' = ' + yagoFolder + '/aidaFacts.tsv\n'
        elif re.match('^' + AIDA_TARGET_LANGUAGES_PROPERTY + '\s*=', line):
            line = AIDA_TARGET_LANGUAGES_PROPERTY + ' = ' + ','.join(doubleLangMappings) + '\n'
        sys.stdout.write(line)

    # Adapt the ner properties - KnowNER does not support chinese, remove from languages
    nerLanguages = languages[:]
    if 'zh' in nerLanguages:
        nerLanguages.remove('zh')
    nerPropertiesFile = os.path.join(aidaConfigTmpDir, AIDA_NER_PROPERTIES_FILE)
    for line in fileinput.input(nerPropertiesFile, inplace=1):
        if re.match('^' + AIDA_NER_LANGUAGES_PROPERTY + '\s*=', line):
            line = AIDA_NER_LANGUAGES_PROPERTY + ' = ' + ','.join(nerLanguages) + '\n'
        # Adapt the load_models_from_classpath to read and write to the filesystem
        elif re.match('^' + AIDA_LOAD_MODELS_FORM_CLASS_PATH + '\s*=', line):
            line = AIDA_LOAD_MODELS_FORM_CLASS_PATH + '=false\n'
        sys.stdout.write(line)

    # Adapt the Cassandra properties
    cassandraPropertiesFile = os.path.join(aidaConfigTmpDir, AIDA_CASSANDRA_PROPERTIES_FILE)
    for line in fileinput.input(cassandraPropertiesFile, inplace=1):
        if re.match('^' + AIDA_HOST_PROPERTY + '\s*=', line):
            line = AIDA_HOST_PROPERTY + ' = ' + ','.join(cassandraHosts) + '\n'
        elif re.match('^' + AIDA_REPLICATION_FACTOR_PROPERTY + '\s*=', line):
            line = AIDA_REPLICATION_FACTOR_PROPERTY + ' = ' + str(len(cassandraHosts)) + '\n'
        # The keyspace should be exactly the same as the SQL database name
        elif re.match('^' + AIDA_KEYSPACE_PROPERTY + '\s*=', line):
            line = AIDA_KEYSPACE_PROPERTY + ' = ' + dbName + '\n'
        sys.stdout.write(line)
    
    return dbName


"""
Runs AIDA with the adapted configuration dir
"""


def runAida():
    # Make sure the tmp directory is there
    if not os.path.exists(aidaTempDir):
        os.makedirs(aidaTempDir)

    # Switch to AIDA dir
    os.chdir(aidaDistDir)

    # Give enough memory
    myEnv = os.environ.copy()
    myEnv['MAVEN_OPTS'] = '-Xmx212G'

    if include_concepts:
        stages = AIDA_CONCEPT_CONFIGURATION_STAGES
    else :
        stages = AIDA_CONFIGURATION_STAGES

    if stagesOverride is not None:
        stages = stagesOverride

    execute(
        [os.path.join(AIDA_RUN_DATA_PREPARATION_SCRIPT),
         AIDA_CONFIGURATION_TMP_DIR, aidaTempDir, stages],
        myEnv)


"""
Converts from ISO 639-1 into ISO 639-2 format. For creating the mapping, we referred to this website:
https://www.loc.gov/standards/iso639-2/php/code_list.php
"""


def getThreeLetterLanguageCode(twoLetterCode):
    codeTable = {
        'ar': "ara",
        'de': "deu",
        'en': "eng",
        'fr': "fra",
        'it': "ita",
        'jp': "jpn",
        'es': "spa",
        'pt': "por",
        'ru': "rus",
        'cs': "ces",
        'zh': "zho"
    }
    return codeTable.get(twoLetterCode, "xx")


"""
Constructs the database ID from yagoFolder name
"""


def getDatabaseId(yagoFolder):
    languages = []
    dates = []

    languageDates = yagoFolder[yagoFolder.find("/yago_aida_") + len("/yago_aida_"):].split("_")

    pattern = re.compile("^\d+$")

    for languageDate in languageDates:
        languages.append(languageDate[0:2])
        date = languageDate[2:]
        if pattern.match(date):
            dates.append(date)

    print("CHECK DB ID: " + max(dates) + '_' + '_'.join(sorted(languages)))
    return max(dates) + '_' + '_'.join(sorted(languages))


def persistAidaConfiguration(dbName):
  aidaConfigDir = os.path.join(aidaDistDir, AIDA_CONFIGURATION_DIR)
  
  aidaConfigTmpDir = os.path.join(aidaConfigDir, AIDA_CONFIGURATION_TMP_DIR)
  aidaConfigTargetDir = os.path.join(aidaConfigDir, dbName + "_cass")

  # Make sure the new configuration folder doesn't exist yet. It it does, delete it.
  if os.path.exists(aidaConfigTargetDir):
      shutil.rmtree(aidaConfigTargetDir)

  # Copy the AIDA configuration to its final destination
  shutil.copytree(aidaConfigTmpDir, aidaConfigTargetDir)
  
  # Commit and push configuration to the Git repository
  repo = Repo(aidaDistDir)
  repo.index.add(AIDA_CONFIGURATION_DIR + "/" + os.path.basename(aidaConfigTargetDir))
  repo.index.commit("Added configuration " + os.path.basename(aidaConfigTargetDir))
  repo.remotes.origin.pull()    # Attempt a pull first, in case things were added meanwhile.
  repo.remotes.origin.push()
  
  return aidaConfigTargetDir
  
def persistentlyStoreDumps():
  os.chdir(os.path.abspath(aidaDistDir))

  processCall = ['python3', os.path.join(os.path.abspath(aidaDistDir), AIDA_PERSISTENTLY_STORE_DUMPS_SCRIPT),
                 '-y', os.path.join(os.path.abspath(yagoDistDir), YAGO3_CONFIGURATION_SUBDIR, YAGO3_TMP_CONFIGURATION),
                 '-d', targetDir,
                 '-o', dumpsOutputDir]

  print(processCall)

  execute(processCall)


"""
Execute the import script to import yago to neo4j
"""

def importYagoToNeo4j(neo4jFolder):
  graphDBname = os.path.basename(os.path.normpath(neo4jFolder))
  
  if os.path.exists(NEO4J_SOURCE_SERVER_PATH + 'data/databases/' + graphDBname + '.db'):
    print("Graph database already exists.")
    os.chdir(neo4jFileDir);
    f=open(NEO4J_GRAPH_NAME_FILE, 'w')
    f.write('DatabaseName=' + graphDBname)
    f.close()
    return graphDBname
    

  command = open(os.path.join(neo4jFolder, NEO4J_IMPORT_SCRIPT_FILE_NAME), "r").read()
  command =  command.replace(NEO4J_SCRIPT_NEO4J_FOLDER_PLACEHOLDER, neo4jFolder)
  command = command.replace('"', '')
  commands = command.split(' ') 

  processCall = [NEO4J_SOURCE_SERVER_PATH + 'bin/neo4j-admin', 'import',
                 '--mode', 'csv',
                 '--ignore-duplicate-nodes', 'true',
                 '--ignore-missing-nodes', 'true',
                 '--delimiter', 'TAB',
                  '--database', graphDBname + '.db']

  for cmd in commands:
    if cmd != '':
      processCall.append(cmd)
  
  myEnv = os.environ.copy()
  myEnv['JAVA_OPTS'] = '-Xmx250G'
  
  print("Neo4j import start...")
  execute(processCall, myEnv)
  print("Neo4j import finished.")
  
  # Write the graph name to a file, to use it in jenkins
  os.chdir(neo4jFileDir);
  f=open(NEO4J_GRAPH_NAME_FILE, 'w')
  f.write('DatabaseName=' + graphDBname)
  f.close()
  
  return graphDBname
  
# Define progress callback that prints the current percentage completed for the file
def progress(filename, size, sent):
    sys.stdout.write("%s\'s progress: %.2f%%   \r" % (filename, float(sent)/float(size)*100) )
    
def copyGraphToDestination(graphDBname):
  # If the graph folder existed, remove it.
  transport = paramiko.Transport(NEO4J_DESTINATION_SERVER_NAME)
  transport.connect(username = ssh_username, password = ssh_password)

  sftp = paramiko.SFTPClient.from_transport(transport)
  dbFolderExisted = False
  try:
    sftp.stat(os.path.join(NEO4J_DESTINATION_DIR_PATH, 'data/databases/', graphDBname + '_successful_copy'))
    print("Database exists remotely.")
    return graphDBname
  except IOError:
    print("Database copy flag does not exist remotely.")

  try:
    sftp.stat(os.path.join(NEO4J_DESTINATION_DIR_PATH, 'data/databases/', graphDBname + '.db'))  # Test if remote_path exists
    print("Database folder existed remotely, but the copy flag does not exist.")
    print(os.path.join(NEO4J_DESTINATION_DIR_PATH, 'data/databases/', graphDBname + '.db'))
    sys.exit("Error: Need manual check, database exists remotely.")
  except IOError:
    print("Database folder did not existed remotely.")
  sftp.close()
  
  print ("Start copying graph Database...")
  ssh = SSHClient()
  ssh.load_system_host_keys()
  ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
  ssh.connect(NEO4J_DESTINATION_SERVER_NAME, username=ssh_username, password=ssh_password)
   
  # SCPCLient takes a paramiko transport as an argument, used this one to copy recursive
  scp = SCPClient(ssh.get_transport(), progress = progress)
  scp.put(os.path.join(NEO4J_SOURCE_SERVER_PATH, 'data/databases/', graphDBname + '.db'), recursive=True, remote_path=os.path.join(NEO4J_DESTINATION_DIR_PATH, 'data/databases/'))
  scp.put(os.path.join(NEO4J_SOURCE_SERVER_PATH, 'data/databases/', 'db_successful_copy'), os.path.join(NEO4J_DESTINATION_DIR_PATH, 'data/databases/', graphDBname + '_successful_copy'))
  scp.close()
  ssh.close()
  print ("Finished copying graph Database.")
  
  

if __name__ == "__main__":
    # parse options
    options = docopt(__doc__)

    targetDir = options['--target-dir']
    languages = options['--language']
    dates = options['--date']
    wikidataDate = options['--wikidata-date']
    basicsDistDir = options['--basics-dist-dir']
    yagoDistDir = options['--yago-dist-dir']

    yagoIndexDir = options['--yago-index-dir']
    aidaDistDir = options['--aida-dist-dir']
    aidaTempDir = options['--aida-temp-dir']
    dumpsOutputDir = options['--dumps-output-dir']

    dbServer = options['--db-server']
    dbUsername = options['--db-username']
    dbPassword = options['--db-password']
    cassandraHosts = options['--cassandra-host']

    # Read optional arguments with dynamic defaults
    if options['--start-date']:
        startDate = datetime.strptime(options['--start-date'], '%Y%m%d')
    else:
        startDate = datetime.today()

    if options['--skip-aida']:
        skipAida = True
    else:
        skipAida = False

    if options['--reuse-yago']:
        reuse_yago = True
    else:
        reuse_yago = False

    if options['--yago-ini']:
        yagoConfigurationTemplate = options['--yago-ini']
    else:
        yagoConfigurationTemplate = YAGO3_CONFIGURATION_TEMPLATE

    if options['--stages']:
        stagesOverride = options['--stages']
    else:
        stagesOverride = None

    if options['--include-concepts']:
      include_concepts = True
    else:
      include_concepts = False

    if options['--run-neo4j']:
      run_neo4j = True
      
      if not options['--neo4j-file-dir']:
        print ("Error: Must provide neo4j-file-dir")
        sys.exit(1)
        
      neo4jFileDir = options['--neo4j-file-dir']
      
      if options['--neo4j-server']:
        NEO4J_DESTINATION_SERVER_NAME = options['--neo4j-server'] 
      
      if not options['--neo4j-ssh-username'] or not options['--neo4j-ssh-password']:
        print ("Error: Must provide neo4j-ssh-username and neo4j-ssh-password")
        sys.exit(1)
      
      ssh_username = options['--neo4j-ssh-username']
      ssh_password = options['--neo4j-ssh-password']
      
      if options['--neo4j-import-server-dir']:
        NEO4J_SOURCE_SERVER_PATH = options['--neo4j-import-server-dir']
        
      if options['--neo4j-destination-dir']:
        NEO4J_DESTINATION_DIR_PATH = options['--neo4j-destination-dir']
        
    else:
      run_neo4j = False

    yagoSubgraphEntities = ""
    yagoSubgraphClasses = ""

    if options['--subgraph-entities']:
        yagoSubgraphEntities = options['--subgraph-entities']

    if options['--subgraph-classes']:
        yagoSubgraphClasses = options['--subgraph-classes']
      
    sys.exit(main())
