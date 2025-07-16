package com.murell.axtpvp;

import org.bukkit.Location;

public class Zone {
    private final String name, world;
    private final int x1, y1, z1, x2, y2, z2;

    public Zone(String name, Location a, Location b) {
        this.name = name;
        this.world = a.getWorld().getName();
        this.x1 = Math.min(a.getBlockX(), b.getBlockX());
        this.y1 = Math.min(a.getBlockY(), b.getBlockY());
        this.z1 = Math.min(a.getBlockZ(), b.getBlockZ());
        this.x2 = Math.max(a.getBlockX(), b.getBlockX());
        this.y2 = Math.max(a.getBlockY(), b.getBlockY());
        this.z2 = Math.max(a.getBlockZ(), b.getBlockZ());
    }

    public String getName() { return name; }

    public boolean contains(Location loc) {
        if (!loc.getWorld().getName().equals(world)) return false;
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        return x >= x1 && x <= x2
            && y >= y1 && y <= y2
            && z >= z1 && z <= z2;
    }
}
