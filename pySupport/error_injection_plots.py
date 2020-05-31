import matplotlib.pyplot as plt
import csv
import numpy as np
import sys
import plotly
import plotly.tools as tls
import plotly.express as px
import plotly.graph_objects as go
import pandas

constraint_white_list = {
    'AtMostOne',  # == DMM 'Absence2',
    # 'End',
    # 'ExactlyOne', # == DMM 'Exactly1',
    'Init',
    'Participation'  # == DMM 'Existence', 
    'AlternatePrecedence',
    'AlternateResponse',
    'ChainPrecedence',
    'ChainResponse',
    'Precedence',
    'RespondedExistence',
    'Response'
}


def get_constraints_vector_mfout(original_file):
    """
    get the constraints and their sup/conf from a CVS generated by MINERful framework
    :param original_file:
    :return:
    """
    try:
        inFile = open(original_file, 'r', newline='')
        csvReader = csv.reader(inFile, delimiter=';', quotechar="'")
    except IndexError:
        print("No input file provided")
        exit(1)

    # constraint(X,Y):sup
    res_map_sup = {}
    # constraint(X,Y):conf
    res_map_conf = {}

    i = 0
    init = True
    for trace_line in csvReader:
        i += 1
        if init:
            init = False
            continue
        if trace_line[1] not in constraint_white_list:
            continue
        res_map_sup[trace_line[0]] = float(trace_line[4])
        res_map_conf[trace_line[0]] = float(trace_line[5])

    # ['Name', 'Constraint template', 'Implying activity', 'Implied activity', 'Support', 'Confidence level','Interest factor']

    return res_map_sup, res_map_conf


def get_constraints_vector_mfvout(original_file):
    """
    get the constraints and their sup/conf from a CVS generated by MINERful framework
    :param original_file:
    :return:
    """
    try:
        inFile = open(original_file, 'r', newline='')
        csvReader = csv.reader(inFile, delimiter=';', quotechar="'")
    except IndexError:
        print("No input file provided")
        exit(1)

    # constraint(X,Y):sup
    res_map_sup = {}
    # constraint(X,Y):conf
    res_map_conf = {}

    init = True
    for trace_line in csvReader:
        if init:
            init = False
            continue
        if trace_line[0] not in constraint_white_list:
            continue
        res_map_sup[trace_line[1]] = float(trace_line[2]) * 100

    # ['Name', 'Constraint template', 'Implying activity', 'Implied activity', 'Support', 'Confidence level','Interest factor']

    return res_map_sup


def get_constraints_vector_dmmout(original_file):
    """
    get the constraints and their sup/conf from a CVS generated by declare miner framework
    :param original_file:
    :return:
    """
    try:
        inFile = open(original_file, 'r', newline='')
        csvReader = csv.reader(inFile, delimiter=':')
    except IndexError:
        print("No input file provided")
        exit(1)

    # constraint(X,Y):sup
    res_map_sup = {}
    # constraint(X,Y):conf
    res_map_conf = {}

    for trace_line in csvReader:
        # preprocessing cleaning
        constraint = str(trace_line[0])
        constraint = constraint.replace("-complete", "")
        constraint = constraint.replace("_", "")

        if constraint.split("(")[0] == 'Existence':
            constraint = 'Participation(' + constraint.split("(")[1].split(",")[0] + ')'
        if constraint.split("(")[0] == 'Init':
            constraint = 'Init(' + constraint.split("(")[1].split(",")[0] + ')'
        if constraint.split("(")[0] == 'Absence2':
            constraint = 'AtMostOne(' + constraint.split("(")[1].split(",")[0] + ')'

        if constraint.split("(")[0] not in constraint_white_list:
            continue
        # data retrieval
        res_map_sup[constraint] = float(trace_line[1]) * 100

    # ['Name', 'Constraint template', 'Implying activity', 'Implied activity', 'Support', 'Confidence level','Interest factor']

    return res_map_sup


def plot_comparison(janus_map_sup, other_map_sup, other_name, measure_janus="sup", measure_other="sup"):
    symbol_generic = '.'
    symbol_prec = '<'
    symbol_max1 = 's'
    symbol_respEx = 'x'
    label_prec = '(Alt./Chain/.)Precedence'
    label_max1 = 'AtMostOne'
    label_respEx = 'RespondedExistence'
    label_generic = "Others"
    alpha = 0.15
    color = 'k'

    for current in janus_map_sup:
        try:
            if "Precedence" in current:
                handler_prec, = plt.plot(other_map_sup[current], janus_map_sup[current], symbol_prec, label=label_prec,
                                         alpha=alpha,
                                         color=color)
            elif "AtMostOne" in current:
                handler_max1, = plt.plot(other_map_sup[current], janus_map_sup[current], symbol_max1, label=label_max1,
                                         alpha=alpha,
                                         color=color)
            elif "RespondedExistence" in current:
                handler_respEx, = plt.plot(other_map_sup[current], janus_map_sup[current], symbol_respEx,
                                           label=label_respEx, alpha=alpha,
                                           color=color)
            else:
                handler_generic, = plt.plot(other_map_sup[current], janus_map_sup[current], symbol_generic,
                                            label=label_generic,
                                            alpha=alpha, color=color)
        except KeyError:
            plt.plot(0, janus_map_sup[current], '+')
    for current in other_map_sup:
        try:
            if "Precedence" in current:
                handler_prec, = plt.plot(other_map_sup[current], janus_map_sup[current], symbol_prec, label=label_prec,
                                         alpha=alpha,
                                         color=color)
            elif "AtMostOne" in current:
                handler_max1, = plt.plot(other_map_sup[current], janus_map_sup[current], symbol_max1, label=label_max1,
                                         alpha=alpha,
                                         color=color)
            elif "RespondedExistence" in current:
                handler_respEx, = plt.plot(other_map_sup[current], janus_map_sup[current], symbol_respEx,
                                           label=label_respEx, alpha=alpha,
                                           color=color)
            else:
                handler_generic, = plt.plot(other_map_sup[current], janus_map_sup[current], symbol_generic,
                                            label=label_generic,
                                            alpha=alpha, color=color)
        except KeyError:
            plt.plot(other_map_sup[current], 0, '+')

    plt.ylabel('Janus ' + measure_janus)
    plt.xlabel(other_name + ' ' + measure_other)
    plt.legend([handler_max1, handler_prec, handler_respEx, handler_generic],
               [label_max1, label_prec, label_respEx, label_generic])

    extension = 'svg'
    plt.savefig('test-Janus-sepsis_J-' + measure_janus + '_' + other_name + '-' + measure_other + '.' + extension)
    # plt.show()
    plt.close()


def load_measures(file_csv_base_name, err_percent):
    constraint_measures_trend = {}

    for err in err_percent:
        print("loading err:" + str(err), end='\r')
        with open(file_csv_base_name + str(err) + ".csv", 'r') as csv_file:
            reader = csv.DictReader(csv_file, delimiter=';')
            for line in reader:
                constraint = line['Constraint']
                for measure in reader.fieldnames[1:]:
                    constraint_measures_trend.setdefault(constraint, {}).setdefault(measure, [])
                    constraint_measures_trend[constraint][measure] += [line[measure]]

    print("Measures loaded")
    return constraint_measures_trend


def plot_decay_single_constraint_single_measure(constraint, measure, constraint_measures_trend):
    handler_generic, = plt.plot(range(len(constraint_measures_trend[constraint][measure])),
                                np.round(np.array(constraint_measures_trend[constraint][measure]).astype(np.float),
                                         decimals=2),
                                label=measure)

    plt.title(constraint)
    plt.ylabel("Value")
    plt.xlabel("Error%")
    plt.legend([handler_generic],
               [measure])

    extension = 'svg'
    plt.savefig('tests-SJ2T/ERROR-INJECTION-' + constraint + '_' + measure + '.' + extension)
    plt.show()
    plt.close()


def plot_decay_single_constraint(constraint, constraint_measures_trend, threshold=sys.maxsize, altered_task='',
                                 alteration_type=''):
    handlers = []
    labels = []

    for measure in constraint_measures_trend[constraint]:
        if float(constraint_measures_trend[constraint][measure][0]) > threshold:
            continue
        handler_generic, = plt.plot(np.arange(len(constraint_measures_trend[constraint][measure]) * 10, step=10),
                                    np.round(np.array(constraint_measures_trend[constraint][measure]).astype(np.float),
                                             decimals=2),
                                    label=measure)
        handlers += [handler_generic]
        labels += [measure]

    plt.title(constraint + ' ' + 'alteration: ' + alteration_type + ':' + altered_task)
    plt.ylabel("Value")
    plt.xlabel("Error%")
    plt.legend(handlers, labels,
               bbox_to_anchor=(1.05, 1),
               loc='upper left',
               borderaxespad=0.,
               fontsize=5)

    # see: https://matplotlib.org/3.1.1/gallery/pyplots/pyplot_scales.html#sphx-glr-gallery-pyplots-pyplot-scales-py
    # plt.yscale('log')
    # plt.yscale('logit')

    extension = 'svg'
    plt.savefig(
        'tests-SJ2T/ERROR-INJECTION_output/ERROR-INJECTION-' + altered_task + '-' + constraint + '.' + extension)
    # plt.show()
    plt.close()


def plotly_decay_single_constraint(constraint, constraint_measures_trend, altered_task='', alteration_type=''):
    fig = go.Figure()

    for measure in constraint_measures_trend[constraint]:
        fig.add_trace(go.Scatter(
            x=np.arange(len(constraint_measures_trend[constraint][measure]) * 10, step=10),
            y=np.round(np.array(constraint_measures_trend[constraint][measure]).astype(np.float), decimals=2),
            mode='lines',
            name=measure))

    fig.update_layout(
        title=constraint + ' ' + 'alteration: ' + alteration_type + ':' + altered_task,
        xaxis_title="Error%",
        yaxis_title="Value",
        # font=dict(
        #     family="Courier New, monospace",
        #     size=18,
        #     color="#7f7f7f"
        # )
    )

    # fig.show()
    extension = 'html'
    fig.write_html(
        'tests-SJ2T/ERROR-INJECTION_output/plotly/ERROR-INJECTION-' + altered_task + '-' + constraint + '.' + extension)


def load_results_average(file_csv_base_name, err_percent, iteration):
    # file_csv_base_name = "tests-SJ2T/ERROR-INJECTION-output.jsonAggregatedMeasures[MEAN]_"
    # file_csv_base_name = "tests-SJ2T/ERROR-INJECTION-output.jsonAggregatedMeasures[MEAN]_ITERATION_ERR"
    result = {}
    for i in iteration:
        measures = load_measures(file_csv_base_name + str(i) + "_", err_percent)
        if len(result) == 0:
            result = measures
            continue
        for constraint in measures:
            for measure in measures[constraint]:
                af = [float(i) for i in measures[constraint][measure]]
                bf = [float(i) for i in result[constraint][measure]]
                result[constraint][measure] = [sum(t) for t in list(zip(af, bf))]

    for constraint in result:
        for measure in result[constraint]:
            result[constraint][measure] = [i / len(iteration) for i in result[constraint][measure]]
    return result


def main():
    err_percent = [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
    file_csv_base_name = sys.argv[1]
    iterations = sys.argv[2]
    iteration = range(1, int(iterations) + 1)
    # file_csv_base_name = "tests-SJ2T/ERROR-INJECTION-output.jsonAggregatedMeasures[MEAN]_"
    # {constraint:{measure:[value.....]}}
    # constraint_measures_trend = load_measures(file_csv_base_name, err_percent)
    constraint_measures_trend = load_results_average(file_csv_base_name, err_percent, iteration)

    # plot_decay_single_constraint_single_measure('Init(a)', 'Certainty factor', constraint_measures_trend)
    # plot_decay_single_constraint('Init(a)', constraint_measures_trend, 1)
    # plotly_decay_single_constraint('Init(a)', constraint_measures_trend)

    if len(sys.argv) > 3:
        altered_task = sys.argv[3]
        alteration_type = sys.argv[4]
        for constraint in constraint_measures_trend:
            if altered_task in constraint.split("(")[1]:
                plot_decay_single_constraint(constraint, constraint_measures_trend, 1, altered_task, alteration_type)
                plotly_decay_single_constraint(constraint, constraint_measures_trend, altered_task, alteration_type)
    else:
        for constraint in constraint_measures_trend:
            plot_decay_single_constraint(constraint, constraint_measures_trend)
            # plotly_decay_single_constraint(constraint, constraint_measures_trend)


if __name__ == "__main__":
    main()
