package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;

import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

public class JDBCPoolCredentialConfiguration extends CredentialConfiguration {

    private JDBCPool jdbcPool;

    public JDBCPoolCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId, OnAccess.JDBC_SQL,configurableObject);
        this.typeId = typeId;
    }

    @Override
    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        ConfigurationObject configurationObject = saveConfigurationObject("DBCPPool",serviceContext.deploymentServiceProvider(),dataStore);
        JsonObject poolConfig = JsonUtil.parse(configurationObject.value());
        jdbcPool = new JDBCPool(poolConfig);
        return jdbcPool.validate(serviceContext);
    }

    public JDBCTask task(String name){
        return new StatisticsTask(jdbcPool);
    }
}
