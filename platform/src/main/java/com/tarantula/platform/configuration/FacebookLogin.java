package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class FacebookLogin implements VendorValidator{

    private final static String ACCESS_TOKEN_URI = "https://graph.facebook.com/oauth/access_token";

    private TarantulaLogger logger;
    private JsonObject header;
    private String accessToken;
    public FacebookLogin(JsonObject payload){
        this.header = payload;
    }
    public String appName(){
        return header.get("AppName").getAsString();
    }
    public String appId(){
        return header.get("AppId").getAsString();
    }
    public String secretKey(){
        return header.get("SecretKey").getAsString();
    }

    public String accessToken(){
        return accessToken;
    }
    @Override
    public boolean validate(ServiceContext serviceContext) {
        try{
            logger = JDKLogger.getLogger(FacebookLogin.class);
            String query = new StringBuffer("?client_id=").append(appId()).append("&client_secret=")
                    .append(secretKey()).append("&grant_type=client_credentials").toString();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ACCESS_TOKEN_URI+query))
                    .timeout(Duration.ofSeconds(HttpCaller.TIME_OUT))
                    .header(HttpCaller.ACCEPT, HttpCaller.ACCEPT_JSON)
                    .GET()
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = response.body();
                return response.statusCode();
            });
            if(code!=200) return false;
            JsonObject j = JsonParser.parseString(responseData.dataAsString).getAsJsonObject();
            if(!j.has("access_token")) return false;
            String acc = j.get("access_token").getAsString();
            int ix = acc.lastIndexOf('|');
            accessToken = acc.substring(ix+1);
            return true;
        }catch (Exception ex){
            logger.error("facebook validation failed",ex);
            return false;
        }
    }
}
