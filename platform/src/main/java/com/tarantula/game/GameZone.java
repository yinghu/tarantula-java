package com.tarantula.game;

import com.icodesoftware.Configurable;
import com.icodesoftware.DataStore;

public interface GameZone extends Configurable, DataStore.Updatable{

    String PLAY_MODE_PVE = "pve"; //player versus computer
    String PLAY_MODE_PVP = "pvp"; //player versus player
    String PLAY_MODE_TVE = "tve"; //team versus computer
    String PLAY_MODE_TVT = "tvt"; //team versus team

    int DEFAULT_LEVEL_COUNT = 10;
    int DEFAULT_LEVEL_UP_BASE = 1000;

    String playMode();
    int levelLimit();
    int capacity();
    Stub join(Rating rating);
}
