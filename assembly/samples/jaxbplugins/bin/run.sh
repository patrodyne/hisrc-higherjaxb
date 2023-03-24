#!/bin/sh
MAIN_CLASS=${1:-org.example.po.Main}
mvn compile exec:java \
	-Dexec.mainClass="$MAIN_CLASS" \
	-Dexec.args="$2 $3 $4 $5 $6 $7 $8 $9"
