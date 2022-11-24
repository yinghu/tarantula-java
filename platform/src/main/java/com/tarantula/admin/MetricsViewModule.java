package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.service.metrics.ServiceView;
import com.tarantula.platform.service.metrics.ServiceViewMonitor;


import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class MetricsViewModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;

    private UserService userService;

    private ConcurrentHashMap<String,ServiceView> viewMap = new ConcurrentHashMap<>();
    private Configuration chartConfiguration;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            Access acc = userService.loadUser(session.systemId());
            session.write(new PermissionContext(acc.role(),true).toJson().toString().getBytes());
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
        else if(session.action().equals("onMetrics")){
            String[] query = session.name().split("#");
            Metrics metrics = context.metrics(query[0]);
            JsonObject m = new JsonObject();
            JsonArray ms = new JsonArray();
            for(Property p : metrics.snapshot(query[2],query[1])){
                JsonObject js = new JsonObject();
                js.addProperty("x",p.name());
                js.addProperty("y",p.value().toString());
                ms.add(js);
            }
            m.add("metrics",ms);
            session.write(m.toString().getBytes());
        }
        else if(session.action().equals("onMetricsArchive")){
            String[] query = session.name().split("#");
            Metrics metrics = context.metrics(query[0]);
            JsonObject m = new JsonObject();
            JsonArray ms = new JsonArray();
            for(Property p : metrics.archive(query[2],LocalDateTime.now(),LocalDateTime.now())[0].hourlyGain()){
                JsonObject js = new JsonObject();
                js.addProperty("x",p.name());
                js.addProperty("y",p.value().toString());
                ms.add(js);
            }
            m.add("metrics",ms);
            session.write(m.toString().getBytes());
        }
        else if(session.action().equals("onServiceViewList")){
            ClusterProvider.Summary summary = this.deploymentServiceProvider.clusterSummary();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("successful",true);
            JsonArray list = new JsonArray();
            list.add("tarantula");
            list.add("UDPEndpoint");
            list.add("MirrorClusterBackup");
            jsonObject.add("list",list);
            jsonObject.add("cluster",summary.toJson());
            session.write(jsonObject.toString().getBytes());
        }
        else if(session.action().equals("onEnableServiceView")){
            ServiceProvider serviceProvider = context.serviceProvider(session.name());
            if(serviceProvider != null){
                viewMap.computeIfAbsent(session.name(),k->{
                    ServiceView view = new ServiceView(session.name(),chartConfiguration,()->viewMap.remove(session.name()));
                    ServiceViewMonitor monitor = new ServiceViewMonitor(context,serviceProvider,1000,view);
                    context.schedule(monitor);
                    return view;
                });
                ServiceView view = viewMap.get(session.name());
                session.write(view.toCategoryJson().toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"service provider ["+session.name()+"] not existed").getBytes());
            }
        }
        else if(session.action().equals("onUpdateServiceView")){
            JsonArray categories = JsonUtil.parseAsJsonElement(payload).getAsJsonArray();
            ServiceView view = viewMap.get(session.name());
            if(view!=null&&categories.size()>0){
                session.write(view.toMetricsJson(categories).toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"service view ["+session.name()+"] not existed").getBytes());
            }
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
        this.userService = this.context.serviceProvider(UserService.NAME);
        this.chartConfiguration = this.deploymentServiceProvider.configuration("metrics-view-settings");
        this.context.log("Metrics view module started", OnLog.INFO);
    }

}
