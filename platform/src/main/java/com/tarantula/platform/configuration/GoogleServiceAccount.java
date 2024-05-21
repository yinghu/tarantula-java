package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JWTUtil;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.util.SystemUtil;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;

public class GoogleServiceAccount implements VendorValidator{

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private TarantulaLogger logger;
    private final JsonObject serviceAccount;

    public GoogleServiceAccount(JsonObject payload){
        this.serviceAccount = payload;
    }

    //public String clientId(){
        //return serviceAccount.get("client_id").getAsString();
    //}
    public String clientEmail(){
        return serviceAccount.get("client_email").getAsString();
    }
    //public String projectId(){
        //return serviceAccount.get("project_id").getAsString();
    //}

    //public String authUri(){
        //return serviceAccount.get("auth_uri").getAsString();
    //}

    public String tokenUri(){
        return TOKEN_URL;//serviceAccount.get("token_uri").getAsString();
    }

    //public String certUri(){
        //return serviceAccount.get("auth_provider_x509_cert_url").getAsString();
    //}

    public String privateKey(){
        return serviceAccount.get("private_key").getAsString();
    }

    public String privateKeyId(){
        return serviceAccount.get("private_key_id").getAsString();
    }

    //public String clientCertUri(){
        //return serviceAccount.get("CLIENT_x509_cert_url").getAsString();
    //}

    //public String universeDomain(){
        //return serviceAccount.get("universe_domain").getAsString();
   //}

    @Override
    public boolean validate(ServiceContext serviceContext) {
        logger = JDKLogger.getLogger(GoogleServiceAccount.class);
        try {
            return token(serviceContext) != null;
        }catch (Exception ex){
            logger.error("validation failed google service account",ex);
            return false;
        }
    }

    public String token(ServiceContext serviceContext) throws Exception{
        byte[] key = SystemUtil.fromPemString(privateKey());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        PrivateKey pkey = keyFactory.generatePrivate(keySpec);
        JWTUtil.JWT jwt = JWTUtil.init(pkey);
        String token = jwt.token((h,p)->{
            h.addProperty("kid",privateKeyId());
            p.addProperty("aud",tokenUri());
            long x = Instant.now().getEpochSecond();
            p.addProperty("iat",x);
            long y = x+3600;
            p.addProperty("exp",y);
            p.addProperty("iss",clientEmail());
            p.addProperty("scope","https://www.googleapis.com/auth/androidpublisher");
            p.addProperty("sub",clientEmail());
            return true;
        });
        StringBuffer query = new StringBuffer(tokenUri());
        query.append("?");
        query.append("grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=");
        query.append(token);
        String ACCEPT = "Accept";
        String ACCEPT_JSON = "application/json";
        String CONTENT_TYPE = "Content-type";
        String CONTENT_FORM = "application/x-www-form-urlencoded";
        HttpRequest _request = HttpRequest.newBuilder()
                .uri(URI.create(query.toString()))
                .timeout(Duration.ofSeconds(10))
                .header(ACCEPT, ACCEPT_JSON)
                .header(CONTENT_TYPE, CONTENT_FORM)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
        int code = serviceContext.httpClientProvider().request(client->{
            HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
            responseData.dataAsString = _response.body();
            return _response.statusCode();
        });
        if(code != 200) throw new RuntimeException(responseData.dataAsString);
        JsonObject resp = JsonUtil.parse(responseData.dataAsString);
        return resp.get("access_token").getAsString();
    }
}
