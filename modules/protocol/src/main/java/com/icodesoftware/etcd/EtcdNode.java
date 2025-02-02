package com.icodesoftware.etcd;

import com.google.gson.JsonObject;
import com.icodesoftware.service.HttpClientProvider;
import com.icodesoftware.util.RecoverableObject;

import java.util.concurrent.atomic.AtomicInteger;

public class EtcdNode extends RecoverableObject {


    public String endpoint;

    public AtomicInteger nextPing = new AtomicInteger(0);

    public HttpClientProvider httpClientProvider;
    public String etcdHost;

    private EtcdNode(){

    }

    private EtcdNode(String name){
        this();
        this.name = name;
    }

    private EtcdNode(String name,String endpoint){
        this(name);
        this.endpoint = endpoint;
    }


    public String name(){
        return name;
    }



    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("endpoint",endpoint);
        return jsonObject;
    }

    public String toString(){
        return "Etcd node : "+name+" Ping Count "+nextPing.get();
    }

    public String protocol(){
        return endpoint.split("//")[0];
    }
    public int port(){
        return Integer.parseInt(endpoint.split(":")[2]);
    }
    public String host(){
        return endpoint.split(":")[1].substring(2);
    }

    public static EtcdNode create(String name){
        return new EtcdNode(name);
    }

    public static EtcdNode create(String name,String endpoint){
        return new EtcdNode(name,endpoint);
    }

}
