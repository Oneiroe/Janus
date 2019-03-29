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
 * 0 -> 00 -> Activator: False, Target: False
 * 1 -> 01 -> Activator: False, Target: true
 * 2 -> 10 -> Activator: True,  Target: False
 * 3 -> 11 -> Activator: True,  Target: True
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

}
