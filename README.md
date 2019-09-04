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

## How To Deploy the module

```
<?xml version="1.0" encoding="UTF-8"?>
<tarantula>
    <lobby-context>
        <type-id>demo</type-id>
        <type>lobby</type>
        <category>game</category>
        <icon>html/blackjack/blackjack_icon.png</icon>
        <view-id>game.lobby</view-id>
        <tag>demo/lobby</tag>
        <name>DemoSync</name>
        <response-label>demo</response-label>
        <access-mode>12</access-mode>
        <deploy-code>1</deploy-code>
        <description>Tarantula Demo Sync</description>
        <application-list>
            <application>
                <type-id>demo</type-id>
                <subtype-id>demo-sync</subtype-id>
                <view-id>demo.sync.game</view-id>
                <name>DemoSync1</name>
                <type>application</type>
                <category>demo</category>
                <entry-cost>5000</entry-cost>
                <capacity>10</capacity>
                <max-instances-per-partition>10</max-instances-per-partition>
                <instances-on-startup-per-partition>1</instances-on-startup-per-partition>
                <max-idles-on-instance>3</max-idles-on-instance>
                <timer-on-module>50</timer-on-module>
                <module-name>com.tarantula.demo.Boost</module-name>
                <description>Tarantula Demo Sync Game</description>
            </application>
            <application>
                <type-id>demo</type-id>
                <subtype-id>demo-sync</subtype-id>
                <view-id>demo.sync.game</view-id>
                <name>DemoSync2</name>
                <type>application</type>
                <category>demo</category>
                <entry-cost>5000</entry-cost>
                <capacity>10</capacity>
                <instances-on-startup-per-partition>1</instances-on-startup-per-partition>
                <max-instances-per-partition>10</max-instances-per-partition>
                <max-idles-on-instance>3</max-idles-on-instance>
                <timer-on-module>50</timer-on-module>
                <module-name>com.tarantula.demo.Boost</module-name>
                <description>Tarantula Demo Sync Game</description>
            </application>
            <application>
                <type-id>demo</type-id>
                <subtype-id>demo-sync</subtype-id>
                <view-id>demo.sync.game</view-id>
                <name>DemoSync3</name>
                <type>application</type>
                <category>demo</category>
                <entry-cost>5000</entry-cost>
                <capacity>10</capacity>
                <instances-on-startup-per-partition>1</instances-on-startup-per-partition>
                <max-instances-per-partition>10</max-instances-per-partition>
                <max-idles-on-instance>3</max-idles-on-instance>
                <timer-on-module>50</timer-on-module>
                <module-name>com.tarantula.demo.Boost</module-name>
                <description>Tarantula Demo Sync Game</description>
            </application>
        </application-list>

    </lobby-context>
</tarantula>    
```

## How To Setup the platform