#!/bin/sh
FOLDER="src/test/resources"
SOURCE="${FOLDER}/schema-relations.dot"
TARGET="${FOLDER}/schema-relations.png"
dot -Tpng "${SOURCE}" >"${TARGET}"
