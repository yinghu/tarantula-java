package com.tarantula.cci;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.OnSessionTrack;
import com.tarantula.platform.event.ServiceActionEvent;



public class ServiceEventHandler extends AbstractRequestHandler {

	private static final JDKLogger log = JDKLogger.getLogger(ServiceEventHandler.class);

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
        String tournamentId = exchange.header(Session.TARANTULA_TOURNAMENT_ID);//instance Id
        String name = exchange.header(Session.TARANTULA_NAME);//key name
        String clientId = exchange.header(Session.TARANTULA_CLIENT_ID);
        String trackId = exchange.header(Session.TARANTULA_TRACK_ID);
        byte[]  _payload = exchange.payload();
        if(path.startsWith("/service/action")){
            OnSession id = new OnSessionTrack();//place holder for public access applications
            RoutingKey routingKey = eventService.routingKey(this.bucket+"/"+exchange.id(),tag);
            if((token!=null)&&(!token.equals("undefined"))){
                id = auth.validateToken(token);//first entry point check
                routingKey = eventService.routingKey(id.distributionId(),tag);
            }
            ServiceActionEvent actionEvent = new ServiceActionEvent(this.serviceTopic,exchange.id(),_payload);
            actionEvent.distributionId(id.distributionId());
            actionEvent.stub(id.stub());
            actionEvent.ticket(id.ticket());
            //actionEvent.token(token);
            actionEvent.trackId(trackId);
            actionEvent.action(action!=null?action:path);
            actionEvent.routingNumber(routingKey.routingNumber());
            actionEvent.destination(routingKey.route());
            if(tournamentId!=null&&tournamentId.length()>5) actionEvent.tournamentId(tournamentId);
            actionEvent.name(name);
            actionEvent.clientId(clientId);
            this.eventService.publish(actionEvent);
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
}
