package minerful.reactive.io;

import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.reactive.checking.Measures;
import minerful.reactive.checking.MegaMatrixMonster;
import minerful.reactive.params.JanusVariantCmdParameters;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;

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
     */
    public void manageVariantOutput(Map<String, Float> variantResults, NavigableMap<Constraint, String> additionalCnsIndexedInfo, JanusVariantCmdParameters varParams, ViewCmdParameters viewParams, SystemCmdParameters systemParams, TaskCharArchive alphabet) {
        File outputFile = null;

        // ************* CSV
        if (varParams.outputCvsFile != null) {
            outputFile = retrieveFile(varParams.outputCvsFile);
            logger.info("Saving variant analysis result as CSV in " + outputFile + "...");
            double before = System.currentTimeMillis();

            exportVariantResultsToCSV(variantResults, outputFile, varParams, alphabet);

            double after = System.currentTimeMillis();
            logger.info("Total CSV serialization time: " + (after - before));
        }

        if (viewParams != null && !viewParams.suppressScreenPrintOut) {
            printVariantResultsToScreen(variantResults, varParams, alphabet);
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

    private void printVariantResultsToScreen(Map<String, Float> variantResults, JanusVariantCmdParameters varParams, TaskCharArchive alphabet) {
        //		header row
        System.out.println("--------------------");
        System.out.println("relevant constraints differences");
        System.out.println("CONSTRAINT : P_VALUE");

        Map<Character, TaskChar> translationMap = alphabet.getTranslationMapById();
        for (String constraint : variantResults.keySet()) {
            if (variantResults.get(constraint) <= varParams.pValue) {
                System.out.println(decodeConstraint(constraint, translationMap) + " : " + variantResults.get(constraint).toString());
            }
        }
    }

    private void exportVariantResultsToCSV(Map<String, Float> variantResults, File outputFile, JanusVariantCmdParameters varParams, TaskCharArchive alphabet) {
        //		header row
        String[] header = {"Constraint", "p_value"};
        try {
            FileWriter fw = new FileWriter(outputFile);
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT.withHeader(header).withDelimiter(';'));

            Map<Character, TaskChar> translationMap = alphabet.getTranslationMapById();
            for (String constraint : variantResults.keySet()) {
//                decode constraint
                String decodedConstraint = decodeConstraint(constraint, translationMap);
//                Row builder
                if (varParams.oKeep) {
                    printer.printRecord(new String[]{decodedConstraint, variantResults.get(constraint).toString()});
                } else {
                    if (variantResults.get(constraint) <= varParams.pValue) {
                        printer.printRecord(new String[]{decodedConstraint, variantResults.get(constraint).toString()});
                    }
                }
            }

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                                    ViewCmdParameters viewParams, JanusVariantCmdParameters varParams, SystemCmdParameters systemParams, TaskCharArchive alphabet) {
        this.manageVariantOutput(variantResults, null, varParams, viewParams, systemParams, alphabet);
    }


}
