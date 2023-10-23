package com.tarantula.platform.service.cluster;

import com.icodesoftware.Recoverable;

import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.*;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.event.SessionForward;
import com.tarantula.platform.room.*;
import com.tarantula.platform.service.AccessKey;
import com.tarantula.platform.service.KeyIndexTrack;
import com.tarantula.platform.service.PresenceKey;
import com.tarantula.platform.service.ServiceEventLog;
import com.tarantula.platform.service.persistence.TransactionLog;
import com.tarantula.platform.service.persistence.TransactionResult;
import com.tarantula.platform.tournament.TournamentEntry;
import com.tarantula.platform.tournament.TournamentInstance;
import com.tarantula.platform.tournament.TournamentManager;
import com.tarantula.platform.tournament.TournamentRaceBoard;


public class PortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

	public static final int OID = 1;

    public static final int PROPERTY_CID = 3;

    public static final int PARTITION_STATE_OID = 6;

    public static final int TRANSACTION_LOG_CID = 7;
    public static final int TRANSACTION_RESULT_CID = 8;
    public static final int APPLICATION_CONFIGURATION_CID = 11; //DEPLOY OBJECT
    public static final int ON_LOBBY_CID = 12;

    public static final int LOBBY_TYPE_ID_INDEX_CID = 13;
    public static final int MODULE_INDEX_CID = 14;

    public static final int ACCESS_KEY = 16;

    public static final int PRESENCE_KEY_CID = 17;

    public static final int ON_SESSION_CID = 25;//DEPLOY OBJECT

    public static final int ON_VIEW_OID = 28;

    public static final int PROPERTY_INDEX_SET_CID = 29;

    public static final int SERVICE_EVENT_LOG_CID = 30;

    //START 100 working with EVENT PORTABLE on same OID
    public static final int SINGLETON_FORWARD_CID = PortableEventRegistry.SINGLETON_FORWARD_CID;


    public static final int LOBBY_CID =PortableEventRegistry.LOBBY_CID;//DEPLOY OBJECT

    public static final int APPLICATION_DESCRIPTOR_CID = PortableEventRegistry.APPLICATION_DESCRIPTOR_CID; //DEPLOY OBJECT

    public static final int GAME_CLUSTER_CID = PortableEventRegistry.GAME_CLUSTER_CID;
    public static final int ACCESS_INDEX_CID = PortableEventRegistry.ACCESS_INDEX_CID;

    public static final int TOURNAMENT_CID = PortableEventRegistry.TOURNAMENT_CID;

    public static final int TOURNAMENT_INSTANCE_CID = PortableEventRegistry.TOURNAMENT_INSTANCE_CID;

    public static final int TOURNAMENT_ENTRY_CID = PortableEventRegistry.TOURNAMENT_ENTRY_CID;

    public static final int TOURNAMENT_RACE_BOARD_CID = PortableEventRegistry.TOURNAMENT_RACE_BOARD_CID;
    public static final int GAME_ENTRY_CID = PortableEventRegistry.GAME_ENTRY_CID;

    public static final int PVE_ROOM_CID = PortableEventRegistry.PVE_ROOM_CID;
    public static final int PVP_ROOM_CID = PortableEventRegistry.PVP_ROOM_CID;
    public static final int TVE_ROOM_CID = PortableEventRegistry.TVE_ROOM_CID;
    public static final int TVT_ROOM_CID = PortableEventRegistry.TVT_ROOM_CID;
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
            case PARTITION_STATE_OID:
                _ins = new PartitionState();
                break;
            case TRANSACTION_LOG_CID:
                _ins = new TransactionLog();
                break;
            case TRANSACTION_RESULT_CID:
                _ins = new TransactionResult();
                break;
            case APPLICATION_CONFIGURATION_CID:
                _ins = new ApplicationConfiguration();
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

            case ACCESS_KEY:
                _ins = new AccessKey();
                break;
            case PRESENCE_KEY_CID:
                _ins = new PresenceKey();
                break;
            case ON_SESSION_CID:
                _ins = new OnSessionTrack();
                break;
            case ON_VIEW_OID:
                _ins = new OnViewTrack();
                break;
            case PROPERTY_INDEX_SET_CID:
                _ins = new PropertyIndexSet();
                break;
            case SERVICE_EVENT_LOG_CID:
                _ins = new ServiceEventLog();
                break;
            case SINGLETON_FORWARD_CID:
                _ins = new SessionForward();
                break;

            case APPLICATION_DESCRIPTOR_CID:
                _ins = new DeploymentDescriptor();
                break;
            case LOBBY_CID:
                _ins = new LobbyDescriptor();
                break;
            case GAME_CLUSTER_CID:
                _ins = new GameCluster();
                break;
            case ACCESS_INDEX_CID:
                _ins = new AccessIndexTrack();
                break;
            case TOURNAMENT_CID:
                _ins = new TournamentManager();
                break;
            case TOURNAMENT_INSTANCE_CID:
                _ins = new TournamentInstance();
                break;
            case TOURNAMENT_ENTRY_CID:
                _ins = new TournamentEntry();
                break;
            case TOURNAMENT_RACE_BOARD_CID:
                _ins = new TournamentRaceBoard();
                break;
            case CLIENT_CONNECTION_CID:
                _ins = new ClientConnection();
                break;
            case GAME_ENTRY_CID:
                _ins = new GameEntry();
                break;

            case PVE_ROOM_CID:
                _ins = new PVEGameRoom();
                break;
            case PVP_ROOM_CID:
                _ins = new PVPGameRoom();
                break;
            case TVE_ROOM_CID:
                _ins = new TVEGameRoom();
                break;
            case TVT_ROOM_CID:
                _ins = new TVTGameRoom();
                break;
            case KEY_INDEX_CID:
                _ins = new KeyIndexTrack();
                break;
            default:
                throw new IllegalArgumentException("Not supported event type ["+cid+"]");
		}
		return (T)_ins;
	}

    @Override
    public int registryId() {
        return OID;
    }

}
