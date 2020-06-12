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
TEST_BASE_NAME="PERFORMANCE-SYNTHETIC"

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
TESTBED_SIZE=500
MEMORY_MAX="12048m"
LOG_ENCODING="strings"
TEMP_TEXT_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}"-log-original.txt"
ORIGINAL_GENERATED_LOG=${TEST_FOLDER}/${TEST_BASE_NAME}/${TEST_BASE_NAME}"-log[min_${MIN_STRLEN}_max_${MAX_STRLEN}_size_${TESTBED_SIZE}].txt"

## model checking settings
OUTPUT_CHECK_CSV=${TEST_FOLDER}/${TEST_BASE_NAME}"-output.csv"
OUTPUT_CHECK_JSON=${TEST_FOLDER}/${TEST_BASE_NAME}"-output.json"

# LOGS vars
LOGS_FOLDER=${TEST_FOLDER}/${TEST_BASE_NAME}/
TIME_RECORDS=${TEST_FOLDER}/${TEST_BASE_NAME}"-TIMES.csv"
SUPPORT=0.05
CONFIDENCE=0.8


for MAX_STRLEN in {0..1000000..500}; do
    ###################################################################
    ## Generate log
    MIN_STRLEN=$((MAX_STRLEN-500))
    echo "########### Generate Log"
    ORIGINAL_GENERATED_LOG=${TEST_FOLDER}/${TEST_BASE_NAME}/${TEST_BASE_NAME}"-log[min_${MIN_STRLEN}_max_${MAX_STRLEN}_size_${TESTBED_SIZE}].txt"
    if test -f ${ORIGINAL_GENERATED_LOG}; then
        continue
    fi
    ### GENERATE LOG with MinerFulLogMakerStarter ****
    java -Xmx$MEMORY_MAX -cp Janus.jar $LOG_MAINCLASS \
        --input-model-file $MODEL \
        --input-model-encoding $MODEL_ENCODING  \
        --size $TESTBED_SIZE \
        --minlen $MIN_STRLEN \
        --maxlen $MAX_STRLEN \
        --out-log-encoding $LOG_ENCODING \
        --out-log-file $TEMP_TEXT_FILE

    # remove the unwanted characters to make it readable in input by Janus
    python ${TEST_FOLDER}/cleanStringLog.py $TEMP_TEXT_FILE $ORIGINAL_GENERATED_LOG
    rm $TEMP_TEXT_FILE


    ##################################################################
    echo "######################### Discover Missing Models"
    SECONDS=0
    # BPICs discover models
#    for LOG in ${LOGS_FOLDER}*.txt
#    do
    LOG=${ORIGINAL_GENERATED_LOG}
    echo ${LOG}
#    Skip if model already exists
    if test -f ${LOG}-model[s_${SUPPORT}_c_${CONFIDENCE}].json; then
        continue
    fi
    START=$SECONDS

    java -Xmx$MEMORY_MAX -cp Janus.jar $JANUS_MINER_MAINCLASS \
     -iLF $LOG \
     -iLE ${LOG_ENCODING} \
     -s $SUPPORT -c $CONFIDENCE \
     -oJSON ${LOG}-model[s_${SUPPORT}_c_${CONFIDENCE}].json \
     -vShush true

    DURATION=$((SECONDS-START))
    echo ${LOG}";discovery;"$DURATION >> ${TIME_RECORDS}
#    done

    ### BPICs check models
    echo "######################### Conformance Checking"
    SECONDS=0
    # BPICs discover models
#    for LOG in ${LOGS_FOLDER}*.txt
#    do
#        echo ${LOG}

    START=$SECONDS

    java -Xmx$MEMORY_MAX -cp Janus.jar $JANUS_CHECK_MAINCLASS \
    -iLF $LOG \
    -iLE ${LOG_ENCODING} \
    -iMF ${LOG}-model[s_${SUPPORT}_c_${CONFIDENCE}].json \
    -iME ${MODEL_ENCODING}
#    -oCSV $OUTPUT_CHECK_CSV \
#    -oJSON $OUTPUT_CHECK_JSON \

    DURATION=$((SECONDS-START))
    echo ${DURATION}s
    echo ${LOG}";check;"${MIN_STRLEN}";"${MAX_STRLEN}";"${TESTBED_SIZE}";"${DURATION}";"${MEMORY_MAX} >> ${TIME_RECORDS}
#    done
###################################################################
done

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

###################################################################
### Cleaning
##rm ${TEST_FOLDER}/${TEST_BASE_NAME}"-output"*
