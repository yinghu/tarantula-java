package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.serialization.Portable;
import com.tarantula.platform.*;
import com.tarantula.platform.service.Batch;
import com.tarantula.platform.service.deployment.ServiceConfiguration;
import com.tarantula.platform.service.persistence.*;

public class PortableRegistry extends AbstractRecoverableListener{

	public static final int OID =1;

    public static final int DISTRIBUTION_KEY_CID = 1;
    public static final int APPLICATION_DESCRIPTOR_CID = 2;
    public static final int PROPERTY_CID = 3;
    public static final int DELTA_STAT_CID = 4;
    public static final int SINGLETON_FORWARD_CID = 5;
    public static final int HOUSE_CID = 6;
    public static final int COMPOSITE_KEY_CID = 7;

    public static final int ACCESS_INDEX_CID = 10;
    public static final int APPLICATION_CONFIGURATION_CID = 11;
    public static final int ON_LOBBY_CID = 12;
    public static final int ON_VIEW_OID = 13;
    public static final int STATISTICS_ENTRY_CID = 14;
    public static final int NODE_CID = 15;
    public static final int RESOURCE_KEY_CID = 16;
    public static final int METADATA_CID = 17;

    public static final int NATURAL_KEY_CID = 19;
    public static final int SERVICE_CONFIGURATION_CID = 20;

    public static final int BATCH_CID = 21;
    public static final int LOBBY_CID = 22;

    public static final int ON_INSTANCE_CID = 23;

    public static final int INSTANCE_INDEX_CID = 24;

    public Portable create(int cid) {
		Portable _ins;
		switch(cid){
            case DISTRIBUTION_KEY_CID:
                _ins = new DistributionKey();
                break;
			case APPLICATION_DESCRIPTOR_CID:
				_ins = new DeploymentDescriptor();
				break;
            case PROPERTY_CID:
                _ins = new DistributedProperty();
                break;
            case DELTA_STAT_CID:
                _ins = new DeltaStatistics();
                break;
            case SINGLETON_FORWARD_CID:
                _ins = new SessionForward();
                break;
            case HOUSE_CID:
                _ins = new HouseTrack();
                break;
            case COMPOSITE_KEY_CID:
                _ins = new CompositeKey();
                break;

            case METADATA_CID:
                _ins = new RecoverableMetadata();
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
            case RESOURCE_KEY_CID:
                _ins = new ResourceKey();
                break;
            case  NATURAL_KEY_CID:
                _ins = new NaturalKey();
                break;
            case SERVICE_CONFIGURATION_CID:
                _ins = new ServiceConfiguration();
                break;
            case BATCH_CID:
                _ins = new Batch();
                break;
            case ON_INSTANCE_CID:
                _ins = new OnInstanceTrack();
                break;
            case INSTANCE_INDEX_CID:
                _ins = new InstanceIndex();
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
