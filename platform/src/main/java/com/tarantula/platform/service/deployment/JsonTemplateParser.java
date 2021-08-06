package com.tarantula.platform.service.deployment;

import com.google.gson.JsonObject;
import com.icodesoftware.Descriptor;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.DeploymentDescriptor;

public class JsonTemplateParser {
    private static String CONFIG_TEMPLATE = "template/";
    public static Descriptor descriptor(String templateName){
        try{
            DeploymentDescriptor descriptor = new DeploymentDescriptor();
            JsonObject temp = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_TEMPLATE+templateName+".json"));
            descriptor.typeId(temp.get("typeId").getAsString());
            descriptor.type(temp.get("type").getAsString());
            descriptor.name(temp.get("name").getAsString());
            descriptor.category(temp.get("category").getAsString());
            descriptor.tag(temp.get("tag").getAsString());
            descriptor.moduleName(temp.get("moduleName").getAsString());
            descriptor.applicationClassName(temp.get("applicationClassName").getAsString());
            descriptor.capacity(temp.get("capacity").getAsInt());
            descriptor.accessRank(temp.get("accessRank").getAsInt());
            descriptor.timerOnModule(temp.get("timerOnModule").getAsLong());
            return descriptor;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
