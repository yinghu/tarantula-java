package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;

public class GoogleWebClient {

    private final JsonObject webClient;

    public GoogleWebClient(JsonObject payload){
        this.webClient = payload.get("web").getAsJsonObject();
    }

    public String applicationId(){
        return clientId().split("-")[0];
    }
    public String clientId(){
        return webClient.get("client_id").getAsString();
    }
    public String clientSecret(){
        return webClient.get("client_secret").getAsString();
    }

    public String projectId(){
        return webClient.get("project_id").getAsString();
    }

    public String authUri(){
        return webClient.get("auth_uri").getAsString();
    }

    public String tokenUri(){
        return webClient.get("token_uri").getAsString();
    }

    public String certUri(){
        return webClient.get("auth_provider_x509_cert_url").getAsString();
    }

}
