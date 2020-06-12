#!/bin/bash

# Author:       Alessio Cecconi
# Date:         2020/05/25
# Description:  This script launches the janus model checker on a set of generated log and BPIC logs to measure its performances
#               Run this launcher with "-h" to understand the meaning of options you can pass.


## Import the shell functions to create Regular Expressions expressing constraints
#. ./constraintsFunctions.cfg

## Clean up the screen
clear

##################################################################
##################################################################
## script variables
TEST_FOLDER="./tests-SJ2T"
TEST_BASE_NAME="PERFORMANCE-BPIC"

## Runtime environment constants
LOG_MAINCLASS="minerful.MinerFulLogMakerStarter"
ERROR_MAINCLASS="minerful.MinerFulErrorInjectedLogMakerStarter"
JANUS_CHECK_MAINCLASS="minerful.JanusModelCheckStarter"
JANUS_MINER_MAINCLASS="minerful.JanusOfflineMinerStarter"

# Input Model
MODEL_ENCODING="json"
MODEL=${TEST_FOLDER}/${TEST_BASE_NAME}"-model.json"

## Log generation settings
MIN_STRLEN=10
MAX_STRLEN=100
TESTBED_SIZE=100
#MEMORY_MAX="2048m"
MEMORY_MAX="15048m"
LOG_ENCODING="strings"
TEMP_TEXT_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}"-log-original.txt"
CLEAN_TEMP_TEXT_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}"-log-original-clean.txt"
ORIGINAL_GENERATED_LOG=${TEST_FOLDER}/${TEST_BASE_NAME}"-original-log.txt"

## model checking settings
OUTPUT_CHECK_CSV=${TEST_FOLDER}/${TEST_BASE_NAME}"-output.csv"
OUTPUT_CHECK_JSON=${TEST_FOLDER}/${TEST_BASE_NAME}"-output.json"

## error injection settings
TARGET_CHAR=a # Just one task at the time
ERROR_PERCENTAGE=10
ERROR_POLICY="collection"
ERROR_TYPE="ins"
ERROR_LOG=${TEST_FOLDER}/${TEST_BASE_NAME}"-error-log.txt"

# BPICs vars
BPIC_LOGS_FOLDER="./logs-only-events"
BPIC_LOGS_ENCODING="xes"
BPIC_MODEL_ENCODING="json"
SUPPORT=0.05
CONFIDENCE=0.8

BPIC_TIME_RECORDS=${TEST_FOLDER}/${TEST_BASE_NAME}"-TIMES.csv"

# Test variables
ITERATIONS=10

##################################################################
#### BPICs tests
echo "######################### Discover Missing Models"
SECONDS=0
# BPICs discover models
for LOG in ${BPIC_LOGS_FOLDER}/*.xes
do
    echo ${LOG}
#    Skip if model already exists
    if test -f ${LOG}-model[s_${SUPPORT}_c_${CONFIDENCE}].json; then
        continue
    fi
    START=$SECONDS

    java -Xmx$MEMORY_MAX -cp Janus.jar $JANUS_MINER_MAINCLASS \
     -iLF $LOG \
     -iLE ${BPIC_LOGS_ENCODING} \
     -s $SUPPORT -c $CONFIDENCE \
     -oJSON ${LOG}-model[s_${SUPPORT}_c_${CONFIDENCE}].json \
     -vShush true

    DURATION=$((SECONDS-START))
    echo ${LOG}";discovery;"${DURATION} >> ${BPIC_TIME_RECORDS}
done

### BPICs check models
for ITERATION in $(seq 1 ${ITERATIONS}); do
echo "######################### Conformance Checking------ Iteration:"${ITERATION}
SECONDS=0
# BPICs discover models
for LOG in ${BPIC_LOGS_FOLDER}/*.xes
do
    echo ${LOG}

    START=$SECONDS

    java -Xmx$MEMORY_MAX -cp Janus.jar $JANUS_CHECK_MAINCLASS \
    -iLF $LOG \
    -iLE $BPIC_LOGS_ENCODING \
    -iMF ${LOG}-model[s_${SUPPORT}_c_${CONFIDENCE}].json \
    -iME $BPIC_MODEL_ENCODING
#    -oCSV $OUTPUT_CHECK_CSV \
#    -oJSON $OUTPUT_CHECK_JSON \

    DURATION=$((SECONDS-START))
    echo ${DURATION}s
    echo ${LOG}";check;"${DURATION}";"${MEMORY_MAX} >> ${BPIC_TIME_RECORDS}
done
done
###################################################################
###################################################################
## Generate log
#echo "########### Generate Log"
#### GENERATE LOG with MinerFulLogMakerStarter ****
#java -Xmx$MEMORY_MAX -cp Janus.jar $LOG_MAINCLASS \
#    --input-model-file $MODEL \
#    --input-model-encoding $MODEL_ENCODING  \
#    --size $TESTBED_SIZE \
#    --minlen $MIN_STRLEN \
#    --maxlen $MAX_STRLEN \
#    --out-log-encoding $LOG_ENCODING \
#    --out-log-file $TEMP_TEXT_FILE
#
## remove the unwanted characters to make it readable in input by Janus
#python ${TEST_FOLDER}/cleanStringLog.py $TEMP_TEXT_FILE $ORIGINAL_GENERATED_LOG
#rm $TEMP_TEXT_FILE
#
## check measures with janus
#echo "##### SJ2T Check original log"
#java -cp Janus.jar minerful.JanusModelCheckStarter -iLF $ORIGINAL_GENERATED_LOG -iLE $LOG_ENCODING -iMF $MODEL -iME $MODEL_ENCODING -oCSV $OUTPUT_CHECK_CSV -oJSON $OUTPUT_CHECK_JSON
#
## save result
## generate MEAN-only CSV of aggregated measures
#echo "########### Post Processing"
#python pySupport/singleAggregationPerspectiveFocusCSV.py $OUTPUT_CHECK_JSON"AggregatedMeasures.json" $OUTPUT_CHECK_JSON"AggregatedMeasures[MEAN]_0.csv"
#
#
###################################################################
### injection error cycle
#for TARGET_CHAR in   "a" "b" "c" "d" "e" "f" "g" "h" "i" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "z"
#do
###################################################################
### injection error cycle single target
#echo "########### Error-injection cycle"
#for ERROR_PERCENTAGE in 10 20 30 40 50 60 70 80 90 100
#do
#    # Error injection
#    echo "### Error-injection level:"$ERROR_PERCENTAGE
#    java -Xmx$MEMORY_MAX -cp Janus.jar $ERROR_MAINCLASS \
#    -iLF $ORIGINAL_GENERATED_LOG \
#    -iLE $LOG_ENCODING \
#    --err-target $TARGET_CHAR \
#    --err-out-log $ERROR_LOG \
#    --err-percentage $ERROR_PERCENTAGE \
#    --err-spread-policy $ERROR_POLICY \
#    --err-type $ERROR_TYPE \
#    -iME $MODEL_ENCODING \
#    -iMF $MODEL
#
#    # check measures with janus
#    echo "### SJ2T Check"
#    java -cp Janus.jar minerful.JanusModelCheckStarter -iLF $ERROR_LOG -iLE $LOG_ENCODING -iMF $MODEL -iME $MODEL_ENCODING -oCSV $OUTPUT_CHECK_CSV -oJSON $OUTPUT_CHECK_JSON
#
#    # save result
#    echo "########### Post Processing"
#    python pySupport/singleAggregationPerspectiveFocusCSV.py $OUTPUT_CHECK_JSON"AggregatedMeasures.json" $OUTPUT_CHECK_JSON"AggregatedMeasures[MEAN]_"${ERROR_PERCENTAGE}".csv"
#
#done
#
###################################################################
### Plot results
#echo "########### Plot results"
#/home/alessio/Data/Phd/my_code/PyVEnv/pySupport/bin/python pySupport/error_injection_plots.py $OUTPUT_CHECK_JSON"AggregatedMeasures[MEAN]_" $TARGET_CHAR
#
#done
#
###################################################################
### Cleaning
##rm ${TEST_FOLDER}/${TEST_BASE_NAME}"-output"*
