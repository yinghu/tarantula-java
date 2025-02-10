package com.icodesoftware;

public interface Rating extends Recoverable, DataStore.Updatable {

    int rank();
    int level();
    double xp();
    void level(int eloAssigned);
    Rating update(double delta,double levelUpLimit);

    default Rating elo(boolean win,long opponentId,int teamPower){ return null;}
}
