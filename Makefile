default: build dockerize

build:
	mvn clean package -U -Dmaven.test.skip=true

dockerize:
	docker build -t git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.8-SNAPSHOT .

push:
	docker tag git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.8 git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:latest
	docker push git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage
