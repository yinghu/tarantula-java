package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.UDPConnection;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.ResponseSerializer;


public class DedicatedServerEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(DedicatedServerEventHandler.class);

    //private String bucket;
    //private EventService eventService;
    private TokenValidatorProvider tokenValidator;
    private DeploymentServiceProvider deploymentServiceProvider;

    //private String serverTopic;
    //private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();
    private GsonBuilder builder;

    public String name(){
        return "/dedicated";
    }
    public void onRequest(OnExchange exchange){
        try{
            String action = exchange.header(Session.TARANTULA_ACTION);
            String serverId = exchange.header(Session.TARANTULA_SERVER_ID);
            String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
            byte[] _payload = exchange.payload();
            if(action.equals("onRegistered")){ //dedicated server register on cluster
                //String typeId = exchange.header(Session.TARANTULA_TYPE_ID);
                String host = exchange.header("Tarantula-host");
                int port = Integer.parseInt(exchange.header("Tarantula-port"));
                String vLobby = tokenValidator.validateGameClusterAccessKey(accessKey);
                if(vLobby!=null){
                    Connection connection = new UDPConnection(serverId,host,port);
                    this.deploymentServiceProvider.onUDPConnection(vLobby,connection);
                }
                byte[] eb = this.builder.create().toJson(new ResponseHeader("onRegistered",vLobby!=null?"Ok":"invalid access key",vLobby!=null)).getBytes();
                exchange.onEvent(new ResponsiveEvent("","",eb,"dedicated",true));
            }
            else if(action.equals("onStarted")){
                byte[] eb = "{}".getBytes();
                if(tokenValidator.validateGameClusterAccessKey(accessKey)!=null) {
                    eb = this.deploymentServiceProvider.onStartedUDPConnection(serverId);
                }
                exchange.onEvent(new ResponsiveEvent("","",eb,"dedicated",true));
            }
            else if(action.equals("onUpdated")){
                if(tokenValidator.validateGameClusterAccessKey(accessKey)!=null){
                    this.deploymentServiceProvider.onUpdatedUDPConnection(serverId,_payload);
                }
                byte[] eb = this.builder.create().toJson(new ResponseHeader("onUpdated","ok",true)).getBytes();
                exchange.onEvent(new ResponsiveEvent("","",eb,"dedicated",true));
            }
            else if(action.equals("onEnded")){
                if(tokenValidator.validateGameClusterAccessKey(accessKey)!=null){
                    this.deploymentServiceProvider.onEndedUDPConnection(serverId,_payload);
                }
                byte[] eb = this.builder.create().toJson(new ResponseHeader("onEnded","ok",true)).getBytes();
                exchange.onEvent(new ResponsiveEvent("","",eb,"dedicated",true));
            }
        }catch (Exception ex){
            ex.printStackTrace();
            //_hex.remove(exchange.id()); //removed cache on any errors
            exchange.onError(ex,"Bad request");
        }
    }

    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        log.info("Dedicated server event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext tcx){
        this.tokenValidator = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
    }
    public  boolean onEvent(Event event){
        return false;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
