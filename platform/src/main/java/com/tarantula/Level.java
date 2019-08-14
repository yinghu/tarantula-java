package com.tarantula;

import java.util.List;

/**
 * Developer: YINGHU LU
 * Date updated: 4/23/18
 * Time: 12:33 PM
 */
public interface Level extends Recoverable{

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
