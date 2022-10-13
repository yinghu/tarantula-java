package com.tarantula.cci;

import com.icodesoftware.Event;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.metrics.PerformanceMetrics;
import com.tarantula.platform.service.persistence.RecoverableMetadata;


public class BackupEventHandler extends AbstractRequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(BackupEventHandler.class);

    private TokenValidatorProvider tokenValidatorProvider;
    private BackupProvider backupProvider;


    public String name(){
        return BACKUP_PATH;
    }

    public void onRequest(OnExchange exchange) throws Exception{

        String action = exchange.header(Session.TARANTULA_ACTION);
        String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
        String key = exchange.header(Session.TARANTULA_NAME);
        byte[] _payload = exchange.payload();
        String access = this.tokenValidatorProvider.validateAccessKey(accessKey);
        if(access==null) throw new IllegalAccessException("Invalid key");
        exchange.onEvent(new ResponsiveEvent("","","{}".getBytes(),true));
        log.warn(access);
        log.warn(action);
        log.warn(key);
        log.warn(accessKey);
        String[] km = key.split("#");
        Metadata m = new RecoverableMetadata();
        m.fromBinary(km[1].getBytes());
        this.backupProvider.update(null,km[0],_payload);
        metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1);
    }

    @Override
    public void start() throws Exception {
        //this.builder = new GsonBuilde
        // r();
        //this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        //this.builder.registerTypeAdapter(ConnectionStub.class,new ConnectionDeserializer());
        //this.builder.registerTypeAdapter(ChannelStub.class,new ChannelDeserializer());
        log.info("Backup event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext tcx){
        this.tokenValidatorProvider = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.backupProvider = tcx.backupProvider();
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
