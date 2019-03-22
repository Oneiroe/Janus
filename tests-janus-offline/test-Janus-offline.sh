#!/bin/bash

# Author:       Alessio Cecconi
# Date:         2019/03/22
# Description:  This script launches Janus offline fine grain analysis (codename: "Janus-Z").

## Clean up the screen
clear

##################################################################
##################################################################
## script variables
TEST_FOLDER="./tests-janus-offline"
TEST_BASE_NAME="test-Janus-offline"

## Runtime environment constants
JANUS_MAINCLASS="minerful.MinerFulMinerStarter"

## LOGS PATHS
XES_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}".xes"
TEXT_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}".txt"

### INPUT LOG
#INPUT_FILE=${XES_FILE} ## XES log
INPUT_FILE=${TEXT_FILE}" -iE strings" ## hand written txt

##################################################################
##################################################################
# Janus
echo "################### JANUS"
echo -jar Janus.jar $JANUS_MAINCLASS -iLF ${INPUT_FILE}
java -jar Janus.jar $JANUS_MAINCLASS -iLF ${INPUT_FILE}

echo "find the output at \"./output.json\". Preview:"
head ./output.json
echo "..."
