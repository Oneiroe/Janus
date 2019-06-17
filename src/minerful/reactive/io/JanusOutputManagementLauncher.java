package minerful.reactive.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.constraint.Constraint;
import minerful.io.params.OutputModelParameters;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.reactive.automaton.SeparatedAutomatonOfflineRunner;
import minerful.reactive.checking.Measures;
import minerful.reactive.checking.MegaMatrixMonster;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;

/**
 * Class to handle the output of Janus
 */
public class JanusOutputManagementLauncher extends MinerFulOutputManagementLauncher {

	/**
	 * reads the terminal input parameters and launch the proper output functions
	 *
	 * @param matrix
	 * @param additionalCnsIndexedInfo
	 * @param outParams
	 * @param viewParams
	 * @param systemParams
	 * @param logParser
	 */
	public void manageCheckOutput(MegaMatrixMonster matrix, NavigableMap<Constraint, String> additionalCnsIndexedInfo, OutputModelParameters outParams, ViewCmdParameters viewParams, SystemCmdParameters systemParams, LogParser logParser) {
		File outputFile = null;
		File outputAggregatedMeasuresFile = null;

		if (outParams.fileToSaveConstraintsAsCSV != null) {
			outputFile = retrieveFile(outParams.fileToSaveConstraintsAsCSV);
			logger.info("Saving the discovered process as CSV in " + outputFile + "...");
			double before = System.currentTimeMillis();

//			Detailed traces results
			exportEncodedReadable3DMatrixToCSV(matrix, outputFile);

//			Aggregated Log measures
			outputAggregatedMeasuresFile = new File(outParams.fileToSaveConstraintsAsCSV.getAbsolutePath().concat("AggregatedMeasures.CSV"));
			exportEncodedAggregatedMeasuresToCSV(matrix, outputAggregatedMeasuresFile);

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

		if (outParams.fileToSaveAsJSON != null) {
			outputFile = retrieveFile(outParams.fileToSaveAsJSON);
			logger.info("Saving the discovered process as JSON in " + outputFile + "...");

			double before = System.currentTimeMillis();


//			Detailed traces results
// 			TODO parametrize the choice between encoded/unencoded result
			exportEncodedReadable3DMatrixToJson(matrix, outputFile);
//			exportReadable3DMatrixToJson(matrix, outputFile);

//			Aggregated Log measures
			outputAggregatedMeasuresFile = new File(outParams.fileToSaveAsJSON.getAbsolutePath().concat("AggregatedMeasures.json"));
			exportEncodedAggregatedMeasuresToJson(matrix, outputAggregatedMeasuresFile);

			double after = System.currentTimeMillis();
			logger.info("Total JSON serialization time: " + (after - before));
		}
	}

	public void manageCheckOutput(MegaMatrixMonster matrix,
								  ViewCmdParameters viewParams, OutputModelParameters outParams, SystemCmdParameters systemParams) {
		this.manageCheckOutput(matrix, null, outParams, viewParams, systemParams, null);
	}


	/**
	 * Export to CSV the detailed result at the level of the events in all the traces
	 *
	 * @param megaMatrix
	 * @param outputFile
	 */
	public void exportEncodedReadable3DMatrixToCSV(MegaMatrixMonster megaMatrix, File outputFile) {
		logger.debug("CSV encoded readable serialization...");

//		header row
//		TODO make the columns parametric, not hard-coded
		String[] header = new String[]{
				"Trace",
				"Constraint",
				"Events-Evaluation",
				"Support",
				"Confidence",
				"Lovinger"
		};

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
				String traceString = tr.encodeTrace();

//              for each trace
				for (int constraint = 0; constraint < matrix[trace].length; constraint++) {
//                  for each constraint
					String[] row = new String[]{
							traceString,
							automata.get(constraint).toString(),
							Arrays.toString(matrix[trace][constraint]),
							String.valueOf(megaMatrix.getSpecificSupport(trace, constraint)),
							String.valueOf(megaMatrix.getSpecificConfidence(trace, constraint)),
							String.valueOf(megaMatrix.getSpecificLovinger(trace, constraint))
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
	 */
	public void exportEncodedAggregatedMeasuresToCSV(MegaMatrixMonster megaMatrix, File outputAggregatedMeasuresFile) {
		DescriptiveStatistics[][] constraintsLogMeasure = megaMatrix.getConstraintLogMeasures();

		List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

//		header row
//		TODO make the columns parametric, not hard-coded
		String[] header = new String[]{
				"Constraint",
				"Quality-Measure",
				"Duck-Tape",
				"Mean",
				"Geometric-Mean",
				"Variance",
				"Population-variance",
				"Standard-Deviation",
				"Percentile-75th",
				"Max",
				"Min"
		};

		try {
			FileWriter fw = new FileWriter(outputAggregatedMeasuresFile);
			CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT.withHeader(header).withDelimiter(';'));


			//		Row builder
			for (int constraint = 0; constraint < constraintsLogMeasure.length; constraint++) {
				String constraintName = automata.get(constraint).toString();
				DescriptiveStatistics[] constraintLogMeasure = constraintsLogMeasure[constraint];

				for (int measureIndex = 0; measureIndex < megaMatrix.getMeasureNames().length; measureIndex++) {
					String[] row = new String[]{
							constraintName,
							megaMatrix.getMeasureName(measureIndex),
							String.valueOf(Measures.getLogDuckTapeMeasures(constraint, measureIndex, megaMatrix.getMatrix())),
							String.valueOf(constraintLogMeasure[measureIndex].getMean()),
							String.valueOf(constraintLogMeasure[measureIndex].getGeometricMean()),
							String.valueOf(constraintLogMeasure[measureIndex].getVariance()),
							String.valueOf(constraintLogMeasure[measureIndex].getPopulationVariance()),
							String.valueOf(constraintLogMeasure[measureIndex].getStandardDeviation()),
							String.valueOf(constraintLogMeasure[measureIndex].getPercentile(75)),
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
	 * Builds the json structure for a given constraint
	 */
	private JsonElement aggregatedConstraintMeasuresJsonBuilder(MegaMatrixMonster megaMatrix, int constaintIndex, DescriptiveStatistics[] constraintLogMeasure) {
		JsonObject constraintJson = new JsonObject();

		for (int measureIndex = 0; measureIndex < megaMatrix.getMeasureNames().length; measureIndex++) {
			JsonObject measure = new JsonObject();

			JsonObject stats = new JsonObject();

			stats.addProperty("Mean", constraintLogMeasure[measureIndex].getMean());
			stats.addProperty("Geometric Mean", constraintLogMeasure[measureIndex].getGeometricMean());
			stats.addProperty("Variance", constraintLogMeasure[measureIndex].getVariance());
			stats.addProperty("Population  variance", constraintLogMeasure[measureIndex].getPopulationVariance());
			stats.addProperty("Standard Deviation", constraintLogMeasure[measureIndex].getStandardDeviation());
			stats.addProperty("Percentile 75th", constraintLogMeasure[measureIndex].getPercentile(75));
			stats.addProperty("Max", constraintLogMeasure[measureIndex].getMax());
			stats.addProperty("Min", constraintLogMeasure[measureIndex].getMin());


			measure.add("stats", stats);
			measure.addProperty("duck tape", Measures.getLogDuckTapeMeasures(constaintIndex, measureIndex, megaMatrix.getMatrix()));

			constraintJson.add(megaMatrix.getMeasureName(measureIndex), measure);
		}

		return constraintJson;
	}

	/**
	 * write the jon file with the aggregated measures
	 *
	 * @param megaMatrix
	 * @param outputFile
	 */
	public void exportEncodedAggregatedMeasuresToJson(MegaMatrixMonster megaMatrix, File outputFile) {
		logger.debug("JSON encoded aggregated measures...");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			FileWriter fw = new FileWriter(outputFile);
			JsonObject jsonOutput = new JsonObject();

			List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

//			\/ \/ \/ LOG RESULTS
			DescriptiveStatistics[][] constraintLogMeasure = megaMatrix.getConstraintLogMeasures();

			for (int constraint = 0; constraint < constraintLogMeasure.length; constraint++) {
				jsonOutput.add(
						automata.get(constraint).toString(),
						aggregatedConstraintMeasuresJsonBuilder(megaMatrix, constraint, constraintLogMeasure[constraint])
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
	 * Serialize the 3D matrix as-is into a Json file
	 */
	public void exportRaw3DMatrixToJson(MegaMatrixMonster megaMatrix, File outputFile) {
		logger.info("JSON serialization...");

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			FileWriter fw = new FileWriter(outputFile);
			gson.toJson(megaMatrix.getMatrix(), fw);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug("JSON serialization...DONE!");

	}

	/**
	 * Serialize the 3D matrix into a Json file to have a readable result, but encoded events
	 */
	public void exportEncodedReadable3DMatrixToJson(MegaMatrixMonster megaMatrix, File outputFile) {
		logger.debug("JSON encoded readable serialization...");
		try {
			FileWriter fw = new FileWriter(outputFile);
			fw.write("{\n");

			byte[][][] matrix = megaMatrix.getMatrix();
			Iterator<LogTraceParser> it = megaMatrix.getLog().traceIterator();
			List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

//        for the entire log
			for (int trace = 0; trace < matrix.length; trace++) {
				LogTraceParser tr = it.next();
				String traceString = tr.encodeTrace();
				fw.write("\t\"" + traceString + "\": [\n");

//              for each trace
				for (int constraint = 0; constraint < matrix[trace].length; constraint++) {

//                  for each constraint
					String constraintString = automata.get(constraint).toString();
					fw.write("\t\t{\"" + constraintString + "\": [ ");
					for (int eventResult = 0; eventResult < matrix[trace][constraint].length; eventResult++) {
						char event = traceString.charAt(eventResult);
						byte result = matrix[trace][constraint][eventResult];
						if (eventResult == (matrix[trace][constraint].length - 1)) {
							fw.write("{\"" + event + "\": " + result + "}");
						} else {
							fw.write("{\"" + event + "\": " + result + "},");
						}
					}
//					TODO do not hard code the specific measure, but use a method that takes all the available one
					if (constraint == (matrix[trace].length - 1)) {
						fw.write(" ],\n\t\t \"Support\": " + megaMatrix.getSpecificSupport(trace, constraint)
								+ " , \"Confidence\": " + megaMatrix.getSpecificConfidence(trace, constraint)
								+ " , \"Lovinger\": " + megaMatrix.getSpecificLovinger(trace, constraint)
								+ "}\n");
					} else {
						fw.write(" ],\n\t\t \"Support\": " + megaMatrix.getSpecificSupport(trace, constraint)
								+ " , \"Confidence\": " + megaMatrix.getSpecificConfidence(trace, constraint)
								+ " , \"Lovinger\": " + megaMatrix.getSpecificLovinger(trace, constraint)
								+ "},\n");
					}

				}

				if (trace == (matrix.length - 1)) {
					fw.write("\t]\n");
				} else {
					fw.write("\t],\n");
				}

			}
			fw.write("}");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug("JSON encoded readable serialization...DONE!");

	}

	/**
	 * Serialize the 3D matrix into a Json file to have a readable result
	 */
	public void exportReadable3DMatrixToJson(MegaMatrixMonster megaMatrix, File outputFile) {
		logger.info("JSON readable serialization...");
		try {
			FileWriter fw = new FileWriter(outputFile);
			fw.write("{\n");

			byte[][][] matrix = megaMatrix.getMatrix();
			Iterator<LogTraceParser> it = megaMatrix.getLog().traceIterator();
			List<SeparatedAutomatonOfflineRunner> automata = (List) megaMatrix.getAutomata();

//        for the entire log
			for (int trace = 0; trace < matrix.length; trace++) {
				LogTraceParser tr = it.next();
				tr.init();
				fw.write("\t\"<");
				int i = 0;
				while (i < tr.length()) {
					String traceString = tr.parseSubsequent().getEvent().getTaskClass().toString();
					if (i == (tr.length() - 1)) {
						fw.write(traceString);
					} else {
						fw.write(traceString + ",");
					}
					i++;
				}
				fw.write(">\": [\n");

//              for each trace
				for (int constraint = 0; constraint < matrix[trace].length; constraint++) {
					tr.init();
//                  for each constraint
					String constraintString = automata.get(constraint).toStringDecoded(tr.getLogParser().getTaskCharArchive().getTranslationMapById());

					fw.write("\t\t{\"" + constraintString + "\": [ ");
					for (int eventResult = 0; eventResult < matrix[trace][constraint].length; eventResult++) {
						String event = tr.parseSubsequent().getEvent().getTaskClass().toString();
						byte result = matrix[trace][constraint][eventResult];
						if (eventResult == (matrix[trace][constraint].length - 1)) {
							fw.write("{\"" + event + "\": " + result + "}");
						} else {
							fw.write("{\"" + event + "\": " + result + "},");
						}
					}
					if (constraint == (matrix[trace].length - 1)) {
						fw.write(" ]}\n");
					} else {
						fw.write(" ]},\n");
					}

				}

				if (trace == (matrix.length - 1)) {
					fw.write("\t]\n");
				} else {
					fw.write("\t],\n");
				}

			}
			fw.write("}");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug("JSON readable serialization...DONE!");

	}


}
