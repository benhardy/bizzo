#!/usr/bin/env sh

set -o errexit

if [ "$TRAVIS_BRANCH" = "master" -a "$TRAVIS_PULL_REQUEST_BRANCH" = "" ]; then
    mvn -B -fae install sonar:sonar
else
    mvn -B -fae clean compile
    sonar-scanner -X -Dsonar.projectKey=net.bhardy.bizzo:bizzo-core -Dsonar.sources=src -Dsonar.java.binaries=./target/classes
    mvn -B -fae install
fi

