#!/bin/bash

# Author:       Alessio Cecconi
# Date:         2020/05/11
# Description:  This script launches the MinerFulErrorInjectedTracesMakerStarter to create synthetic log (as a collections of strings) according to the input model but containing selected errors and then computes the measures with SJ2T of the same model on the log.
#               Run this launcher with "-h" to understand the meaning of options you can pass.

## Import the shell functions to create Regular Expressions expressing constraints
#. ./constraintsFunctions.cfg

## Clean up the screen
clear

##################################################################
##################################################################
## script variables
TEST_FOLDER="./tests-Journal-SJ2T/"
TEST_BASE_NAME="ERROR-INJECTION-NEU-WHITE-NOISE"

mkdir ${TEST_FOLDER}/${TEST_BASE_NAME}"/"
mkdir ${TEST_FOLDER}/${TEST_BASE_NAME}"/plotly"

## Runtime environment constants
LOG_MAINCLASS="minerful.MinerFulLogMakerStarter"
ERROR_MAINCLASS="minerful.MinerFulErrorInjectedLogMakerStarter"
JANUS_CHECK_MAINCLASS="minerful.JanusModelCheckStarter"

# Input Model
MODEL_ENCODING="json"
MODEL=${TEST_FOLDER}/${TEST_BASE_NAME}"-model.json"

## Log generation settings
MIN_STRLEN=10
MAX_STRLEN=100
TESTBED_SIZE=100
MEMORY_MAX="2048m"
LOG_ENCODING="strings"
TEMP_TEXT_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}"-log-original.txt"
CLEAN_TEMP_TEXT_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}"-log-original-clean.txt"
ORIGINAL_GENERATED_LOG=${TEST_FOLDER}/${TEST_BASE_NAME}"-original-log.txt"

## model checking settings
OUTPUT_CHECK_CSV=${TEST_FOLDER}/${TEST_BASE_NAME}"-output.csv"
OUTPUT_CHECK_JSON=${TEST_FOLDER}/${TEST_BASE_NAME}"-output.json"
NaN_LOG="-nanLogSkip"
#NaN_LOG=""
DETAILS_LEVEL="aggregated"

## error injection settings
TARGET_CHAR=a # Just one task at the time
ERROR_PERCENTAGE=10
ERROR_POLICY="string"
# policy for the distribution of the errors. Possible values are:
#      'collection'
#      to spread the errors over the whole collection of traces [DEFAULT];
#      'string'
#      to inject the errors in every trace
ERROR_TYPE="white"
#type of the errors to inject. Possible values are:
#      'ins'
#      suppression of the target task;
#      'del'
#      insertion of the target task;
#      'insdel'
#      mixed (suppressions or insertions, as decided by random) [DEFAULT]
ERROR_LOG=${TEST_FOLDER}/${TEST_BASE_NAME}"-error-log.txt"

# Test variables
ITERATIONS=10

##################################################################
## injection error cycle
#for EXCLUDED in "a" "b" "c" "d" "ef" "gh" "il" "mn" "op" "qr" "st"
for EXCLUDED in "a" "b" "c" "d" "ef" "gh" "il" "mn" "op" "qr" "st" "uv" "wx" "yz" "jk" "ü§"; do

  echo "########### Error-injection cycle"

  for ITERATION in $(seq 1 ${ITERATIONS}); do
    #for ERROR_PERCENTAGE in 0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0
    for ERROR_PERCENTAGE in 0 10 20 30 40 50 60 70 80 90 100; do
      echo "########### exclusion:"${EXCLUDED}" error-percent:"${ERROR_PERCENTAGE}" ----------iteration:"${ITERATION}
      ##################################################################
      ## error injection
      # white-noise injection
      ERROR_LOG=${TEST_FOLDER}/${TEST_BASE_NAME}/${TEST_BASE_NAME}-log-${EXCLUDED}[err-${ERROR_PERCENTAGE}].txt
      python3 pySupport/white-noise.py ${ORIGINAL_GENERATED_LOG} ${ERROR_PERCENTAGE} ${EXCLUDED} ${ERROR_LOG}

      ##################################################################
      ## retrieve measures
      echo "######################### Conformance Checking"
      #	 check measures with janus
      java -cp Janus-NEU.jar minerful.JanusModelCheckStarter -iLF $ERROR_LOG -iLE $LOG_ENCODING -iMF $MODEL -iME $MODEL_ENCODING -oCSV $OUTPUT_CHECK_CSV -oJSON $OUTPUT_CHECK_JSON -d none $NaN_LOG -detailsLevel $DETAILS_LEVEL

      # save result
      echo "########### Post Processing"
      python3 pySupport/singleAggregationNeuCSV.py $OUTPUT_CHECK_JSON"NeuLogMeasures.json" $OUTPUT_CHECK_JSON"NeuLogMeasures[MEAN]_"${ITERATION}"_"${ERROR_PERCENTAGE}".csv"

    done
  done

  ##################################################################
  ## Plot results
  echo "########### Plot results"
  /home/alessio/Data/Phd/my_code/PyVEnv/pySupport/bin/python pySupport/error_injection_plots.py $OUTPUT_CHECK_JSON"NeuLogMeasures[MEAN]_" $ITERATIONS ${TEST_FOLDER}/${TEST_BASE_NAME}"/" $EXCLUDED $ERROR_TYPE

done

##################################################################
## Cleaning
rm ${TEST_FOLDER}/${TEST_BASE_NAME}"-output"*
rm ${TEST_FOLDER}/${TEST_BASE_NAME}"/"*.txt
