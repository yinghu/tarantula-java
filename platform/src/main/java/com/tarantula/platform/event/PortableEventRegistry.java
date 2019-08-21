package com.tarantula.platform.event;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;


public class PortableEventRegistry extends AbstractRecoverableListener {

	public static final int OID =2;

	public static final int APPLICATION_ACTION_EVENT_CID = 1;

    public static final int RESPONSIVE_EVENT_CID = 2;

    public static final int INSTANCE_PLAY_EVENT_CID = 3;

    public static final int INDEX_EVENT_CID = 4;

    public static final int FAST_PLAY_EVENT_CID = 5;

    public static final int ON_DEPLOY_EVENT_CID = 6;

    public static final int PENDING_REQUEST_EVENT_CID = 7;

    public static final int SERVICE_ACTION_EVENT_CID = 9;

    public static final int APPLICATION_SERVICE_EVENT_CID = 10;

    public static final int MAP_STORE_SYNC_EVENT_CID =11;

    public static final int MAP_STORE_VOTING_EVENT_CID = 12;

    public static final int MAP_STORE_RECOVERY_EVENT_CID = 13;

    public static final int SERVER_PUSH_EVENT_CID = 14;

    public static final int MODULE_RESET_EVENT_CID = 15;

    public static final int MODULE_LAUNCH_EVENT_CID = 16;

    public static final int MODULE_APPLICATION_EVENT_CID = 17;

    public static final int MODULE_SHUTDOWN_EVENT_CID = 18;

    public static final int MESSAGE_EVENT_CID = 19;

    public Recoverable create(int cid) {
        Recoverable _ins;
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
            case INDEX_EVENT_CID:
                _ins = new IndexEvent();
                break;
            case FAST_PLAY_EVENT_CID:
                _ins = new FastPlayEvent();
                break;
            case ON_DEPLOY_EVENT_CID:
                _ins = new OnDeployEvent();
                break;

            case PENDING_REQUEST_EVENT_CID:
                _ins = new PendingRequestEvent();
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
            case MAP_STORE_VOTING_EVENT_CID:
                _ins = new MapStoreVotingEvent();
                break;
            case MAP_STORE_RECOVERY_EVENT_CID:
                _ins = new MapStoreRecoveryEvent();
                break;
            case SERVER_PUSH_EVENT_CID:
                _ins = new ServerPushEvent();
                break;
            case MODULE_RESET_EVENT_CID:
                _ins = new ModuleResetEvent();
                break;
            case MODULE_LAUNCH_EVENT_CID:
                _ins = new ModuleLaunchEvent();
                break;
            case MESSAGE_EVENT_CID:
                _ins = new MessageEvent();
                break;
            case MODULE_APPLICATION_EVENT_CID:
                _ins = new ModuleApplicationEvent();
                break;
            case MODULE_SHUTDOWN_EVENT_CID:
                _ins = new ModuleShutdownEvent();
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
