package com.murell.axtpvp;

import org.bukkit.Location;

public class Selection {
    private Location pos1, pos2;

    public void setPos1(Location loc) { this.pos1 = loc; }
    public void setPos2(Location loc) { this.pos2 = loc; }

    public Location getPos1() { return pos1; }
    public Location getPos2() { return pos2; }

    public boolean isComplete() {
        return pos1 != null
            && pos2 != null
            && pos1.getWorld().equals(pos2.getWorld());
    }
}
