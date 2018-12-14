NAME = zkuiboot
VERSION=0.2.0

.PHONY: all build publish

all: build publish

build:
	mvn clean install
	cp config.cfg docker
	cp target/$(NAME)-*-jar-with-dependencies.jar docker
	docker build -t $(NAME):$(VERSION) --no-cache --rm docker
	rm docker/$(NAME)-*.jar
	rm docker/config.cfg

publish:
	docker tag $(NAME):$(VERSION) $(NAME):$(VERSION)
	docker tag $(NAME):$(VERSION) $(NAME):latest
	docker push $(NAME)
