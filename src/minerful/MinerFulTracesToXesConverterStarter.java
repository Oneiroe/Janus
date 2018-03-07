package minerful;

import minerful.io.encdec.log.IOutEncoder;
import minerful.io.encdec.log.MxmlEncoder;
import minerful.io.encdec.log.XesEncoder;
import minerful.params.SystemCmdParameters;
import minerful.stringsmaker.MinerFulStringTracesMaker;
import minerful.stringsmaker.params.StringTracesToXesConverterCmdParameters;
import minerful.utils.MessagePrinter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.*;
import java.util.ArrayList;

public class MinerFulTracesToXesConverterStarter extends AbstractMinerFulStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulTracesToXesConverterStarter.class);
			
	@Override
	public Options setupOptions() {
    	Options cmdLineOptions = new Options();
    	
    	Options systemOptions = SystemCmdParameters.parseableOptions(),
    			tracesMakOptions = StringTracesToXesConverterCmdParameters.parseableOptions();
    	
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: tracesMakOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	
    	return cmdLineOptions;
	}

    public static void main(String[] args) {
    	MinerFulTracesToXesConverterStarter traMakeStarter = new MinerFulTracesToXesConverterStarter();
    	Options cmdLineOptions = traMakeStarter.setupOptions();

		StringTracesToXesConverterCmdParameters tracesMakParams =
    			new StringTracesToXesConverterCmdParameters(
    					cmdLineOptions,
    					args);
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
        
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
        
    	MessagePrinter.configureLogging(systemParams.debugLevel);

    	MinerFulStringTracesMaker traMaker = new MinerFulStringTracesMaker();
    	
//    	String[] traces = traMaker.makeTraces(tracesMakParams);
		String[] traces = new String[0];

		try {
			File inFile = new File(tracesMakParams.inLogFile.toPath().toAbsolutePath().toString());
			BufferedReader inBuf = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String line;
			ArrayList<String> appLog = new ArrayList();
			while ((line = inBuf.readLine()) != null) {
				appLog.add(line);
			}
			traces=appLog.toArray(traces);

		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception");
			e.printStackTrace();
		}

		store(tracesMakParams, traces);
	}

	public static boolean store(StringTracesToXesConverterCmdParameters params, String[] traces) {
		// saving
		IOutEncoder outEnco = null;
		switch (params.outputEncoding) {
			case xes:
				outEnco = new XesEncoder(traces);
				break;
			case mxml:
				outEnco = new MxmlEncoder(traces);
				break;
			default:
				break;
		}

		if (outEnco != null) {
			try {
				if (params.logFile != null) {
					outEnco.encodeToFile(params.logFile);
				} else {
					MessagePrinter.printlnOut(outEnco.encodeToString());
					System.out.flush();
				}
			} catch (IOException e) {
				logger.error("Encoding error", e);
				return false;
			}
		} else {
			FileWriter fileWri = null;
			if (params.logFile != null) {
				try {
					fileWri = new FileWriter(params.logFile);
				} catch (IOException e) {
					logger.error("File writing error", e);
					return false;
				}

				if (traces.length > 0) {
					StringBuffer tracesBuffer = new StringBuffer();

					for (int i = 0; i < traces.length; i++) {
						tracesBuffer.append(traces[i] + "\n");
					}

					try {
						fileWri.write(tracesBuffer.toString());
						fileWri.flush();
					} catch (IOException e) {
						logger.error("File writing error", e);
						return false;
					}
				}
			}
		}
		return true;
	}
}