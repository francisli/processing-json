#!/bin/sh
git diff-index --quiet HEAD -- || { echo "Uncommmitted changes detected- please run this script from a clean working directory."; exit; }
mvn clean package
mkdir -p target/json/library
cp library.properties target/json
mv target/json.jar target/json/library
mv target/dependency/* target/json/library
cd target
zip -r json.zip json
