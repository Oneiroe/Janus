package minerful.reactive.io;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.reactive.params.JanusVariantCmdParameters;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Class to handle the output of Janus
 */
public class JanusVariantOutputManagementLauncher extends MinerFulOutputManagementLauncher {

    /**
     * reads the terminal input parameters and launch the proper output functions
     *
     * @param variantResults
     * @param additionalCnsIndexedInfo
     * @param varParams
     * @param viewParams
     * @param systemParams
     * @param alphabet
     * @param measurementsSpecification1
     * @param measurementsSpecification2
     */
    public void manageVariantOutput(Map<String, Float> variantResults, NavigableMap<Constraint, String> additionalCnsIndexedInfo, JanusVariantCmdParameters varParams, ViewCmdParameters viewParams, SystemCmdParameters systemParams, TaskCharArchive alphabet, Map<String, Float> measurementsSpecification1, Map<String, Float> measurementsSpecification2) {
        File outputFile = null;

        // ************* CSV
        if (varParams.outputCvsFile != null) {
            outputFile = retrieveFile(varParams.outputCvsFile);
            logger.info("Saving variant analysis result as CSV in " + outputFile + "...");
            double before = System.currentTimeMillis();

            exportVariantResultsToCSV(variantResults, outputFile, varParams, alphabet, measurementsSpecification1, measurementsSpecification2);

            double after = System.currentTimeMillis();
            logger.info("Total CSV serialization time: " + (after - before));
        }

        if (viewParams != null && !viewParams.suppressScreenPrintOut) {
            printVariantResultsToScreen(variantResults, varParams, alphabet, measurementsSpecification1, measurementsSpecification2);
        }

        // ************* JSON
        if (varParams.outputJsonFile != null) {
            outputFile = retrieveFile(varParams.outputJsonFile);
            logger.info("Saving variant analysis result as JSON in " + outputFile + "...");

            double before = System.currentTimeMillis();

//            TODO
            logger.info("JSON output yet not implemented");

            double after = System.currentTimeMillis();
            logger.info("Total JSON serialization time: " + (after - before));
        }

    }

    private void printVariantResultsToScreen(Map<String, Float> variantResults, JanusVariantCmdParameters varParams, TaskCharArchive alphabet, Map<String, Float> measurementsSpecification1, Map<String, Float> measurementsSpecification2) {
        //		header row
        System.out.println("--------------------");
        System.out.println("relevant constraints differences");
        System.out.println("CONSTRAINT : P_VALUE");

        Map<Character, TaskChar> translationMap = alphabet.getTranslationMapById();
        for (String constraint : variantResults.keySet()) {
            if (variantResults.get(constraint) <= varParams.pValue) {
                System.out.println(decodeConstraint(constraint, translationMap) + " : " + variantResults.get(constraint).toString() + " [Var1: " + measurementsSpecification1.get(constraint).toString() + " | Var2: " + measurementsSpecification2.get(constraint).toString() + "]");
            }
        }
    }

    private void exportVariantResultsToCSV(Map<String, Float> variantResults, File outputFile, JanusVariantCmdParameters varParams, TaskCharArchive alphabet, Map<String, Float> measurementsSpecification1, Map<String, Float> measurementsSpecification2) {
        //		header row
        try {
            int totRulesNum = variantResults.keySet().size();
            int relevantRulesNum = 0;


            String[] headerDetailed = {"Constraint", "p_value", "Measure_VAR1", "Measure_VAR2", "ABS-Difference", "Natural_Language_Description"};
            FileWriter fwDetailed = new FileWriter(outputFile);
            CSVPrinter printerDetailed = new CSVPrinter(fwDetailed, CSVFormat.DEFAULT.withHeader(headerDetailed).withDelimiter(';'));

            String fileNameBestOf = outputFile.getAbsolutePath().substring(0, outputFile.getAbsolutePath().indexOf(".csv")).concat("[Best-" + varParams.bestNresults + "].txt");
            String[] headerBestOf = {"RESULTS"};
            FileWriter fwBestOf = new FileWriter(fileNameBestOf);
            CSVPrinter printerBestOf = new CSVPrinter(fwBestOf, CSVFormat.DEFAULT.withHeader(headerBestOf).withDelimiter(';'));

//            Sort results by difference in decreasing order
            TreeMultimap<Float, String> sortedDiffResults = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());

            Map<Character, TaskChar> translationMap = alphabet.getTranslationMapById();
            for (String constraint : variantResults.keySet()) {

//                decode constraint
                String decodedConstraint = decodeConstraint(constraint, translationMap);
//                Row builder
                float difference = Math.abs(measurementsSpecification1.get(constraint) - measurementsSpecification2.get(constraint));
                if (varParams.oKeep || variantResults.get(constraint) <= varParams.pValue) {
                    relevantRulesNum++;
                    printerDetailed.printRecord(new String[]{
                            decodedConstraint,
                            variantResults.get(constraint).toString(),
                            String.format("%.3f", measurementsSpecification1.get(constraint)),
                            String.format("%.3f", measurementsSpecification2.get(constraint)),
                            String.format("%.3f", difference),
                            getNaturalLanguageDescription(decodedConstraint, varParams.measure, measurementsSpecification1.get(constraint), measurementsSpecification2.get(constraint), difference, varParams)}
                    );
                    sortedDiffResults.put(difference, getNaturalLanguageDescription(decodedConstraint, varParams.measure, measurementsSpecification1.get(constraint), measurementsSpecification2.get(constraint), difference, varParams));
                }
            }
            fwDetailed.close();

            int counter = varParams.bestNresults;
            for (Float key : sortedDiffResults.keySet()) {
                for (String line : sortedDiffResults.get(key)) {
                    printerBestOf.printRecord(line);
                    counter--; // first N results
                }
//                counter--; // First N distinct results
                if (counter < 0) break;
            }
            fwBestOf.close();

            logger.info("Rules Number: " + totRulesNum + " ; relevant: " + relevantRulesNum + " ; non-relevant: " + (totRulesNum - relevantRulesNum));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final Map<String, String> DESCRIPTION = new HashMap<String, String>() {{
        put("RespondedExistence", "if [$1] occurs, also [$2] occurs. ");
        put("CoExistence", "[$1] and [$2] co-occur. ");
        put("Succession", "[$1] is followed by [$2] and [$2] is preceded by [$1]. ");
        put("Precedence", "if [$2] occurs, [$1] occurred before it. ");
        put("Response", "if [$1] occurs, [$2] will occur afterwards. ");
        put("AlternateSuccession", "[$1] is followed by [$2] and [$2] is preceded by [$1], without any other occurrence of [$1] and [$2] in between. ");
        put("AlternatePrecedence", "if [$2] occurs, [$1] occurred before it without any other occurrence of [$2] in between. ");
        put("AlternateResponse", "if [$1] occurs, [$2] will occur afterwards without any other occurrence of [$1] in between. ");
        put("ChainSuccession", "[$1] is immediately followed by [$2] and [$2] is immediately preceded by [$1]. ");
        put("ChainPrecedence", "if [$2] occurs, [$1] occurred immediately before it. ");
        put("ChainResponse", "if [$1] occurs, [$2] occurs immediately afterwards. ");
        put("NotCoExistence", "[$1] and [$2] do not occur in together in the same process instance. ");
        put("NotSuccession", "[$1] is not followed by [$2] and [$2] is not preceded by [$1]. ");
        put("NotChainSuccession", "[$1] is not immediately followed by [$2] and [$2] is not immediately preceded by [$1]. ");
        put("Participation", "[$1] occurs in a process instance. ");
        put("AtMostOne", "[$1] may occur at most one time in a process instance. ");
        put("End", "the process ends with [$1]. ");
        put("Init", "the process starts with [$1]. ");
    }};

    private String getNaturalLanguageDescription(String constraint, String measure, float var1measure, float var2measure, float difference, JanusVariantCmdParameters varParams) {
        String template = constraint.split("\\(")[0];
        String result;

        if (Float.isNaN(difference)) {
            if (Float.isNaN(var1measure)) {
                if (var2measure < varParams.measureThreshold) result = "It may happen only in variant 2 that ";
                else result = "It happens only in variant 2 that ";
            } else {
                if (var1measure < varParams.measureThreshold) result = "It may happen only in variant 1 that ";
                else result = "It happens only in variant 1 that ";
            }
        } else {
            String greaterVariance = (var1measure > var2measure) ? "1" : "2";
            String smallerVariance = (var1measure > var2measure) ? "2" : "1";
            result = "In variant " + greaterVariance + " it is " + String.format("%.1f", difference * 100) + "% more likely than variant" + smallerVariance + " that ";
//        3)    .... In [Varaint 1/2] it is [diff %] more likely than [Variant 2/1]
        }
        if (DESCRIPTION.get(template) == null) {
            logger.error("[Constraint without natural language description: " + template + "]");
            result += "[Constraint without natural language description: " + template + "]";
        } else result += DESCRIPTION.get(template);
        if (!constraint.contains(",")) {
            String task = constraint.split("\\(")[1].replace(")", "");
            result = result.replace("$1", task);
        } else {
            String task1 = constraint.split("\\(")[1].replace(")", "").split(",")[0];
            String task2 = constraint.split("\\(")[1].replace(")", "").split(",")[1];
            result = result.replace("$1", task1).replace("$2", task2);
        }

        return result;
    }

    private String decodeConstraint(String encodedConstraint, Map<Character, TaskChar> translationMap) {
        StringBuilder resultBuilder = new StringBuilder();
        String constraint = encodedConstraint.substring(0, encodedConstraint.indexOf("("));
        resultBuilder.append(constraint);
        String[] encodedVariables = encodedConstraint.substring(encodedConstraint.indexOf("(")).replace("(", "").replace(")", "").split(",");
        resultBuilder.append("(");
        String decodedActivator = translationMap.get(encodedVariables[0].charAt(0)).toString();
        resultBuilder.append(decodedActivator);
        if (encodedVariables.length > 1) { //constraints with 2 variables
            resultBuilder.append(",");
            String decodedTarget = translationMap.get(encodedVariables[1].charAt(0)).toString();
            resultBuilder.append(decodedTarget);
        }
        resultBuilder.append(")");
        return resultBuilder.toString();
    }

    public void manageVariantOutput(Map<String, Float> variantResults,
                                    ViewCmdParameters viewParams, JanusVariantCmdParameters varParams, SystemCmdParameters systemParams, TaskCharArchive alphabet, Map<String, Float> measurementsSpecification1, Map<String, Float> measurementsSpecification2) {
        this.manageVariantOutput(variantResults, null, varParams, viewParams, systemParams, alphabet, measurementsSpecification1, measurementsSpecification2);
    }


}
