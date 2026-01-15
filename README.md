<div style="text-align: center;">
<h1>Deposit Dashboard</h1>
</div>


## Introduction
The dashboard provides a deposit tool  for digital content to be ingested to Rosetta. The solution also provides an UI for users to browse and manage the deposited jobs.
A primary use case for this solution is scanning the given root location of each material flow and trying to create deposit jobs when there are new SIPs (sub-folders). Once the deposit jobs are created successfully, the dashboard would get a SIP ID from Rosetta, and the dashboard will check the status of the job by polling the progress from Rosetta according to the SIP ID.

## Getting started
### Installation
1. Get the installation package dashboard.tar.gz from: file://wlgprdfile13/DFS_Shares/ndha/pre-deposit_prod/frank/dashboard
2. Upload dashboard.tar.gz to the directory on the target server.
3. Uncompress dashboard.tar.gz to the current directory with the command: tar -zxvf dashboard.tar.gz

### Configuration
1. Find the configuration file  conf/application.properties
2. Change the configuration items:
- server.port=8080

  The default port is 8080, change to the real port number to be used.
- system.storage.path=/exlibris/dps/nlnz_tools/dashboard/data

  Make the directory: /exlibris/dps/nlnz_tools/dashboard/data. The directory for storing the metadata of dashboard, such as: the material flow settings and the deposit jobs.
  
  For UAT, the value is: /exlibris/dps/nlnz_tools/dashboard/data
  
### Running
1. Check and make sure java 17 is accessible, by running the command: java -version
2. Execute the shell script to start dashboard: ./startup.sh
3. Execute the shell script to stop dashboard: ./shutdown.sh


## Containerization (Podman based)
### 1. Build, Run, Stop, Print Logs and Debug the container:
A shell script was provided for the convenient usage. Run './dev -h', it will print the usage help:
```aiignore
./dev -h
deposit-dashboard version: 3.0.0
dev: Deposit Dashboard
Usage:
dev build
    build the docker image, all the service in one container.
dev up
    start the docker container
dev down
    stop the docker container
dev logs
    print the logs of the docker container
dev exec
    enter the container for debuging
dev push
    push the current image to docker hub
```

***
Please note that to build the image, the commits must be pushed to Github and the latest source codes in the main branch will be pulled for the build. 
***

### 2. Run Spring Boot
#### 1) Run with 'mvn' command:
```aiignore
export SPRING_PROFILES_ACTIVE=dep
export SPRING_CONFIG_ADDITIONAL_LOCATION=file:/exlibris/dps/nlnz_tools/dashboard/conf/
mvn clean sprint-boot:run
```
#### 2) Run the packaged war with 'java' command:
java -jar target/deposit-dashboard-3.0.0.war --spring.profiles.active=dep --spring.config.additional-location=file:/exlibris/dps/nlnz_tools/dashboard/conf/

### 3. Limit the log size of the containers of podman:
#### 1) Check which log driver podman is using
```aiignore
podman info --format '{{.Host.LogDriver}}'
```
#### 2) Most common case is 'journald'. If the output is: journald, then:
```aiignore
sudo nano /etc/systemd/journald.conf
```
#### 3) And added the setting items:
```aiignore
SystemMaxUse=1G
SystemKeepFree=500M
MaxFileSize=100M
MaxRetentionSec=7day
```
#### 4) Then restart:
```aiignore
sudo systemctl restart systemd-journald
```

