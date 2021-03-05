import random
import sys

complete_alphabet = [
    "a",
    "b",
    "c",
    "d",
    "e",
    "f",
    "g",
    "h",
    "i",
    "l",
    "m",
    "n",
    "o",
    "p",
    "q",
    "r",
    "s",
    "t",
    "u",
    "v",
    "w",
    "x",
    "y",
    "z",
    "j",
    "k",
    "ü",
    "§",
    "ß"
]


def main():
    log_txt = sys.argv[1]
    err_percent = float(sys.argv[2])*0.01
    excluded = list(sys.argv[3])
    output_log_file = sys.argv[4]

    # process alphabet without the activators and target fo the constraint
    alphabet = list(set(complete_alphabet) - set(excluded))

    with open(output_log_file, 'w') as output:
        with open(log_txt, 'r') as clean_log:
            for trace in clean_log:
                new_trace = ""
                for task in trace:
                    if random.random() < err_percent:
                        if random.random() < 0.5:
                            # del
                            if task in excluded:
                                # do not delete tasks related to the constraint
                                new_trace += task
                            continue
                        else:
                            # ins rand task
                            new_trace += random.choice(alphabet)
                            new_trace += task
                    else:
                        new_trace += task
                output.writelines(new_trace)


if __name__ == "__main__":
    main()
