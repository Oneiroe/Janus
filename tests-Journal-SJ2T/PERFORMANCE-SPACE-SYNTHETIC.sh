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
TEST_FOLDER="./tests-Journal-SJ2T"
TEST_BASE_NAME="PERFORMANCE-SPACE-SYNTHETIC"

mkdir ${TEST_FOLDER}"/"${TEST_BASE_NAME}
TEST_BASE_FOLDER=${TEST_FOLDER}"/"${TEST_BASE_NAME}
#TEST_FOLDER=${TEST_FOLDER}"/"${TEST_BASE_NAME}


## Runtime environment constants
LOG_MAINCLASS="minerful.MinerFulLogMakerStarter"
ERROR_MAINCLASS="minerful.MinerFulErrorInjectedLogMakerStarter"
JANUS_CHECK_MAINCLASS="minerful.JanusModelCheckStarter"
JANUS_MINER_MAINCLASS="minerful.JanusOfflineMinerStarter"

# Input Model
MODEL_ENCODING="json"
MODEL=${TEST_FOLDER}/${TEST_BASE_NAME}"-model.json"

## Log generation settings
MIN_STRLEN=15
MAX_STRLEN=15
TESTBED_SIZE=1000
MEMORY_MAX="15048m"
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

ITERATIONS=1

    echo "######################### Conformance Checking"

#LOG="logs/SEPSIS.xes"
#LOG_ENCODING="xes"
#IMF="tests-SJ2T/USE-CASE/SEPSIS.xes-model[hand-made].json"
#    java -Xmx$MEMORY_MAX -cp Janus-SPACE-SYNTH.jar $JANUS_CHECK_MAINCLASS \
#    -iLF ${LOG} \
#    -iLE ${LOG_ENCODING} \
#    -iMF ${IMF} \
#    -iME ${MODEL_ENCODING}
##    -oCSV $OUTPUT_CHECK_CSV \
##    -oJSON $OUTPUT_CHECK_JSON \
##    -iMF ${LOG}-model[s_${SUPPORT}_c_${CONFIDENCE}].json \

for ITERATION in $(seq 1 ${ITERATIONS}); do
for MAX_STRLEN in {15..685000..5}; do
#for TESTBED_SIZE in {1000..45000..250}; do
#    echo "########### Log size:"${TESTBED_SIZE}" ----------------iteration:"${ITERATION}
    echo "########### Trace size:"${MAX_STRLEN}" ----------------iteration:"${ITERATION}
    ###################################################################
    ## Generate log
    MIN_STRLEN=$((MAX_STRLEN))
    echo "########### Generate Log"
    ORIGINAL_GENERATED_LOG=${TEST_FOLDER}/${TEST_BASE_NAME}/${TEST_BASE_NAME}"-log[min_${MIN_STRLEN}_max_${MAX_STRLEN}_size_${TESTBED_SIZE}].txt"
    if ! test -f ${ORIGINAL_GENERATED_LOG}; then
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
        python3 pySupport/cleanStringLog.py $TEMP_TEXT_FILE $ORIGINAL_GENERATED_LOG
        rm $TEMP_TEXT_FILE
    fi


    ##################################################################
#    echo "######################### Discover Missing Models"
#    SECONDS=0
#    # BPICs discover models
##    for LOG in ${LOGS_FOLDER}*.txt
##    do
#    LOG=${ORIGINAL_GENERATED_LOG}
#    echo ${LOG}
##    Skip if model already exists
#    if test -f ${LOG}-model[s_${SUPPORT}_c_${CONFIDENCE}].json; then
#        continue
#    fi
#    START=$SECONDS
#
#    java -Xmx$MEMORY_MAX -cp Janus.jar $JANUS_MINER_MAINCLASS \
#     -iLF $LOG \
#     -iLE ${LOG_ENCODING} \
#     -s $SUPPORT -c $CONFIDENCE \
#     -oJSON ${LOG}-model[s_${SUPPORT}_c_${CONFIDENCE}].json \
#     -vShush true
#
#    DURATION=$((SECONDS-START))
#    echo ${LOG}";discovery;"$DURATION >> ${TIME_RECORDS}
#    done

    ### BPICs check models
    echo "######################### Conformance Checking"
    SECONDS=0
    # BPICs discover models
#    for LOG in ${LOGS_FOLDER}*.txt
#    do
#        echo ${LOG}

    java -Xmx$MEMORY_MAX -cp Janus-SPACE-SYNTH.jar $JANUS_CHECK_MAINCLASS \
    -iLF ${ORIGINAL_GENERATED_LOG} \
    -iLE ${LOG_ENCODING} \
    -iMF ${MODEL} \
    -iME ${MODEL_ENCODING}
#    -oCSV $OUTPUT_CHECK_CSV \
#    -oJSON $OUTPUT_CHECK_JSON \
#    -iMF ${LOG}-model[s_${SUPPORT}_c_${CONFIDENCE}].json \

#    echo ${ORIGINAL_GENERATED_LOG}";check;"${MIN_STRLEN}";"${MAX_STRLEN}";"${TESTBED_SIZE}";"${DURATION}";"${MEMORY_MAX} >> ${TIME_RECORDS}
#    done
###################################################################
done
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

