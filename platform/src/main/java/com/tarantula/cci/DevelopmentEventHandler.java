package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.UniverseConnection;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.event.ServerPushEvent;
import com.tarantula.platform.util.ConnectionDeserializer;
import com.tarantula.platform.util.ConnectionSerializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class DevelopmentEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(DevelopmentEventHandler.class);

    private EventService eventService;
    private TokenValidatorProvider tokenValidatorProvider;

    private String serverTopic;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();
    private GsonBuilder builder;

    public String name(){
        return "/development";
    }

    public void onRequest(OnExchange exchange){
        try{
            String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
            String typeId = this.tokenValidatorProvider.validateAccessKey(accessKey);
            if(typeId==null){
                throw new RuntimeException("Illegal access");
            }
            String _homeDir = System.getProperty("user.home");
            String _file = exchange.path().replaceFirst("/development",_homeDir);
            log.warn(_file);
            InputStream inputStream = new FileInputStream(new File(_file));
            byte[] _payload = inputStream.readAllBytes();
            inputStream.close();
            ResponsiveEvent responsiveEvent = new ResponsiveEvent("","",_payload,"start",true);
            exchange.onEvent(responsiveEvent);

        }catch (Exception ex){
            ex.printStackTrace();
            _hex.remove(exchange.id()); //removed cache on any errors
            exchange.onError(ex,ex.getMessage());
        }
    }

    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(UniverseConnection.class,new ConnectionSerializer());
        this.builder.registerTypeAdapter(Connection.class,new ConnectionDeserializer());
        this.serverTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serverTopic,this);
        log.info("Development event handler started->"+System.getProperty("user.home"));
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext tcx){
        this.eventService = tcx.eventService(Distributable.INTEGRATION_SCOPE);
        this.tokenValidatorProvider = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
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
