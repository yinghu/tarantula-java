package com.tarantula.platform.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    private static String ACCEPT = "Accept";
    private static String ACCEPT_JSON = "application/json";
    private static int TIMEOUT = 10;
    private HttpClient client;

    public FacebookAuthProvider(String clientId, String secureKey, String authUri, String tokenUri, String certUri, String[] origins) {
        super("facebook", clientId, secureKey, authUri, tokenUri, certUri, origins);
        try{
            SSLContext sct = SSLContext.getInstance("TLS");
            sct.init(null,new TrustManager[]{new _X509TrustManager()},null);
            client = HttpClient.newBuilder().sslContext(sct).build();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    @Override
    public boolean validate(Map<String,Object> params){
        try{
            validateToken(params);
            metricsListener.onUpdated(Metrics.FACEBOOK_COUNT,1);
            return validateUser(params);
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private boolean validateToken(Map<String,Object> params) throws Exception{
        String token = params.get("token").toString();
        String query = new StringBuffer("?input_token=").append(token).append("&access_token")
                .append(clientId()).append("%7C").append(secureKey()).toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUri()+query))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header(ACCEPT, ACCEPT_JSON)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonParser p = new JsonParser();
        JsonObject j = p.parse(response.body()).getAsJsonObject();
        return !j.has("error");
    }
    private boolean validateUser(Map<String,Object> params) throws Exception{
        String uid = params.get("login").toString().split("_")[1];
        String query = new StringBuffer(uid).append("?access_token=")
                .append(clientId()).append("%7C").append(secureKey()).toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUri()+query))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header(ACCEPT, ACCEPT_JSON)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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
