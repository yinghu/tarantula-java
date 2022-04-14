package com.tarantula.platform.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;

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


public class GoogleStorePurchaseValidator extends AuthObject {

    private HttpClient client;
    private NetHttpTransport transport;
    private JacksonFactory jsonFactory;
    private String accessKey;
    private String packageName;
    private String validationUri;
    public GoogleStorePurchaseValidator(String typeId,String validationUri, String packageName, String accessKey) {
        super(typeId,"", "", "", "","", new String[0]);
        this.validationUri = validationUri;
        this.packageName = packageName;
        this.accessKey = accessKey;
        transport = new NetHttpTransport();
        jsonFactory = JacksonFactory.getDefaultInstance();
        try{
            SSLContext sct = SSLContext.getInstance("TLS");
            sct.init(null,new TrustManager[]{new GoogleStorePurchaseValidator._X509TrustManager()},null);
            client = HttpClient.newBuilder().sslContext(sct).build();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
    }
    @Override
    public boolean validate(Map<String,Object> params) {
        try{
            String token = (String) params.get("token");
            String typeId = (String) params.get("typeId");
            GoogleAuthorizationCodeTokenRequest request =
                    new GoogleAuthorizationCodeTokenRequest(transport,jsonFactory,tokenUri(),clientId(typeId),secureKey(),token,"");
            GoogleTokenResponse response = request.execute();
            return verifyPlayer(response.getAccessToken(),params);
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    private boolean verifyPlayer(String accessToken,Map<String,Object> params){
        try{
            String query = new StringBuffer(validationUri).append(packageName).append("/verify").append("?key=").append(accessKey).toString();
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(query))
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    .header(AUTHORIZATION,"Bearer "+ accessToken)
                    .header(ACCEPT, ACCEPT_JSON)
                    .GET()
                    .build();
            HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
            JsonObject payload = JsonUtil.parse(_response.body());
            if(!payload.has("player_id")) return false;
            String pendingPlayerId = (String) params.get(OnAccess.LOGIN);
            return pendingPlayerId.endsWith(payload.get("player_id").getAsString());
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
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
