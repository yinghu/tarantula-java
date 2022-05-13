package com.tarantula.game.service;

import com.tarantula.game.GameLobby;

public class ServiceCommand {

    public final static short REQUEST_STATISTICS = 1;
    public final static short COMMIT_STATISTICS = 2;

    public final static short REQUEST_TOURNAMENT_LEADERBOARD = 3;
    public final static short COMMIT_TOURNAMENT_SCORE = 4;

    public final static short REQUEST_ACHIEVEMENT_LIST = 5;
    public final static short COMMIT_ACHIEVEMENT = 6;

    public static GameLobby.ServiceMessageListener messageListener(short command){
        GameLobby.ServiceMessageListener callback = new UnsupportedCommandListener();
        switch (command){
            case REQUEST_ACHIEVEMENT_LIST:
                break;
            case COMMIT_STATISTICS:
                break;
            case REQUEST_STATISTICS:
                break;
            case REQUEST_TOURNAMENT_LEADERBOARD:
                break;
            case COMMIT_TOURNAMENT_SCORE:
                break;
            case COMMIT_ACHIEVEMENT:
                break;
        }
        return callback;
    }

}
