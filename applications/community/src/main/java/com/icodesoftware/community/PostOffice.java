package com.icodesoftware.community;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import com.icodesoftware.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;

public class PostOffice {


    static Logger logger = LoggerFactory.getLogger(PostOffice.class);
    private static ManagedChannel channel;


    public static void start() {
        logger.debug("starting post office");
        channel = ManagedChannelBuilder.forAddress("192.168.1.6", 7001).usePlaintext().build();
        PostofficeServiceGrpc.PostofficeServiceStub stub = PostofficeServiceGrpc.newStub(channel);
        var topic = TopicFactory.Topic.newBuilder().setNodeId("community.1").build();
        stub.receive(topic, new StreamObserver<>() {
            @Override
            public void onNext(TopicFactory.Topic topic) {
                logger.debug("onNext topic: " + topic.getName() + ">>" + topic.getEvent().getMessage().getTypeUrl());
                var msg = topic.getEvent().getMessage();
                try{
                    var me = msg.unpack(MessageEventFactory.MessageEvent.class);
                    logger.debug(me.getMessage()+" : "+me.getTitle()+":: "+me.getDateTime()+" : "+me.getDateTime());
                }catch (Exception ex){
                    logger.error(ex.getMessage());
                }
                //logger.debug("any",topic.getEvent().getMessage());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    public static void subscribe(String topic) {
        PostofficeServiceGrpc.PostofficeServiceStub stub = PostofficeServiceGrpc.newStub(channel);
        var top = TopicFactory.Topic.newBuilder().setNodeId("community.1").setTag("community").setName(topic).build();
        stub.subscribe(top, new StreamObserver<>() {
            @Override
            public void onNext(ResponseFactory.Response response) {
                logger.debug("onNext response: " + response.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    public static void disconnect() {
        PostofficeServiceGrpc.PostofficeServiceStub stub = PostofficeServiceGrpc.newStub(channel);
        var top = TopicFactory.Topic.newBuilder().setNodeId("community.1").build();
        stub.disconnect(top, new StreamObserver<>() {
            @Override
            public void onNext(ResponseFactory.Response response) {
                logger.debug("onNext response: " + response.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    public static void message(String message) {
        PostofficeServiceGrpc.PostofficeServiceStub stub = PostofficeServiceGrpc.newStub(channel);
        var tm = Instant.now();
        var tmp = Timestamp.newBuilder().setSeconds(tm.getEpochSecond()).setNanos(tm.getNano()).build();
        var msg = MessageEventFactory.MessageEvent.newBuilder().setMessage(message).setDateTime(tmp).setTitle("msg").setSource("admin").build();
        var hd = HeaderFactory.Header.newBuilder().setFactoryId(10).setClassId(10).build();
        var evt = EventFactory.Event.newBuilder().setHeader(hd).setId(System.currentTimeMillis()).setMessage(Any.pack(msg)).build();
        var topic =  TopicFactory.Topic.newBuilder().setNodeId("community.1").setTag("community").setName("message").setEvent(evt).build();
        stub.publish(topic,new PostOffice.Callback());
    }


    public static void stop(){
        logger.debug("stoping post office");
        disconnect();
        channel.shutdown();
    }

    static class Callback implements StreamObserver<ResponseFactory.Response> {
        static Logger logger = LoggerFactory.getLogger(Callback.class);
        @Override
        public void onNext(ResponseFactory.Response response) {
            logger.debug("onNext response: " + response.getMessage());
        }

        @Override
        public void onError(Throwable throwable) {
            logger.error(throwable.getMessage());
        }

        @Override
        public void onCompleted() {
            logger.debug("onCompleted");
        }
    }
}
