package com.icodesoftware;

public interface Rating extends Recoverable, DataStore.Updatable {

    int rank();
    int level();
    double xp();

    Rating update(double delta,double levelUpLimit);
}
