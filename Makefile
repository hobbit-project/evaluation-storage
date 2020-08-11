default: build dockerize

build:
	mvn clean package -U -Dmaven.test.skip=true

dockerize:
	docker build -t hobbitproject/defaultevaluationstorage:1.0.9 .

push:
	docker tag hobbitproject/defaultevaluationstorage:1.0.9 hobbitproject/defaultevaluationstorage:latest
	docker push hobbitproject/defaultevaluationstorage
