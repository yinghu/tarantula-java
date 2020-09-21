package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.cci.udp.UDPSession;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.event.ServerPushEvent;
import com.tarantula.platform.service.DeployService;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.ConnectionDeserializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class GameServerEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(GameServerEventHandler.class);

    //private String bucket;
    private EventService eventService;
    private TokenValidatorProvider tokenValidatorProvider;
    private DeploymentServiceProvider deploymentServiceProvider;

    private String serverTopic;
    private final ConcurrentHashMap<String, OnExchange> _hex = new ConcurrentHashMap<>();
    private GsonBuilder builder;

    private DeployService deployService;
    public String name(){
        return "/server";
    }
    public void onRequest(OnExchange exchange){
        try{
            String action = exchange.header(Session.TARANTULA_ACTION);
            String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
            String serverId = exchange.header(Session.TARANTULA_SERVER_ID);
            byte[] _payload = exchange.payload();
            if(action.equals("onStart")){//access key
                if(tokenValidatorProvider.validateAccessKey(accessKey)){
                    Connection connection = this.builder.create().fromJson(new String(_payload),Connection.class);
                    //this.deploymentServiceProvider.distributionCallback().onConnection("typeId",new UDPConnection());
                    ServerPushEvent pushEvent = new ServerPushEvent(this.serverTopic,serverId,serverId,_payload);
                    deployService.addServerPushEvent(pushEvent);
                    DatagramChannel datagramChannel = DatagramChannel.open();
                    datagramChannel.connect(new InetSocketAddress(connection.host(),connection.port()));
                    UDPSession udpSession = new UDPSession(serverId,datagramChannel);
                    _hex.put(udpSession.id(),udpSession);
                }
                else{
                    log.warn("Invalid ticket on start");
                }
                exchange.onEvent(new ResponsiveEvent("","",_payload,"server",true));
            }
            else if(action.equals("onStop")){//no more access key check event from server socket
                if(tokenValidatorProvider.validateAccessKey(accessKey)){
                    log.warn("push->"+exchange.path()+"/"+serverId+"/"+exchange.id()+"/"+"/"+action+"/"+exchange.streaming());
                    _hex.forEach((k,v)->{
                        if(v.id().equals(serverId)){
                            _hex.remove(k);
                            deployService.removeServerPushEvent(serverId);
                        }
                    });
                }
                else{
                    log.warn("Invalid ticket on stop");
                }
                exchange.onEvent(new ResponsiveEvent("","",_payload,"server",true));
            }
        }catch (Exception ex){
            ex.printStackTrace();
            _hex.remove(exchange.id()); //removed cache on any errors
            exchange.onError(ex,"Bad request");
        }
    }

    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(Connection.class,new ConnectionDeserializer());
        this.serverTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serverTopic,this);

        log.info("Game server event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext tcx){
        this.eventService = tcx.eventService(Distributable.INTEGRATION_SCOPE);
        this.deployService = tcx.clusterProvider(Distributable.INTEGRATION_SCOPE).deployService();
        this.tokenValidatorProvider = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = tcx.deploymentServiceProvider();
    }
    public  boolean onEvent(Event event){
        OnExchange hx = this._hex.get(event.sessionId());
        if(hx!=null){
           if(hx.onEvent(event)){ //remove on true marked as closed connect or session
               _hex.remove(event.sessionId());
           }
        }
        else{
           log.warn(event.toString()+" unexpected removed on server push");
        }
        return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
