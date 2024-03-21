package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;

import com.tarantula.platform.configuration.FileCredentialConfiguration;


public class DownloadCenterProvider extends AuthObject {

    private static JDKLogger log = JDKLogger.getLogger(DownloadCenterProvider.class);
    private PlatformConfigurationServiceProvider configurationServiceProvider;


    public DownloadCenterProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public String name() {
        return OnAccess.DOWNLOAD_CENTER;
    }

    @Override
    public byte[] download(String query) {
        FileCredentialConfiguration fileCredentialConfiguration = configurationServiceProvider.credentialConfiguration(query);
        if(fileCredentialConfiguration==null){
            log.warn("No file configuration available for ["+typeId+"]");
            return new byte[0];
        }
        return fileCredentialConfiguration.load();
    }
}
