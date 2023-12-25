package com.tarantula.platform.achievement;

import com.icodesoftware.Achievement;

public class AchievementProxy implements Achievement {

    private AchievementProgress achievementProgress;
    private OnProgress progress;
    public AchievementProxy(AchievementProgress achievementProgress,OnProgress onProgress){
        this.achievementProgress = achievementProgress;
        this.progress = onProgress;
    }
    @Override
    public String name() {
        return achievementProgress.name();
    }

    @Override
    public int tier() {
        return achievementProgress.tier();
    }

    @Override
    public int target() {
        return achievementProgress.target();
    }

    @Override
    public double objective() {
        return achievementProgress.objective();
    }

    @Override
    public void onProgress(double delta) {
        progress.progress(delta);
    }
}
