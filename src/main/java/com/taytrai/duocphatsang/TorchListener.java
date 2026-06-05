package com.taytrai.duocphatsang;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class TorchListener implements Listener {

    private final HashMap<UUID, Location> playerLights = new HashMap<>();
    // Lưu thời gian bấm nút ngồi của người chơi để tính toán "Double Crouch"
    private final HashMap<UUID, Long> lastSneakTime = new HashMap<>();

    // --- TÍNH NĂNG ĐỔI TAY BẰNG CÁCH NGỒI 2 LẦN (DÀNH CHO PE) ---
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        // Chỉ xử lý khi người chơi bắt đầu ngồi xuống (isSneaking = true)
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Nếu khoảng cách giữa 2 lần bấm ngồi nhỏ hơn 500 mili-giây (0.5 giây)
        if (lastSneakTime.containsKey(uuid) && (currentTime - lastSneakTime.get(uuid) < 500)) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            // Kiểm tra nếu một trong hai tay đang cầm đuốc/nguồn sáng thì mới cho đổi
            if (isSourceOfLight(mainHand.getType()) || isSourceOfLight(offHand.getType())) {
                // Hoán đổi item giữa tay phải và tay trái
                player.getInventory().setItemInMainHand(offHand);
                player.getInventory().setItemInOffHand(mainHand);
                
                // Cập nhật lại ánh sáng ngay lập tức
                handleTorchLight(player, player.getLocation());
            }
            lastSneakTime.remove(uuid); // Reset sau khi đổi thành công
        } else {
            // Lưu lại thời gian bấm ngồi lần 1
            lastSneakTime.put(uuid, currentTime);
        }
    }

    // --- CÁC LOGIC XỬ LÝ ÁNH SÁNG GIỮ NGUYÊN ---
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

            if (blockLoc.getBlock().getType().isAir()) {
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
