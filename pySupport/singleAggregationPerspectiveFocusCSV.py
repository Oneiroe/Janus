# build a CSV tabular file where onlz one aggregated perspective is shown
# e.g. onlz the mean

# INPUT: JSON aggragated output
# OUTPUT: CSV

import json
import csv
import sys


def main():
    file_path = sys.argv[1]
    output_path = sys.argv[2]

    # file_path = "../tests-SJ2T/GROUND-TRUTH-output.jsonAggregatedMeasures.json"
    # output_path = "../tests-SJ2T/GROUND-TRUTH-output.jsonAggregatedMeasures[MEAN].csv"

    perspective = "Mean"

    with open(file_path, 'r') as file:
        jFile = json.load(file)
        with open(output_path, 'w') as out_file:
            writer = csv.writer(out_file, delimiter=';')
            header = ["Constraint",
                      "Support",
                      "Confidence",
                      "Recall",
                      "Lovinger",
                      "Specificity",
                      "Accuracy",
                      "Lift",
                      "Leverage",
                      "Compliance",
                      "Odds Ratio",
                      "Gini Index",
                      "Certainty factor",
                      "Coverage",
                      "Prevalence",
                      "Added Value",
                      "Relative Risk",
                      "Jaccard",
                      "Ylue Q",
                      "Ylue Y",
                      "Klosgen",
                      "Conviction",
                      "Interestingness Weighting Dependency",
                      "Collective Strength",
                      "Laplace Correction",
                      "J Measure",
                      "One-way Support",
                      "Two-way Support",
                      "Two-way Support Variation",
                      "Linear Correlation Coefficient",
                      "Piatetsky-Shapiro",
                      "Cosine",
                      "Information Gain",
                      "Sebag-Schoenauer",
                      "Least Contradiction",
                      "Odd Multiplier",
                      "Example and Counterexample Rate",
                      "Zhang"
                      ]
            writer.writerow(header)
            for constraint in jFile:
                row = [
                    constraint,
                    jFile[constraint]['Support']['stats'][perspective],
                    jFile[constraint]['Confidence']['stats'][perspective],
                    jFile[constraint]['Recall']['stats'][perspective],
                    jFile[constraint]['Lovinger']['stats'][perspective],
                    jFile[constraint]['Specificity']['stats'][perspective],
                    jFile[constraint]['Accuracy']['stats'][perspective],
                    jFile[constraint]['Lift']['stats'][perspective],
                    jFile[constraint]['Leverage']['stats'][perspective],
                    jFile[constraint]['Compliance']['stats'][perspective],
                    jFile[constraint]["Odds Ratio"]['stats'][perspective],
                    jFile[constraint]["Gini Index"]['stats'][perspective],
                    jFile[constraint]["Certainty factor"]['stats'][perspective],
                    jFile[constraint]["Coverage"]['stats'][perspective],
                    jFile[constraint]["Prevalence"]['stats'][perspective],
                    jFile[constraint]["Added Value"]['stats'][perspective],
                    jFile[constraint]["Relative Risk"]['stats'][perspective],
                    jFile[constraint]["Jaccard"]['stats'][perspective],
                    jFile[constraint]["Ylue Q"]['stats'][perspective],
                    jFile[constraint]["Ylue Y"]['stats'][perspective],
                    jFile[constraint]["Klosgen"]['stats'][perspective],
                    jFile[constraint]["Conviction"]['stats'][perspective],
                    jFile[constraint]["Interestingness Weighting Dependency"]['stats'][perspective],
                    jFile[constraint]["Collective Strength"]['stats'][perspective],
                    jFile[constraint]["Laplace Correction"]['stats'][perspective],
                    jFile[constraint]["J Measure"]['stats'][perspective],
                    jFile[constraint]["One-way Support"]['stats'][perspective],
                    jFile[constraint]["Two-way Support"]['stats'][perspective],
                    jFile[constraint]["Two-way Support Variation"]['stats'][perspective],
                    jFile[constraint]["Linear Correlation Coefficient"]['stats'][perspective],
                    jFile[constraint]["Piatetsky-Shapiro"]['stats'][perspective],
                    jFile[constraint]["Cosine"]['stats'][perspective],
                    jFile[constraint]["Information Gain"]['stats'][perspective],
                    jFile[constraint]["Sebag-Schoenauer"]['stats'][perspective],
                    jFile[constraint]["Least Contradiction"]['stats'][perspective],
                    jFile[constraint]["Odd Multiplier"]['stats'][perspective],
                    jFile[constraint]["Example and Counterexample Rate"]['stats'][perspective],
                    jFile[constraint]["Zhang"]['stats'][perspective]
                ]
                writer.writerow(row)


if __name__ == "__main__":
    main()
