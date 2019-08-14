package com.tarantula.cci.http;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.Session;

import java.io.BufferedInputStream;
import java.io.IOException;

public class HttpRootHandler extends RequestParser implements HttpHandler {


    public HttpRootHandler(){

    }
    public void handle(HttpExchange hex) throws IOException {
        try{
            String path = hex.getRequestURI().getPath();
            if(path.equals("/")){
                path = "/tarantula.html";
            }
            byte[] _load;
            String contentType = "text/html";
            BufferedInputStream in = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(path.substring(1)));
            try{
                _load = new byte[in.available()];
                in.read(_load);
            }catch (Exception ex){
               throw new RuntimeException(path);
            }
            finally {
                try{in.close();}catch (Exception exx){}
            }
            if(path.endsWith(".css")){
                contentType = "text/css";
            }
            else if(path.endsWith(".html")){
                contentType = "text/html";
            }
            else if(path.endsWith(".js")){
               contentType = "text/javascript";
            }
            else if(path.endsWith(".png")){
                contentType = "image/png";
            }
            else if(path.endsWith(".jpeg")){
                contentType = "image/jpeg";
            }
            else if(path.endsWith(".jpg")){
                contentType = "image/jpeg";
            }
            hex.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,contentType);
            hex.sendResponseHeaders(200,_load.length);
            hex.getResponseBody().write(_load);
            hex.close();
        } catch (Exception exx){
            throw exx;
        }
    }

}
