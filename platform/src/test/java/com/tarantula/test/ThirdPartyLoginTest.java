package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.LoginProvider;
import com.tarantula.platform.presence.ThirdPartyLogin;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ThirdPartyLoginTest extends DataStoreHook{

    @Test(groups = { "ThirdPartyLogin" })
    public void thirdPartyLoginTest() {
        String token = "fzzkU$DnJ$KPP3-(!7=5*+e5M%Dk,PhyG38n/g_nN-AJnq0X:6qR3j:B+K.;LS4Ye#$WY]cr{=%fVn=k;ZzLb!ptG,{#n=nNZFF}W1VWMB@%WtAX;dcA#(}cxF}v+AUxk.0uMB0UK&QBKjUP/R?%,5C*GA.He1$Fr2QP!xr&uCzS,9PFkza}K@c]gB=]::U}VBL[z?a197CjuWdiwD+zvBcaBtr&XLtB(";


        DataStore dataStore = dataStoreProvider.createDataStore(LoginProvider.DataStore);
        ThirdPartyLogin thirdPartyLogin = new ThirdPartyLogin("provider", SystemUtil.oid(),"SDGDGFDSG45236");
        thirdPartyLogin.distributionId(serviceContext.distributionId());
        thirdPartyLogin.dataStore(dataStore);
        Assert.assertTrue(dataStore.createIfAbsent(thirdPartyLogin,false));

        thirdPartyLogin.thirdPartyToken(token);
        thirdPartyLogin.update();

        Assert.assertEquals(thirdPartyLogin.thirdPartyToken(), token);

        ThirdPartyLogin thirdPartyLoginLoaded = new ThirdPartyLogin();
        thirdPartyLoginLoaded.distributionId(thirdPartyLogin.distributionId());
        thirdPartyLoginLoaded.dataStore(dataStore);
        Assert.assertTrue(dataStore.load(thirdPartyLoginLoaded));

        Assert.assertEquals(thirdPartyLoginLoaded.thirdPartyToken(), token);
    }
}
}
