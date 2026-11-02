package model.attack;

import model.entity.Boss;
import scene.BossScene;

public interface Attack {
    void update(double dt, Boss boss, BossScene scene);
}
