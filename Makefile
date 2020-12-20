.PHONY: build
build: clean
	./gradlew build

.PHONY: clean
clean:
	./gradlew clean

.PHONY: publish-local
publish-local:
	./gradlew publishToMavenLocal
