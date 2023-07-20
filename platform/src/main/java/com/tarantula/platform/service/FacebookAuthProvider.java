package com.tarantula.platform.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.FacebookCredentialConfiguration;
import com.tarantula.platform.configuration.FacebookLogin;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class FacebookAuthProvider extends AuthObject{

    private final static String TOKEN_DEBUG_URI = "https://graph.facebook.com/debug_token";
    //private final static String ACCESS_TOKEN_URI = "https://graph.facebook.com/oauth/access_token";
    private final static String ME_URI = "https://graph.facebook.com/me";
    private final static String GRAPH_URI = "https://graph.facebook.com";

    private PlatformConfigurationServiceProvider configurationServiceProvider;

    public FacebookAuthProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        logger = serviceContext.logger(FacebookAuthProvider.class);
    }
    @Override
    public String name(){
        return OnAccess.FACEBOOK;
    }
    @Override
    public boolean validate(Map<String,Object> params){
        FacebookCredentialConfiguration facebookCredentialConfiguration = configurationServiceProvider.credentialConfiguration(OnAccess.FACEBOOK);
        if(facebookCredentialConfiguration==null){
            logger.warn("no facebook credential available->"+typeId);
            return false;
        }
        FacebookLogin facebookLogin = facebookCredentialConfiguration.facebookLogin();
        try{
            boolean validated;
            if(validateToken(facebookLogin,params)){
                validated = validateUser(facebookLogin,params);
            }
            else{
                validated = validateMe(params);
            }
            onMetrics(GameClusterMetrics.ACCESS_FACEBOOK_LOGIN_COUNT);
            return validated;
        }catch (Exception ex){
            logger.error("facebook validate error ["+typeId+"]",ex);
            return false;
        }
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
        String[] resp = new String[1];
        int code = serviceContext.httpClientProvider().request(client->{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            resp[0]=response.body();
            return response.statusCode();
        });
        if(code!=200) return false;
        JsonObject j = JsonParser.parseString(resp[0]).getAsJsonObject();
        return j.has("id") && j.get("id").getAsString().equals(uid);
    }

    private boolean validateToken(FacebookLogin facebookLogin,Map<String,Object> params) throws Exception{
        String token = params.get("token").toString();
        String query = new StringBuffer("?input_token=").append(token).append("&access_token=")
                .append(facebookLogin.appId()).append("%7C").append(facebookLogin.accessToken()).toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_DEBUG_URI+query))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header(ACCEPT, ACCEPT_JSON)
                .GET()
                .build();
        String[] resp = new String[1];
        int code = serviceContext.httpClientProvider().request(client->{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            resp[0]=response.body();
            return response.statusCode();
        });
        if(code!= 200) return false;
        JsonObject j = JsonParser.parseString(resp[0]).getAsJsonObject();
        return !j.has("error");
    }
    private boolean validateUser(FacebookLogin facebookLogin,Map<String,Object> params) throws Exception{
        String uid = params.get("login").toString().split("_")[1];
        String query = new StringBuffer(uid).append("?access_token=")
                .append(facebookLogin.appId()).append("%7C").append(facebookLogin.accessToken()).toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GRAPH_URI+query))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header(ACCEPT, ACCEPT_JSON)
                .GET()
                .build();
        String[] resp = new String[1];
        int code = serviceContext.httpClientProvider().request(client->{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            resp[0]=response.body();
            return response.statusCode();
        });
        if(code!=200) return false;
        JsonObject j = JsonParser.parseString(resp[0]).getAsJsonObject();
        return !j.has("error");
    }

}
