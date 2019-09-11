package com.tarantula.platform.service.deployment;

import com.tarantula.Connection;

import java.util.concurrent.CopyOnWriteArraySet;

public class ConnectionManager {

    private Connection connection;
    private CopyOnWriteArraySet<String> cSet = new CopyOnWriteArraySet<>();

    public ConnectionManager(Connection connection){
        this.connection = connection;
    }
    public Connection connection(){
        return connection;
    }
    public void onConnection(String instanceId,OnConnection onConnection){
        cSet.add(instanceId);
        onConnection.on(connection);
    }
    public void offConnection(OffConnection offConnection){
        cSet.forEach(s -> offConnection.on(s));
        cSet.clear();
    }
    interface OffConnection{
        void on(String instanceId);
    }
    interface OnConnection{
        void on(Connection connection);
    }
}
