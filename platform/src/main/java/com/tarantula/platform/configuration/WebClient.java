package com.tarantula.platform.configuration;

import com.icodesoftware.OnLog;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WebClient implements VendorValidator{

    private final String host;
    private final String path;

    public WebClient(String host,String path){
        this.host = host;
        this.path = path;
    }

    public String host(){
        return host;
    }

    public String path(){
        return path;
    }
    public boolean validate(ServiceContext serviceContext){ return true;}
    public boolean post(ServiceContext serviceContext,byte[] data){
        try{
            String h = host+"/"+path;
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(h))
                    .timeout(Duration.ofSeconds(HttpCaller.TIME_OUT))
                    .header(HttpCaller.CONTENT_TYPE, HttpCaller.ACCEPT_JSON)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = _response.body();
                return _response.statusCode();
            });
            if(code!=200) throw new RuntimeException(responseData.dataAsString);
            return false;
        }catch (Exception ex){
            serviceContext.log("Error on post ["+host+"/"+path+"]",ex, OnLog.ERROR);
            return false;
        }
    }

}
