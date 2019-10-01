## How To Code (Write A Module)
A module implementation is a deployable and distributed in the cluster scope.
```JAVA
    //the module contract interface 
    public interface Module {
        default void onJoin(Session session,Connection connection,OnUpdate update) throws Exception{}

        boolean onRequest(Session session, byte[] payload,OnUpdate update) throws Exception;

        void setup(ApplicationContext context) throws Exception;

        String label();
        default void clear(){}
        default void onTimeout(Session session){}                     
        default void onTimer(OnUpdate update){}
        interface OnUpdate{
            void on(String uid,byte[] delta);
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
        public void onJoin(Session session,Connection connection,OnUpdate update) throws Exception{
            session.write("your joined".getBytes(),label());
        }                                
        
        //call when a client request from http request or a web socket data send                                    
        public boolean onRequest(Session session, byte[] payload,OnUpdate update) throws Exception{
            byte[] echo = ("Echo->"+new String(payload)).getBytes();
            //write echo back to the client event
            session.write(echo,label());
            //streaming echo to all clients in this module instance
            update.on(echo); 
                                                                                         
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
            update.on(echoHello);                                          
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