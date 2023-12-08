package com.tarantula.cci.http;

import com.icodesoftware.Event;
import com.icodesoftware.Session;
import com.icodesoftware.service.OnExchange;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.OutputStream;

public class HttpUploadSession implements OnExchange {

	private final HttpExchange hex;
    private final long id;
	public HttpUploadSession(long id,HttpExchange hex){
        this.id = id;
	    this.hex = hex;
    }
    public boolean onEvent(Event event) {
        try{
            hex.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,event.contentType());
            hex.sendResponseHeaders(200,event.payload().length);
            hex.getResponseBody().write(event.payload());
        }catch(Exception ex){
            ex.printStackTrace();
            //skip client disconnect
        }
        finally{
            this.hex.close();
        }
        return true;
    }
    public long id(){
	    return id;
    }

    public String path() {
        return hex.getRequestURI().getPath();
    }


    public String method() {
        return hex.getRequestMethod();
    }


    public String header(String name) {
        return hex.getRequestHeaders().getFirst(name);
    }


    public String query() {
        return hex.getRequestURI().getQuery();
    }

    public String remoteAddress(){
        return this.hex.getRemoteAddress().getAddress().toString();
    }

    public byte[] payload(){
        return null;
    }
    public InputStream onStream(){
	    return this.hex.getRequestBody();
    }

    public void onStream(InputStream inputStream){
        try {
            hex.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE, "application/octet-stream");
            hex.sendResponseHeaders(200, inputStream.available());
            OutputStream out = hex.getResponseBody();
            inputStream.transferTo(out);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            hex.close();
        }
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
