/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.*;
import minerful.concept.constraint.relation.*;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapReaderWriter;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.LogParser;
import minerful.logparser.StringLogParser;
import minerful.logparser.XesLogParser;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.relevance.ConstraintsRelevanceEvaluator;
import minerful.utils.MessagePrinter;

import org.apache.log4j.Logger;

public class MinerFulVacuityChecker {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulVacuityChecker.class);
			
	/**
	 * Task place-holders to be used as parameters for the constraint templates to check.
	 */
    public static TaskChar
		a = new TaskChar('A'),
		b = new TaskChar('B'),
		c = new TaskChar('C'),
		x = new TaskChar('X'),
		y = new TaskChar('Y');

	/**
	 * Constraint templates to be checked.
	 */
	 // Toggle the comment to add/remove the template from the set of checked ones.
	 public static Constraint[] parametricConstraints =
		new Constraint[] {
//			new SequenceResponse21(a,b,x),
//			new SequenceResponse22(a,b,x,y),
//			new SequenceResponse32(a,b,c,x,y),
			new Participation(a),	// a.k.a. Existence(1, a)
			new AtMostOne(a),	// a.k.a. Absence(2, a)
			new Init(a),
			new End(a),
			new RespondedExistence(a,b),
			new Response(a, b),
			new AlternateResponse(a,b),
			new ChainResponse(a,b),
			new Precedence(a,b),
			new AlternatePrecedence(a,b),
			new ChainPrecedence(a,b),
			new CoExistence(a,b),
			new Succession(a,b),
			new AlternateSuccession(a, b),
			new ChainSuccession(a, b),
			new NotChainSuccession(a, b),
			new NotSuccession(a, b),
			new NotCoExistence(a, b),
    };

	public static void main(String[] args) throws Exception {
		System.err.println(
				"#### WARNING"
				+ "\n" +
				"This class is not yet part of the MINERful framework. It is meant to be the proof-of-concept software for the paper entitled \"Semantical Vacuity Detection in Declarative Process Mining\", authored by F.M. Maggi, M. Montali, C. Di Ciccio, and J. Mendling. Please use it for testing purposes only."
				+ "\n\n" +
		
				"#### USAGE"
				+ "\n" +
				"Usage: java " + MinerFulVacuityChecker.class.getCanonicalName() + " <XES-log-file-path> [threshold] [Declare-map-output-file-path]."
				+ "\n" +
				"Param:    <XES-log-file-path>: the path to a XES event log file (mandatory)"
				+ "\n" +
				"Param:    [threshold]: the ratio of traces in which the constraints have to be non-vacuously satisfied, from 0.0 to 1.0 (default: " + ConstraintsRelevanceEvaluator.DEFAULT_SATISFACTION_THRESHOLD + ") (optional)"
				+ "\n" +
				"Param:    [Declare-map-output-file-path]: the path of the file in which the returned constraints are stored as a Declare Map XML file (by default, no Declare Map XML file is saved) (optional)"
				+ "\n\n" +

				"#### OUTPUT"
				+ "\n" +
				"To customise the constraint templates to be checked, please change the code of this class (" + MinerFulVacuityChecker.class.getCanonicalName() + ") in the specified point and recompile."
				+ "\n" +
				"The printed output is a CSV-encoding of constraints that are non-vacuously satisfied in the given log. The output can be also saved as a Declare Map XML file by specifying the third optional command parameter (for standard Declare constraints only) -- see above: [Declare-map-output-file-path]."
				+ "\n\n" +
				
				"Press any key to continue..."
		);
		
//		System.in.read();

		MessagePrinter.configureLogging(DebugLevel.all);

		LogParser loPar = null;
		try {
			loPar = new XesLogParser(new File(args[0]), ClassificationType.LOG_SPECIFIED);
		} catch (Exception e) {
			MessagePrinter.printlnOut(args[0] + " is not an XES file");
			loPar = new StringLogParser(new File(args[0]), ClassificationType.NAME);
		}

		ConstraintsRelevanceEvaluator evalon = null;

		if (args.length > 1) {
			evalon = new ConstraintsRelevanceEvaluator(loPar, parametricConstraints, Double.valueOf(args[1]));
		} else {
			evalon = new ConstraintsRelevanceEvaluator(loPar, parametricConstraints);
		}
		evalon.runOnTheLog();

		MessagePrinter.printlnOut(evalon.printEvaluationsCSV());

		/*Alessio save output model as CSV, not just print it*/

		try {
			PrintWriter outWriter = new PrintWriter(args[3]);
			outWriter.print(evalon.printEvaluationsCSV());
			outWriter.flush();
			outWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/**/

		if (args.length > 2) {
			logger.debug("Storing fully-supported default-Declare constraints as a Declare map on " + args[2]);

			Collection<Constraint> nuStandardConstraints = new ArrayList<Constraint>();
			Double supportThreshold = Double.valueOf(args[1]);

			for (Constraint con : evalon.getNuConstraints()) {
				if (con.getFamily() != null && con.getSupport() >= supportThreshold) {
					nuStandardConstraints.add(con);
				}
			}

			ConstraintsBag coBag = new ConstraintsBag(loPar.getTaskCharArchive().getTaskChars(), nuStandardConstraints);
			ProcessModel model = new ProcessModel(loPar.getTaskCharArchive(), coBag);
			DeclareMapReaderWriter.marshal(args[2], new DeclareMapEncoderDecoder(model).createDeclareMap());
			
			logger.debug("Done.");
		}
	}
}