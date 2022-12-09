package com.tarantula.admin;

import com.google.gson.*;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.*;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.service.metrics.MetricsSnapshotRequest;
import com.tarantula.platform.service.metrics.MetricsViewMonitor;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.List;

public class SudoRoleModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private AccessIndexService accessIndexService;

    private UserService userService;
    private GsonBuilder builder;
    private MetricsViewMonitor metricsViewMonitor;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            Access acc = userService.loadUser(session.systemId());
            session.write(new PermissionContext(acc.role(),true).toJson().toString().getBytes());
        }
        else if(session.action().equals("onEnablePresenceService")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String root = (String) onAccess.property("user");
            String password = (String) onAccess.property("password");
            String host = (String) onAccess.property("host");
            String suffix = (String) onAccess.property("suffix");
            String localPassword = (String) onAccess.property("localPassword");
            boolean suc = this.tokenValidatorProvider.enablePresenceService(root,password,suffix,host);
            if(suc){
                this.context.clusterProvider().deployService().onEnablePresenceService(root,password,suffix,host);
            }
            session.write(JsonUtil.toSimpleResponse(suc,suc?"remote presence service enabled on ["+host+"]":"failed").getBytes());
        }
        else if(session.action().equals("onDisablePresenceService")){
            this.tokenValidatorProvider.disablePresenceService(session.name());
            this.context.clusterProvider().deployService().onDisablePresenceService(session.name());
            session.write(JsonUtil.toSimpleResponse(true,"remote presence service disabled").getBytes());
        }
        else if(session.action().equals("onResetClusterKey")){
            boolean suc = this.tokenValidatorProvider.resetClusterKey();
            if(suc){
                this.context.clusterProvider().deployService().onResetClusterKey();
            }
            session.write(JsonUtil.toSimpleResponse(suc,suc?"Cluster reset":"failed to reset key").getBytes());
        }
        else if(session.action().equals("onPresenceKey")){
            byte[] key = this.tokenValidatorProvider.clusterKey(session.name());
            PermissionContext permissionContext = new PermissionContext(key!=null?SystemUtil.toBase64String(key):null);
            session.write(permissionContext.toJson().toString().getBytes());
        }
        else if(session.action().equals("onCreateLabeledKey")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String key = tokenValidatorProvider.createAccessKey(acc.typeId());
            PermissionContext pc = new PermissionContext(key);
            session.write(pc.toJson().toString().getBytes());
        }
        else if(session.action().equals("onLabeledKeyList")){
            List<OnAccess> keys = tokenValidatorProvider.accessKeyList();
            session.write(new LabeledAccessKeyContext(keys).toJson().toString().getBytes());
        }
        else if(session.action().equals("onRevokeLabeledKey")){
            //revoke access key
            tokenValidatorProvider.revokeAccessKey(session.name());
            List<OnAccess> keys = tokenValidatorProvider.accessKeyList();
            session.write(new LabeledAccessKeyContext(keys).toJson().toString().getBytes());
        }
        else if(session.action().equals("onTestLabeledKey")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            boolean suc = tokenValidatorProvider.validateAccessKey((String)acc.property(OnAccess.ACCESS_KEY))!=null;
            session.write(toMessage(suc?"key passed":"key failed",suc).getBytes());
        }
        else if(session.action().equals("onStopAccessIndex")){
            accessIndexService.onDisable();
            session.write(toMessage(session.action(),true).getBytes());
        }
        else if(session.action().equals("onStartAccessIndex")){
            accessIndexService.onEnable();
            session.write(toMessage(session.action(),true).getBytes());
        }
        else if(session.action().equals("onFindUser")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String login = (String)acc.property(OnAccess.LOGIN);
            AccessIndex accessIndex = accessIndexService.get(login);
            if(accessIndex!=null){
                session.write(toMessage(accessIndex.distributionKey(),true).getBytes());
            }else{
                session.write(toMessage("["+login+"] not found",false).getBytes());
            }
        }

        else if(session.action().equals("onDeployView")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            OnView onView = new OnViewTrack();
            onView.owner(onAccess.typeId());//associated with a lobby type Id
            onView.viewId((String)onAccess.property("viewId"));
            String moduleContext = onAccess.property("deployUrl")!=null?(String) onAccess.property("deployUrl"):"root";
            if(moduleContext.startsWith("root")){
                int tix = moduleContext.lastIndexOf('/');
                if(tix<0){
                    onView.moduleResourceFile((String) onAccess.property("resourceName"));
                }
                else{
                    onView.moduleResourceFile(moduleContext.substring(tix+1)+"/"+onAccess.property("resourceName"));
                }
            }else{
                String rname = onAccess.property("deployUrl")+"/"+onAccess.property("resourceName");
                onView.moduleResourceFile(rname);
            }
            onView.moduleContext(moduleContext);
            Response suc = this.deploymentServiceProvider.createView(onView);
            session.write(toMessage(suc.message(),suc.successful()).getBytes());
        }
        else if(session.action().equals("onDeployResource")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Response suc = this.deploymentServiceProvider.deployResource((String)onAccess.property("deployUrl"),(String)onAccess.property("resourceName"));
            session.write(toMessage(suc.message(),suc.successful()).getBytes());
        }
        else if(session.action().equals("onMetricsCategory")){
            Metrics metrics = context.metrics(session.name());
            List<String> categories = metrics.categories();
            JsonObject m = new JsonObject();
            JsonArray ms = new JsonArray();
            categories.forEach(category->ms.add(category));
            m.add("categories",ms);
            session.write(m.toString().getBytes());
        }
        else if(session.action().equals("onMetricsRegister")){
            String[] query = session.name().split("#");
            this.metricsViewMonitor.register(new MetricsSnapshotRequest(query[0],query[2],query[1]));
            session.write(toMessage(session.action(),true).getBytes());
        }
        else if(session.action().equals("onMetrics")){
            String[] query = session.name().split("#");
            JsonObject m = this.metricsViewMonitor.metrics(query[0],query[2],query[1]);
            session.write(m.toString().getBytes());
        }
        else if(session.action().equals("onMetricsArchive")){
            String[] query = session.name().split("#");
            Metrics metrics = context.metrics(query[0]);
            JsonObject m = new JsonObject();
            JsonArray ms = new JsonArray();
            LocalDateTime end = LocalDateTime.parse(query[3]);
            for(Property p : metrics.archive(query[2],query[1],end)[0].hourlyGain()){
                JsonObject js = new JsonObject();
                js.addProperty("x",p.name());
                js.addProperty("y",p.value().toString());
                ms.add(js);
            }
            m.add("metrics",ms);
            session.write(m.toString().getBytes());
        }
        else if(session.action().equals("onClusterList")){
            ClusterProvider.Summary summary = this.deploymentServiceProvider.clusterSummary();
            session.write(summary.toJson().toString().getBytes());
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
        this.userService = this.context.serviceProvider(UserService.NAME);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.metricsViewMonitor = new MetricsViewMonitor(this.context);
        this.context.schedule(this.metricsViewMonitor);
        this.context.log("Sudo setup module started", OnLog.INFO);
    }

    private String toMessage(String msg,boolean suc){

        return JsonUtil.toSimpleResponse(suc,msg);
    }


}
