package com.tarantula.game.service;

import com.tarantula.game.Rating;
import com.tarantula.game.Stub;

public interface DistributionRoomService {

    String NAME = "RoomService";

    Stub join(Rating rating);
}
