package com.tarantula.platform.service.user;

import com.icodesoftware.Access;
import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.service.DataStoreProvider;

import com.tarantula.platform.presence.User;
import java.util.UUID;

public class UserMigrationListener implements DataStoreProvider.MigrationListener {

    protected String login;
    protected String password;//hash of the password
    protected String emailAddress; //reset validation email address
    protected boolean activated;
    protected boolean validated;
    protected boolean primary;
    protected long primaryId;
    protected String validator;
    protected String role;
    protected int routingNumber;
    protected long distributionId;
    @Override
    public void migrate(DataStoreProvider dataStoreProvider) {
        DataStore userData = dataStoreProvider.createDataStore(Access.DataStore);
        String tem = UUID.randomUUID().toString();
        DataStore userTem = dataStoreProvider.createLocalDataStore(tem);
        DataStore userIndex = dataStoreProvider.createKeyIndexDataStore(TransactionLogManager.DATA_PREFIX_I+Access.DataStore);
        userData.backup().forEach((k,v)->{
            readKey(k);
            Recoverable.DataHeader header = v.readHeader();
            read(v);
            _User md = toUser(header);
            userTem.backup().set((kb,vb)->{
                md.user.writeKey(kb);
                vb.writeHeader(md.header);
                md.user.write(vb);
                return true;
            });
            return true;
        });
        userData.backup().drop(false);
        userIndex.backup().drop(false);
        userTem.backup().forEach((k,v)->{
            User user = new User();
            user.readKey(k);
            Recoverable.DataHeader header = v.readHeader();
            user.read(v);
            userIndex.backup().set((keyBuffer, dataBuffer) -> {
                user.writeKey(keyBuffer);
                dataBuffer.writeHeader(header);
                user.write(dataBuffer);
                return true;
            });
            return true;
        });
        userTem.backup().drop(true);
    }
    public boolean readKey(Recoverable.DataBuffer buffer){
        this.distributionId = buffer.readLong();
        return true;
    }

    public boolean write(Recoverable.DataBuffer buffer){
        buffer.writeUTF8(login);
        buffer.writeUTF8(password);
        buffer.writeUTF8(role);
        buffer.writeBoolean(activated);
        buffer.writeInt(routingNumber);
        buffer.writeBoolean(validated);
        buffer.writeUTF8(emailAddress);
        buffer.writeUTF8(validator);
        buffer.writeBoolean(this.primary);
        buffer.writeLong(primaryId);
        return true;
    }
    public boolean read(Recoverable.DataBuffer buffer) {
        this.login = buffer.readUTF8();
        this.password = buffer.readUTF8();
        this.role = buffer.readUTF8();
        this.activated = buffer.readBoolean();
        this.routingNumber = buffer.readInt();
        this.validated = buffer.readBoolean();
        this.emailAddress = buffer.readUTF8();
        this.validator = buffer.readUTF8();
        this.primary = buffer.readBoolean();
        this.primaryId = buffer.readLong();
        return true;
    }
    private _User toUser(Recoverable.DataHeader header){
        User user = new User(login,validated,validator);
        user.login(login);
        user.password(password);
        user.role(role);
        user.activated(activated);
        user.emailAddress(emailAddress);
        user.primary(primary);
        user.primaryId(primaryId);
        user.routingNumber(routingNumber);
        user.distributionId(distributionId);
        return new _User(header,user);
    }

    private static class _User{
        public _User(Recoverable.DataHeader header,User user){
            this.header = header;
            this.user = user;
        }
        Recoverable.DataHeader header;
        User user;
    }


    public boolean migrate(DataStore dataStore){
        return false;
    }

}
