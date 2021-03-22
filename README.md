Janus
=========================
Janus is a tool-set for the discovery, evaluation, and anlysis of declarative process specifications.

It is based on Linear Temporal Logic over finite traces with past operators (LTLp~f~) and specifically on the concept  of reactive constraints: formulae with an explicit distinction between the activator and consequent factors of the formula itself.

Janus software base is forked from [MINERful](https://github.com/cdc08x/MINERful), mostly for the input handling and the command line interface.

Features
---------------------------------------
- **declarative process discovery**: 
	- Input: rules template set and an event log;
	- Output: declarative process model.
- **declarative specification measurements**: 
	- Input: declarative process model and an event log
	- Output: set of measurements for the rules containend in the model with respect to the event log.
- **variant analysis based on declarative specifications**
	- Input: 2 event logs.
	- Output: set of rules with a statistically significant difference between the logs.

For the features inherited from MINERful (e.g., declarative models simulator) refer to its [offical page](https://github.com/cdc08x/MINERful).


How-to
=========================

For further details consult the [wiki page](https://github.com/Oneiroe/Janus/wiki) 


Publications
=========================
Janus features have been introduced or employed in the following scientific publications:

- Discovery algorithm, reactive constraints introduction
Alessio Cecconi, Claudio Di Ciccio, Giuseppe De Giacomo, Jan Mendling:
*Interestingness of Traces in Declarative Process Mining: The Janus LTLp~f~ Approach*. BPM 2018
	- DOI: [https://doi.org/10.1007/978-3-319-98648-7_8](https://doi.org/10.1007/978-3-319-98648-7_8)

- Declarative Measurement System
Alessio Cecconi, Giuseppe De Giacomo, Claudio Di Ciccio, Fabrizio Maria Maggi, Jan Mendling:
*A Temporal Logic-Based Measurement Framework for Process Mining*. ICPM 2020
	- DOI: [https://doi.org/10.1109/ICPM49681.2020.00026](https://doi.org/10.1109/ICPM49681.2020.00026)


Licensing
=========================
Please read the [LICENSE](https://github.com/Oneiroe/MINERful/blob/master/LICENSE) file.

