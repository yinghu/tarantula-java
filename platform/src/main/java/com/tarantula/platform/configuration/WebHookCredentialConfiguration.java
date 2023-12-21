package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

import java.util.concurrent.ConcurrentHashMap;

public class WebHookCredentialConfiguration extends CredentialConfiguration {


    private ConcurrentHashMap<String,WebClient> webClients = new ConcurrentHashMap<>();

    public WebHookCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.WEB_HOOK,configurableObject);
    }

    @Override
    public boolean setup(ServiceContext serviceContext, DataStore dataStore){
        ConfigurationObject configurationObject = saveConfigurationObject("Endpoint",serviceContext.deploymentServiceProvider(),dataStore);
        JsonObject endpoint = JsonUtil.parse(configurationObject.value());
        endpoint.entrySet().forEach(e->{
            JsonObject config = e.getValue().getAsJsonObject();
            WebClient wc = toWebClient(config);
            if(wc!=null) webClients.put(e.getKey(),wc);
        });
        return endpoint.size()>0;
    }

    public WebClient webClient(String name){
        return webClients.get(name);
    }

    private WebClient toWebClient(JsonObject config){
        try{
            WebClient wc = new WebClient(
                    config.get("Host").getAsString(),
                    config.get("Path").getAsString()
            );
            return wc;
        }catch (Exception ex){
            return null;
        }
    }




}
