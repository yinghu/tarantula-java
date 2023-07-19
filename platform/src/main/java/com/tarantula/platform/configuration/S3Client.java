package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;

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

    public AWSSigner signer(){
        return signer;
    }
    @Override
    public boolean validate(ServiceContext serviceContext) {
        try{
            logger = serviceContext.logger(S3Client.class);
            signer = new AWSSigner();
            signer.init(secretAccessKey());
            S3ListBucket s3ListBucket = new S3ListBucket();
            s3ListBucket.request(this,serviceContext,bucket -> {
                logger.warn("BUCKET->"+bucket);
            });
            return true;
        }catch (Exception ex){
            logger.error("validation failed",ex);
            return false;
        }
    }
}
