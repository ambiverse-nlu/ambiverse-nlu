#!/usr/bin/env python
# encoding: utf-8

"""
Copies the dumps that are referenced in the adapted version of the input configuration file to a persistent storage location.

Usage:
  persistentlyStoreDumps.py -y YAGO_CONFIGURATION_FILE -d DUMPS_BASE_DIR -o OUTPUT_DIR

Options:
  -y YAGO_CONFIGURATION_FILE --yago-configuration-file=YAGO_CONFIGURATION_FILE    the YAGO3 configuration file
  -o OUTPUT_DIR --output-dir=OUTPUT_DIR                                           the output folder
  -d DUMPS_BASE_DIR --dumps-base-dir=DUMPS_BASE_DIR                               base directory of the dumps folder
"""
from docopt import docopt
import sys
import os
import fileinput
import re
import shutil
import errno

# Constants
YAGO3_ADAPTED_CONFIGURATION_EXTENSION = '.adapted.ini'

YAGO3_WIKIPEDIAS_PROPERTY = 'wikipedias'
YAGO3_WIKIDATA_PROPERTY = 'wikidata'
YAGO3_COMMONSWIKI_PROPERTY = "commons_wiki"
YAGO3_GEONAMES_PROPERTY = "geonames"

# Initialize variables
yagoConfigurationFile = None
outputDir = None
dumpsBaseDir = None


class Usage(Exception):
  def __init__(self, msg):
    self.msg = msg
    
def main(argv=None):
  global dumpsBaseDir
  
  # Load the - adapted! - version of the YAGO configuration file
  yagoAdaptedConfigurationFile = os.path.join(
    os.path.dirname(yagoConfigurationFile), os.path.basename(yagoConfigurationFile) + YAGO3_ADAPTED_CONFIGURATION_EXTENSION)
    
  sources = loadYagoConfiguration(yagoAdaptedConfigurationFile)
  
  # Copy the dumps to the persistent storage location
  for source in sources:
    relOutputPath = source[len(dumpsBaseDir):]
    
    # Make sure there's no slash at the beginning of the relative path
    if (relOutputPath[0:1] == '/'):
      relOutputPath = relOutputPath[1:]
    
    target = os.path.join(outputDir, relOutputPath)

    try:
      if os.path.exists(target):
        print("Target '{}' exists, skipping.".format(target))
        continue
      if os.path.isdir(source):
        print("Copying source directory '{}' to '{}'.".format(source, target))
        shutil.copytree(source, target)
      else:
        print("Copying source '{}' to '{}'.".format(source, target))
        makedirs_p(os.path.dirname(target))
        shutil.copyfile(source, target)
    except OSError:
      print("Could not copy {}. Target already exists?".format(source))
  
"""
Loads the YAGO configuration file.
"""
def loadYagoConfiguration(configFile):
  ret = []
  
  for line in fileinput.input(configFile):
    if re.match('^' + YAGO3_WIKIPEDIAS_PROPERTY + '\s*=', line):
      wikipedias = re.sub(r'\s', '', line).split("=")[1].split(",")
    elif re.match('^' + YAGO3_COMMONSWIKI_PROPERTY + '\s*=', line):
      commonsWiki = re.sub(r'\s', '', line).split("=")[1]
    elif re.match('^' + YAGO3_WIKIDATA_PROPERTY + '\s*=', line):
      wikidata = re.sub(r'\s', '', line).split("=")[1]
    elif re.match('^' + YAGO3_GEONAMES_PROPERTY + '\s*=', line):
      geonames = re.sub(r'\s', '', line).split("=")[1]

  if wikipedias is None or commonsWiki is None or wikidata is None or geonames is None:
    print("ERROR: One of these properties is missing: {}, {}, {}, {}".format(YAGO3_WIKIPEDIAS_PROPERTY, YAGO3_COMMONSWIKI_PROPERTY, YAGO3_WIKIDATA_PROPERTY, YAGO3_GEONAMES_PROPERTY))
    sys.exit(1)
    
  # The return object is a flattened list of all dumps
  ret = []
  ret.append(geonames)
  ret.extend(wikipedias)
  ret.append(commonsWiki)
  ret.append(wikidata)

  print(ret)
  
  return ret

def makedirs_p(path):
    try:
        os.makedirs(path)
    except OSError as exc:  # Python >2.5
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else:
            raise

if __name__ == "__main__":
  # Parse options
  options = docopt(__doc__)
  
  yagoConfigurationFile = options['--yago-configuration-file']
  outputDir = options['--output-dir']
  dumpsBaseDir = options['--dumps-base-dir']
  
  # Make sure the output dir exists
  if not os.path.exists(outputDir):
    os.makedirs(outputDir)
  
  sys.exit(main())