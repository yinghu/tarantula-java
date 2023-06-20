package com.tarantula.platform.presence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.achievement.Achievement;
import com.tarantula.platform.achievement.AchievementProgress;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.inventory.PendingReward;
import com.tarantula.platform.lobby.ArenaItem;
import com.tarantula.platform.lobby.LobbyItem;
import com.tarantula.platform.lobby.RoomItem;
import com.tarantula.platform.lobby.ZoneItem;
import com.tarantula.platform.presence.dailygiveaway.DailyLoginTrack;
import com.tarantula.platform.presence.saves.*;
import com.tarantula.platform.resource.GameResource;

public class PresencePortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 3;

    public static final int PRESENCE_CID = 1;

    public static final int DAILY_LOGIN_TRACK_CID = 2;

    public static final int ACHIEVEMENT_CID = 3;

    public static final int ACHIEVEMENT_PROGRESS_CID = 4;

    public static final int PLAY_LIST_CID = 5;

    public static final int DAILY_GIVEAWAY_CID = 6;

    public static final int PROFILE_CID = 7;

    public static final int SAVED_GAME_CID = 8;

    public static final int CURRENT_SAVE_INDEX_CID = 9;
    public static final int SAVE_GAME_INDEX_CID = 10;

    public static final int PENDING_REWARD_CID = 11;

    public static final int THIRD_PARTY_LOGIN_CID = 12;

    public static final int PLAYER_SAVE_INDEX_CID = 13;

    public static final int PERSONAL_DATA_INDEX_CID = 14;

    public static final int LOBBY_ITEM_CID = 15;

    public static final int ZONE_ITEM_CID = 16;

    public static final int ARENA_ITEM_CID = 17;

    public static final int ROOM_ITEM_CID = 18;

    public static final int GAME_RESOURCE_CID = 19;

    public static final int PERSONAL_DATA_OBJECT_CID = 20;

    public static final int DEVICE_SAVE_INDEX_CID = 21;

    public static final int GAME_CLUSTER_CID = PortableEventRegistry.GAME_CLUSTER_CID;

    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case PRESENCE_CID:
                pt = new PresenceIndex();
                break;
            case DAILY_LOGIN_TRACK_CID:
                pt = new DailyLoginTrack();
                break;
            case GAME_CLUSTER_CID:
                pt = new GameCluster();
                break;
            case ACHIEVEMENT_CID:
                pt = new Achievement();
                break;
            case ACHIEVEMENT_PROGRESS_CID:
                pt = new AchievementProgress();
                break;
            case PLAY_LIST_CID:
                pt = new PlayList();
                break;
            case PROFILE_CID:
                pt = new Profile();
                break;
            case SAVED_GAME_CID:
                pt = new SavedGame();
                break;
            case CURRENT_SAVE_INDEX_CID:
                pt = new CurrentSaveIndex();
                break;
            case SAVE_GAME_INDEX_CID:
                pt = new SavedGameIndex();
                break;
            case PENDING_REWARD_CID:
                pt = new PendingReward();
                break;
            case THIRD_PARTY_LOGIN_CID:
                pt = new ThirdPartyLogin();
                break;
            case PLAYER_SAVE_INDEX_CID:
                pt = new PlayerSaveIndex();
                break;
            case PERSONAL_DATA_INDEX_CID:
                pt = new PersonalDataIndex();
                break;
            case LOBBY_ITEM_CID:
                pt = new LobbyItem();
                break;
            case ZONE_ITEM_CID:
                pt = new ZoneItem();
                break;
            case ARENA_ITEM_CID:
                pt = new ArenaItem();
                break;
            case ROOM_ITEM_CID:
                pt = new RoomItem();
                break;
            case GAME_RESOURCE_CID:
                pt = new GameResource();
                break;
            case PERSONAL_DATA_OBJECT_CID:
                pt = new PersonalDataObject();
                break;
            case DEVICE_SAVE_INDEX_CID:
                pt = new DeviceSaveIndex();
                break;
            default:
        }
        return pt;
    }

    public int registryId() {
        return OID;
    }
}
