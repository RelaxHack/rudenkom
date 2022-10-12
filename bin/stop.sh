#!/bin/bash
APP_HOME=$(pwd)
PID=$(cat $APP_HOME/libs/app.pid)
echo "Closing app..."
kill $PID
rm $APP_HOME/libs/app.pid
