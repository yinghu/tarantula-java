package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.MailboxCredentialConfiguration;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;


public class PostOfficeProvider extends AuthObject {

    private static JDKLogger log = JDKLogger.getLogger(PostOfficeProvider.class);
    private PlatformConfigurationServiceProvider configurationServiceProvider;


    public PostOfficeProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public String name() {
        return OnAccess.POST_OFFICE;
    }

    @Override
    public byte[] download(String query) {
        MailboxCredentialConfiguration fileCredentialConfiguration = configurationServiceProvider.credentialConfiguration(query);
        if(fileCredentialConfiguration==null){
            log.warn("No file configuration available for ["+typeId+"]");
            return new byte[0];
        }
        return fileCredentialConfiguration.load();
    }
}
