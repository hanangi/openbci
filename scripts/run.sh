#!/bin/bash

echo "Start running..."


DIRNAME=`dirname $0`
PROJ_HOME=`cd $DIRNAME/.;pwd;`
export PROJ_HOME;

if [ ! -d $PROJ_HOME/logs ]; then
  mkdir -p $PROJ_HOME/logs;
fi

JAR=`ls $PROJ_HOME/libs/*`

java -DconfFolder=${PROJ_HOME}/conf -jar $JAR ${PROJ_HOME}/conf/conf.xml
