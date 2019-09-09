# Goal
A scaling, fault-tolerant, asynchronous event messaging application/game integration platform at lowest cost
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
### 10. Server-Less Module Deployment Support
### 11. Pure JAVA Implmentation With High Concurrecy Throughput
### 12. Hot Runtime Deployment Support 


[Platform Architecture Document](doc/gec-summary.pdf)

[Programming Server Module API](doc/gec-module.pdf)

[Programming Client AJAX/WEBSOCKET API](doc/gec-client-api.pdf)

[Load Test Result](doc/gec-test-result.pdf)
## How To Build
1. Over 9.* JAVA runtime is required. JDK 12 is suggested.
2. Maven 3 is the build tool.
3. Node JS 10.* is to run a websocket server working as the websocket frontend for the platform.
4. Download the source tree from github.com/yinghu/gameenginecluster.
5. Go to platform to run mvn clean first (MAKE SURE Berkeley DB lib on local maven repository) and then run mvn install separately for maven local repository.
```
    mvn clean
    mvn install
```
6. Create a standard maven project for the following code.
```
   src/main/java/com/tarantula/echo/Echo.java
   src/main/resources/descriptor.xml
   pom.xml
```
```XML
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <groupId>com.tarantula</groupId>
        <artifactId>gec-echo</artifactId>
        <version>1.0</version>
        <properties>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        </properties>
        <build>
            <plugins>
                <plugin>
                    <inherited>true</inherited>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>2.3.2</version>
                    <configuration>
                        <source>12</source>
                        <target>12</target>
                    </configuration>
                </plugin>
            </plugins>
        </build>
        <dependencies>
            <dependency>
                <groupId>com.tarantula</groupId>
                <artifactId>gec-platform</artifactId>
                <version>1.0</version>
            </dependency>
        </dependencies>
    </project>
```

## How To Code (Write A Module)
A module implementation is a deployable and distributed in the cluster scope.
```JAVA
    //the module contract interface 
    public interface Module {
        default void onJoin(Session session) throws Exception{}

        boolean onRequest(Session session, byte[] payload,OnUpdate update) throws Exception;

        void setup(ApplicationContext context) throws Exception;

        String label();
        default void clear(){}

        default void onTimer(OnUpdate update){

        }
        interface OnUpdate{
            void on(String systemId,byte[] delta);
        }
        interface OnResource{
            void on(InputStream in);
        }
    }
    //Simple echo module with following TO-DO list:
    1. Reponse a echo message back to the client.
    2. Broadcast a echo message to all clients in the module instance.
    3. Push a notification to all subscribers on presence/notice in cluster scope.
    4. Update the entry of statistics of the module instance.
    5. Update the Level XP and leader board in runtime.
    6. Persistent the echo message into the distributed key value store.
    7. Broadcast a hello message at a timer such as broadcasting per 100ms.
    
    package com.tarantula.echo
    public class Echo implements Module{
        //the application resource lookup context
        private ApplicationContext context;
        //call when a client join the instance
        public void onJoin(Session session) throws Exception{
            session.write("your joined".getBytes(),label());
        }                                
        
        //call when a client request from http request or a web socket data send                                    
        public boolean onRequest(Session session, byte[] payload,OnUpdate update) throws Exception{
            byte[] echo = ("Echo->"+new String(payload)).getBytes();
            //write echo back to the client event
            session.write(echo,label());
            //streaming echo to all clients in this module instance
            update.on(null,echo); 
            //streaming another echo to myself                                                                                       
            update.on(session.systemId(),"stremaing to my self".getBytes());                                                                                       
            //post notice to all subscribers with presence/notice in cluster scope                                                                                      
            context.postOffice().onLabel().send("presence/notice",echo);
            
            //update statistics entry 
            OnStatistics delta = this.context.statistics().value("EchoCount",1);                                                                                            
            //update xp level and leader board in runtime
            delta.xpDelta(10);
            delta.owner(session.systemId());
            delta.onEntry("EchoCount",1);
            //send the delta to level module
            context.postOffice().onTag(Level.LEVEL_TAG).send(delta.owner(),delta);
                
            //save the original payload in distributed local key value store                                                                                       
            context.dataStore("echo").set(session.systemId().getBytes(),payload);                                                                                                   
            //return false to keep in the instance                                                                                        
            return false;                                                                                                                                                                   
        }
        //call at a defined interval time such as 100ms                                
        public void onTimer(OnUpdate update){
        
            //streaming echo hello every interval time
            byte[] echoHello = "Echo->Hello".getBytes();
            update.on(null,echoHello);                                          
        }
        //call when the instance is launched
        public void setup(ApplicationContext context) throws Exception{
            this.context = context;
            context.log("echo application is started",OnLog.Info);
        }
        //the label is the client message filter
        public String label(){
            return "echo";
        }
        //call before the module is reloaded or upgraded                                    
        public void clear(){
            //you can save the instance state before the module reloaded.
            //recover the state on setup call on reload.
        }
    
    }
```
The deploymenet descriptor.xml defines the module behaviors in the platform runtime.
A module is deployed as a lobby context including multiple modules. The typeId is the ID of the module package.
```XML
<?xml version="1.0" encoding="UTF-8"?>
<tarantula>
    <lobby-context>
        <type-id>echo</type-id>
        <type>lobby</type>
        <category>game</category>
        <icon>game_lobby_icon.png</icon>
        <view-id>game.lobby</view-id>
        <tag>echo/lobby</tag>
        <name>EchoSync</name>
        <response-label>echo</response-label>
        <access-mode>12</access-mode>
        <deploy-code>1</deploy-code>
        <description>Tarantula Echo Sync</description>
        <application-list>
            <application>
                <type-id>echo</type-id>
                <subtype-id>echo-sync</subtype-id>
                <view-id>echo.sync.game</view-id>
                <name>Echo Game</name>
                <type>application</type>
                <category>echo</category>
                <entry-cost>0</entry-cost>
                <capacity>100</capacity>
                <max-instances-per-partition>10</max-instances-per-partition>
                <instances-on-startup-per-partition>1</instances-on-startup-per-partition>
                <max-idles-on-instance>3</max-idles-on-instance>
                <timer-on-module>100</timer-on-module>
                <module-name>com.tarantula.echo.Echo</module-name>
                <description>Tarantula Echo Sync Game</description>
            </application>
        </application-list>
    </lobby-context>
</tarantula>    
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

