package com.tarantula.platform.service;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.configuration.GooglePlayConfiguration;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
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
            StringBuffer query = new StringBuffer(TOKEN_URI);
            query.append("?client_id=").append(clientId(typeId));
            query.append("&client_secret=").append(secureKey);
            query.append("&grant_type=authorization_code");
            query.append("&code=").append(token);
            query.append("&redirect_uri=\"\"");
            //POST /token HTTP/1.1
            //Host: oauth2.googleapis.com
            //Content-Type: application/x-www-form-urlencoded

            //code=4/P7q7W91a-oMsCeLvIaQm6bTrgtp7&
              //      client_id=your_client_id&
                //    client_secret=your_client_secret&
                 //   redirect_uri=https%3A//oauth2.example.com/code&
                  //  grant_type=authorization_code
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(query.toString()))
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    //.header(AUTHORIZATION,"Bearer "+ accessToken)
                    .header(CONTENT_TYPE, CONTENT_FORM)
                    .header(ACCEPT, ACCEPT_JSON)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            int code = this.serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = _response.body();
                return _response.statusCode();
            });
            logger.warn(token);
            logger.warn("code->"+code);
            logger.warn("resp->"+responseData.dataAsString);
            JsonObject resp = JsonUtil.parse(responseData.dataAsString);
            //return resp.get("access_token").getAsString();
            //String token = (String) params.get("token");
            //String typeId = (String) params.get("typeId");
            GoogleAuthorizationCodeTokenRequest request =
                    new GoogleAuthorizationCodeTokenRequest(transport,jsonFactory,TOKEN_URI,clientId(typeId),secureKey,token,"");
            GoogleTokenResponse response = request.execute();
            onMetrics(GameClusterMetrics.ACCESS_GOOGLE_LOGIN_COUNT);
            return verifyPlayer(response.getAccessToken(),params);
        }catch (Exception ex){
            logger.error("error on validate type ["+typeId+"]",ex);
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
            return pendingPlayerId.endsWith(payload.get("player_id").getAsString());
        }catch (Exception ex){
            logger.error("Error on google auth ["+typeId+"]",ex);
            return false;
        }
    }

}
