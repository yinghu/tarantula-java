package com.tarantula.platform.service.deployment;

import com.tarantula.Connection;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConnectionManager {

    private Connection connection;
    private int pendingConnections;
    private CopyOnWriteArraySet<String> cSet = new CopyOnWriteArraySet<>();

    public ConnectionManager(int pendingConnections, Connection connection){
        this.connection = connection;
        this.pendingConnections = pendingConnections;
    }
    public Connection connection(){
        return connection;
    }
    public synchronized boolean onConnection(String instanceId,int capacity,OnConnection onConnection){
        if(pendingConnections-capacity<0){
            return false;
        }
        pendingConnections -=capacity;
        cSet.add(instanceId);
        onConnection.on(connection);
        return true;
    }
    public Set<String> onConnection(){
        return cSet;
    }

    interface OnConnection{
        void on(Connection connection);
    }
}
