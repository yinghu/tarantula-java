package com.tarantula.game.service;

import com.tarantula.game.GameLobby;

public class ServiceCommand {

    public final static short REQUEST_STATISTICS = 1;
    public final static short COMMIT_STATISTICS = 2;

    public final static short REQUEST_TOURNAMENT_LEADERBOARD = 3;
    public final static short COMMIT_TOURNAMENT_SCORE = 4;

    public final static short REQUEST_ACHIEVEMENT_LIST = 5;
    public final static short COMMIT_ACHIEVEMENT = 6;

    public final static short REQUEST_LEADER_BOARD = 7;

    public static GameLobby.ServiceMessageListener messageListener(short command){
        GameLobby.ServiceMessageListener callback = new ErrorCommand();
        switch (command){
            case REQUEST_ACHIEVEMENT_LIST:
                break;
            case COMMIT_STATISTICS:
                callback = new CommitStatisticsCommand();
                break;
            case REQUEST_STATISTICS:
                callback = new RequestStatisticsCommand();
                break;
            case REQUEST_TOURNAMENT_LEADERBOARD:
                callback = new RequestTournamentLeaderBoardCommand();
                break;
            case COMMIT_TOURNAMENT_SCORE:
                callback = new CommitTournamentScoreCommand();
                break;
            case COMMIT_ACHIEVEMENT:
                callback = new CommitAchievementCommand();
                break;
            case REQUEST_LEADER_BOARD:
                callback = new RequestLeaderBoardCommand();
                break;
        }
        return callback;
    }

}
