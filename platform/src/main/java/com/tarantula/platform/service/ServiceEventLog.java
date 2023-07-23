package com.tarantula.platform.service;

import com.icodesoftware.service.ServiceEvent;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.Map;

public class ServiceEventLog extends RecoverableObject implements ServiceEvent {

    private int code;
    private Level level;
    private String source;

    private String message;

    private String stackTrace;

    private Exception exception;

    public ServiceEventLog(){

    }

    public ServiceEventLog(Level level,String source,Exception exception){
        this.level = level;
        this.source = source;
        this.exception = exception;
    }

    public int code(){
        return code;
    }
    @Override
    public Level level() {
        return level;
    }
    @Override
    public String source() {
        return source;
    }

    public String message(){
        return message;
    }
    public String stackTrace(){
        return stackTrace;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("code",code);
        this.properties.put("level",level.ordinal());
        this.properties.put("source",source);
        this.properties.put("timestamp", TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        this.properties.put("message",exception.getMessage());
        this.properties.put("stackTrace", SystemUtil.toString(exception));
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.code = ((Number)properties.getOrDefault("code",0)).intValue();
        this.level = Level.values()[((Number)properties.getOrDefault("level",0)).intValue()];
        this.source = (String) properties.get("source");
        this.timestamp = ((Number)properties.getOrDefault("timestamp",0)).longValue();
        this.message = (String) properties.get("message");
        this.stackTrace =(String) properties.get("stackTrace");
    }

    public int getFactoryId() {
        return PortableRegistry.OID;
    }


    public int getClassId() {
        return PortableRegistry.SERVICE_EVENT_LOG_CID;
    }

}
