package com.taytrai.duocphatsang;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class TorchListener implements Listener {

    private final JavaPlugin plugin;
    private final HashMap<UUID, Location> playerLights = new HashMap<>();
    private final HashMap<UUID, Long> lastSneakTime = new HashMap<>();

    // Nhận bản cài đặt từ file gốc để chạy Delay
    public TorchListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ⚡ MỚI: Xử lý khi đập block đi xuống
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        // Chờ 1 tick cho hệ thống phá block xong rồi mới bật đuốc ảo
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            handleTorchLight(player, player.getLocation());
        }, 1L);
    }

    // ⚡ MỚI: Xử lý khi đặt block đi lên (Tower)
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        // Chờ 1 tick cho block mới ổn định rồi mới tính toán lại ánh sáng
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            handleTorchLight(player, player.getLocation());
        }, 1L);
    }

    // --- CÁC SỰ KIỆN CŨ GIỮ NGUYÊN ---
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (lastSneakTime.containsKey(uuid) && (currentTime - lastSneakTime.get(uuid) < 500)) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            if (isSourceOfLight(mainHand.getType()) || isSourceOfLight(offHand.getType())) {
                player.getInventory().setItemInMainHand(offHand);
                player.getInventory().setItemInOffHand(mainHand);
                
                // Delay 1 chút khi đổi tay bằng nút ngồi
                Bukkit.getScheduler().runTaskLater(plugin, () -> handleTorchLight(player, player.getLocation()), 1L);
            }
            lastSneakTime.remove(uuid);
        } else {
            lastSneakTime.put(uuid, currentTime);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        handleTorchLight(event.getPlayer(), event.getTo());
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        handleTorchLight(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        handleTorchLight(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeLight(event.getPlayer());
        lastSneakTime.remove(event.getPlayer().getUniqueId());
    }

    private void handleTorchLight(Player player, Location loc) {
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (isSourceOfLight(offHandItem.getType())) {
            Location blockLoc = loc.getBlock().getLocation();

            if (playerLights.containsKey(player.getUniqueId()) && 
                playerLights.get(player.getUniqueId()).equals(blockLoc)) {
                return;
            }

            removeLight(player);

            // Cho phép phát sáng ở cả không khí hoặc khi đang bơi dưới nước
            if (blockLoc.getBlock().getType().isAir() || blockLoc.getBlock().getType() == Material.WATER) {
                Light lightData = (Light) Material.LIGHT.createBlockData();
                lightData.setLevel(15);

                player.sendBlockChange(blockLoc, lightData);
                playerLights.put(player.getUniqueId(), blockLoc);
            }
        } else {
            removeLight(player);
        }
    }

    private void removeLight(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerLights.containsKey(uuid)) {
            Location oldLoc = playerLights.get(uuid);
            player.sendBlockChange(oldLoc, oldLoc.getBlock().getBlockData());
            playerLights.remove(uuid);
        }
    }

    private boolean isSourceOfLight(Material material) {
        return material == Material.TORCH || 
               material == Material.SOUL_TORCH || 
               material == Material.REDSTONE_TORCH || 
               material == Material.LANTERN || 
               material == Material.SOUL_LANTERN ||
               material == Material.GLOWSTONE;
    }
}
