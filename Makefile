default: build dockerize

build:
	mvn clean package -U -Dmaven.test.skip=true

dockerize:
	docker build -t git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.6 .

push:
	docker push git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.6