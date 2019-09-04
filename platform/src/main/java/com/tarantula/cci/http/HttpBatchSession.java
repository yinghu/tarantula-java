package com.tarantula.cci.http;

import com.sun.net.httpserver.HttpExchange;
import com.tarantula.Event;
import com.tarantula.Session;
import com.tarantula.cci.OnExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpBatchSession extends RequestParser implements OnExchange{

	private final HttpExchange hex;
    private final String id;
    private Map<String,Object> requestMapping;
    private boolean headerSent;
    private int batchNumber = 0;
    private Map<Integer,Event> pMap = new HashMap<>();
	public HttpBatchSession(String id, HttpExchange hex){
        this.id  = id;
	    this.hex = hex;
    }
    public void parse() throws IOException{
	    requestMapping = this.parse(this.hex);
	}
	public synchronized boolean onEvent(Event event){
        Event resp = event;
        boolean closed = false;
        try{
            if((!headerSent)&&resp.retries()==0){
                hex.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,resp.contentType());
                hex.getResponseHeaders().set("Cache-Control","no-cache");
                hex.getResponseHeaders().set("Connection","keep-alive");
                hex.sendResponseHeaders(200,0);
                headerSent = true;
            }
            if(resp.retries()==batchNumber){
                hex.getResponseBody().write(resp.payload());
                if(!resp.closed()){
                    batchNumber++;
                    resp = pMap.remove(batchNumber);
                    while (resp!=null){
                        hex.getResponseBody().write(resp.payload());
                        if(resp.closed()){
                            closed = true;
                            break;
                        }
                        batchNumber++;
                        resp = pMap.remove(batchNumber);
                    }
                }
                else{
                    closed = true;
                }
            }
            else{
                pMap.put(resp.retries(),resp);
            }
        }catch (Exception ex){
            ex.printStackTrace();
            closed = true;
            hex.close();
        }
        finally {
            if(closed){
                hex.close();
            }
        }
        return closed;
    }
    public String id(){
	    return this.id;
    }

    public String path() {
        return hex.getRequestURI().getPath();
    }


    public String method() {
        return hex.getRequestMethod();
    }


    public String header(String name) {
        return (String)requestMapping.get(name);
    }


    public String query() {
        return hex.getRequestURI().getQuery();
    }
    public boolean streaming(){
        return false;
    }
    public String remoteAddress(){
        return this.hex.getRemoteAddress().getAddress().toString();
    }

    public byte[] payload(){
        return (byte[]) this.requestMapping.get(Session.TARANTULA_PAYLOAD);
    }
    public void onError(Exception error,String message){
        try{
            hex.sendResponseHeaders(500,message.length());
            hex.getResponseBody().write(message.getBytes());
        }catch (Exception ex){
            //skip client disconnect
        }
        finally {
            hex.close();
        }
    }
}
