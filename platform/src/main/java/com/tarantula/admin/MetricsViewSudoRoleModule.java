package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.service.metrics.MetricsSnapshotRequest;
import com.tarantula.platform.service.metrics.MetricsViewMonitor;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.List;

public class MetricsViewSudoRoleModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;

    private UserService userService;
    private GsonBuilder builder;
    private MetricsViewMonitor metricsViewMonitor;
    private Configuration chartConfiguration;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            Access acc = userService.loadUser(session.systemId());
            session.write(new PermissionContext(acc.role(),true).toJson().toString().getBytes());
        }
        else if(session.action().equals("onMetricsCategory")){
            ClusterProvider.Summary summary = this.deploymentServiceProvider.clusterSummary();
            Metrics metrics = context.metrics(session.name());
            List<String> categories = metrics.categories();
            JsonObject m = new JsonObject();
            JsonArray ms = new JsonArray();
            categories.forEach(category->ms.add(category));
            m.add("categories",ms);
            JsonArray nodes = new JsonArray();
            JsonArray chs = ((JsonElement)chartConfiguration.property("charts")).getAsJsonArray();
            int[] i = {0};
            summary.clusterNodes().forEach(n->{
                JsonObject nd = new JsonObject();
                nd.addProperty("nodeName",n.nodeName());
                nd.addProperty("memberId",n.memberId());
                nd.add("chart",chs.get(i[0]).getAsJsonObject());
                nodes.add(nd);
                i[0]++;
            });
            m.add("cluster",nodes);
            session.write(m.toString().getBytes());
        }
        else if(session.action().equals("onMetricsRegister")){
            JsonObject query = JsonUtil.parse(payload);
            boolean archived = query.get("archive").getAsBoolean();
            String type = query.get("type").getAsString();
            String category = query.get("category").getAsString();
            String classifier = query.get("classifier").getAsString();
            String queryId;
            if(archived) {
                LocalDateTime endTime = LocalDateTime.parse(query.get("endDate").getAsString());
                queryId = this.metricsViewMonitor.register(new MetricsSnapshotRequest(type,category,classifier,endTime));
            }
            else{
                queryId = this.metricsViewMonitor.register(new MetricsSnapshotRequest(type,category,classifier));
            }
            session.write(toMessage(queryId,true).getBytes());
        }
        else if(session.action().equals("onMetrics")){
            JsonObject m = this.metricsViewMonitor.snapshot(session.name());
            session.write(m.toString().getBytes());
        }
        else if(session.action().equals("onMetricsArchive")){
            JsonObject m = this.metricsViewMonitor.archive(session.name());
            session.write(m.toString().getBytes());
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
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.chartConfiguration = this.deploymentServiceProvider.configuration("metrics-view-settings");
        this.metricsViewMonitor = new MetricsViewMonitor(this.context);
        this.context.schedule(this.metricsViewMonitor);
        this.context.log("Metrics view sudo role module started", OnLog.INFO);
    }

    private String toMessage(String msg,boolean suc){
       return SystemUtil.toJsonMessage(msg,suc);
    }


}
