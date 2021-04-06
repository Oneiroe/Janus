package minerful.reactive.variant;

import minerful.concept.ProcessModel;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.reactive.automaton.SeparatedAutomatonOfflineRunner;
import minerful.reactive.measurements.MegaMatrixMonster;
import minerful.reactive.measurements.ReactiveMeasurementsOfflineQueryingCore;
import minerful.reactive.params.JanusMeasurementsCmdParameters;
import minerful.reactive.params.JanusVariantCmdParameters;
import org.apache.log4j.Logger;

import java.util.*;


/**
 * Class to organize the variant analysis
 */
public class ReactiveVariantAnalysisCore {

    protected static Logger logger;
    private final LogParser logParser_1;  // original log1 parser
    private final ProcessModel processSpecification1;  // original set of constraints mined from log1
    private final LogParser logParser_2; // original log2 parser
    private final ProcessModel processSpecification2;  // original set of constraints mined from log2
    private final JanusVariantCmdParameters janusVariantParams;  // input parameter of the analysis

    private float[][] lCodedIndex; // encoded log for efficient permutations. only constraints and traces indices are used
    private ProcessModel processSpecificationUnion;  // total set of constraints to analyse, i.e., union of process specification 1 adn 2
    private int processSpecificationUnionSize; // number of constraints in the specification union
    private ProcessModel processSpecificationDifference;  // initial differences of specification 1 and 2 to be checked through the analysis
    private double pValueThreshold; //TODO never used here
    private final String measure;  // expose measure selection to the user
    private static final double MEASURE_THRESHOLD = 0.8;

    private Map<String, Float> spec1; // measurement of the union model over the first variant
    private Map<String, Float> spec2; // measurement of the union model over the second variant

    public static final Map<String, String[]> HIERARCHY = new HashMap<String, String[]>() {{
        put("Participation", new String[]{});
        put("RespondedExistence", new String[]{"Participation($2)"}); //this link is ok only if the simplification works with equivalences of measures, otherwise it is not direct
        put("CoExistence", new String[]{"RespondedExistence($1,$2)", "RespondedExistence($2,$1)"});
        put("Succession", new String[]{"Response($1,$2)", "Precedence($1,$2)", "CoExistence($1,$2)"});
        put("Precedence", new String[]{"RespondedExistence($2,$1)"});
        put("Response", new String[]{"RespondedExistence($1,$2)"});
        put("AlternateSuccession", new String[]{"AlternateResponse($1,$2)", "AlternatePrecedence($1,$2)", "Succession($1,$2)"});
        put("AlternatePrecedence", new String[]{"Precedence($1,$2)"});
        put("AlternateResponse", new String[]{"Response($1,$2)"});
        put("ChainSuccession", new String[]{"ChainResponse($1,$2)", "ChainPrecedence($1,$2)", "AlternateSuccession($1,$2)"});
        put("ChainPrecedence", new String[]{"AlternatePrecedence($1,$2)"});
        put("ChainResponse", new String[]{"AlternateResponse($1,$2)"});
        put("NotCoExistence", new String[]{});
        put("NotSuccession", new String[]{"NotCoExistence($1,$2)"});
        put("NotChainSuccession", new String[]{"NotSuccession($1,$2)"});
    }}; // TODO only direct derivation for now, implement also simplification from combination of rules

    private Map<Integer, String> indexToTraceMap;
    private Map<String, Integer> traceToIndexMap;
    private Map<Integer, String> indexToConstraintMap;
    private Map<String, Integer> constraintToIndexMap;


    private static class PermutationResult {
        float[][] result1; // permutation test results for first group
        float[][] result2; // permutation test results for second group
        float[] test;  // test statistics for the difference of the groups, mind that constraints and permutations indices as swapped wrt the permutation results

        public PermutationResult(float[][] result1, float[][] result2) {
            this.result1 = result1;
            this.result2 = result2;
        }
    }

    {
        if (logger == null) {
            logger = Logger.getLogger(ReactiveMeasurementsOfflineQueryingCore.class.getCanonicalName());
        }
    }

    /**
     * Constructor
     *
     * @param logParser_1
     * @param logParser_2
     * @param janusVariantParams
     */
    public ReactiveVariantAnalysisCore(LogParser logParser_1, ProcessModel processSpecification1, LogParser logParser_2, ProcessModel processSpecification2, JanusVariantCmdParameters janusVariantParams) {
        this.logParser_1 = logParser_1;
        this.processSpecification1 = processSpecification1;
        this.logParser_2 = logParser_2;
        this.processSpecification2 = processSpecification2;
        this.janusVariantParams = janusVariantParams;
        this.pValueThreshold = janusVariantParams.pValue;
        this.measure = janusVariantParams.measure;
    }

    /**
     * Launcher for variant analysis of two logs
     *
     * @return
     */
    public Map<String, Float> check() {
        logger.info("Variant Analysis start");
//        PREPROCESSING
        double before = System.currentTimeMillis();
        //        1. Models differences
//                NOTE USED FOR NOW
        //        setModelsDifferences(processSpecification1, processSpecification2);
        //        2. Models Union (total set of rules to check afterwards)
//        setModelsUnion(processSpecification1, processSpecification2);
        processSpecificationUnion = ProcessModel.union(processSpecification1, processSpecification2);
        processSpecificationUnionSize = processSpecificationUnion.howManyConstraints();
        //        3. Encode log (create efficient log structure for the permutations)
        //        4. Precompute all possible results for the Encoded Log
        encodeLogsIndex(logParser_1, logParser_2, processSpecificationUnion);
        double after = System.currentTimeMillis();
        logger.info("Pre-processing time: " + (after - before));

//        PERMUTATION TEST
        before = System.currentTimeMillis();
        logger.info("Permutations processing...");
//        PermutationResult pRes = permuteResults(lCoded, janusVariantParams.nPermutations, true);
        PermutationResult pRes = permuteResultsIndex(lCodedIndex, janusVariantParams.nPermutations, true);
        logger.info("Significance testing...");
//        Map<String, Double> results = significanceTest(pRes, pValueThreshold);
        Map<String, Float> results = significanceTestIndex(pRes, pValueThreshold);
        after = System.currentTimeMillis();
        logger.info("Permutation test time: " + (after - before));

//        POST-PROCESSING
//        TODO Sort result according to ABS-diff
        return results;
    }


    /**
     * Checks the significance of the permutation test results
     *
     * @param pRes
     * @param pValueThreshold
     * @return
     */
    private Map<String, Float> significanceTestIndex(PermutationResult pRes, double pValueThreshold) {
        Map<String, Float> result = new HashMap<String, Float>();
        int nConstraints = processSpecificationUnionSize;
        int nPermutations = pRes.result1.length;
        pRes.test = new float[nConstraints];
        for (int cIndex = 0; cIndex < nConstraints; cIndex++) {
//            double initialreference = pRes.result1[0][cIndex] - pRes.result2[0][cIndex];  // for NEGATIVE/POSITIVE DISTANCE
            double initialreference = Math.abs(pRes.result1[0][cIndex] - pRes.result2[0][cIndex]); // for ABSOLUTE DISTANCE
            boolean negRef = initialreference < 0;
            pRes.test[cIndex] += 1.0;
            for (int permutation = 1; permutation < nPermutations; permutation++) {
//                NEGATIVE/POSITIVE DISTINCTION:
//                consider the difference in the permutation only if it is extreme in the same sign of the reference difference
//                if (negRef) {
//                    if (pRes.result1[permutation][cIndex] - pRes.result2[permutation][cIndex] <= initialreference) {
//                        pRes.test[cIndex] += 1.0;
//                    }
//                } else {
//                    if (pRes.result1[permutation][cIndex] - pRes.result2[permutation][cIndex] >= initialreference) {
//                        pRes.test[cIndex] += 1.0;
//                    }
//                }
//                ABSOLUTE DISTANCE:
//                consider the absolute difference, regardless of the direction
                if (Math.abs(pRes.result1[permutation][cIndex] - pRes.result2[permutation][cIndex]) >= initialreference) {
                    pRes.test[cIndex] += 1.0;
                }
            }
            pRes.test[cIndex] = pRes.test[cIndex] / nPermutations;

            if (janusVariantParams.oKeep || pRes.test[cIndex] <= pValueThreshold) {
                result.put(indexToConstraintMap.get(cIndex), pRes.test[cIndex]);
            }
        }
        logger.info("Rules Number: " + nConstraints + " ; relevant: " + result.size() + " ; non-relevant: " + (nConstraints - result.size()));
        return result;
    }

    /**
     * Permutation test in which is taken the encoded results.
     *
     * @param lCodedIndex
     * @param nPermutations
     * @param nanCheck
     * @return
     */
    private PermutationResult permuteResultsIndex(float[][] lCodedIndex, int nPermutations, boolean nanCheck) {
        int nConstraints = processSpecificationUnionSize;
        float[][] result1 = new float[nPermutations][nConstraints];
        float[][] result2 = new float[nPermutations][nConstraints];

        int log1Size = logParser_1.length();
        int log2Size = logParser_2.length();
        logger.info("[Tot traces:" + (log1Size + log2Size) + " Constraints:" + processSpecificationUnionSize + "]");
        List<String> permutableTracesList = new LinkedList<>();
        for (Iterator<LogTraceParser> it = logParser_1.traceIterator(); it.hasNext(); ) {
            permutableTracesList.add(it.next().printStringTrace());
        }
        for (Iterator<LogTraceParser> it = logParser_2.traceIterator(); it.hasNext(); ) {
            permutableTracesList.add(it.next().printStringTrace());
        }
        List<Integer> permutableTracesIndexList = new LinkedList<>();
        for (String t : permutableTracesList) {
            permutableTracesIndexList.add(traceToIndexMap.get(t));
        }

        float[] pValues = new float[nConstraints];
        Set<Integer> blackList = new HashSet();

        int step = 25;
        for (int i = 0; i < nPermutations; i++) {
            if (i % step == 0)
                System.out.print("\rPermutation: " + i + "/" + nPermutations);  // Status counter "current trace/total trace"

            for (int c = 0; c < nConstraints; c++) {
                if (!janusVariantParams.oKeep && blackList.contains(c))
                    continue;
                int traceIndex = -1;
                int nanTraces1 = 0;
                int nanTraces2 = 0;
                for (int t : permutableTracesIndexList) {
                    traceIndex++;
                    if (traceIndex < log1Size) {
                        if (nanCheck & Float.isNaN(lCodedIndex[t][c])) {
                            nanTraces1++;
                            continue; // TODO expose in input
                        }
                        result1[i][c] += lCodedIndex[t][c];
                    } else {
                        if (nanCheck & Float.isNaN(lCodedIndex[t][c])) {
                            nanTraces2++;
                            continue; // TODO expose in input
                        }
                        result2[i][c] += lCodedIndex[t][c];
                    }
                }
                result1[i][c] = result1[i][c] / (log1Size - nanTraces1);
                result2[i][c] = result2[i][c] / (log2Size - nanTraces2);
                if (Math.abs(result1[i][c] - result2[i][c]) >= Math.abs(result1[0][c] - result2[0][c])) {
                    pValues[c] += 1.0;
                }
                if (!janusVariantParams.oKeep && (pValues[c] / nPermutations) > janusVariantParams.pValue)
                    blackList.add(c); //if the constraints presernts a pValues greater than the threshold before the end of the permutations, we can discard it immediately
            }
//            permutation "0" are the original logs
            Collections.shuffle(permutableTracesIndexList);
        }
        System.out.print("\rPermutation: " + nPermutations + "/" + nPermutations);
        System.out.println();

        return new PermutationResult(result1, result2);
    }

    /**
     * Encode the input traces for efficient permutation.
     * the result is a Map where the keys are the hash of the traces and the content in another map with key:value as constrain:measure.
     * In this way we check only here the constraints in each trace and later we permute only the results
     * <p>
     * Transform the encoded map into a matrix where traces and constraints are referred by indices.
     * compute the encoding and return the reference mappings
     *
     * @param model
     * @param logParser_1
     * @param logParser_2
     */
    private void encodeLogsIndex(LogParser logParser_1, LogParser logParser_2, ProcessModel model) {
//        encode
        Map<String, Map<String, Float>> lCoded = new HashMap<String, Map<String, Float>>();  // trace: constraint: measure
        lCoded.putAll(encodeLog(logParser_1, model));
        lCoded.putAll(encodeLog(logParser_2, model));

        //  get original measures
        spec1 = getMeasurementsOfOneVariant(true, logParser_1, lCoded);
        spec2 = getMeasurementsOfOneVariant(true, logParser_2, lCoded);

        //  hierarchical simplification
        if (janusVariantParams.simplify) {
            logger.info("Rules simplification...");

            Set<String> constraintsRemovalCandidate = new HashSet<>();

            Set<String> constraintsList = new HashSet<>(spec1.keySet());
            int initConstrNum = processSpecificationUnionSize;

            for (String c : constraintsList) {
                String template = c.split("\\(")[0];
                // skip constraints with only one variable from simplification
                if (c.contains(",") == false || HIERARCHY.get(template) == null) continue;
                String cVar1 = c.split("\\(")[1].replace(")", "").split(",")[0];
                String cVar2 = c.split("\\(")[1].replace(")", "").split(",")[1];
                for (String d : HIERARCHY.get(template)) {
                    String derived = d.replace("$1", cVar1).replace("$2", cVar2);
                    if (constraintsList.contains(derived)) {
                        if (spec1.get(derived) - spec1.get(c) == 0 || spec2.get(derived) - spec2.get(c) == 0) {
                            constraintsRemovalCandidate.add(c);
                        }
                    }
                }
            }
            for (String c : constraintsRemovalCandidate) {
                spec1.remove(c);
                spec2.remove(c);
                for (Map<String, Float> t : lCoded.values()) {
                    t.remove(c);
                }
                processSpecificationUnionSize--;
            }
            logger.info("Number of simplified constraints: " + (initConstrNum - processSpecificationUnionSize));

            //            simplification of symmetric constraints [CoExistence, NotCoExistence]
            constraintsRemovalCandidate = new HashSet<>();
            constraintsList = new HashSet<>(spec1.keySet());
            initConstrNum = processSpecificationUnionSize;

            for (String c : constraintsList) {
                // skip constraints with only one variable from simplification
                if (!c.contains(",")) continue;
                String template = c.split("\\(")[0];
                // only symmetric constraints
                if (!template.equals("CoExistence") && !template.equals("NotCoExistence")) continue;
                // skip constraints already labelled for removal
                if (constraintsRemovalCandidate.contains(c)) continue;


                String cVar1 = c.split("\\(")[1].replace(")", "").split(",")[0];
                String cVar2 = c.split("\\(")[1].replace(")", "").split(",")[1];
                String symmetricConstraint = template + "(" + cVar2 + "," + cVar1 + ")";
                if (constraintsList.contains(symmetricConstraint)) constraintsRemovalCandidate.add(symmetricConstraint);
            }
            for (String c : constraintsRemovalCandidate) {
                spec1.remove(c);
                spec2.remove(c);
                for (Map<String, Float> t : lCoded.values()) {
                    t.remove(c);
                }
                processSpecificationUnionSize--;
            }
            logger.info("Number of simplified symmetric constraints: " + (initConstrNum - processSpecificationUnionSize));
        }
        //  difference min cut
        if (!janusVariantParams.oKeep) {
            logger.info("Removing rules with not enough initial difference...");
            String[] initialConstraintsList = new String[spec1.size()];
            spec1.keySet().toArray(initialConstraintsList);
            for (String constraint : initialConstraintsList) {
                float difference = Math.abs(spec1.get(constraint) - spec2.get(constraint));
//          if one is NaN, the rule is removed if the non-NaN value is below the difference threshold (like if NaN=0)
                if ((difference < janusVariantParams.differenceThreshold) ||
                        (Float.isNaN(spec1.get(constraint)) && spec2.get(constraint) < janusVariantParams.differenceThreshold) ||
                        (Float.isNaN(spec2.get(constraint)) && spec1.get(constraint) < janusVariantParams.differenceThreshold)) {
                    spec1.remove(constraint);
                    spec2.remove(constraint);
                    for (Map<String, Float> t : lCoded.values()) {
                        t.remove(constraint);
                    }
                    processSpecificationUnionSize--;
                }
            }
            logger.info("Number of removed constraints: " + (initialConstraintsList.length - processSpecificationUnionSize));
        }
//        Measures below threshold
        if (janusVariantParams.measureThreshold > 0) {
            logger.info("Removing rules below threshold in both variants...");
            int initialConstrNum = processSpecificationUnionSize;

            String[] initialConstraintsList = new String[spec1.size()];
            spec1.keySet().toArray(initialConstraintsList);
            for (String constraint : initialConstraintsList) {
                if (spec1.get(constraint) < janusVariantParams.measureThreshold && spec2.get(constraint) < janusVariantParams.measureThreshold) {
                    spec1.remove(constraint);
                    spec2.remove(constraint);
                    for (Map<String, Float> t : lCoded.values()) {
                        t.remove(constraint);
                    }
                    processSpecificationUnionSize--;
                }
            }
            logger.info("Number of removed constraints: " + (initialConstrNum - processSpecificationUnionSize));
        }

//        encode index
        indexToTraceMap = new HashMap<>();
        traceToIndexMap = new HashMap<>();
        indexToConstraintMap = new HashMap<>();
        constraintToIndexMap = new HashMap<>();

        int cIndex = 0;
        for (String c : lCoded.values().iterator().next().keySet()) {
            indexToConstraintMap.put(cIndex, c);
            constraintToIndexMap.put(c, cIndex);
            cIndex++;
        }
        int traceIndex = 0;
        for (String t : lCoded.keySet()) {
            indexToTraceMap.put(traceIndex, t);
            traceToIndexMap.put(t, traceIndex);
            traceIndex++;
        }

        lCodedIndex = new float[lCoded.size()][processSpecificationUnionSize]; // lCodedIndex[trace index][constraint index]
        for (int t = 0; t < lCodedIndex.length; t++) {
            for (int c = 0; c < lCodedIndex[0].length; c++) {
                lCodedIndex[t][c] = lCoded.get(indexToTraceMap.get(t)).get(indexToConstraintMap.get(c));
            }
        }

    }


    /**
     * Precompute the evaluation and encode a map where each distinct trace is linked to all the constraints measumentents
     *
     * @param logParser
     * @param model
     * @return
     */
    private Map<String, Map<String, Float>> encodeLog(LogParser logParser, ProcessModel model) {
        Map<String, Map<String, Float>> result = new HashMap();
        JanusMeasurementsCmdParameters janusCheckingParams = new JanusMeasurementsCmdParameters(false, 0, true, false);
        ReactiveMeasurementsOfflineQueryingCore reactiveMeasurementsOfflineQueryingCore = new ReactiveMeasurementsOfflineQueryingCore(
                0, logParser, janusCheckingParams, null, logParser.getTaskCharArchive(), null, model.bag);
        double before = System.currentTimeMillis();
        MegaMatrixMonster measures = reactiveMeasurementsOfflineQueryingCore.check();
        double after = System.currentTimeMillis();

        logger.info("Total KB checking time: " + (after - before));

//      compute only the desired measure
        float[][] tracesMeasure = measures.retrieveSingleTraceMeasures(this.measure, janusCheckingParams.nanTraceSubstituteFlag, janusCheckingParams.nanTraceSubstituteValue);

        int currentTrace = 0;
        for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
            LogTraceParser tr = it.next();
            String stringTrace = tr.printStringTrace();
            if (!result.containsKey(stringTrace)) {
                result.put(stringTrace, new HashMap<String, Float>());
                int cIndex = 0;
                for (SeparatedAutomatonOfflineRunner c : measures.getAutomata()) {
                    result.get(stringTrace).put(
                            c.toString(),
                            tracesMeasure[currentTrace][cIndex]
                    );
                    cIndex++;
                }
            }
            currentTrace++;
        }

        return result;
    }

    /**
     * Get the log level measurement of a given log parser using already encoded log measurements
     * *
     *
     * @param nanCheck
     * @return Map<String, Float>  constraint-name:measurement
     */
    private Map<String, Float> getMeasurementsOfOneVariant(boolean nanCheck, LogParser logParser, Map<String, Map<String, Float>> lCoded) {
        Map<String, Float> result = new HashMap<>(); // constraint->measurement
        int logSize = logParser.length();
        List<String> permutableTracesList = new LinkedList<>();
        for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
            permutableTracesList.add(it.next().printStringTrace());
        }

        Set<String> constraints = lCoded.values().iterator().next().keySet();
        for (String c : constraints) {
            int nanTraces = 0;
            float constraintResult = 0;
            for (String t : permutableTracesList) {
                if (nanCheck & Float.isNaN(lCoded.get(t).get(c))) {
                    nanTraces++;
                    continue; // TODO expose in input
                }
                constraintResult += lCoded.get(t).get(c);

            }
            constraintResult = constraintResult / (logSize - nanTraces);
            result.put(c, constraintResult);
        }

        return result;
    }

    /**
     * the first variant
     * Get the original log level measurement of the first variant
     *
     * @param nanCheck
     * @return Map<String, Float>  constraint-name:measurement
     */
    public Map<String, Float> getMeasurementsVar1(boolean nanCheck) {
        return spec1;
    }

    /**
     * Get the original log level measurement of the second variant
     *
     * @param nanCheck
     * @return Map<String, Float>  constraint-name:measurement
     */
    public Map<String, Float> getMeasurementsVar2(boolean nanCheck) {
        return spec2;
    }

}
