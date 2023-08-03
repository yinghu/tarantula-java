package com.tarantula.admin;

import com.google.gson.*;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.*;

import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.service.persistence.RevisionObject;
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
            Access acc = userService.loadUser(session.systemId());
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
            DataStore.Summary sum = this.deploymentServiceProvider.validDataStore(query[0]);
            JsonObject summary = new JsonObject();
            summary.addProperty("name",sum.name());
            summary.addProperty("partitionNumber",sum.partitionNumber());
            summary.addProperty("totalRecords",sum.totalRecords());
            JsonArray keys = new JsonArray();
            int[] kn = {Integer.parseInt(query[1])};
            int[] batch = {Integer.parseInt(query[2])};
            summary.addProperty("keyStartIndex",kn[0]);
            summary.addProperty("keyEndIndex",kn[0]+batch[0]);
            sum.dataStore().backup().list((k,v)->{
                kn[0]--;
                if(kn[0]<0) {
                    RevisionObject revisionObject = RevisionObject.fromBinary(v);
                    JsonObject debug = new JsonObject();
                    debug.addProperty("key",new String(k));
                    debug.addProperty("local",revisionObject.local);
                    debug.addProperty("revision",Long.toString(revisionObject.revision));
                    debug.addProperty("node",new String(revisionObject.nodeList));
                    debug.add("content",JsonUtil.parse(revisionObject.data));
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
            DataStore.Summary sum = this.deploymentServiceProvider.validDataStore(query[0]);
            byte[] data = sum.dataStore().load(query[1].getBytes());
            JsonObject summary = new JsonObject();
            if(data!=null) {
                RevisionObject revisionObject = RevisionObject.fromBinary(data);
                JsonObject debug = new JsonObject();
                debug.addProperty("local",revisionObject.local);
                debug.addProperty("revision",Long.toString(revisionObject.revision));
                debug.addProperty("node",new String(revisionObject.nodeList));
                debug.add("content",JsonUtil.parse(revisionObject.data));
                summary.add("debug",debug);
            }
            session.write(summary.toString().getBytes());
        }
        else if(session.action().equals("onAccessIndexStore")){
            String[] query = session.name().split("#");
            int[] kn = {Integer.parseInt(query[0])};
            int[] batch = {Integer.parseInt(query[1])};
            AccessIndexService.AccessIndexStore accessIndexStore = this.deploymentServiceProvider.accessIndexStore();
            JsonObject summary = new JsonObject();
            summary.addProperty("name",accessIndexStore.name());
            summary.addProperty("partitionNumber",accessIndexStore.partitionNumber());
            summary.addProperty("totalRecords",accessIndexStore.count());
            summary.addProperty("keyStartIndex",kn[0]);
            summary.addProperty("keyEndIndex",kn[0]+batch[0]);
            JsonArray keys = new JsonArray();
            accessIndexStore.list((k,v)->{
                kn[0]--;
                if(kn[0]<0) {
                    JsonObject debug = new JsonObject();
                    debug.addProperty("key",new String(k));
                    debug.add("content",JsonUtil.parse(v));
                    keys.add(debug);
                    batch[0]--;
                }
                return batch[0] > 0;
            });
            summary.add("keys",keys);
            session.write(summary.toString().getBytes());
        }
        else if(session.action().equals("onAccessIndexStoreValue")){
            AccessIndexService.AccessIndexStore accessIndexStore = this.deploymentServiceProvider.accessIndexStore();
            session.write(accessIndexStore.get(session.name().getBytes()));
        }
        else if(session.action().equals("onKeyIndexStore")){
            String[] query = session.name().split("#");
            int[] kn = {Integer.parseInt(query[0])};
            int[] batch = {Integer.parseInt(query[1])};
            KeyIndexService.KeyIndexStore accessIndexStore = this.deploymentServiceProvider.keyIndexStore();
            JsonObject summary = new JsonObject();
            summary.addProperty("name",accessIndexStore.name());
            summary.addProperty("partitionNumber",accessIndexStore.partitionNumber());
            summary.addProperty("totalRecords",accessIndexStore.count());
            summary.addProperty("keyStartIndex",kn[0]);
            summary.addProperty("keyEndIndex",kn[0]+batch[0]);
            JsonArray keys = new JsonArray();
            accessIndexStore.list((k,v)->{
                kn[0]--;
                if(kn[0]<0) {
                    JsonObject debug = new JsonObject();
                    debug.addProperty("key",new String(k));
                    debug.add("content",JsonUtil.parse(v));
                    keys.add(debug);
                    batch[0]--;
                }
                return batch[0] > 0;
            });
            summary.add("keys",keys);
            session.write(summary.toString().getBytes());
        }
        else if(session.action().equals("onKeyIndexStoreValue")){
            KeyIndexService.KeyIndexStore keyIndexStore = this.deploymentServiceProvider.keyIndexStore();
            session.write(keyIndexStore.get(session.name().getBytes()));
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
