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
        tot = 0
        previous = ""
        #         test = 0
        # for value, constraint in ordered_measures[m][:best_N_threshold]:
        for value, constraint in ordered_measures[m]:
            # test += 1
            if value == 'nan':
                continue
            tot += 1
            if constraint in model:
                counter += 1
            if value != previous:
                #               in this way we can keep constraints that have the same values together,
                #               i.e. ranking of the first N DISTINCT results, not the first N
                previous = value
                index += 1
                if tot > best_N_threshold:
                    # Not the first n distinct,
                    break
            if index > best_N_threshold:
                break
        #         print(m + " stopped at ordered constraint number " + str(test))
        measures_ranking += [(counter, tot, m)]

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
        header = ["Rank-" + str(best_N_threshold) + "-true", "Rank-" + str(best_N_threshold) + "-TOT", "Measure"]
        writer.writerow(header)
        writer.writerow([str(len(model)), str(len(ordered_measures['Confidence'])), "ORIGINAL-MODEL"])
        writer.writerows(measures_ranking)


def rank_average():
    """
Given the results of the previous experiment, return the average of the leaderboards
    """
    experiment_base_folder = sys.argv[1]
    best_N_threshold = int(sys.argv[2])
    output_path = sys.argv[3]

    temp_measures_ranking = {}  # measure:ranks
    temp_measures_ranking_tot = {}  # measure:ranks
    tot = 0
    iterations = 0

    for iteration in os.listdir(experiment_base_folder):
        try:
            file_name = fnmatch.filter(os.listdir(os.path.join(experiment_base_folder, iteration)),
                                       "*measures-ranking*top" + str(best_N_threshold) + "-*")[0]
            with open(os.path.join(experiment_base_folder, iteration, file_name), 'r') as ranking_file:
                csv_reader = csv.reader(ranking_file, delimiter=';')
                # [0]Rank-N-true; [1]Rank-N-TOT; [2]Measure
                for line in csv_reader:
                    if line[-1] == "Measure":
                        continue
                    if line[-1] == "ORIGINAL-MODEL":
                        tot += int(line[0])
                        iterations += 1
                        # continue
                    temp_measures_ranking[line[-1]] = temp_measures_ranking.setdefault(line[-1], 0) + int(line[0])
                    temp_measures_ranking_tot[line[-1]] = temp_measures_ranking_tot.setdefault(line[-1], 0) + int(
                        line[1])
        except NotADirectoryError:
            pass

    measures_ranking = []
    for m in temp_measures_ranking.keys():
        # measures_ranking += [(temp_measures_ranking[m] / tot, m)]
        measures_ranking += [(temp_measures_ranking[m] / iterations, temp_measures_ranking_tot[m] / iterations, m)]
    measures_ranking = sorted(measures_ranking, reverse=True)

    print("Saving average measure ranking in... " + output_path)
    with open(output_path, 'w') as output_file:
        csv_writer = csv.writer(output_file, delimiter=';')
        header = ["Rank-" + str(best_N_threshold) + "-true", "Rank-" + str(best_N_threshold) + "-TOT", "Measure"]
        csv_writer.writerow(header)
        csv_writer.writerows(measures_ranking)


def rank_tot():
    experiment_base_folder = sys.argv[1]
    output_path = sys.argv[2]

    ranks = []
    result = {}

    modelConstraints = 0

    for iteration_result in os.listdir(experiment_base_folder):
        # TODO adjust condition chek: problem: the csv generated with this call
        if iteration_result.endswith(".csv"):
            if "TOT" in iteration_result or "[top" not in iteration_result:
                continue
            topN = re.findall(r'\d+', iteration_result)[-1]
            rank = "Rank-" + topN + "-true"
            tot = "Rank-" + topN + "-TOT"
            ranks += [int(topN)]
            with open(os.path.join(experiment_base_folder, iteration_result), 'r') as input_file:
                csv_reader = csv.DictReader(input_file, delimiter=';')
                keys = csv_reader.fieldnames
                for line in csv_reader:
                    result.setdefault(line['Measure'], {})
                    result[line['Measure']][rank] = line[rank]
                    result[line['Measure']][tot] = line[tot]
                    result[line['Measure']]['Measure'] = line['Measure']
                    if line['Measure'] == "ORIGINAL-MODEL":
                        modelConstraints = float(line[rank])

    ranks.sort()

    print("Saving total measure ranking in... " + output_path)
    with open(output_path, 'w') as output_file:
        header = ["Measure"]
        for i in ranks:
            header += ["Rank-" + str(i) + "-true", "Rank-" + str(i) + "-TOT"]
        csv_writer = csv.DictWriter(output_file, fieldnames=header, delimiter=';')
        csv_writer.writeheader()
        for m in result.values():
            csv_writer.writerow(m)


    output_path = output_path[:-4] + "[STATS].csv"
    print("Saving total measure ranking STATS in... " + output_path)
    with open(output_path, 'w') as output_file:
        header = ["Measure"]
        for i in ranks:
            header += ["Rank-" + str(i) + "-true/totTrue", "Rank-" + str(i) + "-true/TOT"]
        csv_writer = csv.DictWriter(output_file, fieldnames=header, delimiter=';')
        csv_writer.writeheader()
        for m in result.values():
            temp = {"Measure": m["Measure"]}
            for i in ranks:
                try:
                    temp["Rank-" + str(i) + "-true/totTrue"] = float(m["Rank-" + str(i) + "-true"]) / modelConstraints
                except ZeroDivisionError:
                    temp["Rank-" + str(i) + "-true/totTrue"] = "NaN"
                try:
                    temp["Rank-" + str(i) + "-true/TOT"] = float(m["Rank-" + str(i) + "-true"]) / float(m["Rank-" + str(i) + "-TOT"])
                except ZeroDivisionError:
                    temp["Rank-" + str(i) + "-true/TOT"] = "NaN"

            csv_writer.writerow(temp)


if __name__ == "__main__":
    if len(sys.argv) == 4 + 1:
        rank_single_experiment()
    elif len(sys.argv) == 3 + 1:
        rank_average()
    elif len(sys.argv) == 2 + 1:
        rank_tot()
    else:
        print("ERR: Input parameters number is not correct")
