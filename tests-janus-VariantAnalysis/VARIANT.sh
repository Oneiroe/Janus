#!/bin/bash

# Author:       Alessio Cecconi
# Date:         2021/02/03
# Description:  This script launches the JanusVariantAnalysisStarter
#               Run this launcher with "-h" to understand the meaning of options you can pass.

## Import the shell functions to create Regular Expressions expressing constraints
#. ./constraintsFunctions.cfg

## Clean up the screen
clear

##################################################################
##################################################################
## script variables
TEST_FOLDER="./tests-janus-VariantAnalysis"
#TEST_BASE_NAME="VARIANT"
#
#mkdir ${TEST_FOLDER}"/"${TEST_BASE_NAME}
#TEST_FOLDER=${TEST_FOLDER}"/"${TEST_BASE_NAME}

## Runtime environment constants
JANUS_VARIANT_MAINCLASS="minerful.JanusVariantAnalysisStarter"

# Variant parameters
LOG_VAR_1=${TEST_FOLDER}/"Sepsis(ageAbove70).xes"
LOG_VAR_2=${TEST_FOLDER}/"Sepsis(ageUnder35).xes"
VARIANT_RESULTS_CSV=${TEST_FOLDER}/"Sepsis-result-simplified.csv"
#LOG_VAR_1=${TEST_FOLDER}/"BPIC13_incidents_orgline_A2.xes"
#LOG_VAR_2=${TEST_FOLDER}/"BPIC13_incidents_orgline_C.xes"
#VARIANT_RESULTS_CSV=${TEST_FOLDER}/"BPIC13-result.csv"
#LOG_VAR_1=${TEST_FOLDER}/"BPIC15_1.xes"
#LOG_VAR_2=${TEST_FOLDER}/"BPIC15_2.xes"
#VARIANT_RESULTS_CSV=${TEST_FOLDER}/"BPIC15-result.csv"
#LOG_VAR_1=${TEST_FOLDER}/"Road_teraffic_fineamount_Above_and_equal50.xes"
#LOG_VAR_2=${TEST_FOLDER}/"Road_teraffic_fineamount_Below50.xes"
#VARIANT_RESULTS_CSV=${TEST_FOLDER}/"Road_teraffic-result.csv"

MODEL_JSON_1=${LOG_VAR_1}"-model.json"
MODEL_CSV_1=${LOG_VAR_1}"-model.csv"
MODEL_JSON_2=${LOG_VAR_2}"-model.json"
MODEL_CSV_2=${LOG_VAR_2}"-model.csv"

LOGS_ENCODING="xes"
MEASURE="Confidence"  # decide the measure to use for the analysis
MEASURE_THRESHOLD=0.8 # NOT YET USED in the software
P_VALUE=0.01
PERMUTATIONS=1000
#NaN_LOG="-nanLogSkip"
#NaN_LOG=""
#DIFFERENCE_POLICY="absolute" # {"absolute", "distinct"} decide if considering the ABSOLUTE distance between the results or keep the DISTINCT sign/orientation of the relations, i.e., to keep the sign of the difference
DISCOVERY_SUPPORT=0.00 # support threshold used for the initial discovery of the constraints of the variances
DISCOVERY_CONFIDENCE=0.8 # confidence threshold used for the initial discovery of the constraints of the variances
DIFFERENCE_THRESHOLD=0.01

##################################################################
java -cp Janus.jar $JANUS_VARIANT_MAINCLASS \
  -iLE1 $LOGS_ENCODING \
  -iLF1 $LOG_VAR_1 \
  -iLE2 $LOGS_ENCODING \
  -iLF2 $LOG_VAR_2 \
  -measure $MEASURE \
  -measureThreshold $MEASURE_THRESHOLD \
  -pValue $P_VALUE \
  -permutations $PERMUTATIONS \
  -oKeep \
  -oCSV $VARIANT_RESULTS_CSV \
  -s $DISCOVERY_SUPPORT \
  -c $DISCOVERY_CONFIDENCE \
  -oModel1CSV $MODEL_CSV_1 \
  -oModel1JSON $MODEL_JSON_1\
  -oModel2CSV $MODEL_CSV_2 \
  -oModel2JSON $MODEL_JSON_2 \
  --no-screen-print-out \
  -simplify \
  -differenceThreshold $DIFFERENCE_THRESHOLD

##################################################################
#Change ; with ,
#sed -e 's/,/§/g'
