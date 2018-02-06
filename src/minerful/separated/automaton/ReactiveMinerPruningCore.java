package minerful.separated.automaton;

import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.core.MinerFulQueryingCore;
import org.apache.log4j.Logger;

/**
 * Class for cleaning and pruning a process model mined through the reactive miner/separation technique
 */
public class ReactiveMinerPruningCore {
	protected static Logger logger;
	protected ProcessModel processModel;

	{
		if (logger == null) {
			logger = Logger.getLogger(MinerFulQueryingCore.class.getCanonicalName());
		}
	}

	public ReactiveMinerPruningCore(ProcessModel processModel) {
		this.processModel=processModel;
	}

	/**
	 * Removes the constraints not considered in the mining process
	 */
	public void pruneNonActiveConstraints(){
		logger.info("Pruning non active constraints...");
		for(Constraint c: this.processModel.bag.getAllConstraints()){
			if(c.getSupport()>0 || c.getConfidence()>0) continue;

			this.processModel.bag.remove(c);
		}
	}


}
