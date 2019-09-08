package com.tarantula.platform.service.cluster;

import com.tarantula.Recoverable;
import com.tarantula.platform.*;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.event.SessionForward;
import com.tarantula.platform.service.Batch;
import com.tarantula.platform.service.deployment.ServiceConfiguration;
import com.tarantula.platform.service.persistence.*;

public class PortableRegistry extends AbstractRecoverableListener{

	public static final int OID = 1;

    public static final int PROPERTY_CID = 3;
    public static final int DELTA_STAT_CID = 4;

    public static final int HOUSE_CID = 6;

    public static final int ACCESS_INDEX_CID = 10;
    public static final int APPLICATION_CONFIGURATION_CID = 11; //DEPLOY OBJECT
    public static final int ON_LOBBY_CID = 12;
    public static final int ON_VIEW_OID = 13; //DEPLOY OBJECT
    public static final int STATISTICS_ENTRY_CID = 14; //DEPLOY OBJECT
    public static final int NODE_CID = 15;

    public static final int SERVICE_CONFIGURATION_CID = 20;//DEPLOY OBJECT

    public static final int ON_INSTANCE_CID = 23;//DEPLOY OBJECT

    public static final int INSTANCE_INDEX_CID = 24;//DEPLOY OBJECT

    public static final int ON_SESSION_CID = 25;//DEPLOY OBJECT

    public static final int KEY_INDEX_CID = 26;

    public static final int ON_CONNECTION_CID = 27;

    //START 100 working with EVENT PORTABLE on same OID
    public static final int SINGLETON_FORWARD_CID = PortableEventRegistry.SINGLETON_FORWARD_CID;

    public static final int METADATA_CID = PortableEventRegistry.METADATA_CID;

    public static final int LOBBY_CID =PortableEventRegistry.LOBBY_CID;//DEPLOY OBJECT

    public static final int APPLICATION_DESCRIPTOR_CID = PortableEventRegistry.APPLICATION_DESCRIPTOR_CID; //DEPLOY OBJECT

    public static final int BATCH_CID = PortableEventRegistry.BATCH_CID;



    public Recoverable create(int cid) {
        Recoverable _ins;
		switch(cid){
            case PROPERTY_CID:
                _ins = new DistributedProperty();
                break;
            case DELTA_STAT_CID:
                _ins = new DeltaStatistics();
                break;
            case HOUSE_CID:
                _ins = new HouseTrack();
                break;
            case ACCESS_INDEX_CID:
                _ins = new AccessIndexTrack();
                break;
            case APPLICATION_CONFIGURATION_CID:
                _ins = new ApplicationConfiguration();
                break;
            case ON_LOBBY_CID:
                _ins = new OnLobbyTrack();
                break;
            case ON_VIEW_OID:
                _ins = new OnViewTrack();
                break;
            case STATISTICS_ENTRY_CID:
                _ins = new StatisticsEntry();
                break;
            case NODE_CID:
                _ins = new Node();
                break;
            case SERVICE_CONFIGURATION_CID:
                _ins = new ServiceConfiguration();
                break;

            case ON_INSTANCE_CID:
                _ins = new OnInstanceTrack();
                break;
            case INSTANCE_INDEX_CID:
                _ins = new InstanceIndex();
                break;
            case ON_SESSION_CID:
                _ins = new OnSessionTrack();
                break;
            case KEY_INDEX_CID:
                _ins = new IndexSet();
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
            case BATCH_CID:
                _ins = new Batch();
                break;
            case ON_CONNECTION_CID:
                _ins = new ConnectionInfo();
                break;
            default:
                throw new IllegalArgumentException("Not supported event type");
		}
		return _ins;
	}

    @Override
    public int registryId() {
        return OID;
    }
}
