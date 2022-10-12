# Metric Simulator

## Installation

Before installing, you need to set permission to install script

```bash
chmod +x install.sh
```

Use the script to install metric simulator and follow instructions

```bash
bash install.sh
```

### Silent installation

Configure install.config and use -f key

Config file includes:
\
1st line - Metric simulator path (default - current path)  
2nd line - Server port (default - 8086)

## Configuration

Before launching the application, we need to configure it.
\
Application configuration includes two files.

### config.properties

Go to the config directory and open config.properties.
\
Configuration is an array and looks like this:

config app.sm[ ].path - metric simulator file path
\
config app.sm[ ].ip - simulator sending IP
\
config app.sm[ ].port - simulator sending port
\
config app.sm[ ].enable - inclusion marker (0 - off, 1 - on)
\
config app.sm[ ].freq - socket sending frequency

Example:

```
app.sm[0].path=${app.home}/config/file0
app.sm[0].ip=127.0.0.1
app.sm[0].port=9000
app.sm[0].enable=1
app.sm[0].freq=2
```

#### Additional configurations:

default_freq_sec - default frequency is set if app.sm[ ].freq is not specified
(default value is 10)\
Example: default_freq_sec=5  
server.port - spring boot application port

### Logback

Go to the config directory and open logback-spring.xml.
\
Default directory for logs is logs. But you can customize as you like.

## Usage

### Start

In order to run the application, you need to start systemd service.
\
(Your specified port must be freed to run the application)

```bash
systemctl start metricsimulator
```

If any errors occur during the execution of the application, the reason is described in the logs, and the application is
closed automatically.

#### Reconnect

Also, if the connection to the IP:Port fails, an attempt to reconnect will be played back.  
P.S. New attempts will be played starting at 5 seconds, then multiplied by 2 to 160 seconds.

### Stop

If you want to close the application use this command

```bash
systemctl stop metricsimulator
```

### Restart

If you want restart the application you need to use

```bash
systemctl restart metricsimulator
```

## Unistall

In order to remove the metrics simulator, you need to use unistall.sh script

```bash
bash unistall.sh
```

You will be asked to confirm the deletion and then the deletion will take place.