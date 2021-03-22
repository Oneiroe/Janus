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
TEST_FOLDER="tests-Journal-SJ2T"
TEST_BASE_NAME="ERROR-INJECTION-NEU"

mkdir ${TEST_FOLDER}/${TEST_BASE_NAME}"_output/"
mkdir ${TEST_FOLDER}/${TEST_BASE_NAME}"_output/plotly"

## Runtime environment constants
LOG_MAINCLASS="minerful.MinerFulLogMakerStarter"
ERROR_MAINCLASS="minerful.MinerFulErrorInjectedLogMakerStarter"
JANUS_CHECK_MAINCLASS="minerful.JanusModelCheckStarter"

# Input Model
MODEL_ENCODING="JSON"
MODEL=${TEST_FOLDER}/${TEST_BASE_NAME}"-model.json"

## Log generation settings
MIN_STRLEN=10
MAX_STRLEN=100
TESTBED_SIZE=100
MEMORY_MAX="10048m"
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
ERROR_TYPE="del"
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
# Generate log
echo "########### Generate Log"
### GENERATE LOG with MinerFulLogMakerStarter ****
if ! test -f ${ORIGINAL_GENERATED_LOG}; then
  java -Xmx$MEMORY_MAX -cp Janus-NEU.jar $LOG_MAINCLASS \
      --input-model-file $MODEL \
      --input-model-encoding $MODEL_ENCODING  \
      --size $TESTBED_SIZE \
      --minlen $MIN_STRLEN \
      --maxlen $MAX_STRLEN \
      --out-log-encoding $LOG_ENCODING \
      --out-log-file $TEMP_TEXT_FILE \
      -d none

  # remove the unwanted characters to make it readable in input by Janus
  python3 pySupport/cleanStringLog.py $TEMP_TEXT_FILE $ORIGINAL_GENERATED_LOG
  rm $TEMP_TEXT_FILE

else
  echo "Log already generated: "${ORIGINAL_GENERATED_LOG}
fi

# check measures with janus
echo "##### SJ2T Check original log"
java -cp Janus-NEU.jar minerful.JanusModelCheckStarter -iLF $ORIGINAL_GENERATED_LOG -iLE $LOG_ENCODING -iMF $MODEL -iME $MODEL_ENCODING -oCSV $OUTPUT_CHECK_CSV -oJSON $OUTPUT_CHECK_JSON -d none $NaN_LOG -detailsLevel $DETAILS_LEVEL

# save result
# generate MEAN-only CSV of aggregated measures
echo "########### Post Processing"
python3 pySupport/singleAggregationNeuCSV.py $OUTPUT_CHECK_JSON"NeuLogMeasures.json" $OUTPUT_CHECK_JSON"NeuLogMeasures[MEAN]_0.csv"


##################################################################
## injection error cycle
for TARGET_CHAR in "a" "b" "c" "d"  "e"  "f"  "g"  "h"  "i"  "l"  "m"  "n"  "o"  "p"  "q"  "r"  "s"  "t"  "u"  "v"  "w"  "x"  "y"  "z"  "j"  "k"  "ü"  "§"  "ß"
do
    ##################################################################
    ## injection error cycle single target
    echo "########### Error-injection cycle"

    for ITERATION in $(seq 1 ${ITERATIONS}); do
        for ERROR_PERCENTAGE in 0 10 20 30 40 50 60 70 80 90 100
        do
            # Error injection
            echo "### Char:"${TARGET_CHAR}" ------ Iteration:"${ITERATION}" ------ Error-injection level:"${ERROR_PERCENTAGE}
            java -Xmx$MEMORY_MAX -cp Janus-NEU.jar $ERROR_MAINCLASS \
            -iLF $ORIGINAL_GENERATED_LOG \
            -iLE $LOG_ENCODING \
            --err-target $TARGET_CHAR \
            --err-out-log $ERROR_LOG \
            --err-percentage $ERROR_PERCENTAGE \
            --err-spread-policy $ERROR_POLICY \
            --err-type $ERROR_TYPE \
            -iME $MODEL_ENCODING \
            -iMF $MODEL \
            -d none

            # check measures with janus
            echo "### SJ2T Check"
            java -cp Janus-NEU.jar minerful.JanusModelCheckStarter -iLF $ERROR_LOG -iLE $LOG_ENCODING -iMF $MODEL -iME $MODEL_ENCODING -oCSV $OUTPUT_CHECK_CSV -oJSON $OUTPUT_CHECK_JSON -d none $NaN_LOG -detailsLevel $DETAILS_LEVEL

#            echo $'Press any key to continue...\n'
#            read anyKey

            # save result
            echo "########### Post Processing"
            python3 pySupport/singleAggregationNeuCSV.py $OUTPUT_CHECK_JSON"NeuLogMeasures.json" $OUTPUT_CHECK_JSON"NeuLogMeasures[MEAN]_"${ITERATION}"_"${ERROR_PERCENTAGE}".csv"

        done
    done

    ## AVERAGE of results

    ##################################################################
    ## Plot results
    echo "########### Plot results"
    /home/alessio/Data/Phd/my_code/PyVEnv/pySupport/bin/python pySupport/error_injection_plots.py $OUTPUT_CHECK_JSON"NeuLogMeasures[MEAN]_" $ITERATIONS ${TEST_FOLDER}/${TEST_BASE_NAME}"_output/" $TARGET_CHAR $ERROR_TYPE

done

echo "########### Informativeness"
/home/alessio/Data/Phd/my_code/PyVEnv/pySupport/bin/python pySupport/error_injection_informativeness.py ${TEST_FOLDER}/${TEST_BASE_NAME}"_output/" $TEST_BASE_NAME"-INFORMATIVENESS-" ${TEST_FOLDER}/${TEST_BASE_NAME}"_output/"$TEST_BASE_NAME"-"${ERROR_TYPE}"-INFORMATIVENESS_AVG.csv"

##################################################################
## Cleaning
rm ${TEST_FOLDER}/${TEST_BASE_NAME}"-output"*