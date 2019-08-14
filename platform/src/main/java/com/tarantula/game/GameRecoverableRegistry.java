package com.tarantula.game;

import com.hazelcast.nio.serialization.Portable;
import com.tarantula.game.casino.blackjack.BlackJack;
import com.tarantula.game.casino.blackjack.BlackJackSeat;
import com.tarantula.game.scc.ShipCaptainCrew;
import com.tarantula.platform.AbstractRecoverableListener;

/**
 * Update by yinghu lu on 5/10/2019.
 */
public class GameRecoverableRegistry extends AbstractRecoverableListener {

    public static final int OID = 100;
    public static final int BLACKJACK_CID = 1;
    public static final int BLACKJACK_SEAT_CID = 2;

    public static final int SHIP_CAPTAIN_CREW_CID = 5;
    @Override
    public int registryId() {
        return 100;
    }

    @Override
    public Portable create(int i) {
        Portable po;
        switch (i){
            case BLACKJACK_CID:
                po = new BlackJack();
                break;
            case BLACKJACK_SEAT_CID:
                po = new BlackJackSeat();
                break;
            case SHIP_CAPTAIN_CREW_CID:
                po = new ShipCaptainCrew();
                break;
                default:
                    throw new IllegalArgumentException("Not supported event type");
        }
        return po;
    }
}
