package com.icodesoftware.integration.game;

import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;

/**
 * Created by yinghu lu on 11/15/2020.
 */
public class DynamicBattle extends AbstractGame {

    public DynamicBattle(GameChannelService gameChannelService, GameChannel gameChannel){
        super(gameChannelService,gameChannel);
    }

}
