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
        private ApplicationContext context;
        public boolean onRequest(Session session, byte[] payload,OnUpdate update) throws Exception{
            byte[] echo = ("Echo->"+new String(payload)).getBytes();
            //write echo back to the client event
            session.write(echo,label());
            //broadcasting all clients in this module instance
            update(null,echo);
            //post notice to all subscribers with presence/notice in cluster scope                                                                                      
            context.postOffice().onLabel().send("presence/notice",echo);
            
            //update statistics entry 
            OnStatistics delta = this.context.statistics().value("EchoCount",1);                                                                                            
            //update xp level and leader board in runtime
            delta.xpDelta(10);
            delta.owner(session.systemId());
            delta.onEntry("EchoCount",1);
            context.postOffice().onTag(Level.LEVEL_TAG).send(delta.owner(),delta);
                                                                                                       
        
        }

        public void setup(ApplicationContext context) throws Exception{
            this.context = context;
        }

        public String label(){
            return "echo";
        } 
    
    }
                
       

           
## How To Deploy (Drop A Module In Runtime)
    Coming Soon    
## How To Use (Platform Setup)
    Coming Soon
