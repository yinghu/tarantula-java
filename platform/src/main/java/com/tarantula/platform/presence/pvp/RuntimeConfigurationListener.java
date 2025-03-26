package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.configuration.FileCredentialConfiguration;


public class RuntimeConfigurationListener implements Configurable.Listener<FileCredentialConfiguration> {

    private static final TarantulaLogger logger = JDKLogger.getLogger(RuntimeConfigurationListener.class);
    public static final String CONFIG_NAME = "pvp";
    private final PlatformPVPBattleServiceProvider pvpBattleServiceProvider;

    public RuntimeConfigurationListener(PlatformPVPBattleServiceProvider pvpBattleServiceProvider){
        this.pvpBattleServiceProvider = pvpBattleServiceProvider;
    }
    public void onLoaded(FileCredentialConfiguration loaded){
        JsonObject config = JsonUtil.parse(loaded.load());
        this.pvpBattleServiceProvider.resetConfiguration(config);
    }

    public void onRemoved(FileCredentialConfiguration removed){

    }
}
