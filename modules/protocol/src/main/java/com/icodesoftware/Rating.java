package com.icodesoftware;

public interface Rating extends Recoverable, DataStore.Updatable {

    int rank();
    int level();
    double xp();

    Rating update(double delta,double levelUpLimit);
    Rating update(double delta,Listener listener);


    interface Listener{
        boolean levelUp(double xp);
        boolean rankUp(int level);
    }
}
