package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;

import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

import java.util.concurrent.ConcurrentHashMap;

public class JDBCPoolCredentialConfiguration extends CredentialConfiguration {

    private JDBCPool jdbcPool;
    private ConcurrentHashMap<String,JDBCTask> pendingTasks;

    public JDBCPoolCredentialConfiguration(String typeId, JsonObject configurableObject){
        super(typeId,configurableObject);
        this.name = OnAccess.JDBC_SQL;
        pendingTasks = new ConcurrentHashMap<>();
    }

    public JDBCPoolCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId, OnAccess.JDBC_SQL,configurableObject);
        this.typeId = typeId;
        pendingTasks = new ConcurrentHashMap<>();
    }


    public JDBCTask task(String name){
        pendingTasks.putIfAbsent(name,new StatisticsTask(jdbcPool));
        return pendingTasks.get(name);
    }
    @Override
    public void release(){
        jdbcPool.close();
    }

    @Override
    public boolean setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Content content = super.content("DBCPPool");
        if(!content.existed()) return false;
        JsonObject poolConfig = JsonUtil.parse(content.data());
        jdbcPool = new JDBCPool(poolConfig);
        return jdbcPool.validate(serviceContext);
    }
}
