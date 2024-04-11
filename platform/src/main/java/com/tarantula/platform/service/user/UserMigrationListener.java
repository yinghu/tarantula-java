package com.tarantula.platform.service.user;

import com.icodesoftware.Access;
import com.icodesoftware.DataStore;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.service.DataStoreProvider;
import com.tarantula.platform.presence.User;

import java.util.ArrayList;

public class UserMigrationListener implements DataStoreProvider.MigrationListener {


    @Override
    public void migrate(DataStoreProvider dataStoreProvider) {
        DataStore userData = dataStoreProvider.createDataStore(Access.DataStore);
        DataStore userIndex = dataStoreProvider.createKeyIndexDataStore(TransactionLogManager.DATA_PREFIX_I+Access.DataStore);
        ArrayList<User> users = new ArrayList<>();
        userData.backup().forEach((k,v)->{
            User user = new User();
            user.readKey(k);
            v.readHeader();
            user.read(v);
            //System.out.println("1111>>>>>>>>>>>>>"+user.login()+" ::"+ user.distributionId());
            users.add(user);
            return true;
        });
        users.forEach(user -> {
            userIndex.backup().get(user.key(),(keyBuffer, dataBuffer) -> {
                User uc = new User();
                uc.readKey(keyBuffer);
                dataBuffer.readHeader();
                uc.read(dataBuffer);
                //System.out.println("2222>>>>>>>>>>>>>"+uc.login()+" ::"+uc.distributionId());
                return true;
            });
        });
    }

}
