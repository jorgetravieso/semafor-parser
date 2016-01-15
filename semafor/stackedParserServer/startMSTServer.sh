#!/bin/bash
#    The script to start the MST parser server.
#    Written by Dipanjan Das (dipanjan@cs.cmu.edu)
#    Copyright (C) 2011
#    Dipanjan Das
#    Language Technologies Institute, Carnegie Mellon University
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.


#source "$(dirname `readlink -f ${0}`)/config"
#source /Users/jtravieso/IdeaProjects/Semafor/stackedParserServer/config


echo "initializing.."
TEMP_DIR=$TMPDIR
MST_PORT=12345
MST_PARSER_HOME=$PWD
echo "mst parser home: " $MST_PARSER_HOME
cd ..
MODEL_DIR=$PWD
MODEL_DIR="$MODEL_DIR/src/main/resources/fnmfiles/models"
echo "models dir: " $MODEL_DIR


echo "compiling..."
cd ${MST_PARSER_HOME}
find mst \( -name "*.class" \) -exec rm '{}' \;
javac -cp ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" mst/DependencyEnglish2OProjParser.java
javac -cp ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" mst/DependencyParser.java


echo "starting sever..."
#echo "Current directory: ${MST_PARSER_HOME}"
java -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" -Xms8g -Xmx8g \
mst.DependencyEnglish2OProjParser ${MODEL_DIR}/wsj.model ${TEMP_DIR} ${MST_PORT}


