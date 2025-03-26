package com.tarantula.platform.service;

import com.google.gson.JsonObject;

import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.GoogleCredentialConfiguration;

import com.tarantula.platform.configuration.GoogleWebClient;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;


public class GoogleOAuthTokenValidator extends AuthObject {

    private static final TarantulaLogger logger = JDKLogger.getLogger(GoogleOAuthTokenValidator.class);
    private final static String VERIFY_URI = "https://games.googleapis.com/games/v1/applications/";

    private PlatformConfigurationServiceProvider configurationServiceProvider;

    public GoogleOAuthTokenValidator(PlatformGameServiceProvider platformGameServiceProvider,MetricsListener metricsListener){
        super(platformGameServiceProvider.gameCluster().typeId(),"");
        this.configurationServiceProvider = platformGameServiceProvider.configurationServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }


    @Override
    public String name(){
        return OnAccess.GOOGLE;
    }

    @Override
    public boolean validate(Map<String,Object> params) {
        try{
            GoogleCredentialConfiguration configuration = configurationServiceProvider.credentialConfiguration(OnAccess.GOOGLE);
            if(configuration==null){
                logger.warn("No validation available ["+typeId+"]");
                return true;
            }
            GoogleWebClient webClient = configuration.webClient();

            String token = (String) params.get("token");
            StringBuffer query = new StringBuffer(webClient.tokenUri());
            query.append("?client_id=").append(webClient.clientId());
            query.append("&client_secret=").append(webClient.clientSecret());
            query.append("&grant_type=authorization_code");
            query.append("&code=").append(token);
            query.append("&redirect_uri=").append("");
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(query.toString()))
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    .header(CONTENT_TYPE, CONTENT_FORM)
                    .header(ACCEPT, ACCEPT_JSON)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            int code = this.serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = _response.body();
                return _response.statusCode();
            });
            if(code != 200){
                logger.warn(responseData.dataAsString);
                return false;
            }
            JsonObject resp = JsonUtil.parse(responseData.dataAsString);
            String accessToken = resp.get("access_token").getAsString();
            params.put("thirdPartyToken",accessToken);
            if(verifyPlayer(webClient.applicationId(),accessToken,params)){
                onMetrics(GameClusterMetrics.ACCESS_GOOGLE_LOGIN_COUNT);
                return true;
            }
            return false;
        }catch (Exception ex){
            logger.error("error on validate type ["+typeId+"]",ex);
            return false;
        }
    }

    private boolean verifyPlayer(String applicationId,String accessToken,Map<String,Object> params){
        try{
            String query = new StringBuffer(VERIFY_URI).append(applicationId).append("/verify").toString();
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
            if(code != 200) {
                logger.warn(responseData.dataAsString);
                return false;
            }
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
