package com.tarantula.cci;

import com.icodesoftware.Event;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.tarantula.platform.GameCluster;
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
        String path = exchange.path();
        String action = exchange.header(Session.TARANTULA_ACTION);
        String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
        String key = exchange.header(Session.TARANTULA_NAME);
        byte[] _payload = exchange.payload();
        if(path.equals("/backup/system")){
            String access = this.tokenValidatorProvider.validateAccessKey(accessKey);
            if(access==null) throw new IllegalAccessException("Invalid key");
        }
        else if(path.equals("/backup/game")){
            GameCluster gameCluster = this.tokenValidatorProvider.validateGameClusterAccessKey(accessKey);
            if(gameCluster==null) throw new IllegalAccessException("Invalid key");
        }
        else{
            throw new IllegalAccessException("Invalid path ["+path+"]");
        }
        exchange.onEvent(new ResponsiveEvent("","","{}".getBytes(),true));
        if(action.equals("onUpdate")){

        }
        else if(action.equals("onCreate")){

        }
        else if(action.equals("onRegister")){

        }
        else{
            throw new UnsupportedOperationException("Invalid operation ["+action+"]");
        }
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
