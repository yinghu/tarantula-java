package com.tarantula.platform.leveling;

import com.tarantula.Configuration;
import com.tarantula.Level;
import com.tarantula.Serviceable;

import java.util.HashMap;

/**
 * Updated by yinghu lu on 8/23/2019.
 */
public class XPLevelRule implements Serviceable{

    private Configuration configuration;
    private HashMap<Integer,XPRule> rules = new HashMap<>();

    public XPLevelRule(Configuration c){
        this.configuration = c;
    }
    public void onLevel(XPLevel l){
        XPRule xpRule = rules.get(l.level());
        l.levelView = new LevelView();
        l.levelView.name = xpRule.name;
        l.levelView.icon = xpRule.icon;
    }
    public void execute(Level level){
        XPRule cur = rules.get(level.level());
        if(cur!=null&&(!cur.onMatch(level.levelXP(0)))){
            for(int i=level.level()+1;i<11;i++){
                XPRule nex = rules.get(i);
                if(nex.onMatch(level.levelXP(0))){
                    level.level(nex.rank);
                    break;
                }
            }
        }
        else if(cur==null){
            XPRule nex = rules.get(1);
            level.level(nex.rank);
        }
        level.timestamp(System.currentTimeMillis());
    }

    public void start() throws Exception {
        for(int i=1;i<11;i++){
            String[] rc = this.configuration.property(""+i).split(",");
            XPRule r = new XPRule(i,rc[0],rc[1],Double.parseDouble(rc[2]),Double.parseDouble(rc[3]));
            rules.put(i,r);
        }
    }


    public void shutdown() throws Exception {

    }
    private static class XPRule{
        public XPRule(int r,String n,String c,double l,double h){
            this.rank = r;
            this.name = n;
            this.icon = c;
            this.low = l;
            this.high = h;
        }
        public int rank;
        public String name;
        public String icon;
        public double low;
        public double high;
        public boolean onMatch(double xp){
            return xp>=low&&xp<high;
        }
    }
}
