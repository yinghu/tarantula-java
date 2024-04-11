package com.tarantula.platform.service.user;

import com.icodesoftware.Access;
import com.icodesoftware.DataStore;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DataStoreProvider;
import com.tarantula.platform.presence.User;

public class UserMigrationListener implements DataStoreProvider.MigrationListener {

    private static final TarantulaLogger logger = JDKLogger.getLogger(UserMigrationListener.class);
    @Override
    public void migrate(DataStoreProvider dataStoreProvider) {
        DataStore user = dataStoreProvider.createDataStore(Access.DataStore);
        user.backup().forEach((k,v)->{
            v.readHeader();
            User u = new User();
            u.readKey(k);
            u.read(v);
            logger.warn(u.login()+" ::"+u.distributionId());
            return true;
        });
        logger.warn("Migration user data");
    }
}
