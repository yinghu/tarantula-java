package com.tarantula.platform.configuration;

import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.HttpCaller;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class S3Upload {

    public void upload(S3Client s3Client, ServiceContext serviceContext,String name, byte[] content) throws Exception{
        String h = "https://"+s3Client.bucket()+".s3."+s3Client.region()+".amazonaws.com/"+name;
        String date = AWSSigner.signingDate();
        AWSSigner awsSigner = s3Client.signer();
        String signature = awsSigner.sign("PUT",date,"/"+s3Client.bucket()+"/"+name);
        String token = new StringBuffer("AWS ").append(s3Client.accessKeyId()).append(":").append(signature).toString();
        HttpRequest _request = HttpRequest.newBuilder()
                .uri(URI.create(h))
                .timeout(Duration.ofSeconds(HttpCaller.TIME_OUT))
                .header(HttpCaller.AUTHORIZATION,token)
                .header("Date",date)
                .header(HttpCaller.ACCEPT, HttpCaller.ACCEPT_JSON)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(content))
                .build();
        HttpCaller.ResponseData responseData = new HttpCaller.ResponseData();
        int code = serviceContext.httpClientProvider().request(client->{
            HttpResponse<String> _response = client.send(_request, HttpResponse.BodyHandlers.ofString());
            responseData.dataAsString = _response.body();
            return _response.statusCode();
        });
        if(code!=200) throw new RuntimeException(responseData.dataAsString);
    }
}
