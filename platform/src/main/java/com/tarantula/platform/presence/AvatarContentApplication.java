package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.TarantulaApplicationHeader;
import java.io.BufferedInputStream;
import java.nio.ByteBuffer;

/**
 * UPDATED by yinghu lu on 5/1/2018.
 */
public class AvatarContentApplication extends TarantulaApplicationHeader {


    private byte[][] defaultAvatarList = new byte[2][];

    private DataStore avatarDataStore;

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        ContentTransaction ct = new ContentTransaction(session.action(),ContentTransaction.AVATAR);
        if(avatarDataStore.load(ct)){
            for(int i=0;i<ct.batchSize;i++){
                ContentChunk cc = new ContentChunk(session.action(),"avatar/"+i);
                ByteBuffer buffer = ByteBuffer.allocate(ct.chunkSize);
                //avatarDataStore.load(cc.key().toByteArray(),(b,v)->{
                    //buffer.put(v);
                //});
                buffer.flip();
                byte[] bcc = new byte[buffer.limit()];
                buffer.get(bcc);
                session.write(bcc,i,ct.contentType,this.descriptor.responseLabel(),i==ct.batchSize-1);
            }
        }else{
            onDefault(session);
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        BufferedInputStream fis = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("web/man.png"));
        byte[] a = new byte[fis.available()];
        fis.read(a);
        defaultAvatarList[0]=a;
        fis.close();
        fis = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("web/woman.png"));
        byte[] b = new byte[fis.available()];
        fis.read(b);
        defaultAvatarList[1]=b;
        fis.close();
        avatarDataStore = context.dataStore("avatar");
        context.log("Avatar Content Application Started", OnLog.INFO);
    }
    private void onDefault(Session session){
        session.write(defaultAvatarList[0],0,"image/png",this.descriptor.responseLabel(),true);
    }

}
