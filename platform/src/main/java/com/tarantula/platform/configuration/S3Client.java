package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;

import java.io.ByteArrayInputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class S3Client implements VendorValidator{

    private TarantulaLogger logger;
    private JsonObject header;
    private AWSSigner signer;
    public S3Client(JsonObject payload){
        this.header = payload;
    }
    public String region(){
        return header.get("Region").getAsString();
    }
    public String bucket(){
        return header.get("Bucket").getAsString();
    }
    public String accessKeyId(){
        return header.get("AccessKeyId").getAsString();
    }
    public String secretAccessKey(){
        return header.get("SecretAccessKey").getAsString();
    }

/**
    <?xml version="1.0" encoding="UTF-8"?>
<ListAllMyBucketsResult>
   <Buckets>
      <Bucket>
         <CreationDate>timestamp</CreationDate>
         <Name>string</Name>
      </Bucket>
   </Buckets>
   <Owner>
      <DisplayName>string</DisplayName>
      <ID>string</ID>
   </Owner>
</ListAllMyBucketsResult>
 **/
    @Override
    public boolean validate(ServiceContext serviceContext) {
        try{
            logger = serviceContext.logger(S3Client.class);
            String h = "https://s3."+region()+".amazonaws.com";
            signer = new AWSSigner();
            signer.init(secretAccessKey());
            String date = AWSSigner.signingDate();
            String signature = signer.sign("GET",date,"/");
            String token = new StringBuffer("AWS ").append(accessKeyId()).append(":").append(signature).toString();
            HttpRequest _request = HttpRequest.newBuilder()
                    .uri(URI.create(h))
                    .timeout(Duration.ofSeconds(HttpCaller.TIME_OUT))
                    .header(HttpCaller.AUTHORIZATION,token)
                    .header("Date",date)
                    .header(HttpCaller.ACCEPT, HttpCaller.ACCEPT_JSON)
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
                logger.warn(responseData.dataAsString);
                if(code == 200) {
                    S3ListBucket s3ListBucket = new S3ListBucket();
                    s3ListBucket.parse(new ByteArrayInputStream(responseData.dataAsString.getBytes()));
                    //logger.warn(responseData.dataAsString);
                }

            return true;
        }catch (Exception ex){
            return false;
        }
    }
}
