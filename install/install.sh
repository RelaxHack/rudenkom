#!/bin/bash
# 1 - property name 2 - new value 3 - file to write
write_property() {
  sed -i 's|^[ \t]*'$1'[ \t]*=\([ \t]*.*\)$|'$1'='$2'|' $3
}

# 1 - property name 2 - file to be read
read_property() {
  echo $(sed '/^\#/d' $2 | grep "^$1=" | tail -n 1 | cut -d "=" -f2-)
}

# 1 - directory 2 - port 3 - property name server port
install_func() {
  mkdir -p $1/MetricSimulator
  tar -xf MetricSimulator.tar -C $1/MetricSimulator
  chmod +x $1/MetricSimulator/bin/restart.sh $1/MetricSimulator/bin/start.sh $1/MetricSimulator/bin/stop.sh $1/MetricSimulator/bin/uninstall.sh
  write_property $3 $2 $1/MetricSimulator/config/config.properties
  write_property APP_HOME $1/MetricSimulator $1/MetricSimulator/bin/start.sh
  write_property APP_HOME $1/MetricSimulator $1/MetricSimulator/bin/stop.sh
  write_property APP_HOME $1/MetricSimulator $1/MetricSimulator/bin/uninstall.sh
  if [[ -e /etc/systemd/system/metric-simulator.service ]]; then
    echo "Service file will be overwritten"
  else
    cp -i $1/MetricSimulator/metric-simulator.service /etc/systemd/system
    rm $1/MetricSimulator/metric-simulator.service
  fi
  write_property ExecStart $1/MetricSimulator/bin/start.sh /etc/systemd/system/metric-simulator.service
  write_property ExecStop $1/MetricSimulator/bin/stop.sh /etc/systemd/system/metric-simulator.service
  write_property ExecReload $1/MetricSimulator/bin/restart.sh /etc/systemd/system/metric-simulator.service
  echo "Installation completed"
}

echo "Metric Simulator install"
echo "Need root rights for correct installation"
if [[ "$1" == "-f" ]]; then
  port=$(read_property port install.config)
  if [[ "$port" == "" ]]; then
    port=8086
  fi
  directory=$(read_property path install.config)
  if [[ "$directory" == "" ]]; then
    directory=$(pwd)
  fi
  install_func $directory $port server.port
else
  echo -n "Please enter full directory for installation, enter anything to set default(current directory): "
  read directory
  echo -n "Please enter Spring boot app port, enter anything to set default(8086): "
  read port
  if [[ "$port" == "" ]]; then
    port=8086
  fi
  if [[ "$directory" == "" ]]; then
    directory=$(pwd)
    install_func $directory $port server.port
  else
    install_func $directory $port server.port
  fi
fi
