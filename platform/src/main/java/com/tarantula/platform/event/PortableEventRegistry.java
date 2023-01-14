package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.tarantula.cci.udp.GameChannel;
import com.tarantula.game.Arena;
import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.room.*;
import com.tarantula.game.Rating;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.service.persistence.RecoverableMetadata;
import com.tarantula.platform.tournament.TournamentEntry;
import com.tarantula.platform.tournament.TournamentHeader;
import com.tarantula.platform.tournament.TournamentInstanceHeader;
import com.tarantula.platform.tournament.TournamentRaceBoard;


public class PortableEventRegistry implements PortableFactory {

	public static final int OID =1;

    public static final int RESPONSIVE_EVENT_CID = 2;

    public static final int FAST_PLAY_EVENT_CID = 5;

    public static final int SERVICE_ACTION_EVENT_CID = 9;

    public static final int MAP_STORE_SYNC_EVENT_CID =11;

    public static final int MODULE_RESET_EVENT_CID = 15;

    public static final int LEADER_BOARD_GLOBAL_EVENT_CID = 20;

    public static final int GAME_UPDATE_EVENT_CID = 22;

    public static final int TOPIC_MAP_STORE_SYNC_EVENT_CID = 23;


    //EVENT PORTABLE OBJECTS
    public static final int SINGLETON_FORWARD_CID = 100;

    public static final int METADATA_CID = 101;

    public static final int LOBBY_CID = 102;

    public static final int APPLICATION_DESCRIPTOR_CID =103;

    public static final int GAME_CLUSTER_CID = 106;

    public static final int ACCESS_INDEX_CID = 107;

    public static final int EXPOSED_GAME_SERVICE_CID = 108;

    public static final int RATING_CID = 110;

    public static final int TOURNAMENT_CID = 113;

    public static final int TOURNAMENT_INSTANCE_CID = 114;

    public static final int TOURNAMENT_ENTRY_CID = 115;

    public static final int TOURNAMENT_RACE_BOARD_CID = 116;

    public static final int GAME_ROOM_REGISTRY_CID = 117;

    public static final int GAME_ENTRY_CID = 118;

    public static final int ROOM_JOIN_STUB = 119;

    public static final int PVE_ROOM_CID = 120;
    public static final int PVP_ROOM_CID = 121;
    public static final int TVE_ROOM_CID = 122;
    public static final int TVT_ROOM_CID = 123;

    public static final int CLIENT_CONNECTION_CID = 124;

    public static final int GAME_CHANNEL_CID = 125;

    public static final int CHANNEL_STUB_CID = 126;

    public static final int CONNECTION_STUB_CID = 127;

    public Portable create(int cid) {
        Portable _ins;
		switch(cid){

			case RESPONSIVE_EVENT_CID:
                _ins = new ResponsiveEvent();
                break;

            case FAST_PLAY_EVENT_CID:
                _ins = new FastPlayEvent();
                break;

            case SERVICE_ACTION_EVENT_CID:
                _ins = new ServiceActionEvent();
                break;

            case MAP_STORE_SYNC_EVENT_CID:
                _ins = new MapStoreSyncEvent();
                break;
            case GAME_UPDATE_EVENT_CID:
                _ins = new GameUpdateEvent();
                break;
            case TOPIC_MAP_STORE_SYNC_EVENT_CID:
                _ins = new TopicMapStoreSyncEvent();
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

            case LEADER_BOARD_GLOBAL_EVENT_CID:
                _ins = new LeaderBoardGlobalEvent();
                break;

            case GAME_CLUSTER_CID:
                _ins = new GameCluster();
                break;
            case ACCESS_INDEX_CID:
                _ins = new AccessIndexTrack();
                break;
            case RATING_CID:
                _ins = new Rating();
                break;
            case TOURNAMENT_CID:
                _ins = new TournamentHeader();
                break;
            case TOURNAMENT_INSTANCE_CID:
                _ins = new TournamentInstanceHeader();
                break;
            case TOURNAMENT_ENTRY_CID:
                _ins = new TournamentEntry();
                break;
            case TOURNAMENT_RACE_BOARD_CID:
                _ins = new TournamentRaceBoard();
                break;
            case GAME_ROOM_REGISTRY_CID:
                _ins = new GameRoomRegistry();
                break;
            case GAME_ENTRY_CID:
                _ins = new GameEntry();
                break;
            case ROOM_JOIN_STUB:
                _ins = new RoomJoinStub();
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
            case CLIENT_CONNECTION_CID:
                _ins = new ClientConnection();
                break;
            case GAME_CHANNEL_CID:
                _ins = new GameChannel();
                break;
            case CHANNEL_STUB_CID:
                _ins = new ChannelStub();
                break;
            case CONNECTION_STUB_CID:
                _ins = new ConnectionStub();
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
