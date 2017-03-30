FROM openjdk:8

ADD target/evaluation-storage.jar /evalrun/evaluation-storage.jar

WORKDIR /evalrun

CMD java -cp evaluation-storage.jar org.hobbit.core.run.ComponentStarter org.hobbit.evaluationstorage.EvaluationStorage
