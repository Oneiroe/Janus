# Take the aggregate result and rank the results of each measure according to a reference model ("ground truth")
import fnmatch
import os
import sys
import json
import csv
import re


def rank_single_experiment():
    """
Given a reference model and the measurements of a given checking,
returns the measures leaderboard for which the rules of the reference model are among the first top-N
    """
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

    # encode measures for fast ranking
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
        index = 0
        previous = ""
        #         test = 0
        # for value, constraint in ordered_measures[m][:best_N_threshold]:
        for value, constraint in ordered_measures[m]:
            # test += 1
            if value == 'nan':
                continue
            if constraint in model:
                counter += 1
            if value != previous:
                #               in this way we can keep constraints that have the same values together,
                #               i.e. ranking of the first N DISTINCT results, not the first N
                previous = value
                index += 1
            if index > best_N_threshold:
                break
        #         print(m + " stopped at ordered constraint number " + str(test))
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


def rank_average():
    """
Given the results of the previous experiment, return the average of the leaderboards
    """
    experiment_base_folder = sys.argv[1]
    best_N_threshold = int(sys.argv[2])
    output_path = sys.argv[3]

    temp_measures_ranking = {}  # measure:ranks
    tot = 0

    for iteration in os.listdir(experiment_base_folder):
        try:
            file_name = fnmatch.filter(os.listdir(os.path.join(experiment_base_folder, iteration)),
                                       "*measures-ranking*top" + str(best_N_threshold) + "-*")[0]
            with open(os.path.join(experiment_base_folder, iteration, file_name), 'r') as ranking_file:
                csv_reader = csv.reader(ranking_file, delimiter=';')
                # [0]Rank-N; [1]Measure
                for line in csv_reader:
                    if line[1] == "Measure":
                        continue
                    if line[1] == "ORIGINAL-MODEL":
                        tot += int(line[0])
                        continue
                    temp_measures_ranking[line[1]] = temp_measures_ranking.setdefault(line[1], 0) + int(line[0])
        except NotADirectoryError:
            pass

    measures_ranking = []
    for m in temp_measures_ranking.keys():
        measures_ranking += [(temp_measures_ranking[m] / tot, m)]
    measures_ranking = sorted(measures_ranking, reverse=True)

    print("Saving average measure ranking in... " + output_path)
    with open(output_path, 'w') as output_file:
        csv_writer = csv.writer(output_file, delimiter=';')
        header = ["Rank-" + str(best_N_threshold), "Measure"]
        csv_writer.writerow(header)
        csv_writer.writerows(measures_ranking)


def rank_tot():
    experiment_base_folder = sys.argv[1]
    output_path = sys.argv[2]

    ranks = []
    result = {}

    for iteration_result in os.listdir(experiment_base_folder):
        # TODO adjust condition chek: problem: the csv generated with this call
        if iteration_result.endswith(".csv"):
            if "TOT" in iteration_result:
                continue
            topN = re.findall(r'\d+', iteration_result)[-1]
            rank = "Rank-" + topN
            ranks += [int(topN)]
            with open(os.path.join(experiment_base_folder, iteration_result), 'r') as input_file:
                csv_reader = csv.DictReader(input_file, delimiter=';')
                keys = csv_reader.fieldnames
                for line in csv_reader:
                    result.setdefault(line['Measure'], {})
                    result[line['Measure']][rank] = line[rank]
                    result[line['Measure']]['Measure'] = line['Measure']

    ranks.sort()

    with open(output_path, 'w') as output_file:
        header = ["Measure"] + ["Rank-" + str(i) for i in ranks]
        csv_writer = csv.DictWriter(output_file, fieldnames=header, delimiter=';')
        csv_writer.writeheader()
        for m in result.values():
            csv_writer.writerow(m)


if __name__ == "__main__":
    if len(sys.argv) == 4 + 1:
        rank_single_experiment()
    elif len(sys.argv) == 3 + 1:
        rank_average()
    elif len(sys.argv) == 2 + 1:
        rank_tot()
    else:
        print("ERR: Input parameters number is not correct")
