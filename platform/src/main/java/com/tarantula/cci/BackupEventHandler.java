package com.tarantula.cci;

import com.icodesoftware.Event;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.OnExchange;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.service.metrics.PerformanceMetrics;



public class BackupEventHandler extends AbstractRequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(BackupEventHandler.class);

    private TokenValidatorProvider tokenValidatorProvider;
    private DeploymentServiceProvider deploymentServiceProvider;


    public String name(){
        return BACKUP_PATH;
    }

    public void onRequest(OnExchange exchange) throws Exception{

        String action = exchange.header(Session.TARANTULA_ACTION);
        String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
        String serverId = exchange.header(Session.TARANTULA_SERVER_ID);
        byte[] _payload = exchange.payload();
        String typeId = tokenValidatorProvider.validateGameClusterAccessKey(accessKey);
        if(typeId==null){
            throw new RuntimeException("Illegal access");
        }
        metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1);
    }

    @Override
    public void start() throws Exception {
        //this.builder = new GsonBuilder();
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
        this.deploymentServiceProvider = tcx.deploymentServiceProvider();
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
