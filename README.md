# Game Engine Cluster
A scaling, fault-tolerant, asynchronous event messaging application/game integration platform.

### 1. Global Unique Key Cluster Indexing Support 
### 2. Large Data Sharding And Distribution With Master To Master Replication Mode   
### 3. Univeral HTTP Support With Optional Web Socket Plugin
### 4. Easy Integration With Multiple Connections (TCP,WebSocket,UDP,etc)
### 5. High Performance Local Key-Value Disk Store With Fault-Tolerance Replication
### 6. Multiple Ways For Messaging Without Third Party Providers
### 7. Advanced Modularization Architecture
### 8. Multiple Layer Access Control Policy
### 9. Distributed Application/Game Deployment
### 10. Server-Less Module Deployment Support
### 11. Pure JAVA Implmentation With High Concurrecy Throughput
### 12. Simple Cloud Deployment  


[Platform Architecture Document](doc/gec-summary.pdf)

[Programming Module Document](doc/gec-module.pdf)

[Load Test Result](doc/gec-test-result.pdf)

## How To Code (Write A Module)
A module implementation is a deployable and distributed in the cluster scope.
```JAVA
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
    //Simple Echo module
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
            //<b>streaming echo to all clients in this module instance</b>
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
        }
        //the label is the client message filter
        public String label(){
            return "echo";
        }
        //call before the module is reloaded or upgraded                                    
        public void clear(){
    
        }
    
    }
```

## How To Package (Wrap Module As A jar With A deploy descriptor)
Put the XML deployment file in the top of the jar file named descriptor.xml
Use maven artifact name format for jar file name {group-name}-{artifact-name}-{version}.jar
Example : tarantula-echo-1.0.jar
          com/tarantual/echo/Echo.class
          descriptor.xml

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