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

    // Lưu vị trí block ánh sáng ảo của từng người chơi để xóa khi họ di chuyển
    private final HashMap<UUID, Location> playerLights = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Chỉ xử lý nếu người chơi thực sự bước sang block khác
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        handleTorchLight(player, event.getTo());
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        // Cập nhật ánh sáng khi đổi item sang tay trái
        handleTorchLight(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        // Cập nhật khi đổi item tay phải (vì có thể ảnh hưởng trạng thái)
        handleTorchLight(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Xóa dữ liệu khi người chơi thoát để tránh rác bộ nhớ
        removeLight(event.getPlayer());
    }

    private void handleTorchLight(Player player, Location loc) {
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        // Kiểm tra xem tay trái có cầm các loại nguồn sáng không
        if (isSourceOfLight(offHandItem.getType())) {
            Location blockLoc = loc.getBlock().getLocation();

            // Nếu vị trí mới trùng với vị trí cũ đã phát sáng thì bỏ qua
            if (playerLights.containsKey(player.getUniqueId()) && 
                playerLights.get(player.getUniqueId()).equals(blockLoc)) {
                return;
            }

            // Xóa block sáng ở vị trí cũ trước
            removeLight(player);

            // Chỉ tạo ánh sáng ảo nếu chỗ đó là không khí hoặc block đi qua được
            if (blockLoc.getBlock().getType().isAir()) {
                Light lightData = (Light) Material.LIGHT.createBlockData();
                lightData.setLevel(15); // Độ sáng tối đa giống đuốc thật

                // Gửi block sáng ảo chỉ cho người chơi đó thấy
                player.sendBlockChange(blockLoc, lightData);
                playerLights.put(player.getUniqueId(), blockLoc);
            }
        } else {
            // Nếu không cầm đuốc nữa thì tắt ánh sáng đi
            removeLight(player);
        }
    }

    private void removeLight(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerLights.containsKey(uuid)) {
            Location oldLoc = playerLights.get(uuid);
            // Trả lại block thật ban đầu tại vị trí đó cho người chơi thấy
            player.sendBlockChange(oldLoc, oldLoc.getBlock().getBlockData());
            playerLights.remove(uuid);
        }
    }

    // Hàm kiểm tra các loại đuốc hoặc lồng đèn được phép phát sáng ở tay trái
    private boolean isSourceOfLight(Material material) {
        return material == Material.TORCH || 
               material == Material.SOUL_TORCH || 
               material == Material.REDSTONE_TORCH || 
               material == Material.LANTERN || 
               material == Material.SOUL_LANTERN ||
               material == Material.GLOWSTONE;
    }
}
