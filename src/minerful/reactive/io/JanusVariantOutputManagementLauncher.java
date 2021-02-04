package minerful.reactive.io;

import minerful.MinerFulOutputManagementLauncher;
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
    public void manageVariantOutput(Map<String, Double> variantResults, NavigableMap<Constraint, String> additionalCnsIndexedInfo, JanusVariantCmdParameters varParams, ViewCmdParameters viewParams, SystemCmdParameters systemParams, TaskCharArchive alphabet) {
        File outputFile = null;

        // ************* CSV
        if (varParams.outputCvsFile != null) {
            outputFile = retrieveFile(varParams.outputCvsFile);
            logger.info("Saving variant analysis result as CSV in " + outputFile + "...");
            double before = System.currentTimeMillis();

            exportVariantResultsToCSV(variantResults, outputFile, varParams);

            double after = System.currentTimeMillis();
            logger.info("Total CSV serialization time: " + (after - before));
        }

        if (viewParams != null && !viewParams.suppressScreenPrintOut) {
//			TODO print result in terminal
            logger.info("Terminal output yet not implemented");
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

    private void exportVariantResultsToCSV(Map<String, Double> variantResults, File outputFile, JanusVariantCmdParameters varParams) {
        //		header row
        String[] header = {"Constraint", "p_value"};
        try {
            FileWriter fw = new FileWriter(outputFile);
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT.withHeader(header).withDelimiter(';'));

            //		Row builder
            for (String constraint : variantResults.keySet()) {
                if (varParams.oKeep) {
                    printer.printRecord(new String[]{constraint, variantResults.get(constraint).toString()});
                } else {
                    if (variantResults.get(constraint) <= varParams.pValue) {
                        printer.printRecord(new String[]{constraint, variantResults.get(constraint).toString()});
                    }
                }
            }

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void manageVariantOutput(Map<String, Double> variantResults,
                                    ViewCmdParameters viewParams, JanusVariantCmdParameters varParams, SystemCmdParameters systemParams, TaskCharArchive alphabet) {
        this.manageVariantOutput(variantResults, null, varParams, viewParams, systemParams, alphabet);
    }

    public void manageVariantOutput(Map<String, Double> variantResults,
                                    JanusVariantCmdParameters varParams, SystemCmdParameters systemParams) {
        this.manageVariantOutput(variantResults, null, varParams, null, systemParams, null);
    }

}
