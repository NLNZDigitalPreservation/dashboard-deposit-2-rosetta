<h1 align="center">Deposit Dashboard</h1>

## Introduction
The dashboard provides a deposit tool  for digital content to be injected to Rosetta. The solution also provides an UI for users to browse and manage the deposit jobs.
A primary use case for this solution is scanning the given root location of each material flow and trying to create deposit jobs when there are new SIPs (sub-folders). Once the deposit jobs are created successfully, the dashboard would get a SIP-Id from Rosetta, and the dashboard will check the status of the job by polling the progress from Rosetta according to the SIP-Id.

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
1. Check and make sure java 11 is accessible, by running the command: java -version
2. Execute the shell script to start dashboard: ./startup.sh
3. Execute the shell script to stop dashboard: ./shutdown.sh


## Container
### Build
<!-- podman build --secret id=host-certs,src=/etc/ssl/certs/ca-certificates.crt -t deposit-dashboard . -->
podman build -t deposit-dashboard .

### Run Spring Boot
mvn spring-boot:run

### Debug
maven:3.9.9-eclipse-temurin-17
