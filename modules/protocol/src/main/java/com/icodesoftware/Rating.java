package com.icodesoftware;

public interface Rating extends Recoverable, DataStore.Updatable {

    int rank();
    int level();
    double xp();
    void level(int eloAssigned);
    Rating update(double delta,double levelUpLimit);

    default Rating elo(boolean win,long opponentId,long teamId){ return null;}

    boolean onCooldown();

    void startCooldown();
}
