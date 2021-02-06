package com.tarantula.cci.websocket;

import com.icodesoftware.*;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.service.ConnectionEventService;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;

/**
 * Created by yinghu lu on 9/25/2020.
 */
public class WebSocketSessionService implements ConnectionEventService,WebSocket.Listener {

    private Connection serverConnection;
    private WebSocket webSocket;
    private TokenValidatorProvider tokenValidatorProvider;
    private Presence presence;
    private String serverLogin;
    public WebSocketSessionService(Connection serverConnection,TokenValidatorProvider tokenValidatorProvider,String serverLogin){
        this.serverConnection = serverConnection;
        this.tokenValidatorProvider = tokenValidatorProvider;
        this.serverLogin = serverLogin;
    }

    @Override
    public void publish(Event out) {

    }
    public void publish(byte[] payload,String label, Connection connection){
        System.out.println("outbound->"+label+">>"+serverConnection.connectionId());
        if(webSocket==null){
            try {
                String ticket = tokenValidatorProvider.tokenValidator().ticket(serverLogin,presence.count(0));
                String protocol = serverConnection.secured() ? "wss://" : "ws://";
                StringBuffer query = new StringBuffer();
                query.append("connectionId="+serverConnection.connectionId());
                query.append("&accessKey="+ticket);
                query.append("&stub="+presence.count(0));
                query.append("&systemId=root");
                URI uri = new URI(protocol + serverConnection.host() + ":" + serverConnection.port()+"/"+serverConnection.path()+"?"+ URLEncoder.encode(query.toString(),"UTF-8"));
                //System.out.println(uri.toURL().getQuery());
                webSocket = HttpClient.newHttpClient().newWebSocketBuilder().header("origin",serverConnection.host()).subprotocols("tarantula-service").buildAsync(uri, this).join();
            }catch (Exception ex){
                ex.printStackTrace();
           }
        }
        webSocket.sendText(label,true);
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
        presence = this.tokenValidatorProvider.presence(serverLogin);
    }
    @Override
    public void shutdown() throws Exception {

    }
}
