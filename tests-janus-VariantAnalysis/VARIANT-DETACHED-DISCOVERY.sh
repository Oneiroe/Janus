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

## Janus discovery
#JAVA_BIN=java
#DISCOVERY_JAR="Janus.jar"
#JAVA_BIN="/home/alessio/Software/jdk/jdk-11.0.10/bin/java" # ok also for janus
#DISCOVERY_MAINCLASS="minerful.JanusOfflineMinerStarter"
#DISCOVERY_SUPPORT=0.00 # support threshold used for the initial discovery of the constraints of the variances
#DISCOVERY_CONFIDENCE=0.8 # confidence threshold used for the initial discovery of the constraints of the variances
## MINERful discovery
JAVA_BIN="/home/alessio/Software/jdk/jdk-11.0.10/bin/java"
DISCOVERY_JAR="/home/alessio/Data/Phd/code_3rd_party/MINERful/MINERful.jar"
DISCOVERY_MAINCLASS="minerful.MinerFulMinerStarter"
DISCOVERY_SUPPORT=0.80 # support threshold used for the initial discovery of the constraints of the variances
DISCOVERY_CONFIDENCE=0.0 # confidence threshold used for the initial discovery of the constraints of the variances

# Variant parameters
#LOG_VAR_1=${TEST_FOLDER}/"Sepsis(ageAbove70).xes"
#LOG_VAR_2=${TEST_FOLDER}/"Sepsis(ageUnder35).xes"
#VARIANT_RESULTS_CSV=${TEST_FOLDER}/"Sepsis-result-simplified.csv"
#LOG_VAR_1=${TEST_FOLDER}/"BPIC13_incidents_orgline_A2.xes"
#LOG_VAR_2=${TEST_FOLDER}/"BPIC13_incidents_orgline_C.xes"
#VARIANT_RESULTS_CSV=${TEST_FOLDER}/"BPIC13-result-simplified.csv"
LOG_VAR_1=${TEST_FOLDER}/"BPIC15_1.xes"
LOG_VAR_2=${TEST_FOLDER}/"BPIC15_2.xes"
VARIANT_RESULTS_CSV=${TEST_FOLDER}/"BPIC15-result-simplified.csv"
#LOG_VAR_1=${TEST_FOLDER}/"BPIC15_1f.xes"
#LOG_VAR_2=${TEST_FOLDER}/"BPIC15_2f.xes"
#VARIANT_RESULTS_CSV=${TEST_FOLDER}/"BPIC15f-result-simplified.csv"
#LOG_VAR_1=${TEST_FOLDER}/"Road_teraffic_fineamount_Above_and_equal50.xes"
#LOG_VAR_2=${TEST_FOLDER}/"Road_teraffic_fineamount_Below50.xes"
#VARIANT_RESULTS_CSV=${TEST_FOLDER}/"Road_teraffic-result-simplified.csv"

INPUT_MODEL_JSON_1=${LOG_VAR_1}"-model.json"
INPUT_MODEL_CSV_1=${LOG_VAR_1}"-model.csv"
INPUT_MODEL_JSON_2=${LOG_VAR_2}"-model.json"
INPUT_MODEL_CSV_2=${LOG_VAR_2}"-model.csv"
INPUT_MODEL_ENCODING="JSON"

LOGS_ENCODING="xes"
MEASURE="Confidence"  # decide the measure to use for the analysis
#MEASURE_THRESHOLD=0.8 # NOT YET USED in the software
P_VALUE=0.01 # p_value threshold to consider a difference statistically relevant
PERMUTATIONS=1000 # number of permutations of the permutation test
#NaN_LOG="-nanLogSkip" # NOT YET exposed in input
#NaN_LOG=""
#DIFFERENCE_POLICY="absolute" # {"absolute", "distinct"} # NOT YET IMPLEMENTED decide if considering the ABSOLUTE distance between the results or keep the DISTINCT sign/orientation of the relations, i.e., to keep the sign of the difference
DIFFERENCE_THRESHOLD=0.01 # minimum difference between a rule in the two variance to be considered relevant

#-oKeep keep values below the p-valu and difference threshold
#-simplify remove constraints that are redundant

#--no-screen-print-out prehevent the discovery component to print in terminal
##################################################################
# DISCOVERY
$JAVA_BIN -cp $DISCOVERY_JAR $DISCOVERY_MAINCLASS \
  -iLF $LOG_VAR_1\
  -iLE $LOGS_ENCODING \
  -oJSON $INPUT_MODEL_JSON_1 \
  -oCSV $INPUT_MODEL_CSV_1 \
  -s $DISCOVERY_SUPPORT \
  -c $DISCOVERY_CONFIDENCE \
  -vShush true \
  --no-screen-print-out

$JAVA_BIN -cp $DISCOVERY_JAR $DISCOVERY_MAINCLASS \
  -iLF $LOG_VAR_2\
  -iLE $LOGS_ENCODING \
  -oJSON $INPUT_MODEL_JSON_2 \
  -oCSV $INPUT_MODEL_CSV_2 \
  -s $DISCOVERY_SUPPORT \
  -c $DISCOVERY_CONFIDENCE \
  -vShush true \
  --no-screen-print-out

# VARIANT
java -cp Janus.jar $JANUS_VARIANT_MAINCLASS \
  -iLE1 $LOGS_ENCODING \
  -iLF1 $LOG_VAR_1 \
  -iLE2 $LOGS_ENCODING \
  -iLF2 $LOG_VAR_2 \
  -measure $MEASURE \
  -pValue $P_VALUE \
  -permutations $PERMUTATIONS \
  -oCSV $VARIANT_RESULTS_CSV \
  --no-screen-print-out \
  -simplify \
  -differenceThreshold $DIFFERENCE_THRESHOLD \
  -iMF1 $INPUT_MODEL_JSON_1\
  -iMF2 $INPUT_MODEL_JSON_2\
  -iME1 $INPUT_MODEL_ENCODING \
  -iME2 $INPUT_MODEL_ENCODING
#  -oKeep

##################################################################
#Change ; with ,
#sed -e 's/,/§/g'
