package com.tarantula.admin;

import com.google.gson.*;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.*;

import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.util.List;


public class DataStoreSudoRoleModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;

    private AccessIndexService accessIndexService;

    private UserService userService;
    private GsonBuilder builder;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            Access acc = userService.loadUser(session.distributionId());
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
        else if(session.action().equals("onLoadDataStoreKeys")){
            //this.context.log(session.name(),OnLog.WARN);
            String[] query = session.name().split("#");
            DataStoreSummary sum = this.deploymentServiceProvider.validDataStore(query[0]);
            JsonObject summary = new JsonObject();
            summary.addProperty("name",sum.name());
            //summary.addProperty("partitionNumber",sum.partitionNumber());
            summary.addProperty("totalRecords",sum.totalRecords());
            JsonArray keys = new JsonArray();
            int[] kn = {Integer.parseInt(query[1])};
            int[] batch = {Integer.parseInt(query[2])};
            summary.addProperty("keyStartIndex",kn[0]);
            summary.addProperty("keyEndIndex",kn[0]+batch[0]);
            sum.list((n,h,t)->{
                kn[0]--;
                if(kn[0]<0) {
                    JsonObject debug = new JsonObject();
                    debug.addProperty("name",t.getClass().getName());
                    debug.addProperty("key",t.key().asString());
                    debug.addProperty("local",h.local());
                    debug.addProperty("revision",Long.toString(h.revision()));
                    debug.addProperty("node",n.nodeName());
                    debug.add("content",t.toJson());
                    keys.add(debug);
                    batch[0]--;
                }
                return batch[0] > 0;
            });
            summary.add("keys",keys);
            session.write(summary.toString().getBytes());
        }
        else if(session.action().equals("onLoadDataStoreValue")){
            String[] query = session.name().split("#");
            DataStoreSummary sum = this.deploymentServiceProvider.validDataStore(query[0]);
            JsonObject summary = new JsonObject();
            JsonArray data = new JsonArray();
            if(sum!=null){
                sum.load(query[1].getBytes(),(n,h,t)->{
                    //RevisionObject revisionObject = RevisionObject.fromBinary(v);
                    JsonObject debug = new JsonObject();
                    debug.addProperty("address",n.address());
                    //debug.addProperty("local",revisionObject.local);
                    //debug.addProperty("revision",Long.toString(revisionObject.revision));
                    //debug.addProperty("node",new String(revisionObject.node));
                    //debug.add("content",JsonUtil.parse(revisionObject.data));
                    data.add(debug);
                    return true;
                });
            }
            summary.add("list",data);
            session.write(summary.toString().getBytes());
        }

        else if(session.action().equals("onBackupDataStore")){
            this.deploymentServiceProvider.issueDataStoreBackup();
            session.write(toMessage("backup command issued",true).toString().getBytes());
        }
        else if(session.action().equals("onMetrics")){
            Metrics metrics = context.metrics(Metrics.PERFORMANCE);
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
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        this.userService = this.context.serviceProvider(UserService.NAME);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.context.log("Admin Datastore module started", OnLog.INFO);
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
