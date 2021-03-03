import csv
import json
import sys


def main():
    in_csv_path = sys.argv[1]
    out_json_path = sys.argv[2]
    with(open(out_json_path, 'w')) as out_file:
        data = {
            "name": "Model",
            "tasks": set(),
            "constraints": []
        }
        with(open(in_csv_path, 'r')) as in_file:
            reader = csv.DictReader(in_file, delimiter=';', quotechar="'")
            #       HEADER
            #       'Constraint';'Template';'Activation';'Target';'Support';'Confidence level';'Interest factor'
            for line in reader:
                data['tasks'].add(line['Activation'])
                if line['Target'] != "":
                    data['tasks'].add(line['Target'])
                    data["constraints"] += [
                        {
                            "template": line["Template"],
                            "parameters": [
                                [
                                    line["Activation"] if ("Precedence" not in line["Template"]) else line["Target"]
                                ],
                                [
                                    line["Target"] if ("Precedence" not in line["Template"]) else line["Activation"]
                                ]
                            ],
                            "support": float(line["Support"]),
                            "confidence": float(line["Confidence level"]),
                            "interestFactor": float(line["Interest factor"])
                        }
                    ]
                else:
                    data["constraints"] += [
                        {
                            "template": line["Template"],
                            "parameters": [
                                [
                                    line["Activation"]
                                ]
                            ],
                            "support": float(line["Support"]),
                            "confidence": float(line["Confidence level"]),
                            "interestFactor": float(line["Interest factor"])
                        }
                    ]
        data["tasks"]=list(data["tasks"])
        json.dump(data, out_file)


if __name__ == '__main__':
    main()
