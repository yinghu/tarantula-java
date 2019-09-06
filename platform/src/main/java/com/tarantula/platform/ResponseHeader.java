package com.tarantula.platform;

import com.tarantula.Property;
import com.tarantula.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Updated by yinghu on 9/6/2019
 */
public class ResponseHeader extends RecoverableObject implements Response {

    protected String command;
    protected int code;
    protected String message;
    protected boolean successful;

    public ResponseHeader(){}
    public ResponseHeader(String command){
        this.command = command;
        this.successful = true;
    }
    public ResponseHeader(String command,int code){
        this.command = command;
        this.successful = true;
        this.code = code;
    }
    public ResponseHeader(String command,String message){
        this.command = command;
        this.successful = true;
        this.message = message;
    }
    public ResponseHeader(String command,String message,boolean successful){
        this.command = command;
        this.successful = successful;
        this.message = message;
    }
    public ResponseHeader(String command,boolean successful,int code,String message,String label){
        this.command = command;
        this.successful = successful;
        this.code = code;
        this.message = message;
        this.label = label;
    }
    public String command() {
        return command;
    }

    public void command(String command) {
        this.command = command;
    }

    public int code() {
        return code;
    }

    public void code(int code) {
        this.code = code;
    }

    public String message() {
        return message;
    }

    public void message(String message) {
        this.message = message;
    }

    public boolean successful() {
        return successful;
    }
    public void successful(boolean successful) {
        this.successful = successful;
    }

    public void property(String header,String value){
        this.properties.put(header,value);
    }
    public String property(String header){
        return (String)this.properties.get(header);
    }


    public List<Property> list() {
        List<Property> hm = new ArrayList<>();
        this.properties.forEach((String s,Object o)->{
            if(o instanceof String){
                hm.add(new DistributedProperty(s,(String)o));
            }
        });
        return hm;
    }

}
