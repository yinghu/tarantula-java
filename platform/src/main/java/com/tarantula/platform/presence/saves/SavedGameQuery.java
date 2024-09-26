package com.tarantula.platform.presence.saves;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Session;


public class SavedGameQuery implements RecoverableFactory<SavedGame> {

    private Session session;

    public SavedGameQuery(Session session){
        this.session = session;
    }

    @Override
    public SavedGame create() {
        return new SavedGame();
    }


    @Override
    public String label() {
        return SavedGame.USER_SAVE;
    }

    @Override
    public Recoverable.Key key() {
        return session.key();
    }
}
