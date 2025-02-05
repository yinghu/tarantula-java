package com.icodesoftware.etcd;

import com.google.gson.JsonObject;
import com.icodesoftware.service.HttpClientProvider;
import com.icodesoftware.util.AbstractClusterNode;

import java.util.concurrent.atomic.AtomicInteger;

public class EtcdNode extends AbstractClusterNode {


    public String endpoint;

    public AtomicInteger nextPing = new AtomicInteger(0);

    public HttpClientProvider httpClientProvider;
    public String etcdHost;
    private String protocol;
    private String host;
    private int port;
    private EtcdNode(){

    }

    private EtcdNode(String name){
        this();
        this.name = name;
    }

    private EtcdNode(String name,String endpoint){
        this(name);
        this.endpoint = endpoint;
        String[] parts = endpoint.split(":");
        host = parts[1].substring(2);
        port = Integer.parseInt(parts[2]);
        protocol = parts[0];
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
        return protocol;
    }
    public int port(){
        return port;
    }
    public String host(){
        return host;
    }

    public static EtcdNode create(String name){
        return new EtcdNode(name);
    }

    public static EtcdNode create(String name,String endpoint){
        return new EtcdNode(name,endpoint);
    }

}
