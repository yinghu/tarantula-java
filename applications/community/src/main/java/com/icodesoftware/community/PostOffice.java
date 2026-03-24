package com.icodesoftware.community;

import com.icodesoftware.proto.PostofficeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostOffice {


    static Logger logger = LoggerFactory.getLogger(PostOffice.class);
    private static ManagedChannel channel;


    public static void start() {
        logger.debug("starting post office");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("192.168.1.11", 7001).usePlaintext().build();
        PostofficeServiceGrpc.PostofficeServiceStub stub = PostofficeServiceGrpc.newStub(channel);
        stub = PostofficeServiceGrpc.newStub(channel);

    }

    @PreDestroy
    public static void stop(){
        logger.debug("stoping post office");
        if(channel != null){
            channel.shutdown();
        }
    }
}
