
.PHONY: test
test:
	./gradlew test

.PHONY: check
check:
	./gradlew check

.PHONY: clean
clean:
	./gradlew clean

.PHONY: format
format:
	./gradlew spotlessApply

.PHONY: build
build:
	./gradlew build

.PHONY: build-fast
build-fast:
	./gradlew build -x check -x test
