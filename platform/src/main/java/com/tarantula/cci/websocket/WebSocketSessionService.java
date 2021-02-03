package com.tarantula.cci.websocket;

import com.icodesoftware.Connection;
import com.icodesoftware.Event;
import com.icodesoftware.EventListener;
import com.icodesoftware.RoutingKey;
import com.tarantula.platform.service.ConnectionEventService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by yinghu lu on 9/25/2020.
 */
public class WebSocketSessionService implements ConnectionEventService,WebSocket.Listener {

    private Connection serverConnection;
    private WebSocket webSocket;
    private StringBuffer buffer;
    CompletableFuture<?> accumulatedMessage = new CompletableFuture<>();
    public WebSocketSessionService(Connection serverConnection){
        this.serverConnection = serverConnection;
    }

    @Override
    public void publish(Event out) {

    }
    public void publish(byte[] payload,String label, Connection connection){
        System.out.println("outbound->"+label);
        //(webSocket==null){
            //try {
                //String protocol = serverConnection.secured() ? "wss://" : "ws://";
                //URI uri = new URI(protocol + serverConnection.host() + ":" + serverConnection.port()+"/"+serverConnection.path()+"?a=b");
                //webSocket = HttpClient.newHttpClient().newWebSocketBuilder().header("origin",serverConnection.host()).subprotocols("tarantula-service").buildAsync(uri, this).join();
            //}catch (Exception ex){
                //ex.printStackTrace();
           // }
        //}
        //webSocket.sendText(label,true);
    }
    @Override
    public String subscription() {
        return null;
    }

    @Override
    public void retry(String retryKey) {

    }

    @Override
    public void registerEventListener(String topic, EventListener callback) {

    }

    @Override
    public RoutingKey instanceRoutingKey(String applicationId, String instanceId) {
        return null;
    }

    @Override
    public RoutingKey routingKey(String magicKey, String tag) {
        return null;
    }

    @Override
    public RoutingKey routingKey(String magicKey, String tag, int routingNumber) {
        return null;
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }

    @Override
    public void start() throws Exception {
        //String protocol = serverConnection.secured()?"wss://":"ws://";
        //URI uri = new URI(protocol+serverConnection.host()+":"+serverConnection.port());
        //webSocket = HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(uri,this).join();
        buffer = new StringBuffer();
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("web socket-> open");
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        // How do I return the CompletionStage object
        webSocket.request(1);
        buffer.append(data);
        if(last){
            System.out.println(buffer.toString());
            buffer.setLength(0);
            accumulatedMessage.complete(null);
            CompletionStage<?> cf = accumulatedMessage;
            accumulatedMessage = new CompletableFuture<>();
            return cf;
        }
        return accumulatedMessage;
    }
    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        // How do I return the CompletionStage object
        webSocket.request(1);
        System.out.println("received");
        //buffer.append(data);
        if(last){
            System.out.println(buffer.toString());
            buffer.setLength(0);
            accumulatedMessage.complete(null);
            CompletionStage<?> cf = accumulatedMessage;
            accumulatedMessage = new CompletableFuture<>();
            return cf;
        }
        return accumulatedMessage;
    }
    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason){
        System.out.println("web socket error->"+reason);
        return null;
    }
    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.out.println("web socket error->"+error.getMessage());
    }
}
