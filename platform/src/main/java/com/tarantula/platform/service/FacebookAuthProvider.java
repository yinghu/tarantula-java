package com.tarantula.platform.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.platform.configuration.FacebookConfiguration;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;

public class FacebookAuthProvider extends AuthObject{

    private final static String TOKEN_DEBUG_URI = "https://graph.facebook.com/debug_token";
    private final static String ACCESS_TOKEN_URI = "https://graph.facebook.com/oauth/access_token";
    private final static String ME_URI = "https://graph.facebook.com/me";
    private final static String GRAPH_URI = "https://graph.facebook.com";

    private HttpClient client;
    private String accessToken;
    private String secureKey;

    public FacebookAuthProvider(FacebookConfiguration facebookConfiguration, MetricsListener metricsListener){
        this(facebookConfiguration.typeId(),facebookConfiguration.appId(),facebookConfiguration.secretKey());
        this.applicationMetricsListener = metricsListener;
    }

    public FacebookAuthProvider(String typeId,String clientId, String secureKey) {
        super(typeId, clientId);
        this.secureKey = secureKey;
        try{
            SSLContext sct = SSLContext.getInstance("TLS");
            sct.init(null,new TrustManager[]{new _X509TrustManager()},null);
            client = HttpClient.newBuilder().sslContext(sct).build();
            if(!serverToken()) throw new RuntimeException("invalid token");
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    @Override
    public boolean validate(Map<String,Object> params){
        try{
            boolean validated;
            if(validateToken(params)){
                validated = validateUser(params);
            }
            else{
                validated = validateMe(params);
            }
            onMetrics(GameClusterMetrics.ACCESS_FACEBOOK_LOGIN_COUNT);
            return validated;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean serverToken() throws Exception{

        String query = new StringBuffer("?client_id=").append(clientId("typeId")).append("&client_secret=")
                .append(secureKey).append("&grant_type=client_credentials").toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ACCESS_TOKEN_URI+query))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header(ACCEPT, ACCEPT_JSON)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonParser p = new JsonParser();
        JsonObject j = p.parse(response.body()).getAsJsonObject();
        if(!j.has("access_token")) return false;
        String acc = j.get("access_token").getAsString();
        int ix = acc.lastIndexOf('|');
        accessToken = acc.substring(ix+1);
        return true;
    }

    private boolean validateMe(Map<String,Object> params) throws Exception{
        String uid = params.get("login").toString().split("_")[1];
        String token = params.get("token").toString();
        String query = new StringBuffer("?access_token=").append(token).toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ME_URI+query))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header(ACCEPT, ACCEPT_JSON)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!=200) return false;
        JsonParser p = new JsonParser();
        JsonObject j = p.parse(response.body()).getAsJsonObject();
        return j.has("id") && j.get("id").getAsString().equals(uid);
    }

    private boolean validateToken(Map<String,Object> params) throws Exception{
        String token = params.get("token").toString();
        String query = new StringBuffer("?input_token=").append(token).append("&access_token=")
                .append(clientId("")).append("%7C").append(accessToken).toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_DEBUG_URI+query))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header(ACCEPT, ACCEPT_JSON)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!= 200) return false;
        JsonParser p = new JsonParser();
        JsonObject j = p.parse(response.body()).getAsJsonObject();
        return !j.has("error");
    }
    private boolean validateUser(Map<String,Object> params) throws Exception{
        String uid = params.get("login").toString().split("_")[1];
        String query = new StringBuffer(uid).append("?access_token=")
                .append(clientId("")).append("%7C").append(accessToken).toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GRAPH_URI+query))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header(ACCEPT, ACCEPT_JSON)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!=200) return false;
        JsonParser p = new JsonParser();
        JsonObject j = p.parse(response.body()).getAsJsonObject();
        return !j.has("error");
    }

    private class _X509TrustManager implements X509TrustManager {
        private X509Certificate[] certificate;
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //run on server
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //run on client to check if certificate is valid
            //if(!chain[0].getSubjectDN().getName().equals("CN=gameclustering.com")){
                //throw new CertificateException("Invalid certificate");
            //}
            certificate = chain;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return this.certificate;
        }

    }
}
