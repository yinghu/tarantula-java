package com.tarantula;

import java.util.List;

/**
 * Developer: YINGHU LU
 * Date updated: 8/22/19
 */
public interface Level extends Recoverable{

    String LEVEL_TAG = "level/xp";

    int level();
    void level(int level);

    double levelXP(double levelXP);

    void xp(XP xp);
    List<XP> list(String header,String category);
    List<XP> list();
    boolean onDailyGainReset();
    boolean onWeeklyGainReset();
    boolean onMonthlyGainReset();
    boolean onYearlyGainReset();
}
