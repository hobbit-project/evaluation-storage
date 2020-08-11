FROM openjdk:8-jre-alpine

ADD target/evaluation-storage.jar /evalrun/evaluation-storage.jar
ADD log4j.properties /evalrun/log4j.properties
ADD adjust-logging.sh /evalrun/adjust-logging.sh

WORKDIR /evalrun

CMD ./adjust-logging.sh && java -cp evaluation-storage.jar:. org.hobbit.core.run.ComponentStarter org.hobbit.evaluationstorage.EvaluationStorage
