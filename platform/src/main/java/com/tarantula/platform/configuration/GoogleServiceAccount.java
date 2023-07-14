package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;

public class GoogleServiceAccount {

    private final JsonObject serviceAccount;

    public GoogleServiceAccount(JsonObject payload){
        this.serviceAccount = payload;
    }
    public String clientId(){
        return serviceAccount.get("client_id").getAsString();
    }
    public String clientEmail(){
        return serviceAccount.get("client_email").getAsString();
    }
    public String projectId(){
        return serviceAccount.get("project_id").getAsString();
    }

    public String authUri(){
        return serviceAccount.get("auth_uri").getAsString();
    }

    public String tokenUri(){
        return serviceAccount.get("token_uri").getAsString();
    }

    public String certUri(){
        return serviceAccount.get("auth_provider_x509_cert_url").getAsString();
    }

    public String privateKey(){
        return serviceAccount.get("private_key").getAsString();
    }

    public String privateKeyId(){
        return serviceAccount.get("private_key_id").getAsString();
    }

    public String clientCertUri(){
        return serviceAccount.get("CLIENT_x509_cert_url").getAsString();
    }

    public String universeDomain(){
        return serviceAccount.get("universe_domain").getAsString();
    }
}
