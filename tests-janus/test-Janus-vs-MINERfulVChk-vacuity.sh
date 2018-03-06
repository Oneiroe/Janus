#!/bin/bash

# Author:       Alessio Cecconi
# Date:         2018/03/06
# Description:  This script launches the MinerFulTracesMakerStarter to create synthetic collections of strings (which can be seen as logs): the (main) input is a set of regular expressions (declarative process model constraints) determining the language (the process model) that generate strings (traces).
#               Run this launcher with "-h" to understand the meaning of options you can pass.

### DATASET CREATION

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.MinerFulTracesMakerStarter"
JANUS_MAINCLASS="minerful.MinerFulMinerStarter"
MINERFUL_MAINCLASS="minerful.MinerFulVacuityChecker"

MIN_STRLEN=2
MAX_STRLEN=20
TESTBED_SIZE=1000
MEMORY_MAX="2048m"
OUTPUT_FILE="./tests-janus/test-Janus-vs-MINERfulVChk-vacuity.xes"
INPUT_FILE=$OUTPUT_FILE
#INPUT_FILE="./tests-janus/test-Janus-equal-MINERful.txt -iE strings "
Mf_SUPP=0.8
Mf_CONF=0.8
Mf_INTE=0
J_SUPP=0
J_CONF=1

## Global variables
constraints=(
 `Participation a`
 `AtMostOne a`
 `Init a`
 `End a`
 `RespondedExistence a b`
 `Response a b`
 `AlternateResponse a b`
 `ChainResponse a b`
 `Precedence a b`
 `AlternatePrecedence a b`
 `ChainPrecedence a b`
 `CoExistence a b`
 `Succession a b`
 `AlternateSuccession a b`
 `ChainSuccession a b`
 `NotChainSuccession a b`
 `NotSuccession a b`
 `NotCoExistence a b`
)

alphabetCharacters=("a" "b" "c" "d" "e" "f")

## Auxiliary variable
alphabet=`echo ${alphabetCharacters[@]} | sed 's/ /:/g'`

## Run!
. ./libs.cfg
# we really like the current log as example do not overwrite
#java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $alphabet -m $MIN_STRLEN -M $MAX_STRLEN -L $TESTBED_SIZE -oLF $OUTPUT_FILE -oE "xes" -r `Precedence a b`

### TEST

# MINERful
echo "################### MINERFUL"
#java -jar MINERful.jar "minerful.MinerFulVacuityChecker" -iLF temp/handWritten.txt -iE strings -s 0.95
./run-MINERful.sh -iLF $INPUT_FILE -s $Mf_SUPP -c $Mf_CONF -i $Mf_INTE -condec ./tests-janus/test-Janus-vs-MINERfulVChk-vacuity_MODEL_MINERful.xml -CSV ./tests-janus/test-Janus-vs-MINERfulVChk-vacuity_MODEL_MINERful.csv
java -cp out/artifacts/MINERful_jar/MINERful.jar $MINERFUL_MAINCLASS $INPUT_FILE $Mf_SUPP tests-janus/test-Janus-vs-MINERfulVChk-vacuity_MODEL_MINERfulVChk.xml tests-janus/test-Janus-vs-MINERfulVChk-vacuity_MODEL_MINERfulVChk.csv

# Janus
echo "################### JANUS"
java -jar out/artifacts/MINERful_jar/MINERful.jar $JANUS_MAINCLASS -iLF $INPUT_FILE -s $J_SUPP -c $J_CONF -condec ./tests-janus/test-Janus-vs-MINERfulVChk-vacuity_MODEL_Janus.xml -CSV ./tests-janus/test-Janus-vs-MINERfulVChk-vacuity_MODEL_Janus.csv

### OUTPUT

