#!/bin/bash
APP_HOME=$(pwd)
echo "Starting app..."
java -Dlogging.config=$APP_HOME/config/logback-spring.xml -Dfile.encoding=UTF-8 -Dapp.home=$APP_HOME -jar $APP_HOME/libs/MetricSimulator-1.0-GA.jar &
PID=$!
echo $PID >$APP_HOME/libs/app.pid
