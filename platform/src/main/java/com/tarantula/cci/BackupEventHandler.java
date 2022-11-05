package com.tarantula.cci;

import com.icodesoftware.Distributable;
import com.icodesoftware.Event;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.persistence.RecoverableMetadata;


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
        String key = exchange.header(Session.TARANTULA_NAME);
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
        if(action.equals("onUpdate")){
            String[] km = key.split("#");
            Metadata m = new RecoverableMetadata();
            m.fromBinary(km[1].getBytes());
            this.backupProvider.update(m,km[0],_payload);
        }
        else if(action.equals("onCreate")){
            String[] km = key.split("#");
            Metadata m = new RecoverableMetadata();
            m.fromBinary(km[1].getBytes());
            this.backupProvider.create(m,km[0],_payload);

        }
        else if(action.equals("onRegister")){
            String[] km = key.split("#");
            int scope = Integer.parseInt(km[0]);
            if(scope== Distributable.INTEGRATION_SCOPE){
                this.backupProvider.registerDataStore(km[1]);
            }
            else if(scope==Distributable.DATA_SCOPE){
                int partitions = Integer.parseInt(km[2]);
                this.backupProvider.registerDataStore(km[1],partitions);
            }
            else{
                throw new IllegalArgumentException("scope ["+scope+"] not supported");
            }
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
