package minerful.reactive.miner;

import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.miner.core.MinerFulQueryingCore;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import org.apache.log4j.Logger;

/**
 * Class for cleaning and pruning a process model mined through the reactive miner/separation technique
 */
public class ReactiveMinerPruningCore {
	protected static Logger logger;
	protected ProcessModel processModel;
	protected MinerFulCmdParameters minerFulParams;
	protected PostProcessingCmdParameters postParams;

	{
		if (logger == null) {
			logger = Logger.getLogger(MinerFulQueryingCore.class.getCanonicalName());
		}
	}

	public ReactiveMinerPruningCore(ProcessModel processModel, MinerFulCmdParameters minerFulParams, PostProcessingCmdParameters postParams) {
		this.processModel = processModel;
		this.minerFulParams = minerFulParams;
		this.postParams = postParams;
	}

	/**
	 * Removes the constraints not considered in the mining process
	 */
	public void pruneNonActiveConstraints() {
		logger.info("Pruning non active constraints...");
		if(!this.postParams.cropRedundantAndInconsistentConstraints){
			return;
		}
		for (Constraint c : this.processModel.bag.getAllConstraints()) {
			if ((c.getConfidence() >= postParams.confidenceThreshold) && ((c.getSupport() >= postParams.supportThreshold))) continue;
			this.processModel.bag.remove(c);
		}
	}


}
