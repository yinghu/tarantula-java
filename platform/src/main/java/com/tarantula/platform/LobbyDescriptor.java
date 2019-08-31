package com.tarantula.platform;

import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

public class LobbyDescriptor extends DefaultDescriptor {

    public LobbyDescriptor(){
        this.vertex = "Lobby";
        this.label = "LB";
    }
    @Override
    public int getClassId() {
        return PortableRegistry.LOBBY_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }
    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> _props = this.properties;
        _props.put("typeId",this.typeId);
        _props.put("type",this.type);
        _props.put("category",this.category);
        _props.put("name",this.name);
        _props.put("description",this.description);
        _props.put("icon",this.icon);
        _props.put("viewId",this.viewId);
        _props.put("tag",this.tag);
        _props.put("accessControl",this.accessControl);
        _props.put("accessMode",this.accessMode);
        _props.put("deployCode",this.deployCode);
        _props.put("deployPriority",this.deployPriority);
        _props.put("responseLabel",this.responseLabel);
        _props.put("configurationName",this.configurationName);
        _props.put("resetEnabled",this.resetEnabled);
        _props.put("disabled",this.disabled);
        return _props;
    }

    @Override
    public void fromMap(Map<String, Object> properties) {
        this.typeId=(String)properties.get("typeId");
        this.type=(String)properties.get("type");
        this.category=properties.get("category")!=null?(String)properties.get("category"):null;
        this.name=(String)properties.get("name");
        this.description=properties.get("description")!=null?(String)properties.get("description"):null;
        this.icon=properties.get("icon")!=null?(String)properties.get("icon"):null;
        this.viewId=properties.get("viewId")!=null?(String)properties.get("viewId"):null;
        this.tag=properties.get("tag")!=null?(String)properties.get("tag"):null;
        this.accessControl  = properties.get("accessControl")!=null?((Number)properties.get("accessControl")).intValue():0;
        this.accessMode  = properties.get("accessMode")!=null?((Number)properties.get("accessMode")).intValue():12;
        this.deployCode = properties.get("deployCode")!=null?((Number)properties.get("deployCode")).intValue():0;
        this.deployPriority = properties.get("deployPriority")!=null?((Number)properties.get("deployPriority")).intValue():0;
        this.responseLabel =properties.get("responseLabel")!=null? (String)properties.get("responseLabel"):null;
        this.configurationName =properties.get("configurationName")!=null? (String)properties.get("configurationName"):null;
        this.resetEnabled = properties.get("resetEnabled")!=null?(boolean)properties.get("resetEnabled"):false;
        this.disabled = properties.get("disabled")!=null?(boolean)properties.get("disabled"):false;
    }

}
