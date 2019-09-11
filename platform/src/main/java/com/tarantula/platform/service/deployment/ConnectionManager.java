package com.tarantula.platform.service.deployment;

import com.tarantula.Connection;

import java.util.Set;
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
    public Set<String> onConnection(){
        return cSet;
    }

    interface OnConnection{
        void on(Connection connection);
    }
}
