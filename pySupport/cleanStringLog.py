import sys


def main():
    input_log = sys.argv[1]
    output_log = sys.argv[2]


if __name__ == "__main__":
    main()

with open(sys.argv[1], 'r') as input_file:
    with open(sys.argv[2], 'w') as output_file:
        for line in input_file:
            output_file.write(line.replace('<', '').replace('>','').replace(',',''))
