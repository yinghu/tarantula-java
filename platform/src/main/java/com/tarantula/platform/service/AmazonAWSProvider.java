package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.configuration.AwsS3Configuration;
import com.tarantula.platform.service.metrics.GameClusterMetrics;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class AmazonAWSProvider extends AuthObject{

    private S3Client s3Client;
    private String region;
    private String bucket;
    private String secretKey;

    public AmazonAWSProvider(AwsS3Configuration configuration, MetricsListener metricsListener){
        this(configuration.typeId(),configuration.region(),configuration.bucket(),configuration.accessKeyId(),configuration.secretAccessKey());
        this.applicationMetricsListener = metricsListener;
    }

    public AmazonAWSProvider(String typeId,String region,String bucket,String accessKeyId,String secretKey){
        super(typeId,accessKeyId);
        this.region = region;
        this.bucket = bucket;
        this.secretKey = secretKey;
    }

    public String name(){
        return OnAccess.AMAZON;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        logger = serviceContext.logger(AmazonAWSProvider.class);
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(()-> AwsBasicCredentials.create(this.clientId(),this.secretKey))
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
        onMetrics(GameClusterMetrics.ACCESS_AMAZON_S3_COUNT);
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(name).build();
        PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(content));
        return response.sdkHttpResponse().isSuccessful();
    }
}
