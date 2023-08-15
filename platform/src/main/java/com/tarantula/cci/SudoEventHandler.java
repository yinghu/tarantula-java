package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.PermissionCheckEvent;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.util.OnAccessSerializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.UUID;


public class SudoEventHandler extends AbstractRequestHandler{

    private static TarantulaLogger log = JDKLogger.getLogger(SudoEventHandler.class);
    private EventService eventService;
    private TokenValidatorProvider tokenValidator;
    private DeploymentServiceProvider deploymentServiceProvider;
    private GsonBuilder builder;
    private OnView invalidView;

    String serviceTopic;
    public SudoEventHandler(){
        super(true);
    }
    public String name(){
        return SUDO_PATH;
    }
    public void onRequest(OnExchange exchange) throws Exception{
        super.onRequest(exchange);
        String token = exchange.header(Session.TARANTULA_TOKEN);
        String sid = exchange.id();
        OnSession id = tokenValidator.tokenValidator().validateToken(token);
        PermissionCheckEvent actionEvent = new PermissionCheckEvent(this.serviceTopic,sid);
        RoutingKey routingKey = eventService.routingKey(id.systemId(),"role/sudo");
        actionEvent.systemId(id.systemId());
        actionEvent.stub(id.stub());
        actionEvent.ticket(id.ticket());
        actionEvent.token(token);
        actionEvent.action("onCheckPermission");
        actionEvent.routingNumber(routingKey.routingNumber());
        actionEvent.destination(routingKey.route());
        this.eventService.publish(actionEvent);
    }

    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccessTrack.class,new OnAccessSerializer());
        this.invalidView = this.deploymentServiceProvider.view(OnView.INVALID_VIEW_ID);
        this.serviceTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serviceTopic,this);
        log.info("Sudo service event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    public void setup(ServiceContext tcx){
        tokenValidator  = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.eventService = tcx.eventService();
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
    }
    public  boolean onEvent(Event event){
        JsonObject resp = JsonUtil.parse(event.payload());
        if(!resp.get("successful").getAsBoolean()){
            return super.onEvent(event);
        }
        OnExchange exchange = eMap.get(event.sessionId());
        Content ret = this.deploymentServiceProvider.resource(exchange.path().substring(1));
        if(!ret.existed()){
            ret = this.deploymentServiceProvider.resource(invalidView.moduleResourceFile());
        }
        return super.onEvent(new ResponsiveEvent("",event.sessionId(),ret.data(),0,ret.type(),true));
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
    public boolean deployable(){return true;}
}
