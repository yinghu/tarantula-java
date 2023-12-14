package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.JDBCPoolCredentialConfiguration;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;

public class SQLJDBCProvider extends AuthObject {

    private static JDKLogger log = JDKLogger.getLogger(SQLJDBCProvider.class);
    private PlatformConfigurationServiceProvider configurationServiceProvider;


    public SQLJDBCProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public String name() {
        return OnAccess.JDBC_SQL;
    }

    public boolean upload(String query, byte[] bytes) {
        JDBCPoolCredentialConfiguration webHookCredentialConfiguration = configurationServiceProvider.credentialConfiguration(OnAccess.JDBC_SQL);
        if(webHookCredentialConfiguration==null){
            log.warn("No web hook configuration available for ["+typeId+"]");
            return false;
        }
        //WebClient webClient = webHookCredentialConfiguration.webClient(query);
        //if(webClient==null){
            //log.warn("No web client configuration available for ["+query+"]");
            //return false;
        //}
        //return webClient.post(serviceContext,bytes);
        return true;
    }
}
