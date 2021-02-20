package com.tarantula.cci.websocket;

import com.icodesoftware.*;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.HttpCaller;
import com.tarantula.platform.service.ConnectionEventService;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 9/25/2020.
 */
public class WebSocketSessionService implements ConnectionEventService,WebSocket.Listener {

    private Connection serverConnection;
    private WebSocket webSocket;
    private TokenValidatorProvider tokenValidatorProvider;
    private Presence presence;
    private AccessIndex serverLogin;
    public WebSocketSessionService(Connection serverConnection,TokenValidatorProvider tokenValidatorProvider,AccessIndex serverLogin){
        this.serverConnection = serverConnection;
        this.tokenValidatorProvider = tokenValidatorProvider;
        this.serverLogin = serverLogin;
    }

    @Override
    public void publish(Event out) {

    }
    public void publish(byte[] payload,String label, Connection connection){
        if(webSocket==null){
            try {
                String ticket = tokenValidatorProvider.tokenValidator().ticket(serverLogin.distributionKey(),presence.count(0));
                StringBuffer query = new StringBuffer();
                query.append("connectionId="+serverConnection.connectionId());
                query.append("&accessKey="+ticket);
                query.append("&stub="+presence.count(0));
                query.append("&systemId="+serverLogin.owner());
                serverConnection.index(query.toString());
                HttpCaller httpCaller = new HttpCaller("");
                httpCaller._init();
                webSocket = httpCaller.connect(serverConnection,this);//HttpClient.newHttpClient().newWebSocketBuilder().header("origin",serverConnection.host()).subprotocols("tarantula-service").buildAsync(uri, this).join();
            }catch (Exception ex){
                ex.printStackTrace();
           }
        }
        String[] params = label.split(Recoverable.PATH_SEPARATOR);
        int seq = Integer.parseInt(params[0]);
        boolean ack = params.length==2?Boolean.parseBoolean(params[1]):false;
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.ack(ack);
        pendingOutboundMessage.connectionId(connection.connectionId());
        pendingOutboundMessage.sessionId(0);
        pendingOutboundMessage.type(MessageHandler.SERVER_PUSH);
        pendingOutboundMessage.sequence(seq);//client message type
        pendingOutboundMessage.payload(payload);
        webSocket.sendBinary(ByteBuffer.wrap(pendingOutboundMessage.message()),true);
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
        presence = this.tokenValidatorProvider.presence(serverLogin.distributionKey());
    }
    @Override
    public void shutdown() throws Exception {

    }
}
