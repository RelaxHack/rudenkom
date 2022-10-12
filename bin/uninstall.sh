#!/bin/bash
APP_HOME=$(pwd)
echo "Uninstall Metric simulator"
echo -n "Are you sure? [y/n]"
read choice
case "$choice" in
"y")
  echo "Deleting app..."
  rm -rf $APP_HOME
  rm /etc/systemd/system/metric-simulator.service
  ;;
"n")
  echo "Closing uninstall..."
  ;;
esac
