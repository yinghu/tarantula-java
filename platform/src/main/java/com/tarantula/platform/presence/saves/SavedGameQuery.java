package com.tarantula.platform.presence.saves;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class SavedGameQuery<T extends Recoverable> implements RecoverableFactory<T> {

    private Recoverable.Key key;

    public SavedGameQuery(Recoverable.Key key){
        this.key = key;
    }

    @Override
    public T create() {
        return new PresencePortableRegistry<T>().create(PresencePortableRegistry.SAVED_GAME_CID);
    }


    @Override
    public String label() {
        return SavedGame.USER_SAVE;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
