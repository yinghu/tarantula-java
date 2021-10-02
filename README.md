# Goal

### 1. Building a scaling, fault-tolerant application/game integration solution that supports large scale multiple million user base.
### 2. Providing a modularization architecture to support parallel programming mode.
### 3. Creating a stopless deployment mode to support real continuously building/deployment processing.
### 4. Using a simple programming API to decouple coding complexity such as threading, messaging, caching, etc.
### 5. Distributing and scaling persistent connections such as TCP socket, web socket, or UDP. 
### 6. Providing lightweight mobile friendly integration platform. 


# System Features
### 1. Global Unique Key Cluster Indexing Support 
### 2. Large Data Sharding And Distribution With Master To Master Replication Mode   
### 3. Univeral HTTP Support With Web Socket/UDP Integration
### 4. Easy Integration With Multiple Connections (TCP,WebSocket,UDP,etc)
### 5. High Performance Local Key-Value Disk Store With Fault-Tolerance Replication
### 6. Multiple Ways For Messaging Without Third Party Providers
### 7. Advanced Modularization Architecture
### 8. Multiple Layer Access Control Policy
### 9. Distributed Application/Game Deployment
### 10. Stop-Less Module Deployment Support
### 11. Pure JAVA Implmentation With High Concurrecy Throughput
### 12. Hot Runtime Deployment Support 


## How To Use
1. Over 9.* JAVA runtime is required. JDK 12 is suggested.
2. Maven 3 is the build tool.
3. Node JS 10.* is to run a websocket server working as the websocket frontend for the platform.
4. Download the source tree from github.com/yinghu/gameenginecluster.
5. Go to platform to run mvn clean first (MAKE SURE Berkeley DB lib on local maven repository) and then run mvn install separately for maven local repository.
```
    mvn clean
    mvn install
```

## How To Run (Run Module On Platform)
1. All modules are the runtime plugins on the platform runtime. Modules can be launching/shuttingdown on the platform in runtime.
2. Build the platform from source (go to platform folder in the source tree and run mvn clean install).
3. Run node win.bootstrap.js on Windows or run node ux.bootstrap.js on linux/mac (use sudo) in the target/release/gec-platform-bin-1.0/gec-platform-1.0/bin.
```
    cd target/release/gec-platform-bin-1.0/gec-platform-1.0/bin
    tarantula.bat on Windows or tarantula.sh on linux/mac
```
2. Once it is runing, go to http://localhost:8090 via browsers.
3. Use the admin tool to drop the module to run. (login/password as root/root created by the platform).
4. Login as root and go to admin-setup application.
5. Click Add Module to open the module setup page (place the module jar file some folder use file:/// as codebase, remote deployment use http:// ). 
6. Input codebase, artifact name, and version for the module, then click add module.
7. Launch the module on the main setup-tool under the module.
