package com.tarantula.platform.service;

import com.icodesoftware.service.ServiceEvent;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.Map;

public class ServiceEventLog extends RecoverableObject implements ServiceEvent {

    protected String code;
    protected Level level;
    protected String source;

    protected String message;

    protected String stackTrace;

    protected Exception exception;

    public ServiceEventLog(){

    }

    public ServiceEventLog(String code,Level level,String source,String message,Exception exception){
        this.code = code;
        this.level = level;
        this.source = source;
        this.message = message;
        this.exception = exception;
    }
    public ServiceEventLog(String code,Level level,String source,String message,String stackTrace){
        this.code = code;
        this.level = level;
        this.source = source;
        this.message = message;
        this.stackTrace = stackTrace;
    }
    public String code(){
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
        this.properties.put("level",level.name());
        this.properties.put("source",source);
        this.properties.put("timestamp", TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        if(exception!=null){
            this.properties.put("message",exception.getMessage());
            this.properties.put("stackTrace", SystemUtil.toString(exception));
        }
        else{
            this.properties.put("message",message);
            this.properties.put("stackTrace",stackTrace);
        }
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.code = (String) properties.get("code");
        this.level = Level.valueOf((String)properties.get("level"));
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
