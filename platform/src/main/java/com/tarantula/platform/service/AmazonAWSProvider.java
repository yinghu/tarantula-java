package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class AmazonAWSProvider extends AuthObject{

    private TarantulaLogger logger;
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
        logger = serviceContext.logger(AmazonAWSProvider.class);
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(()-> AwsBasicCredentials.create(this.clientId(),this.secureKey()))
                .build();
        int[] bucketCreate ={0};
        s3Client.listBuckets().buckets().forEach(b->{
            if(b.name().equals(bucket)) bucketCreate[0]++;
        });
        if(bucketCreate[0]==0){
            logger.warn("Creating bucket->"+bucket);
            CreateBucketRequest request = CreateBucketRequest.builder().bucket(bucket).build();
            s3Client.createBucket(request);
        }
    }
    public boolean upload(String name,byte[] content){
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(name).build();
        PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(content));
        return response.sdkHttpResponse().isSuccessful();
    }
}
