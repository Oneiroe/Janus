/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.stringsmaker.params;

import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logmaker.params.LogMakerCmdParameters;
import minerful.params.ParamsManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.io.File;

public class StringTracesToXesConverterCmdParameters extends ParamsManager {
	public static final String OUTPUT_FILE_PARAM_NAME = "oLF";
    public static final String OUT_ENC_PARAM_NAME = "oE";
	public static final String INPUT_FILE_PARAM_NAME = "iLF";

    public File logFile;
    public File inLogFile;
    public LogMakerCmdParameters.Encoding outputEncoding;

    public StringTracesToXesConverterCmdParameters() {
    	super();
    	logFile = null;
    	inLogFile = null;
        outputEncoding = LogMakerCmdParameters.Encoding.string;
    }

    public StringTracesToXesConverterCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public StringTracesToXesConverterCmdParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        // validate that block-size has been set
        this.outputEncoding = Enum.valueOf(
        		LogMakerCmdParameters.Encoding.class,
        		line.getOptionValue(OUT_ENC_PARAM_NAME, this.outputEncoding.toString())
		);
        if (line.hasOption(StringTracesToXesConverterCmdParameters.OUTPUT_FILE_PARAM_NAME)) {
        	this.logFile = new File(line.getOptionValue(StringTracesToXesConverterCmdParameters.OUTPUT_FILE_PARAM_NAME));
        }
        if (line.hasOption(StringTracesToXesConverterCmdParameters.INPUT_FILE_PARAM_NAME)) {
        	this.inLogFile = new File(line.getOptionValue(StringTracesToXesConverterCmdParameters.INPUT_FILE_PARAM_NAME));
        }
	}
    
	@Override
    public Options addParseableOptions(Options options) {
		Options myOptions = listParseableOptions();
		for (Object myOpt: myOptions.getOptions())
			options.addOption((Option)myOpt);
        return options;
	}
	
	@Override
    public Options listParseableOptions() {
    	return parseableOptions();
    }
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("file path")
                .withLongOpt("in-log")
                .withDescription("path to the file to read the log from")
                .withType(new String())
                .create(StringTracesToXesConverterCmdParameters.INPUT_FILE_PARAM_NAME)
    	);
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("file path")
                .withLongOpt("out-log")
                .withDescription("path to the file to write the log in")
                .withType(new String())
                .create(StringTracesToXesConverterCmdParameters.OUTPUT_FILE_PARAM_NAME)
    	);
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("encoding")
                .withLongOpt("out-enc")
                .withDescription("encoding language for output log " + printValues(LogMakerCmdParameters.Encoding.values()))
                .withType(new String())
                .create(StringTracesToXesConverterCmdParameters.OUT_ENC_PARAM_NAME)
    	);
        
        return options;
    }
}