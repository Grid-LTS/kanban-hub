#!/usr/bin/env bash

ROOT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd -P )"

cd $ROOT_DIR/sources-api
../gradlew publishToMavenLocal

cd $ROOT_DIR/gtask-plugin
../gradlew publishToMavenLocal

cd $ROOT_DIR/taskwarrior-plugin
../gradlew publishToMavenLocal

cd $ROOT_DIR/todotxt-plugin
../gradlew publishToMavenLocal

cd $ROOT_DIR/console
../gradlew clean bootJar

cd $ROOT_DIR/api
../gradlew clean bootJar
