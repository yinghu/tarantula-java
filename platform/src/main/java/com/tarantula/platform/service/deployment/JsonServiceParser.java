package com.tarantula.platform.service.deployment;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.DeploymentDescriptor;

public class JsonServiceParser {

    private static String CONFIG_DEPLOY = "deploy/";

    public static DeploymentDescriptor descriptor(String templateName){
        try{
            DeploymentDescriptor descriptor = new DeploymentDescriptor();
            JsonObject temp = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_DEPLOY+templateName));
            descriptor.typeId(temp.get("typeId").getAsString());
            descriptor.type(temp.get("type").getAsString());
            descriptor.name(temp.get("name").getAsString());
            descriptor.category(temp.get("category").getAsString());
            descriptor.tag(temp.get("tag").getAsString());
            if(temp.has("resetEnabled")) descriptor.resetEnabled(temp.get("resetEnabled").getAsBoolean());
            descriptor.moduleName(temp.get("moduleName").getAsString());
            descriptor.applicationClassName(temp.get("applicationClassName").getAsString());
            descriptor.disabled(temp.get("disabled").getAsBoolean());
            return descriptor;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
