/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful;

import minerful.io.params.InputModelParameters;
import minerful.logmaker.errorinjector.ErrorInjector;
import minerful.logmaker.errorinjector.ErrorInjectorFactory;
import minerful.logmaker.errorinjector.params.ErrorInjectorCmdParameters;
import minerful.logmaker.params.LogMakerParameters;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.stringsmaker.MinerFulStringTracesMaker;
import minerful.stringsmaker.params.StringTracesMakerCmdParameters;
import minerful.utils.MessagePrinter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class MinerFulErrorInjectedLogMakerStarter extends MinerFulMinerStarter {
    public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulErrorInjectedLogMakerStarter.class);

    @Override
    public Options setupOptions() {
        Options cmdLineOptions = new Options();

        Options systemOptions = SystemCmdParameters.parseableOptions(),
                inputModelOptions = InputModelParameters.parseableOptions(),
                logMakOptions = LogMakerParameters.parseableOptions(),
                errorInjectorOptions = ErrorInjectorCmdParameters.parseableOptions(),
                inputLogOptions = InputLogCmdParameters.parseableOptions();

        for (Object opt : systemOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : inputModelOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : logMakOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : errorInjectorOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : inputLogOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }

        return cmdLineOptions;
    }

    public static String[] injectErrors(String[] testBedArray, Character[] alphabet, ErrorInjectorCmdParameters errorInjexParams) {
        ErrorInjectorFactory errorInjexFactory = new ErrorInjectorFactory();
        ErrorInjector errorInjex = errorInjexFactory.createErrorInjector(
                errorInjexParams.getErrorInjectionSpreadingPolicy(),
                errorInjexParams.getErrorType(),
                testBedArray);

        errorInjex.setAlphabet(alphabet); //TODO take it from InputModel Options, not from traceMaker
        errorInjex.setErrorsInjectionPercentage(errorInjexParams.getErrorsInjectionPercentage());
        if (errorInjexParams.isTargetCharDefined())
            errorInjex.setTargetChar(errorInjexParams.getTargetChar());

        logger.trace(
                (
                        "\n\n"
                                + "Error injection spreading policy: " + errorInjexParams.getErrorInjectionSpreadingPolicy() + "\n"
                                + "Error injection type: " + errorInjexParams.getErrorType() + "\n"
                                + "Error injection percentage: " + errorInjexParams.getErrorsInjectionPercentage() + "\n"
                                + "Target character: " + errorInjexParams.getTargetChar()
                ).replaceAll("\n", "\n\t")
        );

        testBedArray = errorInjex.injectErrors();

        if (errorInjexParams.logFile != null) {
            StringBuffer tracesBuffer = new StringBuffer();
            FileWriter fileWri = null;
            try {
                fileWri = new FileWriter(errorInjexParams.logFile);
            } catch (IOException e) {
                logger.error("File writing error", e);
            }
            for (int i = 0; i < testBedArray.length; i++) {
                tracesBuffer.append(testBedArray[i] + "\n");
            }
            if (tracesBuffer.length() > 0) {
                try {
                    fileWri.write(tracesBuffer.toString());
                    fileWri.flush();
                } catch (IOException e) {
                    logger.error("File writing error", e);
                }
                logger.info("Error-injected log file stored in: " + errorInjexParams.logFile.getAbsolutePath());
            }
        }

        return testBedArray;
    }


    public static void main(String[] args) {
        MinerFulErrorInjectedLogMakerStarter logMakerStarter = new MinerFulErrorInjectedLogMakerStarter();
        Options cmdLineOptions = logMakerStarter.setupOptions();

        SystemCmdParameters systemParams =
                new SystemCmdParameters(
                        cmdLineOptions,
                        args);
        InputModelParameters inputModelParams =
                new InputModelParameters(
                        cmdLineOptions,
                        args);
        LogMakerParameters logMakParameters =
                new LogMakerParameters(
                        cmdLineOptions,
                        args);
        ErrorInjectorCmdParameters errorInjexParams =
                new ErrorInjectorCmdParameters(
                        cmdLineOptions,
                        args);
        InputLogCmdParameters inputLogParams =
                new InputLogCmdParameters(
                        cmdLineOptions,
                        args);

        if (systemParams.help) {
            systemParams.printHelp(cmdLineOptions);
            System.exit(0);
        }

        if (inputModelParams.inputFile == null) {
            systemParams.printHelpForWrongUsage("Input process model file missing!");
            System.exit(1);
        }

        MessagePrinter.configureLogging(systemParams.debugLevel);

        String[] testBedArray =new String[0];

        if (inputLogParams.inputLogFile == null){
            testBedArray = new MinerFulLogMakerLauncher(inputModelParams, logMakParameters, systemParams).makeLog();
        }else {
            logger.info("Reading input log");
            List temp= new LinkedList<String>();
            LogParser logParser= MinerFulMinerLauncher.deriveLogParserFromLogFile(inputLogParams);
            for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
                LogTraceParser tr = it.next();
                tr.init();
                temp.add(tr.printStringTrace());
//                System.out.println(tr.printStringTrace());
            }
            testBedArray= (String[]) temp.toArray(new String[temp.size()]);

        }

        Set<Character> tempAlphabet = new HashSet<Character>();
        for (String trace:testBedArray) {
            for (char event : trace.toCharArray()) {
                tempAlphabet.add(event);
            }
        }
        Character[] alphabet= tempAlphabet.toArray(new Character[tempAlphabet.size()]);

        testBedArray = injectErrors(testBedArray, alphabet, errorInjexParams);

        logger.debug(
                "\n"
                        + "[Testbed after error injection]");
        for (int i = 0; i < testBedArray.length; i++) {
            logger.debug(String.format("%0" + (int) (Math.ceil(Math.log10(testBedArray.length))) + "d", (i)) + ")\t" + testBedArray[i]);
        }

    }
}