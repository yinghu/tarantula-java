package com.tarantula.test.ws;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.tarantula.Configuration;
import com.tarantula.Descriptor;
import com.tarantula.OnSession;
import com.tarantula.Response;
import com.tarantula.platform.presence.IndexContext;
import com.tarantula.platform.presence.PresenceContext;
import com.tarantula.test.integration.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yinghu lu on 7/12/2019.
 */
public class WebSocketSimulator implements Runnable,WebSocket.Listener {


    GsonBuilder gsonBuilder;
    AtomicInteger round;
    CountDownLatch countDownLatch;
    String host;
    String user;
    boolean secured;
    AtomicLong totalBytes;
    AtomicLong start;
    AtomicLong end;
    public WebSocketSimulator(boolean secured, String host, GsonBuilder gsonBuilder, AtomicInteger round, CountDownLatch countDownLatch, String user,AtomicLong totalBytes,AtomicLong start,AtomicLong end){
        this.secured = secured;
        this.host = host;
        this.gsonBuilder = gsonBuilder;
        this.round = round;
        this.countDownLatch = countDownLatch;
        this.user = user;
        this.totalBytes = totalBytes;
        this.start = start;
        this.end = end;
    }
    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("total connected["+round.incrementAndGet()+"]");
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        error.printStackTrace();
        WebSocket.Listener.super.onError(webSocket, error);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        start.compareAndSet(0,System.currentTimeMillis());
        end.set(System.currentTimeMillis());
        totalBytes.addAndGet(data.length()*8);
        //System.out.println(data);
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }
    void onWebSocket(PresenceContext presenceContext) throws Exception{
        Configuration conn = presenceContext.connection;
        OnSession login = presenceContext.presence;
        StringBuffer sb = new StringBuffer(conn.property("protocol"));
        sb.append("://").append(conn.property("host")).append(":").append(conn.property("port")).append("/").append(conn.property("path"));
        URI uri = new URI(sb.toString()+"?accessKey="+ URLEncoder.encode(login.ticket(),"utf-8")+"&stub="+login.stub()+"&systemId="+user);
        WebSocket ws = HttpClient.newHttpClient().newWebSocketBuilder().subprotocols("tarantula-service").buildAsync(uri,this).join();
        JsonObject jo = new JsonObject();
        jo.addProperty("action","onStart");
        jo.addProperty("streaming",true);
        jo.addProperty("label","presence/notice");
        JsonObject jd = new JsonObject();
        jd.addProperty("command","onStart");
        jo.add("data",jd);
        System.out.println(gsonBuilder.create().toJson(jo));
        ws.sendText(gsonBuilder.create().toJson(jo),true);

        countDownLatch.await();
        ws.sendClose(WebSocket.NORMAL_CLOSURE,"closed");
    }
    public void run() {
        try{
            IndexContext index = new OnIndexCommand(secured,host,gsonBuilder).call();
            Descriptor[] app = {null};
            index.lobbyList.get(0).entryList().forEach((a)->{
                if(a.tag().equals("user")){
                    app[0] =a;
                }
            });
            Response register = new OnRegistrationCommand(secured,host,gsonBuilder,user,"password",app[0]).call();
            if(register.successful()){
                PresenceContext login = new OnLoginCommand(secured,host,gsonBuilder,user,"password",app[0]).call();
                if(login.successful()){
                    PresenceContext presence = new OnPresenceCommand(secured,host,gsonBuilder,login).call();
                    if(presence.successful()){
                        presence.presence = login.presence;
                        onWebSocket(presence);
                        Response logout = new OnLogoutCommand(secured,host,gsonBuilder,login).call();
                        if(!logout.successful()){
                            System.out.println("logout failed");
                        }
                    }
                    else{
                        System.out.println("presence failed");
                    }
                }
                else{
                    System.out.println("login failed");
                }
            }
            else{
                System.out.println("registration failed");
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
