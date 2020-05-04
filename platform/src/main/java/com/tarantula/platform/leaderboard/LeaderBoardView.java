package com.tarantula.platform.leaderboard;


import java.util.List;

public class LeaderBoardView {
    public String category;
    public String classifier;
    public int size;
    public List<EntryView> board;

    public static class EntryView{
        public EntryView(int rank,String owner,double value,long timestamp){
            this.rank = rank;
            this.owner = owner;
            this.value = value;
            this.timestamp = timestamp;
        }
        public int rank;
        public String owner;
        public double value;
        public long timestamp;
    }
}
