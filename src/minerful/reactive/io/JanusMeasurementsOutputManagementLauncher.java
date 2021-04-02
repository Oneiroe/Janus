package minerful.reactive.io;

import com.google.gson.*;
import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.io.params.OutputModelParameters;
import minerful.logparser.LogTraceParser;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.reactive.automaton.SeparatedAutomatonOfflineRunner;
import minerful.reactive.measurements.Measures;
import minerful.reactive.measurements.MegaMatrixMonster;
import minerful.reactive.params.JanusMeasurementsCmdParameters;
import minerful.reactive.params.JanusMeasurementsCmdParameters.DetailLevel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;

/**
 * Class to handle the output of Janus
 */
public class JanusMeasurementsOutputManagementLauncher extends MinerFulOutputManagementLauncher {

    /**
     * reads the terminal input parameters and launch the proper output functions
     *
     * @param matrix
     * @param additionalCnsIndexedInfo
     * @param outParams
     * @param viewParams
     * @param systemParams
     * @param alphabet
     */
    public void manageMeasurementsOutput(MegaMatrixMonster matrix, NavigableMap<Constraint, String> additionalCnsIndexedInfo, OutputModelParameters outParams, ViewCmdParameters viewParams, SystemCmdParameters systemParams, JanusMeasurementsCmdParameters measurementsParams, TaskCharArchive alphabet) {
        File outputFile = null;
        File outputAggregatedMeasuresFile = null;
        File outputNeuLogMeasuresFile = null;
        System.gc();

        // ************* CSV
        if (outParams.fileToSaveConstraintsAsCSV != null) {
            outputFile = retrieveFile(outParams.fileToSaveConstraintsAsCSV);
            logger.info("Saving the measures as CSV in " + outputFile + "...");
            double before = System.currentTimeMillis();


            // Events evaluation
            if (
                    measurementsParams.detailsLevel.equals(DetailLevel.event) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.all)
            ) {
                logger.info("Events Evaluation...");
                if (matrix.getMatrixLite() == null) {
                    exportEventsEvaluationToCSV(matrix, outputFile, outParams.encodeOutputTasks, alphabet);
                } else {
                    exportEventsEvaluationLiteToCSV(matrix, outputFile, outParams.encodeOutputTasks, alphabet);
                }
            }
            // Trace Measures
            if (
                    measurementsParams.detailsLevel.equals(DetailLevel.trace) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.allTrace) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.all)
            ) {
                logger.info("Traces Measures...");
//                String fileNameBestOf = outputFile.getAbsolutePath().substring(0, outputFile.getAbsolutePath().indexOf(".csv")).concat("[Best-" + varParams.bestNresults + "].txt");
                outputAggregatedMeasuresFile = new File(outParams.fileToSaveConstraintsAsCSV.getAbsolutePath().concat("TraceMeasures.CSV")); //TODO improve
                exportTracesMeasuresToCSV(matrix, outputAggregatedMeasuresFile, outParams.encodeOutputTasks, alphabet);
            }
            // Trace Measures descriptive statistics
            if (
                    measurementsParams.detailsLevel.equals(DetailLevel.traceStats) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.allTrace) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.allLog) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.all)
            ) {
                logger.info("Traces Measures Stats...");
                outputAggregatedMeasuresFile = new File(outParams.fileToSaveConstraintsAsCSV.getAbsolutePath().concat("AggregatedMeasures.CSV")); //TODO improve
                exportTracesMeasuresStatisticsToCSV(matrix, outputAggregatedMeasuresFile, outParams.encodeOutputTasks, alphabet);
            }
            // Log Measures
            if (
                    measurementsParams.detailsLevel.equals(DetailLevel.log) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.allLog) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.all)
            ) {
                logger.info("Log Measures...");
                outputNeuLogMeasuresFile = new File(outParams.fileToSaveConstraintsAsCSV.getAbsolutePath().concat("NeuLogMeasures.CSV")); //TODO improve
                exportLogMeasuresToCSV(matrix, outputNeuLogMeasuresFile, outParams.encodeOutputTasks, alphabet);
            }

            double after = System.currentTimeMillis();
            logger.info("Total CSV serialization time: " + (after - before));
        }

        if (!viewParams.suppressScreenPrintOut) {
//			TODO print result in terminal
            logger.info("Terminal output yet not implemented");
        }

        if (outParams.fileToSaveAsXML != null) {
//			TODO XML output
            logger.info("XML output yet not implemented");
        }

        // ************* JSON
        if (outParams.fileToSaveAsJSON != null) {
            outputFile = retrieveFile(outParams.fileToSaveAsJSON);
            logger.info("Saving the measures as JSON in " + outputFile + "...");

            double before = System.currentTimeMillis();


            // Events evaluation
            if (
                    measurementsParams.detailsLevel.equals(DetailLevel.event) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.all)
            ) {
                logger.info("Events Evaluation...");
                if (matrix.getMatrixLite() == null) {
                    exportEventsEvaluationToJson(matrix, outputFile, outParams.encodeOutputTasks, alphabet);
                } else {
                    exportEventsEvaluationLiteToJson(matrix, outputFile, outParams.encodeOutputTasks, alphabet);
                }
            }
            // Trace Measures
            if (
                    measurementsParams.detailsLevel.equals(DetailLevel.trace) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.allTrace) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.all)
            ) {
                logger.info("Traces Measures...");
                outputAggregatedMeasuresFile = new File(outParams.fileToSaveAsJSON.getAbsolutePath().concat("TraceMeasures.json")); // TODO improve
                exportTracesMeasuresToJson(matrix, outputAggregatedMeasuresFile, outParams.encodeOutputTasks, alphabet);

            }
            // Trace Measures descriptive statistics
            if (
                    measurementsParams.detailsLevel.equals(DetailLevel.traceStats) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.allTrace) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.allLog) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.all)
            ) {
                logger.info("Traces Measures Stats...");
                outputAggregatedMeasuresFile = new File(outParams.fileToSaveAsJSON.getAbsolutePath().concat("AggregatedMeasures.json")); // TODO improve
                exportTracesMeasuresStatisticsToJson(matrix, outputAggregatedMeasuresFile, outParams.encodeOutputTasks, alphabet);
            }
            // Log Measures
            if (
                    measurementsParams.detailsLevel.equals(DetailLevel.log) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.allLog) ||
                            measurementsParams.detailsLevel.equals(DetailLevel.all)
            ) {
                logger.info("Log Measures...");
                outputAggregatedMeasuresFile = new File(outParams.fileToSaveAsJSON.getAbsolutePath().concat("NeuLogMeasures.json")); //TODO improve
                exportLogMeasuresToJson(matrix, outputAggregatedMeasuresFile, outParams.encodeOutputTasks, alphabet);

            }

            double after = System.currentTimeMillis();
            logger.info("Total JSON serialization time: " + (after - before));
        }
        logger.info("Output encoding: " + outParams.encodeOutputTasks);
    }

    public void manageMeasurementsOutput(MegaMatrixMonster matrix,
                                         ViewCmdParameters viewParams, OutputModelParameters outParams, SystemCmdParameters systemParams, JanusMeasurementsCmdParameters measurementsParams, TaskCharArchive alphabet) {
        this.manageMeasurementsOutput(matrix, null, outParams, viewParams, systemParams, measurementsParams, alphabet);
    }


    /**
     * Export to CSV the events evaluation.
     * The output contains the events evaluation for each traces of each constraint (including the entire model) the evaluation
     *
     * @param megaMatrix        events evaluation matrix
     * @param outputFile        CSV output file base
     * @param encodeOutputTasks if true, the events are encoded, decoded otherwise
     * @param alphabet          alphabet to decode the events
     */
    public void exportEventsEvaluationToCSV(MegaMatrixMonster megaMatrix, File outputFile, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        logger.debug("CSV events serialization...");

//		header row
        String[] header = ArrayUtils.addAll(new String[]{
                "Trace",
                "Constraint",
                "Events-Evaluation"
        });

        try {
            FileWriter fw = new FileWriter(outputFile);
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT.withHeader(header).withDelimiter(';'));

            byte[][][] matrix = megaMatrix.getMatrix();
            Iterator<LogTraceParser> it = megaMatrix.getLog().traceIterator();
            List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

            //		Row builder
//        for the entire log
            for (int trace = 0; trace < matrix.length; trace++) {
                LogTraceParser tr = it.next();

                String traceString;
                if (encodeOutputTasks) {
                    traceString = tr.encodeTrace();
                } else {
                    traceString = tr.printStringTrace();
                }

                for (int constraint = 0; constraint < matrix[trace].length; constraint++) {
//                  for each constraint
                    String constraintName;
                    if (constraint == matrix[trace].length - 1) {
                        constraintName = "MODEL";
                    } else {
                        if (encodeOutputTasks) {
                            constraintName = automata.get(constraint).toString();
                        } else {
                            constraintName = automata.get(constraint).toStringDecoded(alphabet.getTranslationMapById());
                        }
                    }
                    String[] row = ArrayUtils.addAll(
                            new String[]{
                                    traceString,
                                    constraintName,
                                    Arrays.toString(matrix[trace][constraint])
                            });
                    printer.printRecord(row);
                }

            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Export to CSV the detailed result at the level of the events in all the traces.
     *
     * @param megaMatrix
     * @param outputFile
     * @param encodeOutputTasks
     * @param alphabet
     */
    public void exportEventsEvaluationLiteToCSV(MegaMatrixMonster megaMatrix, File outputFile, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        logger.debug("CSV events LITE serialization...");

//		header row
//		TODO make the columns parametric, not hard-coded
        String[] header = ArrayUtils.addAll(new String[]{
                "Trace",
                "Constraint",
                "N(A)",
                "N(T)",
                "N(¬A)",
                "N(¬T)",
                "N(¬A¬T)",
                "N(¬AT)",
                "N(A¬T)",
                "N(AT)",
                "Lenght"
        });

        try {
            FileWriter fw = new FileWriter(outputFile);
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT.withHeader(header).withDelimiter(';'));

            int[][][] matrix = megaMatrix.getMatrixLite();
            Iterator<LogTraceParser> it = megaMatrix.getLog().traceIterator();
            List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

            //		Row builder
//        for the entire log
            for (int trace = 0; trace < matrix.length; trace++) {
                LogTraceParser tr = it.next();

                String traceString;
                if (encodeOutputTasks) {
                    traceString = tr.encodeTrace();
                } else {
                    traceString = tr.printStringTrace();
                }

//              for each trace
                for (int constraint = 0; constraint < matrix[trace].length; constraint++) {
//                  for each constraint
                    String constraintName;
                    if (constraint == matrix[trace].length - 1) {
                        constraintName = "MODEL";
                    } else {
                        if (encodeOutputTasks) {
                            constraintName = automata.get(constraint).toString();
                        } else {
                            constraintName = automata.get(constraint).toStringDecoded(alphabet.getTranslationMapById());
                        }
                    }

                    String[] row = ArrayUtils.addAll(
                            new String[]{
                                    traceString,
                                    constraintName,
                                    String.valueOf(matrix[trace][constraint][0]),
                                    String.valueOf(matrix[trace][constraint][1]),
                                    String.valueOf(matrix[trace][constraint][2]),
                                    String.valueOf(matrix[trace][constraint][3]),
                                    String.valueOf(matrix[trace][constraint][4]),
                                    String.valueOf(matrix[trace][constraint][5]),
                                    String.valueOf(matrix[trace][constraint][6]),
                                    String.valueOf(matrix[trace][constraint][7]),
                                    String.valueOf(matrix[trace][constraint][8])
                            });
                    printer.printRecord(row);

                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Export to CSV the events evaluation.
     * The output contains the events evaluation for each traces of each constraint (including the entire model) the evaluation
     *
     * @param megaMatrix        events evaluation matrix
     * @param outputFile        CSV output file base
     * @param encodeOutputTasks if true, the events are encoded, decoded otherwise
     * @param alphabet          alphabet to decode the events
     */
    public void exportTracesMeasuresToCSV(MegaMatrixMonster megaMatrix, File outputFile, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        logger.debug("CSV trace measures serialization...");

//		header row
        String[] header = ArrayUtils.addAll(new String[]{
                "Trace",
                "Constraint",
        }, Measures.MEASURE_NAMES);

        try {
            FileWriter fw = new FileWriter(outputFile);
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT.withHeader(header).withDelimiter(';'));

            int contraintsNum = megaMatrix.getConstraintsNumber();
            Iterator<LogTraceParser> it = megaMatrix.getLog().traceIterator();
            List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

            //		Row builder
//        for the entire log
            for (int trace = 0; trace < megaMatrix.getLog().wholeLength(); trace++) {
                LogTraceParser tr = it.next();

                String traceString;
                if (encodeOutputTasks) {
                    traceString = tr.encodeTrace();
                } else {
                    traceString = tr.printStringTrace();
                }


                for (int constraint = 0; constraint < contraintsNum; constraint++) {
//                  for each constraint
                    String[] measurements = new String[Measures.MEASURE_NUM];
                    for (int measureIndex = 0; measureIndex < Measures.MEASURE_NUM; measureIndex++) {
                        measurements[measureIndex] = String.valueOf(megaMatrix.getSpecificMeasure(trace, constraint, measureIndex));
                    }
                    String constraintName;
                    if (constraint == contraintsNum - 1) {
                        constraintName = "MODEL";
                    } else {
                        if (encodeOutputTasks) {
                            constraintName = automata.get(constraint).toString();
                        } else {
                            constraintName = automata.get(constraint).toStringDecoded(alphabet.getTranslationMapById());
                        }
                    }
                    String[] row = ArrayUtils.addAll(
                            new String[]{
                                    traceString,
                                    constraintName
                            }, measurements);
                    printer.printRecord(row);
                }

            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Export to CSV format the aggregated measures at the level of log.
     * <p>
     * the columns index is:
     * constraint; quality-measure; duck-tape; mean; geometric-mean; variance; ....(all the other stats)
     *
     * @param megaMatrix
     * @param outputAggregatedMeasuresFile
     * @param encodeOutputTasks
     * @param alphabet
     */
    public void exportTracesMeasuresStatisticsToCSV(MegaMatrixMonster megaMatrix, File outputAggregatedMeasuresFile, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        logger.debug("CSV aggregated measures...");
        SummaryStatistics[][] constraintsLogMeasure = megaMatrix.getConstraintLogMeasures();

        List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

//		header row
//		TODO make the columns parametric, not hard-coded
        String[] header = new String[]{
                "Constraint",
                "Quality-Measure",
//                "Duck-Tape",
                "Mean",
                "Geometric-Mean",
                "Variance",
                "Population-variance",
                "Standard-Deviation",
//                "Percentile-75th",
                "Max",
                "Min"
        };

        try {
            FileWriter fw = new FileWriter(outputAggregatedMeasuresFile);
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT.withHeader(header).withDelimiter(';'));

            //		Row builder
            for (int constraint = 0; constraint < constraintsLogMeasure.length; constraint++) {
                String constraintName;
                if (constraint == constraintsLogMeasure.length - 1) {
                    constraintName = "MODEL";
                } else {
                    if (encodeOutputTasks) {
                        constraintName = automata.get(constraint).toString();
                    } else {
                        constraintName = automata.get(constraint).toStringDecoded(alphabet.getTranslationMapById());
                    }
                }

                SummaryStatistics[] constraintLogMeasure = constraintsLogMeasure[constraint]; //TODO performance slowdown

                for (int measureIndex = 0; measureIndex < megaMatrix.getMeasureNames().length; measureIndex++) {
//                    System.out.print("\rConstraints: " + constraint + "/" + constraintsLogMeasure.length+" Measure: " + measureIndex + "/" +  megaMatrix.getMeasureNames().length);
                    String[] row = new String[]{
                            constraintName,
                            megaMatrix.getMeasureName(measureIndex),
//                            String.valueOf(Measures.getLogDuckTapeMeasures(constraint, measureIndex, megaMatrix.getMatrix())),
                            String.valueOf(constraintLogMeasure[measureIndex].getMean()),
                            String.valueOf(constraintLogMeasure[measureIndex].getGeometricMean()),
                            String.valueOf(constraintLogMeasure[measureIndex].getVariance()),
                            String.valueOf(constraintLogMeasure[measureIndex].getPopulationVariance()),
                            String.valueOf(constraintLogMeasure[measureIndex].getStandardDeviation()),
//                            String.valueOf(constraintLogMeasure[measureIndex].getPercentile(75)),
                            String.valueOf(constraintLogMeasure[measureIndex].getMax()),
                            String.valueOf(constraintLogMeasure[measureIndex].getMin())
                    };
                    printer.printRecord(row);
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Export to CSV format the aggregated measures at the level of log.
     * <p>
     * the columns index is:
     * constraint; quality-measure; duck-tape; mean; geometric-mean; variance; ....(all the other stats)
     *
     * @param megaMatrix
     * @param outputAggregatedMeasuresFile
     * @param encodeOutputTasks
     * @param alphabet
     */
    public void exportLogMeasuresToCSV(MegaMatrixMonster megaMatrix, File outputAggregatedMeasuresFile, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        logger.debug("CSV log measures...");
        float[][] neuConstraintsLogMeasure = megaMatrix.getNeuConstraintLogMeasures();

        List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

//		header row
//		TODO make the columns parametric, not hard-coded
        String[] header = new String[]{
                "Constraint",
                "Quality-Measure",
                "Measure-Value"
        };

        try {
            FileWriter fw = new FileWriter(outputAggregatedMeasuresFile);
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT.withHeader(header).withDelimiter(';'));

            //		Row builder
            for (int constraint = 0; constraint < neuConstraintsLogMeasure.length; constraint++) {
                String constraintName;
                if (constraint == neuConstraintsLogMeasure.length - 1) {
                    constraintName = "MODEL";
                } else {
                    if (encodeOutputTasks) {
                        constraintName = automata.get(constraint).toString();
                    } else {
                        constraintName = automata.get(constraint).toStringDecoded(alphabet.getTranslationMapById());
                    }
                }


                for (int measureIndex = 0; measureIndex < megaMatrix.getMeasureNames().length; measureIndex++) {
//                    System.out.print("\rConstraints: " + constraint + "/" + constraintsLogMeasure.length+" Measure: " + measureIndex + "/" +  megaMatrix.getMeasureNames().length);
                    String[] row = new String[]{
                            constraintName,
                            megaMatrix.getMeasureName(measureIndex),
                            String.valueOf(neuConstraintsLogMeasure[constraint][measureIndex])
                    };
                    printer.printRecord(row);
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Builds the json structure for a given constraint
     */
    private JsonElement tracesMeasuresStatisticsJsonBuilder(MegaMatrixMonster megaMatrix, SummaryStatistics[] constraintLogMeasure) {
        JsonObject constraintJson = new JsonObject();

        for (int measureIndex = 0; measureIndex < megaMatrix.getMeasureNames().length; measureIndex++) {
            JsonObject measure = new JsonObject();

            JsonObject stats = new JsonObject();

            stats.addProperty("Mean", constraintLogMeasure[measureIndex].getMean());
            stats.addProperty("Geometric Mean", constraintLogMeasure[measureIndex].getGeometricMean());
            stats.addProperty("Variance", constraintLogMeasure[measureIndex].getVariance());
            stats.addProperty("Population  variance", constraintLogMeasure[measureIndex].getPopulationVariance());
            stats.addProperty("Standard Deviation", constraintLogMeasure[measureIndex].getStandardDeviation());
//            stats.addProperty("Percentile 75th", constraintLogMeasure[measureIndex].getPercentile(75));
            stats.addProperty("Max", constraintLogMeasure[measureIndex].getMax());
            stats.addProperty("Min", constraintLogMeasure[measureIndex].getMin());


            measure.add("stats", stats);
//            measure.addProperty("duck tape", Measures.getLogDuckTapeMeasures(constraintIndex, measureIndex, megaMatrix.getMatrix()));

            constraintJson.add(megaMatrix.getMeasureName(measureIndex), measure);
        }

        return constraintJson;
    }

    /**
     * Builds the json structure for a given constraint
     */
    private JsonElement tracesMeasuresJsonBuilder(MegaMatrixMonster megaMatrix, int traceIndex, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        JsonObject traceJson = new JsonObject();
        int constraintsnum = megaMatrix.getConstraintsNumber();
        List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

        //              for each trace
        for (int constraint = 0; constraint < constraintsnum; constraint++) {
            JsonObject constraintJson = new JsonObject();
//          Constraint name
            String constraintName;
            if (constraint == constraintsnum - 1) {
                constraintName = "MODEL";
            } else {
                if (encodeOutputTasks) {
                    constraintName = automata.get(constraint).toString();
                } else {
                    constraintName = automata.get(constraint).toStringDecoded(alphabet.getTranslationMapById());
                }
            }
//          trace Measures
            for (int measureIndex = 0; measureIndex < Measures.MEASURE_NUM; measureIndex++) {
                constraintJson.addProperty(Measures.MEASURE_NAMES[measureIndex], megaMatrix.getSpecificMeasure(traceIndex, constraint, measureIndex));
            }
            traceJson.add(constraintName, constraintJson);
        }
        return traceJson;
    }


    /**
     * Builds the json structure for a given constraint
     */
    private JsonElement logMeasuresJsonBuilder(MegaMatrixMonster megaMatrix, float[] constraintLogMeasure) {
        JsonObject constraintJson = new JsonObject();

        for (int measureIndex = 0; measureIndex < megaMatrix.getMeasureNames().length; measureIndex++) {
            constraintJson.addProperty(megaMatrix.getMeasureName(measureIndex), constraintLogMeasure[measureIndex]);
        }

        return constraintJson;
    }

    /**
     * write the Json file with the Traces Measures Statistics
     *
     * @param megaMatrix
     * @param outputFile
     * @param encodeOutputTasks
     * @param alphabet
     */
    public void exportTracesMeasuresStatisticsToJson(MegaMatrixMonster megaMatrix, File outputFile, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        logger.debug("JSON aggregated measures...");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter fw = new FileWriter(outputFile);
            JsonObject jsonOutput = new JsonObject();

            List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

//			\/ \/ \/ LOG RESULTS
            SummaryStatistics[][] constraintLogMeasure = megaMatrix.getConstraintLogMeasures();

            String constraintName;
            for (int constraint = 0; constraint < constraintLogMeasure.length; constraint++) {
                if (constraint == constraintLogMeasure.length - 1) {
                    constraintName = "MODEL";
                } else {
                    if (encodeOutputTasks) {
                        constraintName = automata.get(constraint).toString();
                    } else {
                        constraintName = automata.get(constraint).toStringDecoded(alphabet.getTranslationMapById());
                    }
                }

                jsonOutput.add(
                        constraintName,
                        tracesMeasuresStatisticsJsonBuilder(megaMatrix, constraintLogMeasure[constraint])
                );
            }
            gson.toJson(jsonOutput, fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("JSON encoded aggregated measures...DONE!");
    }

    /**
     * write the jon file with the aggregated measures
     *
     * @param megaMatrix
     * @param outputFile
     * @param encodeOutputTasks
     * @param alphabet
     */
    public void exportLogMeasuresToJson(MegaMatrixMonster megaMatrix, File outputFile, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        logger.debug("JSON log measures...");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter fw = new FileWriter(outputFile);
            JsonObject jsonOutput = new JsonObject();

            List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

//			\/ \/ \/ LOG RESULTS
            float[][] neuConstraintsLogMeasure = megaMatrix.getNeuConstraintLogMeasures();

            String constraintName;
            for (int constraint = 0; constraint < neuConstraintsLogMeasure.length; constraint++) {
                if (constraint == neuConstraintsLogMeasure.length - 1) {
                    constraintName = "MODEL";
                } else {
                    if (encodeOutputTasks) {
                        constraintName = automata.get(constraint).toString();
                    } else {
                        constraintName = automata.get(constraint).toStringDecoded(alphabet.getTranslationMapById());
                    }
                }
                jsonOutput.add(
                        constraintName,
                        logMeasuresJsonBuilder(megaMatrix, neuConstraintsLogMeasure[constraint])
                );
            }
            gson.toJson(jsonOutput, fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("JSON encoded aggregated measures...DONE!");
    }

    /**
     * Serialize the events evaluations into a Json file to have a readable result
     *
     * @param megaMatrix
     * @param outputFile
     * @param encodeOutputTasks
     * @param alphabet
     */
    public void exportEventsEvaluationToJson(MegaMatrixMonster megaMatrix, File outputFile, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        logger.debug("JSON readable serialization...");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter fw = new FileWriter(outputFile);
            JsonObject jsonOutput = new JsonObject();

            byte[][][] matrix = megaMatrix.getMatrix();
            Iterator<LogTraceParser> it = megaMatrix.getLog().traceIterator();
            List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

//        for the entire log
            for (int trace = 0; trace < matrix.length; trace++) {
                JsonObject traceJson = new JsonObject();

                LogTraceParser tr = it.next();
                tr.init();
                String traceString;
                if (encodeOutputTasks) {
                    traceString = tr.encodeTrace();
                } else {
                    traceString = tr.printStringTrace();
                }

//              for each trace
                for (int constraint = 0; constraint < matrix[trace].length; constraint++) {
                    tr.init();
//                  contraint name
                    String constraintName;
                    if (constraint == matrix[trace].length - 1) {
                        constraintName = "MODEL";
                    } else {
                        if (encodeOutputTasks) {
                            constraintName = automata.get(constraint).toString();
                        } else {
                            constraintName = automata.get(constraint).toStringDecoded(alphabet.getTranslationMapById());
                        }
                    }
//                    events evaluation
                    JsonArray eventsJson = new JsonArray();
                    for (byte e : matrix[trace][constraint]) {
                        eventsJson.add(Integer.valueOf(e));
                    }
                    traceJson.add(constraintName, eventsJson);
                }

                jsonOutput.add(traceString, traceJson);
            }
            gson.toJson(jsonOutput, fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("JSON readable serialization...DONE!");

    }

    /**
     * Serialize the 3D matrix into a Json file to have a readable result
     *
     * @param megaMatrix
     * @param outputFile
     * @param encodeOutputTasks
     * @param alphabet
     */
    public void exportEventsEvaluationLiteToJson(MegaMatrixMonster megaMatrix, File outputFile, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        logger.debug("JSON readable serialization...");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter fw = new FileWriter(outputFile);
            JsonObject jsonOutput = new JsonObject();

            int[][][] matrix = megaMatrix.getMatrixLite();
            Iterator<LogTraceParser> it = megaMatrix.getLog().traceIterator();
            List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

//        for the entire log
            for (int trace = 0; trace < matrix.length; trace++) {
                JsonObject traceJson = new JsonObject();

                LogTraceParser tr = it.next();
                tr.init();
                String traceString;
                if (encodeOutputTasks) {
                    traceString = tr.encodeTrace();
                } else {
                    traceString = tr.printStringTrace();
                }

//              for each trace
                for (int constraint = 0; constraint < matrix[trace].length; constraint++) {
                    tr.init();
//                  for each constraint
                    String constraintName;
                    if (constraint == matrix[trace].length - 1) {
                        constraintName = "MODEL";
                    } else {
                        if (encodeOutputTasks) {
                            constraintName = automata.get(constraint).toString();
                        } else {
                            constraintName = automata.get(constraint).toStringDecoded(alphabet.getTranslationMapById());
                        }
                    }

                    JsonObject frequenciesJson = new JsonObject();

                    frequenciesJson.addProperty("N(A)", matrix[trace][constraint][0]);
                    frequenciesJson.addProperty("N(T)", matrix[trace][constraint][1]);
                    frequenciesJson.addProperty("N(¬A)", matrix[trace][constraint][2]);
                    frequenciesJson.addProperty("N(¬T)", matrix[trace][constraint][3]);
                    frequenciesJson.addProperty("N(¬A¬T)", matrix[trace][constraint][4]);
                    frequenciesJson.addProperty("N(¬AT)", matrix[trace][constraint][5]);
                    frequenciesJson.addProperty("N(A¬T)", matrix[trace][constraint][6]);
                    frequenciesJson.addProperty("N(AT)", matrix[trace][constraint][7]);
                    frequenciesJson.addProperty("Length", matrix[trace][constraint][8]);

                    traceJson.add(constraintName, frequenciesJson);
                }
                jsonOutput.add(traceString, traceJson);
            }
            gson.toJson(jsonOutput, fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("JSON readable serialization...DONE!");

    }


    /**
     * Serialize the events evaluations into a Json file to have a readable result
     *
     * @param megaMatrix
     * @param outputFile
     * @param encodeOutputTasks
     * @param alphabet
     */
    public void exportTracesMeasuresToJson(MegaMatrixMonster megaMatrix, File outputFile, boolean encodeOutputTasks, TaskCharArchive alphabet) {
        logger.debug("JSON trace measures...");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter fw = new FileWriter(outputFile);
            JsonObject jsonOutput = new JsonObject();

            Iterator<LogTraceParser> it = megaMatrix.getLog().traceIterator();

//        for the entire log
            for (int trace = 0; trace < megaMatrix.getLog().wholeLength(); trace++) {
                LogTraceParser tr = it.next();
                tr.init();
                String traceString;
                if (encodeOutputTasks) {
                    traceString = tr.encodeTrace();
                } else {
                    traceString = tr.printStringTrace();
                }
                jsonOutput.add(
                        traceString,
                        tracesMeasuresJsonBuilder(megaMatrix, trace, encodeOutputTasks, alphabet)
                );
            }
            gson.toJson(jsonOutput, fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("JSON readable serialization...DONE!");

    }


}
