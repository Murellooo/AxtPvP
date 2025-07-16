package com.murell.axtpvp;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class AxtPvPPlugin extends JavaPlugin implements Listener {
    private final Map<UUID,Selection> selections = new HashMap<>();
    private final Map<String,Zone> zones = new HashMap<>();
    private final Map<UUID,CombatTagData> tags = new HashMap<>();
    private final Set<String> blocked = Set.of("/home","/spawn","/tpa","/rtp","/shop");

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("axtpvp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("axtpvp.use")) {
            p.sendMessage("§cKeine Rechte."); return true;
        }
        if (args.length == 0) {
            giveAxe(p);
            p.sendMessage("§aAxt erhalten.");
            return true;
        }
        if (args[0].equalsIgnoreCase("create") && args.length > 1) {
            Selection sel = selections.get(p.getUniqueId());
            if (sel == null || !sel.isComplete()) {
                p.sendMessage("§cPos1+Pos2 setzen."); return true;
            }
            Zone zone = new Zone(args[1], sel.getPos1(), sel.getPos2());
            zones.put(zone.getName(), zone);
            selections.remove(p.getUniqueId());
            p.sendMessage("§aZone §b" + zone.getName() + " §aerstellt.");
            return true;
        }
        p.sendMessage("§cUnbekannt.");
        return true;
    }

    private void giveAxe(Player p) {
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta meta = axe.getItemMeta();
        meta.setDisplayName("§bAxtPvP-Axt");
        meta.setUnbreakable(true);
        meta.setLore(List.of("§7Linksklick: Pos1", "§7Rechtsklick: Pos2"));
        axe.setItemMeta(meta);
        p.getInventory().addItem(axe);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!hasAxe(p) || e.getClickedBlock() == null) return;
        e.setCancelled(true);
        Selection sel = selections.computeIfAbsent(p.getUniqueId(), k -> new Selection());
        Location loc = e.getClickedBlock().getLocation();
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            sel.setPos1(loc);
            p.sendMessage("§aPos1: " + fmt(loc));
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            sel.setPos2(loc);
            p.sendMessage("§aPos2: " + fmt(loc));
        }
    }

    private boolean hasAxe(Player p) {
        ItemStack i = p.getInventory().getItemInMainHand();
        return i != null && i.hasItemMeta()
            && "§bAxtPvP-Axt".equals(i.getItemMeta().getDisplayName());
    }

    private String fmt(Location l) {
        return l.getWorld().getName()+": "+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player vic) ||
            !(e.getDamager() instanceof Player dam)) return;
        Zone vz = getZone(vic.getLocation()), dz = getZone(dam.getLocation());
        if (vz == null || dz == null || !vz.getName().equals(dz.getName())) return;
        startTag(vic, vz.getName());
        startTag(dam, dz.getName());
    }

    private Zone getZone(Location loc) {
        return zones.values().stream()
            .filter(z -> z.contains(loc))
            .findFirst().orElse(null);
    }

    private void startTag(Player p, String zoneName) {
        tags.computeIfPresent(p.getUniqueId(), (u, old) -> { old.cancel(); return null; });
        BukkitRunnable task = new BukkitRunnable() {
            int sec = 15;
            @Override public void run() {
                if (sec <= 0) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("§aKampf vorbei"));
                    tags.remove(p.getUniqueId()); cancel(); return;
                }
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent("§cEnds in §f"+sec+"s"));
                sec--;
            }
        };
        task.runTaskTimer(this, 0, 20);
        tags.put(p.getUniqueId(), new CombatTagData(zoneName, task));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        CombatTagData tag = tags.get(e.getPlayer().getUniqueId());
        if (tag == null) return;
        Zone z = zones.get(tag.getZoneName());
        if (z != null && !z.contains(e.getTo())) {
            e.setTo(e.getFrom());
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent("§cBleib in der Zone"));
        }
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        if (tags.containsKey(e.getPlayer().getUniqueId())) {
            String cmd = e.getMessage().split(" ")[0].toLowerCase();
            if (blocked.contains(cmd)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cNicht im Kampf");
            }
        }
    }
}
