package com.tarantula.platform.service;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.configuration.GooglePlayConfiguration;
import com.tarantula.platform.service.metrics.GameClusterMetrics;
import com.tarantula.platform.util.GoogleAuthCredentialsDeserializer;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;


public class GoogleOAuthTokenValidator extends AuthObject {

    private final static String AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    private final static String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private final static String AUTH_PROVIDER_X509_CERT_URI = "https://www.googleapis.com/oauth2/v1/certs";
    private final static String VERIFY_URI = "https://games.googleapis.com/games/v1/applications/";

    private NetHttpTransport transport;
    private JacksonFactory jsonFactory;
    private String accessKey;
    private String applicationId;
    private String secureKey;

    private TarantulaLogger logger;

    public GoogleOAuthTokenValidator(GooglePlayConfiguration googlePlayConfiguration, MetricsListener metricsListener){
        this(googlePlayConfiguration.typeId(),googlePlayConfiguration.clientId(),googlePlayConfiguration.clientSecret(),googlePlayConfiguration.applicationId(),googlePlayConfiguration.accessKey());
        this.applicationMetricsListener = metricsListener;
    }

    public GoogleOAuthTokenValidator(String typeId,String clientId, String secureKey,String applicationId,String accessKey) {
        super(typeId, clientId);
        this.applicationId = applicationId;
        this.accessKey = accessKey;
        this.secureKey = secureKey;
        transport = new NetHttpTransport();
        jsonFactory = JacksonFactory.getDefaultInstance();
    }
    @Override
    public String name(){
        return OnAccess.GOOGLE;
    }
    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        logger = serviceContext.logger(GoogleOAuthTokenValidator.class);
    }
    @Override
    public boolean validate(Map<String,Object> params) {
        try{
            String token = (String) params.get("token");
            String typeId = (String) params.get("typeId");
            GoogleAuthorizationCodeTokenRequest request =
                    new GoogleAuthorizationCodeTokenRequest(transport,jsonFactory,TOKEN_URI,clientId(typeId),secureKey,token,"");
            GoogleTokenResponse response = request.execute();
            onMetrics(GameClusterMetrics.ACCESS_GOOGLE_LOGIN_COUNT);
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
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = _response.body();
                return _response.statusCode();
            });
            if(code!=200) return false;
            JsonObject payload = JsonUtil.parse(responseData.dataAsString);
            if(!payload.has("player_id")) return false;
            String pendingPlayerId = (String) params.get(OnAccess.LOGIN);
            boolean verifying = pendingPlayerId.endsWith(payload.get("player_id").getAsString());
            if(verifying) tokenValidatorProvider.updateVendorAccessToken((String)params.get(OnAccess.SYSTEM_ID),accessToken);
            return verifying;
        }catch (Exception ex){
            logger.error("Error on google auth ["+typeId+"]",ex);
            return false;
        }
    }

}
