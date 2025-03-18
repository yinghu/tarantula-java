package com.tarantula.platform.presence.pvp;

public class PVPOpponentsRefreshAnalytic extends PVPAnalytic{

    private static final String MESSAGE_TYPE = "/earth8/pvp/0.0.1/opponentsRefresh";

    public enum CurrencyType {Free, RefreshCurrency ,HardCurrency};

    public PVPOpponentsRefreshAnalytic(long playerId, int currencyUsed)
    {
        super(MESSAGE_TYPE);
        data.addProperty("player_id", playerId);
        data.addProperty("currency_type", CurrencyType.values()[currencyUsed].name());
    }
}
