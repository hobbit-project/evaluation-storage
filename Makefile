default: build dockerize

build:
	mvn clean package -U -Dmaven.test.skip=true

dockerize:
	docker build -t hobbitproject/defaultevaluationstorage:1.0.10-SNAPSHOT .

push:
	docker tag hobbitproject/defaultevaluationstorage:1.0.10-SNAPSHOT hobbitproject/defaultevaluationstorage:latest
	docker push hobbitproject/defaultevaluationstorage
