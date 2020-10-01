package com.tarantula.cci.http;
import com.icodesoftware.Event;
import com.icodesoftware.Session;
import com.sun.net.httpserver.HttpExchange;
import com.tarantula.*;
import com.tarantula.cci.OnExchange;

import java.io.IOException;
import java.util.Map;

public class HttpSession extends RequestParser implements OnExchange{

	private final HttpExchange hex;
    private final String id;
    private Map<String,Object> requestMapping;
	public HttpSession(String id,HttpExchange hex){
        this.id  = id;
	    this.hex = hex;
    }

    public void parse() throws IOException{
	    requestMapping = this.parse(this.hex);
	}

    public boolean onEvent(Event event) {
        try{
            Event resp = event;
            hex.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,resp.contentType());
            hex.sendResponseHeaders(200,resp.payload().length);
            hex.getResponseBody().write(resp.payload());
        }catch(Exception ex){
            //skip client disconnect
        }
        finally{
            this.hex.close();
        }
        return true;
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
