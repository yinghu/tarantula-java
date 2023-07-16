package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;
import com.tarantula.platform.configuration.AWSSigner;
import com.tarantula.platform.configuration.AwsS3Configuration;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;


public class AmazonAWSProvider extends AuthObject{

    private String region;
    private String bucket;
    private String secretKey;

    private AWSSigner awsSigner;

    public AmazonAWSProvider(AwsS3Configuration configuration, MetricsListener metricsListener){
        this(configuration.typeId(),configuration.region(),configuration.bucket(),configuration.accessKeyId(),configuration.secretAccessKey());
        this.applicationMetricsListener = metricsListener;
    }

    public AmazonAWSProvider(String typeId,String region,String bucket,String accessKeyId,String secretKey){
        super(typeId,accessKeyId);
        this.region = region;
        this.bucket = bucket;
        this.secretKey = secretKey;
        try{
            awsSigner = new AWSSigner();
            awsSigner.init(this.secretKey);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public String name(){
        return OnAccess.AMAZON;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        logger = serviceContext.logger(AmazonAWSProvider.class);
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
        }
    }


    public boolean upload(String name,byte[] content){
        onMetrics(GameClusterMetrics.ACCESS_AMAZON_S3_COUNT);
        try{
            String h = "https://"+bucket+".s3."+region+".amazonaws.com/"+name;
            logger.warn("URL->"+h);
            String date = AWSSigner.signingDate();
            String signature = awsSigner.sign("PUT",date,"/"+bucket+"/"+name);
            String token = new StringBuffer("AWS ").append(this.clientId()).append(":").append(signature).toString();
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
