package com.tarantula.cci;


import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.SimpleStub;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.event.ServiceActionEvent;
import com.tarantula.platform.service.deployment.ContentMapping;


public class ServiceEventHandler extends AbstractRequestHandler {

	private static final JDKLogger log = JDKLogger.getLogger(ServiceEventHandler.class);
    private final static String METRICS_CATEGORY = "httpServiceCount";
    private TokenValidator auth;
    private String bucket;

    public ServiceEventHandler(){
        super(true);
	}
    public String name(){
        return SERVICE_PATH;
    }
    public void onRequest(OnExchange exchange) throws Exception{
        super.onRequest(exchange);
        String path = exchange.path();
        String token = exchange.header(Session.TARANTULA_TOKEN);//authenticated token
        String action = exchange.header(Session.TARANTULA_ACTION);
        String tag = exchange.header(Session.TARANTULA_TAG);
        String name = exchange.header(Session.TARANTULA_NAME);//key name
        String clientId = exchange.header(Session.TARANTULA_CLIENT_ID);
        String trackId = exchange.header(Session.TARANTULA_TRACK_ID);
        byte[]  _payload = exchange.payload();
        if(token == null || token.equals("undefined")){
            throw new IllegalAccessException("none game client access");
        }
        if(token.endsWith(".clear.token")){
            log.warn("Game client is not authenticated with token ["+token+"]");
            super.onEvent(new ResponsiveEvent("",0,JsonUtil.toSimpleResponse(false,"game not authenticated").getBytes(),0,"application/json",true));
        }
        else if(path.startsWith("/service/action")){
            OnSession id = auth.validateToken(token);
            if(!id.successful()) throw new IllegalAccessException(id.message());
            RoutingKey  routingKey = eventService.routingKey(id.distributionId(),tag);
            ServiceActionEvent actionEvent = new ServiceActionEvent(this.serviceTopic,exchange.id(),_payload);
            actionEvent.distributionId(id.distributionId());
            actionEvent.stub(id.stub());
            actionEvent.ticket(id.ticket());
            actionEvent.trackId(trackId);
            actionEvent.action(action!=null?action:path);
            actionEvent.routingNumber(routingKey.routingNumber());
            actionEvent.destination(routingKey.route());
            actionEvent.name(name);
            actionEvent.clientId(clientId);
            this.eventService.publish(actionEvent);
        }
        else if(path.startsWith("/service/save")){
            OnSession id = auth.validateToken(token);
            if(!id.successful()) throw new IllegalAccessException(id.message());
            String[] parts = name.split("#");
            if(!checksum(_payload,parts[2])){
                log.warn("Checksum not matched from player ["+id.distributionId()+"]");
                super.onEvent(ResponsiveEvent.create(exchange.id(),JsonUtil.toSimpleResponse(false,name).getBytes()));
            }else {
                boolean saved = serviceContext.deploymentServiceProvider().saveContent(tag.split("/")[0], new SimpleStub(id.distributionId()), ContentMapping.forSave(_payload, parts[0], Integer.parseInt(parts[1])));
                super.onEvent(ResponsiveEvent.create(exchange.id(), JsonUtil.toSimpleResponse(saved, name).getBytes()));
            }
        }
        else if(path.startsWith("/service/load")){
            OnSession id = auth.validateToken(token);
            if(!id.successful()) throw new IllegalAccessException(id.message());
            String[] parts = name.split("#");
            Content saved = serviceContext.deploymentServiceProvider().loadContent(tag.split("/")[0],new SimpleStub(id.distributionId()), ContentMapping.forLoad(parts[0],Integer.parseInt(parts[1])));
            if(saved.existed()){
                super.onEvent(ResponsiveEvent.create(exchange.id(),saved.data()));
            }else{
                super.onEvent(ResponsiveEvent.create(exchange.id(),JsonUtil.toSimpleResponse(false,name).getBytes()));
            }
        }
        else{
            throw new UnsupportedOperationException("HTTP ["+exchange.method()+"] request ["+path+"] not supported");
        }
     }

    @Override
    public void start() throws Exception {
        super.start();
        log.info("Application Service Event Handler Started");
    }

    @Override

    public void setup(ServiceContext tcx){
        super.setup(tcx);
        this.auth = tokenValidator.tokenValidator();
        this.bucket = tcx.node().bucketName();
    }

    public String metricsCategory(){
        return METRICS_CATEGORY;
    }

    private boolean checksum(byte[] payload,String checksum){
        String update = tokenValidator.checksum(payload);
        boolean valid = update.equals(checksum);
        if(valid) return true;
        log.warn("Server Checksum : "+update);
        log.warn("Client Checksum : "+checksum);
        return false;

    }
}
