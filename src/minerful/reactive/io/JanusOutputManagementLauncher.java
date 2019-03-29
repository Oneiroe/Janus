package minerful.reactive.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.constraint.Constraint;
import minerful.io.encdec.ProcessModelEncoderDecoder;
import minerful.io.params.OutputModelParameters;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.reactive.automaton.SeparatedAutomatonOfflineRunner;
import minerful.reactive.checking.MegaMatrixMonster;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;

/**
 * Class to handle the output of Janus
 */
public class JanusOutputManagementLauncher extends MinerFulOutputManagementLauncher {

	public void manageCheckOutput(MegaMatrixMonster matrix, NavigableMap<Constraint, String> additionalCnsIndexedInfo, OutputModelParameters outParams, ViewCmdParameters viewParams, SystemCmdParameters systemParams, LogParser logParser) {
		File outputFile = null;

		if (outParams.fileToSaveConstraintsAsCSV != null) {
//			TODO CSV output
			logger.info("CSV output yet not implemented");
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

// 			TODO parametrize the choice between encoded/unencoded result
			exportEncodedReadable3DMatrixToJson(matrix, outputFile);
//			exportReadable3DMatrixToJson(matrix, outputFile);

			double after = System.currentTimeMillis();
			logger.info("Total JSON serialization time: " + (after - before));
		}
	}

	public void manageCheckOutput(MegaMatrixMonster matrix,
								  ViewCmdParameters viewParams, OutputModelParameters outParams, SystemCmdParameters systemParams) {
		this.manageCheckOutput(matrix, null, outParams, viewParams, systemParams, null);
	}

	/**
	 * Serialize the 3D matrix as-is into a Json file
	 */
	private void exportRaw3DMatrixToJson(MegaMatrixMonster megaMatrix, File outputFile) {
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
	private void exportEncodedReadable3DMatrixToJson(MegaMatrixMonster megaMatrix, File outputFile) {
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
		logger.debug("JSON encoded readable serialization...DONE!");

	}

	/**
	 * Serialize the 3D matrix into a Json file to have a readable result
	 */
	private void exportReadable3DMatrixToJson(MegaMatrixMonster megaMatrix, File outputFile) {
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
