#!/bin/bash

# Author:       Alessio Cecconi
# Date:         2020/05/19
# Description:  This script launches the MinerFulLogMakerStarter to create synthetic log (as a collections of strings) according to the input model and then computes the measures with SJ2T of the same model on the log.
#               Run this launcher with "-h" to understand the meaning of options you can pass.


## Import the shell functions to create Regular Expressions expressing constraints
#. ./constraintsFunctions.cfg

## Clean up the screen
clear

##################################################################
##################################################################
## script variables
TEST_FOLDER="./tests-SJ2T"
TEST_BASE_NAME="GROUND-TRUTH"

## Runtime environment constants
LOG_MAINCLASS="minerful.MinerFulLogMakerStarter"
JANUS_CHECK_MAINCLASS="minerful.JanusModelCheckStarter"

# Input Model
MODEL_ENCODING="json"
MODEL=${TEST_FOLDER}/${TEST_BASE_NAME}"-model.json"

## Log generation settings
MIN_STRLEN=10
MAX_STRLEN=100
TESTBED_SIZE=1000
MEMORY_MAX="2048m"
OUTPUT_ENCODING="strings"
TEMP_TEXT_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}"-log-original.txt"
OUTPUT_TEXT_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}"-log.txt"

## model checking settings
OUTPUT_CHECK_CSV=${TEST_FOLDER}/${TEST_BASE_NAME}"-output.csv"
OUTPUT_CHECK_JSON=${TEST_FOLDER}/${TEST_BASE_NAME}"-output.json"

##################################################################
echo "########### Log Generation"
### GENERATE LOG with MinerFulLogMakerStarter ****
java -Xmx$MEMORY_MAX -cp Janus.jar $LOG_MAINCLASS --input-model-file $MODEL --input-model-encoding $MODEL_ENCODING  --size $TESTBED_SIZE --minlen $MIN_STRLEN --maxlen $MAX_STRLEN --out-log-encoding $OUTPUT_ENCODING --out-log-file $TEMP_TEXT_FILE
# remove the unwanted characters to make it readable in input by Janus
python ${TEST_FOLDER}/cleanStringLog.py $TEMP_TEXT_FILE $OUTPUT_TEXT_FILE
rm $TEMP_TEXT_FILE

# check measures with janus
echo "########### SJ2T Check"
java -cp Janus.jar minerful.JanusModelCheckStarter -iLF $OUTPUT_TEXT_FILE -iLE $OUTPUT_ENCODING -iMF $MODEL -iME $MODEL_ENCODING -oCSV $OUTPUT_CHECK_CSV -oJSON $OUTPUT_CHECK_JSON

# generate MEAN-only CSV of aggregated measures
echo "########### Post Processing"
python pySupport/singleAggregationPerspectiveFocusCSV.py $OUTPUT_CHECK_JSON"AggregatedMeasures.json" $OUTPUT_CHECK_JSON"AggregatedMeasures[MEAN].csv"
echo "MEAN-only aggregated CSV saved in "$OUTPUT_CHECK_JSON"AggregatedMeasures[MEAN].csv"

