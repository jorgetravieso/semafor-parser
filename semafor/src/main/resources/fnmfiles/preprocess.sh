#!/bin/bash

if [ $# -lt 1 -o $# -gt 3 ]; then
   echo "USAGE: `basename "${0}"` <input-file> <semafor-home>"
   exit 1
fi


INPUT_FILE=${1}
SEMAFOR_HOME=${2}
#echo ${1}
#echo ${2}



rm -f ${INPUT_FILE}.tokenized

echo "**********************************************************************"
echo "Tokenizing file: ${INPUT_FILE}"
sed -f ${SEMAFOR_HOME}/scripts/tokenizer.sed ${INPUT_FILE} > ${INPUT_FILE}.tokenized
echo "Finished tokenization."
echo "**********************************************************************"
echo "Part-of-speech tagging tokenized data...."
rm -f ${INPUT_FILE}.pos.tagged
cd ${SEMAFOR_HOME}/scripts/jmx
pwd
./mxpost tagger.project < ${INPUT_FILE}.tokenized > ${INPUT_FILE}.pos.tagged
echo "Finished part-of-speech tagging."
echo "**********************************************************************"


rm -f ${INPUT_FILE}.conll.input
rm -f ${INPUT_FILE}.conll.output
