[![Build Status](https://travis-ci.org/hobbit-project/evaluation-storage.svg?branch=master)](https://travis-ci.org/hobbit-project/evaluation-storage)

The Evaluation Storage is a component that stores the gold standard results as well as the responses of the benchmarked system during the computation phase.
During the evaluation phase it sends this data to the Evaluation Module.
Internally, the component is based on a key-value store and a small java program that handles the communication with other components.

This implementation extends the [abstract implementation provided by the hobbit-core library](https://github.com/hobbit-project/core/blob/master/src/main/java/org/hobbit/core/components/AbstractEvaluationStorage.java). It offers the configuration of the names of the queues the storage uses.

| Env. variable name | meaning |
|---|---|
| `TASK_GEN_2_EVAL_STORAGE_QUEUE_NAME` | The queue for receiving the expected answers |
| `SYSTEM_2_EVAL_STORAGE_QUEUE_NAME`   | The queue for receiving the system responses |
| `EVAL_MODULE_2_EVAL_STORAGE_QUEUE_NAME` | The queue for receiving requests from the evaluation module |
| `EVAL_STORAGE_2_EVAL_MODULE_QUEUE_NAME` | The queue for answering the requests |

The variable names are defined in the [Constants class](https://github.com/hobbit-project/core/blob/develop/src/main/java/org/hobbit/core/Constants.java#L96).
