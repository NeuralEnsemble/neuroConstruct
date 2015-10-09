#!/bin/bash

source $NC_HOME/nCenv.sh
export JYTHONPATH=$NC_HOME:$JYTHONPATH

if [ $# -gt 0 ]; then
  if [ "$1" == "-socket" ]; then
    $JYTHON_HOME/jython $NC_HOME/pythonnC/utils/socketserver.py :8889 &
  else
    $JYTHON_HOME/jython
  fi
else
  $JYTHON_HOME/jython
fi
function kill_socket() {
  export PID=$(lsof -t -i :8889 -sTCP:listen)
  kill $PID
  }
trap kill_socket EXIT
while :
do  
  sleep 1
done
