package minerful.reactive.checking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.reactive.automaton.SeparatedAutomatonOfflineRunner;
import minerful.reactive.miner.ReactiveMinerOfflineQueryingCore;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Data structure for the fine grain evaluation result of constraints in each event of a log traces
 *
 * About variable matrix (byte[][][]) bytes meaning:
 * Each byte stores the results of both Activator and target of a given constraint in a specific trace.
 * The left bit is for the activator, the right bit for the target,i.e.,[activator-bit][target-bit]
 * In details:
 *  0 -> 00 -> Activator: False, Target: False
 *  1 -> 01 -> Activator: False, Target: true
 *  2 -> 10 -> Activator: True,  Target: False
 *  3 -> 11 -> Activator: True,  Target: True
 */
public class MegaMatrixMonster {
    protected static Logger logger;
    private final byte[][][] matrix; // [trace index][constraint index][event index]
    private final LogParser log;
    private final Collection<SeparatedAutomatonOfflineRunner> automata;

    {
        if (logger == null) {
            logger = Logger.getLogger(ReactiveMinerOfflineQueryingCore.class.getCanonicalName());
        }
    }
    public MegaMatrixMonster(byte[][][] matrix, LogParser log, Collection<SeparatedAutomatonOfflineRunner> automata) {
        this.matrix = matrix;
        this.log = log;
        this.automata = automata;
    }

    public byte[][][] getMatrix() {
        return matrix;
    }

    public LogParser getLog() {
        return log;
    }

    public Collection<SeparatedAutomatonOfflineRunner> getAutomata() {
        return automata;
    }

    /**
     * Serialize the 3D matrix as-is into a Json file
     */
    public void exportRaw3DMatrixToJson(String outputPath) {
        logger.info("JSON serialization...");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter fw = new FileWriter(outputPath);
            gson.toJson(this.matrix, fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("JSON serialization...DONE!");

    }

    /**
     * Serialize the 3D matrix into a Json file to have a readable result, but encoded events
     */
    public void exportEncodedReadable3DMatrixToJson(File outputFile) {
        logger.debug("JSON encoded readable serialization...");
        try {
            FileWriter fw = new FileWriter(outputFile);
            fw.write("{\n");

            Iterator<LogTraceParser> it = this.log.traceIterator();
            List<SeparatedAutomatonOfflineRunner> automata = (List) this.automata;

//        for the entire log
            for (int trace = 0; trace < this.matrix.length; trace++) {
                LogTraceParser tr = it.next();
                String traceString = tr.encodeTrace();
                fw.write("\t\"" + traceString + "\": [\n");

//              for each trace
                for (int constraint = 0; constraint < this.matrix[trace].length; constraint++) {

//                  for each constraint
                    String constraintString = automata.get(constraint).toString();
                    fw.write("\t\t{\"" + constraintString + "\": [ ");
                    for (int eventResult = 0; eventResult < this.matrix[trace][constraint].length; eventResult++) {
                        char event = traceString.charAt(eventResult);
                        byte result = this.matrix[trace][constraint][eventResult];
                        if (eventResult == (this.matrix[trace][constraint].length - 1)) {
                            fw.write("{\"" + event + "\": " + result + "}");
                        } else {
                            fw.write("{\"" + event + "\": " + result + "},");
                        }
                    }
                    if (constraint == (this.matrix[trace].length - 1)) {
                        fw.write(" ]}\n");
                    } else {
                        fw.write(" ]},\n");
                    }

                }

                if (trace == (this.matrix.length - 1)) {
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
    public void exportReadable3DMatrixToJson(String outputPath) {
        logger.info("JSON readable serialization...");
        try {
            FileWriter fw = new FileWriter(outputPath);
            fw.write("{\n");

            Iterator<LogTraceParser> it = this.log.traceIterator();
            List<SeparatedAutomatonOfflineRunner> automata = (List) this.automata;

//        for the entire log
            for (int trace = 0; trace < this.matrix.length; trace++) {
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
                for (int constraint = 0; constraint < this.matrix[trace].length; constraint++) {
                    tr.init();
//                  for each constraint
                    String constraintString = automata.get(constraint).toStringDecoded(tr.getLogParser().getTaskCharArchive().getTranslationMapById());

                    fw.write("\t\t{\"" + constraintString + "\": [ ");
                    for (int eventResult = 0; eventResult < this.matrix[trace][constraint].length; eventResult++) {
                        String event = tr.parseSubsequent().getEvent().getTaskClass().toString();
                        byte result = this.matrix[trace][constraint][eventResult];
                        if (eventResult == (this.matrix[trace][constraint].length - 1)) {
                            fw.write("{\"" + event + "\": " + result + "}");
                        } else {
                            fw.write("{\"" + event + "\": " + result + "},");
                        }
                    }
                    if (constraint == (this.matrix[trace].length - 1)) {
                        fw.write(" ]}\n");
                    } else {
                        fw.write(" ]},\n");
                    }

                }

                if (trace == (this.matrix.length - 1)) {
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
