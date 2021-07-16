package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;

import java.time.LocalDateTime;

public class TournamentSchedule implements Tournament.Schedule {
    private String type;
    private String description;
    private String icon;
    private final LocalDateTime start;
    private final LocalDateTime close;
    private final LocalDateTime end;
    private final int duration;
    private final int maxEntries;
    public TournamentSchedule(String type, String description, String icon, LocalDateTime start, LocalDateTime close, LocalDateTime end, int duration, int maxEntries){
        this.type = type;
        this.description = description;
        this.icon = icon;
        this.start = start;
        this.close = close;
        this.end = end;
        this.duration = duration;
        this.maxEntries = maxEntries;
    }
    @Override
    public String type() {
        return type;
    }
    @Override
    public String description() {
        return description;
    }
    @Override
    public String icon() {
        return icon;
    }
    @Override
    public LocalDateTime startTime() {
        return start;
    }

    @Override
    public LocalDateTime closeTime() {
        return close;
    }

    @Override
    public LocalDateTime endTime() {
        return end;
    }

    @Override
    public int maxEntriesPerInstance() {
        return maxEntries;
    }

    @Override
    public int instanceDurationInMinutes() {
        return duration;
    }
}
