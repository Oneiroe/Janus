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
TEST_FOLDER="./tests-Journal-SJ2T"
TEST_BASE_NAME="GROUND-TRUTH-NEU-SEPSIS"

## Runtime environment constants
LOG_MAINCLASS="minerful.MinerFulLogMakerStarter"
JANUS_CHECK_MAINCLASS="minerful.JanusModelCheckStarter"
ERROR_MAINCLASS="minerful.MinerFulErrorInjectedLogMakerStarter"
JANUS_MINER_MAINCLASS="minerful.JanusOfflineMinerStarter"
MINERFUL_SIMPLIFICATION_MAINCLASS="minerful.MinerFulSimplificationStarter"

# Input Model
MODEL_ENCODING="json"
ORIGINAL_MODEL=${TEST_FOLDER}/${TEST_BASE_NAME}"-model.json"

mkdir ${TEST_FOLDER}"/"${TEST_BASE_NAME}
TEST_FOLDER=${TEST_FOLDER}"/"${TEST_BASE_NAME}

## Input LogLog generation settings
LOG_FILE=logs/SEPSIS.xes
LOG_ENCODING="xes"

## model checking settings
NaN_LOG="-nanLogSkip"
#NaN_LOG=""
OUTPUT_CHECK_CSV=${TEST_FOLDER}/${TEST_BASE_NAME}"-output"${NaN_LOG}".csv"
OUTPUT_CHECK_JSON=${TEST_FOLDER}/${TEST_BASE_NAME}"-output"${NaN_LOG}".json"
#-encodeTasksFlag Flag if the output tasks/events should be encoded
#-nanLogSkip Flag to skip or not NaN values when computing log measures
#-nanTraceSubstitute Flag to substitute or not the NaN values when computing trace measures
#-nanTraceValue <number> Value to be substituted to NaN values in trace measures
DETAILS_LEVEL="aggregated"

## Discovery settings
SUPPORT=0.05
CONFIDENCE=0.5
OUTPUT_MODEL_JSON=${TEST_FOLDER}/${TEST_BASE_NAME}"-model-DISCOVERED[s_${SUPPORT}_c_${CONFIDENCE}].json"
OUTPUT_MODEL_CSV=${TEST_FOLDER}/${TEST_BASE_NAME}"-model-DISCOVERED[s_${SUPPORT}_c_${CONFIDENCE}].csv"
SIMPLE_MODEL_JSON=${TEST_FOLDER}/${TEST_BASE_NAME}"-model-SIMPLIFIED[s_${SUPPORT}_c_${CONFIDENCE}].json"
SIMPLE_MODEL_CSV=${TEST_FOLDER}/${TEST_BASE_NAME}"-model-SIMPLIFIED[s_${SUPPORT}_c_${CONFIDENCE}].csv"

# Test variables
ITERATIONS=10
#BEST_N=100
#MEASURES_RANKING_CSV=${TEST_FOLDER}/${TEST_BASE_NAME}"-measures-ranking[top"${BEST_N}${NaN_LOG}"].csv"
MEMORY_MAX="2048m"

##################################################################
# 0 input model M

# 2 mine very loose model out of L -> C
if ! test -f ${OUTPUT_MODEL_JSON}; then
  echo "########### Model mining"
  java -Xmx$MEMORY_MAX -cp Janus-NEU.jar $JANUS_MINER_MAINCLASS \
    -iLF ${LOG_FILE} \
    -iLE ${LOG_ENCODING} \
    -s $SUPPORT -c $CONFIDENCE \
    -oJSON ${OUTPUT_MODEL_JSON} \
    -oCSV ${OUTPUT_MODEL_CSV} \
    -vShush true
  #   2.1 remove/mark constraints (only hierarchically?) derived from M, otherwise the experiment is compromised
  # -prune,--prune-with <type>                            type of post-processing analysis over constraints. It can be one of the following:
  #                                                       {'none','hierarchy','hierarchyconflict','hierarchyconflictredundancy','hierarchyconflictredundancydouble'
  #                                                       }.
  #                                                       Default is: 'hierarchy'
  # -pruneRnk,--prune-ranking-by <policy>                 type of ranking of constraints for post-processing analysis. It can be a :-separated list of the
  #                                                       following:
  #                                                       {'supportconfidenceinterestfactor','familyhierarchy','activationtargetbonds','default','random'}.
  #                                                       Default is: 'activationtargetbonds:familyhierarchy:supportconfidenceinterestfactor'
  # -keepModel <file>   use a reference model to prune out all its derived constraints
  java -cp Janus-NEU.jar $MINERFUL_SIMPLIFICATION_MAINCLASS -iMF $OUTPUT_MODEL_JSON -iME $MODEL_ENCODING -oCSV $SIMPLE_MODEL_CSV -oJSON $SIMPLE_MODEL_JSON -s 0 -c 0 -i 0 -prune hierarchyconflictredundancydouble -keepModel $ORIGINAL_MODEL
#  SIMPLE_MODEL_JSON=$OUTPUT_MODEL_JSON
else
  echo "Model already existing: "${OUTPUT_MODEL_JSON}
fi

# check measures of C with janus
echo "########### SJ2T Check"
java -cp Janus-NEU.jar $JANUS_CHECK_MAINCLASS -iLF $LOG_FILE -iLE $LOG_ENCODING -iMF $SIMPLE_MODEL_JSON -iME $MODEL_ENCODING -oCSV $OUTPUT_CHECK_CSV -oJSON $OUTPUT_CHECK_JSON $NaN_LOG -detailsLevel $DETAILS_LEVEL
# generate MEAN-only CSV of aggregated measures
echo "########### Post Processing"
python pySupport/singleAggregationNeuCSV.py $OUTPUT_CHECK_JSON"NeuLogMeasures.json" $OUTPUT_CHECK_JSON"NeuLogMeasures[MEAN].csv"
echo "MEAN-only aggregated CSV saved in "$OUTPUT_CHECK_JSON"NeuLogMeasures[MEAN].csv"

# 3 for each measure:
#   3.1 rank constraints of C
#   3.2 count how many constraint of M are in the first N constraints of the rank
# 4 rank measure according to 3.2
for BEST_N in 1 5 10 25 50 100 200 500 1000; do
    MEASURES_RANKING_CSV=${TEST_FOLDER}/${TEST_BASE_NAME}"-measures-ranking[top"${BEST_N}${NaN_LOG}"].csv"
    python3 pySupport/measuresRanking.py ${ORIGINAL_MODEL} $OUTPUT_CHECK_JSON"NeuLogMeasures[MEAN].csv" ${BEST_N} ${MEASURES_RANKING_CSV}
done
MEASURES_AVERAGE_RANKING_TOT_CSV=${TEST_FOLDER}/${TEST_BASE_NAME}"-measures-ranking[TOT"${NaN_LOG}"].csv"
python3 pySupport/measuresRanking.py ${TEST_FOLDER} ${MEASURES_AVERAGE_RANKING_TOT_CSV}

# 5 (in paper) compare results with Le&Lo considering that they where only looking at Resp/Prec constraints
