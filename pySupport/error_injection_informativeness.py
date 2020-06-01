import sys
import csv
import os
import math


def export_measure_informativeness(output_file, data):
    with open(output_file, 'w') as csv_file:
        writer = csv.writer(csv_file, delimiter=';')
        header = ['MEASURE', 'INFORMATIVENESS']
        writer.writerow(header)
        for i in data.items():
            writer.writerow(i)


def import_measure_informativeness(input_file):
    data = {}
    with open(input_file, 'r') as csv_file:
        reader = csv.DictReader(csv_file, delimiter=';')
        # header = ['MEASURE', 'INFORMATIVENESS']
        for line in reader:
            data[line['MEASURE']] = line['INFORMATIVENESS']
    return data


def main():
    # files_dir_path = 'tests-SJ2T/ERROR-INJECTION_output/'
    # files_base_name = 'ERROR-INJECTION-INFORMATIVENESS-'
    # output_file_path = 'tests-SJ2T/ERROR-INJECTION_output/ERROR-INJECTION-INFORMATIVENESS_AVG.csv'

    files_dir_path = sys.argv[1]
    files_base_name = sys.argv[2]
    output_file_path = sys.argv[3]

    directory = os.fsencode(files_dir_path)
    result = {}
    constr = []

    for file in os.listdir(directory):
        filename = os.fsdecode(file)
        if filename.startswith(files_base_name):
            # temp = import_measure_informativeness(files_dir_path + filename)
            with open(files_dir_path + filename, 'r') as csv_file:
                reader = csv.DictReader(csv_file, delimiter=';')
                # header = ['MEASURE', 'INFORMATIVENESS']
                constr += [filename.strip(files_base_name)]
                for line in reader:
                    result.setdefault(line['MEASURE'], [])
                    result[line['MEASURE']] += [float(line['INFORMATIVENESS'])]

    for measure in result:
        result[measure] += [sum(result[measure]) / len(result[measure])]

    with open(output_file_path, 'w') as csv_file:
        writer = csv.writer(csv_file, delimiter=';')
        header = ['MEASURE']
        for constraint in constr:
            header += [constraint]
        header += ['AVG']
        writer.writerow(header)
        for measure in result:
            row = [measure]
            row += result[measure]
            writer.writerow(row)


if __name__ == "__main__":
    main()
