The Evaluation Storage is a component that stores the gold standard results as well as the responses of the benchmarked system during the computation phase.
During the evaluation phase it sends this data to the Evaluation Module.
Internally, the component is based on a key-value store and a small java program that handles the communication with other components.
