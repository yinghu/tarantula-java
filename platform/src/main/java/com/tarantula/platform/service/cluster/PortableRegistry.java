package com.tarantula.platform.service.cluster;

import com.icodesoftware.Recoverable;

import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.*;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.event.SessionForward;
import com.tarantula.platform.service.AccessKey;
import com.tarantula.platform.service.KeyIndexTrack;
import com.tarantula.platform.service.PresenceKey;
import com.tarantula.platform.service.ServiceEventLog;
import com.tarantula.platform.service.persistence.*;

public class PortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

	public static final int OID = 1;

    public static final int PROPERTY_CID = 3;

   public static final int PARTITION_STATE_OID = 6;
    public static final int APPLICATION_CONFIGURATION_CID = 11; //DEPLOY OBJECT
    public static final int ON_LOBBY_CID = 12;

    public static final int LOBBY_TYPE_ID_INDEX_CID = 13;
    public static final int MODULE_INDEX_CID = 14;

    public static final int ACCESS_KEY = 16;

    public static final int PRESENCE_KEY_CID = 17;

    public static final int ON_SESSION_CID = 25;//DEPLOY OBJECT

    public static final int INDEX_SET_CID = 26;

    public static final int FIFO_INDEX_SET_CID = 27;

    public static final int ON_VIEW_OID = 28;

    public static final int PROPERTY_INDEX_SET_CID = 29;

    public static final int SERVICE_EVENT_LOG_CID = 30;

    //START 100 working with EVENT PORTABLE on same OID
    public static final int SINGLETON_FORWARD_CID = PortableEventRegistry.SINGLETON_FORWARD_CID;

    public static final int METADATA_CID = PortableEventRegistry.METADATA_CID;

    public static final int LOBBY_CID =PortableEventRegistry.LOBBY_CID;//DEPLOY OBJECT

    public static final int APPLICATION_DESCRIPTOR_CID = PortableEventRegistry.APPLICATION_DESCRIPTOR_CID; //DEPLOY OBJECT

    public static final int ACCESS_INDEX_CID = PortableEventRegistry.ACCESS_INDEX_CID;

    public static final int CLIENT_CONNECTION_CID = PortableEventRegistry.CLIENT_CONNECTION_CID;

    public static final int KEY_INDEX_CID = PortableEventRegistry.KEY_INDEX_CID;

    public static PortableRegistry INS;
    public PortableRegistry(){
        INS = this;
    }

    public T create(int cid) {
        Recoverable _ins;
		switch(cid){
            case PROPERTY_CID:
                _ins = new DistributedProperty();
                break;
            case ACCESS_INDEX_CID:
                _ins = new AccessIndexTrack();
                break;
            case APPLICATION_CONFIGURATION_CID:
                _ins = new ApplicationConfiguration();
                break;
            case PARTITION_STATE_OID:
                _ins = new PartitionState();
                break;
            case ON_LOBBY_CID:
                _ins = new OnLobbyTrack();
                break;
            case LOBBY_TYPE_ID_INDEX_CID:
                _ins = new LobbyTypeIdIndex();
                break;
            case MODULE_INDEX_CID:
                _ins = new ModuleIndex();
                break;
            case ON_VIEW_OID:
                _ins = new OnViewTrack();
                break;

            case ACCESS_KEY:
                _ins = new AccessKey();
                break;

            case ON_SESSION_CID:
                _ins = new OnSessionTrack();
                break;
            case INDEX_SET_CID:
                _ins = new IndexSet();
                break;
            case PROPERTY_INDEX_SET_CID:
                _ins = new PropertyIndexSet();
                break;
            case SINGLETON_FORWARD_CID:
                _ins = new SessionForward();
                break;
            case METADATA_CID:
                _ins = new RecoverableMetadata();
                break;
            case APPLICATION_DESCRIPTOR_CID:
                _ins = new DeploymentDescriptor();
                break;
            case CLIENT_CONNECTION_CID:
                _ins = new ClientConnection();
                break;
            case PRESENCE_KEY_CID:
                _ins = new PresenceKey();
                break;
            case FIFO_INDEX_SET_CID:
                _ins = new FIFOIndexSet();
                break;
            case SERVICE_EVENT_LOG_CID:
                _ins = new ServiceEventLog();
                break;
            case KEY_INDEX_CID:
                _ins = new KeyIndexTrack();
                break;
            default:
                throw new IllegalArgumentException("Not supported event type");
		}
		return (T)_ins;
	}

    @Override
    public int registryId() {
        return OID;
    }

}
