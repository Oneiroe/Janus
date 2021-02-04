/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful;

import minerful.checking.params.CheckingCmdParameters;
import minerful.concept.TaskCharArchive;
import minerful.io.params.InputModelParameters;
import minerful.io.params.OutputModelParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.reactive.checking.MegaMatrixMonster;
import minerful.reactive.io.JanusCheckingOutputManagementLauncher;
import minerful.reactive.params.JanusCheckingCmdParameters;
import minerful.utils.MessagePrinter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class JanusModelCheckStarter extends MinerFulMinerStarter {
    public static MessagePrinter logger = MessagePrinter.getInstance(JanusModelCheckStarter.class);

    @Override
    public Options setupOptions() {
        Options cmdLineOptions = new Options();

        Options systemOptions = SystemCmdParameters.parseableOptions(),
                outputOptions = OutputModelParameters.parseableOptions(),
                postPrOptions = PostProcessingCmdParameters.parseableOptions(),
                viewOptions = ViewCmdParameters.parseableOptions(),
                chkOptions = CheckingCmdParameters.parseableOptions(),
                inputLogOptions = InputLogCmdParameters.parseableOptions(),
                inpuModlOptions = InputModelParameters.parseableOptions(),
                janusOptions = JanusCheckingCmdParameters.parseableOptions();

        for (Object opt : systemOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : outputOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : postPrOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : viewOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : chkOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : inputLogOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : inpuModlOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }
        for (Object opt : janusOptions.getOptions()) {
            cmdLineOptions.addOption((Option) opt);
        }

        return cmdLineOptions;
    }

    public static void main(String[] args) {
        JanusModelCheckStarter checkStarter = new JanusModelCheckStarter();
        Options cmdLineOptions = checkStarter.setupOptions();

        SystemCmdParameters systemParams =
                new SystemCmdParameters(
                        cmdLineOptions,
                        args);
        OutputModelParameters outParams =
                new OutputModelParameters(
                        cmdLineOptions,
                        args);
        PostProcessingCmdParameters preProcParams =
                new PostProcessingCmdParameters(
                        cmdLineOptions,
                        args);
        CheckingCmdParameters chkParams =
                new CheckingCmdParameters(
                        cmdLineOptions,
                        args);
        InputLogCmdParameters inputLogParams =
                new InputLogCmdParameters(
                        cmdLineOptions,
                        args);
        InputModelParameters inpuModlParams =
                new InputModelParameters(
                        cmdLineOptions,
                        args);
        ViewCmdParameters viewParams =
                new ViewCmdParameters(
                        cmdLineOptions,
                        args);
        JanusCheckingCmdParameters janusParams =
                new JanusCheckingCmdParameters(
                        cmdLineOptions,
                        args);

        MessagePrinter.configureLogging(systemParams.debugLevel);

        if (systemParams.help) {
            systemParams.printHelp(cmdLineOptions);
            System.exit(0);
        }
        JanusModelCheckLauncher miFuCheLa = new JanusModelCheckLauncher(inpuModlParams, preProcParams, inputLogParams, chkParams, systemParams, janusParams);
        MegaMatrixMonster evaluation = miFuCheLa.checkModel();

        TaskCharArchive alphabet = miFuCheLa.getProcessSpecification().getTaskCharArchive(); // note. The character mapping of the model is greater or equal to the log parser one because it is constructed starting from it
        new JanusCheckingOutputManagementLauncher().manageCheckOutput(evaluation, viewParams, outParams, systemParams, alphabet);
    }
}