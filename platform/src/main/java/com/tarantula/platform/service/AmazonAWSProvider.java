package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.AWSSigner;
import com.tarantula.platform.configuration.AmazonCredentialConfiguration;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;


public class AmazonAWSProvider extends AuthObject{

    //private String region;
    //private String bucket;
    //private String secretKey;

    //private AWSSigner awsSigner;
    private PlatformConfigurationServiceProvider configurationServiceProvider;
    public AmazonAWSProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }


    public String name(){
        return OnAccess.AMAZON;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        logger = serviceContext.logger(AmazonAWSProvider.class);
        /**
        String h = "https://s3."+region+".amazonaws.com";
        try{
            String date = AWSSigner.signingDate();
            String signature = awsSigner.sign("GET",date,"/");
            String token = new StringBuffer("AWS ").append(this.clientId()).append(":").append(signature).toString();
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(h))
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    .header(AUTHORIZATION,token)
                    .header("Date",date)
                    .header(ACCEPT, ACCEPT_JSON)
                    .GET()
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = _response.body();
                return _response.statusCode();
            });
            logger.warn(token);
            logger.warn("CODE->"+code);
            //if(code != 200) {
            logger.warn(responseData.dataAsString);
            //}
        }catch (Exception ex){
            logger.warn("error on setup",ex);
        }**/
    }


    public boolean upload(String name,byte[] content){
        AmazonCredentialConfiguration credentialConfiguration = configurationServiceProvider.awsCredentialConfiguration();
        if(credentialConfiguration==null){
            logger.warn("no aws credential available ["+typeId+"]");
            return false;
        }
        onMetrics(GameClusterMetrics.ACCESS_AMAZON_S3_COUNT);
        try{

            String h = "https://"+credentialConfiguration.bucket()+".s3."+credentialConfiguration.region()+".amazonaws.com/"+name;
            logger.warn("URL->"+h);
            String date = AWSSigner.signingDate();
            AWSSigner awsSigner = new AWSSigner();
            awsSigner.init(credentialConfiguration.secretAccessKey());
            String signature = awsSigner.sign("PUT",date,"/"+credentialConfiguration.bucket()+"/"+name);
            String token = new StringBuffer("AWS ").append(credentialConfiguration.accessKeyId()).append(":").append(signature).toString();
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(h))
                    .timeout(Duration.ofSeconds(TIMEOUT))
                    .header(AUTHORIZATION,token)
                    .header("Date",date)
                    .header(ACCEPT, ACCEPT_JSON)
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(content))
                    .build();
            HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
            int code = serviceContext.httpClientProvider().request(client->{
                HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
                responseData.dataAsString = _response.body();
                return _response.statusCode();
            });
            logger.warn(token);
            logger.warn("CODE->"+code);
            //if(code != 200) {
            logger.warn(responseData.dataAsString);
        }catch (Exception ex){
            logger.error("upload error",ex);
        }
        return true;
    }
}
