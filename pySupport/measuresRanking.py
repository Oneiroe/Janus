# Take the aggregate result and rank the results of each measure according to a reference model ("ground truth")
import sys
import json
import csv


def main():
    model_path = sys.argv[1]
    aggregated_measure_mean_path = sys.argv[2]
    best_N_threshold = int(sys.argv[3])
    output_path = sys.argv[4]

    #     Encode model
    model = set()
    with open(model_path, 'r') as file:
        jFile = json.load(file)
        for constraint in jFile['constraints']:
            c = constraint['template'] + "("
            for p in constraint["parameters"]:
                c += p[0] + ","
            c = c[:-1] + ")"
            model.add(c)

    # encode measures ofr fast ranking
    ordered_measures = {}
    measures = []
    with open(aggregated_measure_mean_path, 'r') as file:
        csvFile = csv.DictReader(file, delimiter=';')
        measures = csvFile.fieldnames[1:]
        for m in measures:
            ordered_measures[m] = []
        for line in csvFile:
            for m in measures:
                ordered_measures[m] += [(line[m], line['Constraint'])]

    #     Rank
    measures_ranking = []
    for m in measures:
        # sort measured constraints
        ordered_measures[m] = sorted(ordered_measures[m], reverse=True)
        # check how many constraints from the original model are in the top N constraints
        counter = 0
        for value, constraint in ordered_measures[m][:best_N_threshold]:
            if constraint in model:
                counter += 1
        measures_ranking += [(counter, m)]

    measures_ranking = sorted(measures_ranking, reverse=True)

    # results
    # print()
    # print("model size: " + str(len(model)))
    # print("RANK,MEASURE")
    # for i in measures_ranking:
    #     print(i)

    # Export
    print("Saving measure ranking in... " + output_path)
    with open(output_path, 'w') as out_file:
        writer = csv.writer(out_file, delimiter=';')
        header = ["Rank-" + str(best_N_threshold), "Measure"]
        writer.writerow(header)
        writer.writerow([str(len(model)), "ORIGINAL-MODEL"])
        writer.writerows(measures_ranking)


if __name__ == "__main__":
    main()
