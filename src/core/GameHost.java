package core;

import map.Progress;
import scene.Scene;

public interface GameHost {
    void goTo(Scene scene);

    ScoreManager getScoreManager();

    Progress getProgress();
}
