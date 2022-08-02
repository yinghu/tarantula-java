package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class AmazonAWSProvider extends AuthObject{

    private S3Client s3Client;
    private String region;
    private String bucket;
    public AmazonAWSProvider(String region,String bucket,String accessKeyId,String secretKey){
        super(OnAccess.AMAZON,accessKeyId,secretKey,"","","",new String[]{region});
        this.region = region;
        this.bucket = bucket;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(()-> AwsBasicCredentials.create(this.clientId(),this.secureKey()))
                .build();
        s3Client.listBuckets().buckets().forEach(b->{
            System.out.println(b.name());
        });
    }
    public boolean upload(String name,byte[] content){
        //s3Client.putObject()
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(name).build();
        PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(content));
        return response.sdkHttpResponse().isSuccessful();
        //return true;
    }
}
