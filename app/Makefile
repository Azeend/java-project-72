run-dist: #запуск программы
	./build/install/app/bin/app

test:
	./gradlew test

clean:
	./gradlew clean

lint:
	./gradlew checkstyleMain checkstyleTest

build:
	./gradlew clean build

run:
	./gradlew run

install:
	./gradlew clean install

report:
	./gradlew jacocoTestReport

start-dist:
	APP_ENV=production ./build/install/app/bin/app

start:
	APP_ENV=development ./gradlew run

.PHONY: build