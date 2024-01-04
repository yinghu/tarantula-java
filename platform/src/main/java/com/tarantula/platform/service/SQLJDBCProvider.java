package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.Recoverable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.JDBCPoolCredentialConfiguration;
import com.tarantula.platform.configuration.JDBCTask;
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
    @Override
    public <T extends Recoverable> boolean upload(String query, T content) {
        JDBCPoolCredentialConfiguration webHookCredentialConfiguration = configurationServiceProvider.credentialConfiguration(OnAccess.JDBC_SQL);
        if(webHookCredentialConfiguration==null){
            log.warn("No JDBC configuration available for ["+typeId+"]");
            return false;
        }
        JDBCTask task = webHookCredentialConfiguration.task(query);
        return task.execute(content);
    }
}
