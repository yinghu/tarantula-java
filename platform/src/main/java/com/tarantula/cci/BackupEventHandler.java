package com.tarantula.cci;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Distributable;
import com.icodesoftware.Event;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.ReplicationData;


public class BackupEventHandler extends AbstractRequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(BackupEventHandler.class);

    private TokenValidatorProvider tokenValidatorProvider;
    private BackupProvider backupProvider;
    private ServiceContext serviceContext;


    public String name(){
        return BACKUP_PATH;
    }

    public void onRequest(OnExchange exchange) throws Exception{
        String path = exchange.path();
        String action = exchange.header(Session.TARANTULA_ACTION);
        String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
        byte[] _payload = exchange.payload();
        if(path.equals("/backup/deployment")){
            String typeId = this.tokenValidatorProvider.validateAccessKey(accessKey);
            if(typeId==null) throw new IllegalAccessException("Invalid key");
            exchange.onEvent(new ResponsiveEvent("","", JsonUtil.toSimpleResponse(true,serviceContext.node().deploymentId()).getBytes(),true));
            return;
        }
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
        if(action.equals("onBatch")){
            OnReplication[] onReplications = new OnReplication[1];
            onReplications[0] = new ReplicationData(_payload);
            this.backupProvider.batch(onReplications,1);
        }
        else if(action.equals("onDataStore")){
            JsonObject config = JsonUtil.parse(_payload);
            int scope = config.get("scope").getAsInt();
            this.backupProvider.registerDataStore(scope,config.get("name").getAsString());
        }

        else{
            throw new UnsupportedOperationException("Invalid operation ["+action+"]");
        }
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
        this.serviceContext = tcx;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
