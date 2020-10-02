package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.BatchData;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.service.persistence.RecoverableMetadata;


public class PortableEventRegistry implements PortableFactory {

	public static final int OID =1;

	public static final int APPLICATION_ACTION_EVENT_CID = 1;

    public static final int RESPONSIVE_EVENT_CID = 2;

    public static final int INSTANCE_PLAY_EVENT_CID = 3;

    public static final int FAST_PLAY_EVENT_CID = 5;

    public static final int ON_DEPLOY_EVENT_CID = 6;

    public static final int SERVICE_ACTION_EVENT_CID = 9;

    public static final int APPLICATION_SERVICE_EVENT_CID = 10;

    public static final int MAP_STORE_SYNC_EVENT_CID =11;

    public static final int SERVER_PUSH_EVENT_CID = 14;

    public static final int MODULE_RESET_EVENT_CID = 15;

    public static final int LEADER_BOARD_GLOBAL_EVENT_CID = 20;

    public static final int CONNECTION_STATE_EVENT_CID = 21;


    //EVENT PORTABLE OBJECTS
    public static final int SINGLETON_FORWARD_CID = 100;

    public static final int METADATA_CID = 101;

    public static final int LOBBY_CID = 102;
    public static final int APPLICATION_DESCRIPTOR_CID =103;

    public static final int BATCH_CID = 104;

    public static final int GAME_CLUSTER_CID = 106;

    public static final int ACCESS_INDEX_CID = 107;

    public Portable create(int cid) {
        Portable _ins;
		switch(cid){
			case APPLICATION_ACTION_EVENT_CID:
				_ins = new ApplicationActionEvent();
				break;
			case RESPONSIVE_EVENT_CID:
                _ins = new ResponsiveEvent();
                break;
            case INSTANCE_PLAY_EVENT_CID:
                _ins = new InstancePlayEvent();
                break;
            case FAST_PLAY_EVENT_CID:
                _ins = new FastPlayEvent();
                break;
            case ON_DEPLOY_EVENT_CID:
                _ins = new OnDeployEvent();
                break;

            case SERVICE_ACTION_EVENT_CID:
                _ins = new ServiceActionEvent();
                break;
            case APPLICATION_SERVICE_EVENT_CID:
                _ins = new ApplicationServiceEvent();
                break;

            case MAP_STORE_SYNC_EVENT_CID:
                _ins = new MapStoreSyncEvent();
                break;

            case SERVER_PUSH_EVENT_CID:
                _ins = new ServerPushEvent();
                break;

            case MODULE_RESET_EVENT_CID:
                _ins = new ModuleResetEvent();
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
                _ins = new BatchData();
                break;
            case LEADER_BOARD_GLOBAL_EVENT_CID:
                _ins = new LeaderBoardGlobalEvent();
                break;
            case CONNECTION_STATE_EVENT_CID:
                _ins = new ConnectionStateEvent();
                break;
            case GAME_CLUSTER_CID:
                _ins = new GameCluster();
                break;
            case ACCESS_INDEX_CID:
                _ins = new AccessIndexTrack();
                break;
            default:
				throw new IllegalArgumentException("Not supported event type");
		}
		return _ins;
	}

    public int registryId() {
        return OID;
    }
}
