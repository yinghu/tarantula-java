package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.TarantulaApplicationHeader;
import java.io.BufferedInputStream;

/**
 * UPDATED by yinghu lu on 8/25/19
 */
public class AvatarContentApplication extends TarantulaApplicationHeader {


    private byte[][] defaultAvatarList = new byte[2][];


    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        onDefault(session);
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
        context.log("Avatar content application started on ["+descriptor.tag()+"]", OnLog.INFO);
    }
    private void onDefault(Session session){
        session.write(defaultAvatarList[0],0,"image/png",this.descriptor.responseLabel(),true);
    }

}
