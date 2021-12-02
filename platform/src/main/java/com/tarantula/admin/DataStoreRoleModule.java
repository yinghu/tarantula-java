package com.tarantula.admin;

import com.google.gson.*;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.OnViewTrack;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.service.Metrics;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.util.List;


public class DataStoreRoleModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private AccessIndexService accessIndexService;
    private GsonBuilder builder;
    private DataStore uDatastore;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            User acc = new User();
            acc.distributionKey(session.systemId());
            uDatastore.load(acc);
            session.write(new PermissionContext(acc.role(),true).toJson().toString().getBytes());
        }
        else if(session.action().equals("onFindUser")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String login = (String)acc.property(OnAccess.LOGIN);
            AccessIndex accessIndex = accessIndexService.get(login);
            if(accessIndex!=null){
                session.write(toMessage(accessIndex.distributionKey(),true).toString().getBytes());
            }else{
                session.write(toMessage("["+login+"] not found",false).toString().getBytes());
            }
        }
        else if(session.action().equals("onListDataStore")){
            List<String> dlist = this.deploymentServiceProvider.listDataStore();
            session.write(toJsonList(dlist).toString().getBytes());
        }
        else if(session.action().equals("onLoadDataStore")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String dataStore = onAccess.property("dataStore").toString();
            if(this.deploymentServiceProvider.validDataStore(dataStore)){
                DataStore ds = this.context.dataStore(dataStore);
                JsonArray list = new JsonArray();
                JsonParser parser = new JsonParser();
                ds.backup().list((k,v)->{
                    JsonObject r = new JsonObject();
                    r.addProperty("id",new String(k));
                    r.add("payload",_parse(parser,k,v));
                    list.add(r);
                    return true;
                });
                JsonObject ret = new JsonObject();
                ret.add("resultRet",list);
                session.write(ret.toString().getBytes());
            }else{
                session.write(toMessage("data store not existed->"+dataStore,false).toString().getBytes());
            }

        }
        else if(session.action().equals("onBackupDataStore")){
            this.deploymentServiceProvider.issueDataStoreBackup();
            session.write(toMessage("backup commnad issued",true).toString().getBytes());
        }
        else if(session.action().equals("onMetrics")){
            Metrics metrics = this.deploymentServiceProvider.metrics();
            MetricsContext adminContext = new MetricsContext();
            adminContext.metrics = metrics;
            session.write(adminContext.toJson().toString().getBytes());
        }
        else{
           throw new UnsupportedOperationException("operation ["+session.action()+"] not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        this.uDatastore = this.context.dataStore(Access.DataStore);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.context.log("Admin Datastore module started", OnLog.INFO);
    }

    private JsonElement _parse(JsonParser parser, byte[] k,byte[] payload){
        try{
            return parser.parse(new String(payload));
        }catch (Exception ex){
            this.context.log("KEY->"+new String(k),OnLog.WARN);
            this.context.log("LOAD->"+new String(payload),OnLog.WARN);
            return new JsonObject();
        }
    }
    private JsonObject toMessage(String msg,boolean suc){
        JsonObject jms = new JsonObject();
        jms.addProperty("successful",suc);
        jms.addProperty("message",msg);
        return jms;
    }
    private JsonObject toJsonList(List<String> dataStoreList){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonArray clist = new JsonArray();
        dataStoreList.forEach((d)->{
            clist.add(d);
        });
        jsonObject.add("list",clist);
        return jsonObject;
    }
}
