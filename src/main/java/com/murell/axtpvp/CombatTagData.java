package com.murell.axtpvp;

import org.bukkit.scheduler.BukkitTask;

public class CombatTagData {
    private final String zoneName;
    private final BukkitTask task;

    public CombatTagData(String zoneName, BukkitTask task) {
        this.zoneName = zoneName;
        this.task = task;
    }

    public String getZoneName() { return zoneName; }
    public void cancel() { if (task != null) task.cancel(); }
}
