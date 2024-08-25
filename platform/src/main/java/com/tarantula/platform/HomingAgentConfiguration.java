package com.tarantula.platform;

import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;

public class HomingAgentConfiguration {

    private static TarantulaLogger logger = JDKLogger.getLogger(HomingAgentConfiguration.class);
    private static ServiceContext SC;


    public static void init(ServiceContext serviceContext){
        SC = serviceContext;
    }

    public static String configuration(String typeId,String category){
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACCESS_KEY, SC.node().homingAgent().accessKey(),
                    Session.TARANTULA_TYPE_ID,typeId,
                    Session.TARANTULA_NAME, category
            };
            String resp = SC.httpClientProvider().get(SC.node().homingAgent().host(), "configuration", headers);
            return resp;
        }catch (Exception ex){
            logger.error("Homing Error",ex);
            return "{}";
        }
    }

}
