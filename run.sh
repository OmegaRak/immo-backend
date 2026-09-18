#!/usr/bin/env bash
# Compile puis lance le backend
set -e
mvn -q clean package
java -jar target/immo-backend.jar
