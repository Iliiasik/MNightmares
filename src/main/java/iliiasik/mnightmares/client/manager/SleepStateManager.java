package iliiasik.mnightmares.client.manager;

import net.minecraft.world.entity.player.Player;

public class SleepStateManager {
    private boolean sleeping = false;
    private boolean previousSleeping = false;

    public void tick(Player player) {
        previousSleeping = sleeping;
        sleeping = player != null && player.isSleeping();
    }

    public boolean isSleeping() { return sleeping; }
    public boolean justStartedSleeping() { return sleeping && !previousSleeping; }
    public boolean justStoppedSleeping() { return !sleeping && previousSleeping; }
}