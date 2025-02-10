package com.tarantula.platform.presence.pvp;

import com.icodesoftware.Rating;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.game.SimpleStub;
import com.tarantula.game.service.PlatformGameServiceProvider;

public class PlayerEloRatingProxy extends RecoverableObject implements Rating {

    private Rating playerRanking;
    private PlatformGameServiceProvider platformGameServiceProvider;
    public PlayerEloRatingProxy(Rating playerRanking, PlatformGameServiceProvider platformGameServiceProvider){
        this.playerRanking = playerRanking;
        this.platformGameServiceProvider = platformGameServiceProvider;
    }

    @Override
    public int rank() {
        return playerRanking.rank();
    }

    @Override
    public int level() {
        return playerRanking.level();
    }

    @Override
    public double xp() {
        return playerRanking.xp();
    }

    public void level(int eloAssigned){

    }

    @Override
    public Rating update(double delta, double levelUpLimit) {
        return playerRanking;
    }

    public Rating elo(boolean win,long opponentId,long teamId){
        DefenseTeam offenseTeam = this.platformGameServiceProvider.pvpBattleServiceProvider().defenseTeam(teamId);
        DefenseTeam defenseTeam = this.platformGameServiceProvider.pvpBattleServiceProvider().defenseTeam(opponentId);
        Rating opponentRaking = this.platformGameServiceProvider.presenceServiceProvider().rating(new SimpleStub(defenseTeam.playerId));
        int playerElo = playerRanking.level();
        int opponentElo = opponentRaking.level();
        PVPPointGenerator.updateELO(playerRanking,opponentRaking,offenseTeam.teamPower,defenseTeam.teamPower,win);
        playerRanking.update();
        opponentRaking.update();

        return this;
    }
}
