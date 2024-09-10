package com.tarantula.platform.service;

import com.icodesoftware.Session;
import com.icodesoftware.Statistics;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.lmdb.MetricsLog;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.ScheduleRunner;
import com.icodesoftware.util.TarantulaAgent;

import java.util.List;

public class PlatformHomingAgent extends TarantulaAgent {

    private ServiceContext serviceContext;
    private static TarantulaLogger logger = JDKLogger.getLogger(PlatformHomingAgent.class);

    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
    }

    public String onConfiguration(long gameClusterId,String category){
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACCESS_KEY, serviceContext.node().homingAgent().accessKey(),
                    Session.TARANTULA_TRACK_ID,gameClusterId+"",
                    Session.TARANTULA_NAME, category
            };
            String resp = serviceContext.httpClientProvider().get(serviceContext.node().homingAgent().host(), "configuration", headers);
            return resp;
        }catch (Exception ex){
            logger.error("Homing Error",ex);
            return "{}";
        }
    }

    public void onMetrics(String name, List<Statistics.Entry> updates){
        if(!enabled()) return;
        serviceContext.schedule(new ScheduleRunner(100,()->{
            try {
                String[] headers = new String[]{
                        Session.TARANTULA_ACCESS_KEY,accessKey()
                };
                serviceContext.httpClientProvider().post(host(), "metrics", headers, MetricsLog.metricsLog(serviceContext.node().nodeName(),name,updates).toBinary());
            }catch (Exception ex){
                logger.warn("error on homing agent metrics log: "+ex.getMessage());
            }
        }));
    }

    @Override
    public void onTransactionLog(byte[] log) {
        if(!enabled()) return;
        serviceContext.schedule(new ScheduleRunner(100,()->{
            try {
                String[] headers = new String[]{
                        Session.TARANTULA_ACCESS_KEY,serviceContext.node().homingAgent().accessKey(),
                        Session.TARANTULA_DATA_ENCRYPTED,"true"
                };
                byte[] encrypted = serviceContext.node().homingAgent().encrypt(log);
                serviceContext.httpClientProvider().post(serviceContext.node().homingAgent().host(), "log", headers,encrypted);
            }catch (Exception ex){
                logger.warn("error on homing agent : "+ex.getMessage());
            }
        }));
    }

    public  String configurationRegistered(String publishedId){
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACCESS_KEY, serviceContext.node().homingAgent().accessKey(),
                    Session.TARANTULA_TRACK_ID,publishedId,
            };
            String resp = serviceContext.httpClientProvider().get(serviceContext.node().homingAgent().host(), "configuration/registered", headers);
            return resp;
        }catch (Exception ex){
            logger.error("Homing Error",ex);
            return "{}";
        }
    }
    public  String configurationReleased(String publishedId){
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACCESS_KEY, serviceContext.node().homingAgent().accessKey(),
                    Session.TARANTULA_TRACK_ID,publishedId,
            };
            String resp = serviceContext.httpClientProvider().get(serviceContext.node().homingAgent().host(), "configuration/released", headers);
            return resp;
        }catch (Exception ex){
            logger.error("Homing Error",ex);
            return "{}";
        }
    }
}
