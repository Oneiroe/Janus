# build a CSV tabular file where onlz one aggregated perspective is shown
# e.g. onlz the mean

# INPUT: JSON NEU log measures output
# OUTPUT: CSV

import json
import csv
import sys


def main():
    file_path = sys.argv[1]
    output_path = sys.argv[2]

    # file_path = "../tests-SJ2T/GROUND-TRUTH-output.jsonAggregatedMeasures.json"
    # output_path = "../tests-SJ2T/GROUND-TRUTH-output.jsonAggregatedMeasures[MEAN].csv"


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
                    jFile[constraint]['Support'],
                    jFile[constraint]['Confidence'],
                    jFile[constraint]['Recall'],
                    jFile[constraint]['Lovinger'],
                    jFile[constraint]['Specificity'],
                    jFile[constraint]['Accuracy'],
                    jFile[constraint]['Lift'],
                    jFile[constraint]['Leverage'],
                    jFile[constraint]['Compliance'],
                    jFile[constraint]["Odds Ratio"],
                    jFile[constraint]["Gini Index"],
                    jFile[constraint]["Certainty factor"],
                    jFile[constraint]["Coverage"],
                    jFile[constraint]["Prevalence"],
                    jFile[constraint]["Added Value"],
                    jFile[constraint]["Relative Risk"],
                    jFile[constraint]["Jaccard"],
                    jFile[constraint]["Ylue Q"],
                    jFile[constraint]["Ylue Y"],
                    jFile[constraint]["Klosgen"],
                    jFile[constraint]["Conviction"],
                    jFile[constraint]["Interestingness Weighting Dependency"],
                    jFile[constraint]["Collective Strength"],
                    jFile[constraint]["Laplace Correction"],
                    jFile[constraint]["J Measure"],
                    jFile[constraint]["One-way Support"],
                    jFile[constraint]["Two-way Support"],
                    jFile[constraint]["Two-way Support Variation"],
                    jFile[constraint]["Linear Correlation Coefficient"],
                    jFile[constraint]["Piatetsky-Shapiro"],
                    jFile[constraint]["Cosine"],
                    jFile[constraint]["Information Gain"],
                    jFile[constraint]["Sebag-Schoenauer"],
                    jFile[constraint]["Least Contradiction"],
                    jFile[constraint]["Odd Multiplier"],
                    jFile[constraint]["Example and Counterexample Rate"],
                    jFile[constraint]["Zhang"]
                ]
                writer.writerow(row)


if __name__ == "__main__":
    main()
