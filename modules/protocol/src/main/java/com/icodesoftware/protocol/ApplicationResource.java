package com.icodesoftware.protocol;

import com.icodesoftware.Configurable;
import com.icodesoftware.service.ApplicationPreSetup;

public interface ApplicationResource extends Configurable {

    String name();
    interface Listener{
        void onApplicationResourceRegistered(ApplicationResource resource);
        void onApplicationResourceReleased(ApplicationResource resource);
    }

    interface Redeemer{
        void redeem(ApplicationPreSetup applicationPreSetup,ApplicationResource resource);
    }
}
