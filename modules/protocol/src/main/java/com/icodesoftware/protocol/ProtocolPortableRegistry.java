package com.icodesoftware.protocol;

import com.icodesoftware.Recoverable;
import com.icodesoftware.protocol.presence.AccessKeyTrack;
import com.icodesoftware.protocol.statistics.UserRating;
import com.icodesoftware.protocol.statistics.StatisticsEntry;
import com.icodesoftware.protocol.statistics.UserStatistics;
import com.icodesoftware.util.AbstractRecoverableListener;

public class ProtocolPortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

    public static final int OID = 9;

    public static final int USER_RATION_CID = 2;
    public static final int STATISTICS_CID = 3;
    public static final int STATISTICS_ENTRY_CID = 5;
    public static final int ACCESS_KEY_TRACK_ID = 6;


    @Override
    public int registryId() {
        return OID;
    }

    public static ProtocolPortableRegistry INS;

    public ProtocolPortableRegistry(){
        INS = this;
    }
    @Override
    public T create(int cid) {
        Recoverable _ins;
        switch(cid){
            case USER_RATION_CID:
                _ins = new UserRating();
                break;
            case STATISTICS_CID:
                _ins = new UserStatistics();
                break;
            case STATISTICS_ENTRY_CID:
                _ins = new StatisticsEntry();
                break;
            case ACCESS_KEY_TRACK_ID:
                _ins = new AccessKeyTrack();
                break;
            default:
                throw new RuntimeException("Class ID ["+cid+"] not supported");
        }
        return (T)_ins;
    }
}
