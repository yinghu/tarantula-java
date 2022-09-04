package com.tarantula.platform.service;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.configuration.GooglePlayConfiguration;

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


public class GoogleOAuthTokenValidator extends AuthObject {

    private final static String AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    private final static String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private final static String AUTH_PROVIDER_X509_CERT_URI = "https://www.googleapis.com/oauth2/v1/certs";
    private final static String VERIFY_URI = "https://games.googleapis.com/games/v1/applications/";


    private HttpClient client;
    private NetHttpTransport transport;
    private JacksonFactory jsonFactory;
    private String accessKey;
    private String applicationId;
    private String secureKey;

    public GoogleOAuthTokenValidator(GooglePlayConfiguration googlePlayConfiguration){
        this(googlePlayConfiguration.typeId(),googlePlayConfiguration.clientId(),googlePlayConfiguration.clientSecret(),googlePlayConfiguration.applicationId(),googlePlayConfiguration.accessKey());
    }

    public GoogleOAuthTokenValidator(String typeId,String clientId, String secureKey,String applicationId,String accessKey) {
        super(typeId, clientId);
        this.applicationId = applicationId;
        this.accessKey = accessKey;
        this.secureKey = secureKey;
        transport = new NetHttpTransport();
        jsonFactory = JacksonFactory.getDefaultInstance();
        try{
            SSLContext sct = SSLContext.getInstance("TLS");
            sct.init(null,new TrustManager[]{new GoogleOAuthTokenValidator._X509TrustManager()},null);
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
                    new GoogleAuthorizationCodeTokenRequest(transport,jsonFactory,TOKEN_URI,clientId(typeId),secureKey,token,"");
            GoogleTokenResponse response = request.execute();
            return verifyPlayer(response.getAccessToken(),params);
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    private boolean verifyPlayer(String accessToken,Map<String,Object> params){
        try{
            String query = new StringBuffer(VERIFY_URI).append(applicationId).append("/verify").append("?key=").append(accessKey).toString();
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
