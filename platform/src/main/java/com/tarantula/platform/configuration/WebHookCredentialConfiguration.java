package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;

import java.util.concurrent.ConcurrentHashMap;

public class WebHookCredentialConfiguration extends CredentialConfiguration {


    private ConcurrentHashMap<String,WebClient> webClients = new ConcurrentHashMap<>();

    public WebHookCredentialConfiguration(String typeId, JsonObject configurableObject){
        super(typeId,configurableObject);
        this.name = OnAccess.WEB_HOOK;
    }

    public WebHookCredentialConfiguration(String typeId, ConfigurableObject configurableObject){
        super(typeId,OnAccess.WEB_HOOK,configurableObject);
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

    @Override
    public boolean setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Content content = super.content("Endpoint");
        if(!content.existed()) return false;
        JsonObject endpoint = JsonUtil.parse(content.data());
        endpoint.entrySet().forEach(e->{
            JsonObject config = e.getValue().getAsJsonObject();
            WebClient wc = toWebClient(config);
            if(wc!=null) webClients.put(e.getKey(),wc);
        });
        return endpoint.size()>0;
    }



}
