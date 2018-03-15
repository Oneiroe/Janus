#!/bin/bash

# Author:       Alessio Cecconi
# Date:         2018/03/06
# Description:  TEST FOR PLOTS


## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

## Clean up the screen
clear

##################################################################
##################################################################
## script variables
TEST_FOLDER="./tests-janus"
TEST_BASE_NAME="test-Janus-sepsis"

## Runtime environment constants
TRACES_MAINCLASS="minerful.MinerFulTracesMakerStarter"
TXT2XES_MAINCLASS="minerful.MinerFulTracesToXesConverterStarter"
JANUS_MAINCLASS="minerful.MinerFulMinerStarter"
MINERFUL_VACUITY_MAINCLASS="minerful.MinerFulVacuityChecker"

## Log generation
MIN_STRLEN=2
MAX_STRLEN=10
TESTBED_SIZE=1000
MEMORY_MAX="2048m"
XES_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}".xes"

## Hand written log
TEXT_FILE=${TEST_FOLDER}/${TEST_BASE_NAME}".txt"

### INPUT LOG
## XES log (generated or converted from hand written one)
INPUT_FILE=${XES_FILE}
## hand written txt
#INPUT_FILE=${TEXT_FILE} -iE strings

## Miners Configs
#
#Mf_SUPP=0.95
#Mf_CONF=0.25
#Mf_INTE=0
#
#MfV_SUPP=${Mf_SUPP}
#MfV_CONF=${Mf_CONF}
#MfV_INTE=${Mf_INTE}
#
#DMM_SUPP=95
#DMM_ALPHA=0
#
#J_SUPP=0.103
#J_CONF=0.947

Mf_SUPP=0
Mf_CONF=0
Mf_INTE=0

MfV_SUPP=${Mf_SUPP}
MfV_CONF=${Mf_CONF}
MfV_INTE=${Mf_INTE}

DMM_SUPP=0
DMM_ALPHA=0

J_SUPP=0
J_CONF=0
##################################################################
## DMM setup
DMM_CONF=${TEST_FOLDER}/"DeclareMiner2-master/config.properties.example"
DMM_PATH=${TEST_FOLDER}/"DeclareMiner2-master/"
DMM_SCRIPT="./run-DeclareMiner2-example.sh"
cp ${TEST_FOLDER}/DMM2-base-config $DMM_CONF

# output path
echo output_path=../../${TEST_FOLDER}/${TEST_BASE_NAME}_MODEL_DMM2.txt >> ${DMM_CONF}
# output file type: XML; TEXT; REPORT; NONE
echo output_file_type=TEXT >> ${DMM_CONF}
# input log
echo log_file_path=../../${INPUT_FILE} >> ${DMM_CONF}
# templates
echo templates=Alternate_Precedence,Alternate_Response,Chain_Precedence,Chain_Response,Existence,Init,Precedence,Responded_Existence,Response,Absence2 >> ${DMM_CONF}
# support: 0-100
echo min_support=${DMM_SUPP} >> ${DMM_CONF}
# alpha: 0: vacuity detection enabled;100: vacuity detection disabled
echo alpha=${DMM_ALPHA} >> ${DMM_CONF}

##################################################################
##################################################################
### DATASET CREATION
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

####################
### GENERATE LOG
. ./libs.cfg
#java -Xmx$MEMORY_MAX -cp MINERful.jar $TRACES_MAINCLASS -a $alphabet -m $MIN_STRLEN -M $MAX_STRLEN -L $TESTBED_SIZE -oLF $XES_FILE -oE "xes" -r `Participation a` `ChainResponse a b`

### COVERT TXT TO XES
#java -cp out/artifacts/MINERful_jar/MINERful.jar $TXT2XES_MAINCLASS -iLF ${TEXT_FILE} -oLF ${XES_FILE} -oE "xes"

##################################################################
##################################################################
### TEST

# MINERful
echo "################### MINERFUL"
# -ppAT none to avoid post processing
./run-MINERful.sh -iLF $INPUT_FILE -ppAT none -s $Mf_SUPP -c $Mf_CONF -i $Mf_INTE -CSV ${TEST_FOLDER}/${TEST_BASE_NAME}_MODEL_MINERful.csv

# MINERful-Vacuity Check
echo "################### MINERFUL VACUITY CHECK"
java -cp out/artifacts/MINERful_jar/MINERful.jar $MINERFUL_VACUITY_MAINCLASS $INPUT_FILE $MfV_SUPP ${TEST_FOLDER}/${TEST_BASE_NAME}_MODEL_MINERfulVChk.xml ${TEST_FOLDER}/${TEST_BASE_NAME}_MODEL_MINERfulVChk.csv

# Janus
echo "################### JANUS"
java -jar out/artifacts/MINERful_jar/MINERful.jar $JANUS_MAINCLASS -iLF $INPUT_FILE -s $J_SUPP -c $J_CONF -CSV ${TEST_FOLDER}/${TEST_BASE_NAME}_MODEL_Janus.csv

# DMM2
echo "################### DMM2"
cd ${DMM_PATH}
${DMM_SCRIPT}

