package com.icodesoftware.etcd;

import com.google.gson.JsonObject;
import com.icodesoftware.service.HttpClientProvider;
import com.icodesoftware.util.RecoverableObject;

import java.util.concurrent.atomic.AtomicInteger;

public class EtcdNode extends RecoverableObject {


    public String httpEndpoint;

    public AtomicInteger nextPing = new AtomicInteger(0);

    public HttpClientProvider httpClientProvider;
    public String etcdHost;
    private EtcdNode(){

    }

    private EtcdNode(String name){
        this();
        this.name = name;
    }

    private EtcdNode(String name,String httpEndpoint){
        this(name);
        this.httpEndpoint = httpEndpoint;
    }


    public String name(){
        return name;
    }



    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("endpoint",httpEndpoint);
        return jsonObject;
    }

    public String toString(){
        return "Etcd node : "+name+" Ping Count "+nextPing.get();
    }

    public static EtcdNode create(String name){
        return new EtcdNode(name);
    }

    public static EtcdNode create(String name,String httpEndpoint){
        return new EtcdNode(name,httpEndpoint);
    }

}
