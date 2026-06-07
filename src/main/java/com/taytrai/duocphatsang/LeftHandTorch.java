package com.taytrai.duocphatsang;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class LeftHandTorch extends JavaPlugin {

    @Override
    public void onEnable() {
        // Đã truyền 'this' vào đây để kích hoạt bộ hẹn giờ
        getServer().getPluginManager().registerEvents(new TorchListener(this), this);
        getLogger().info("Plugin Cam Duoc Tay Trai phat sang da hoat dong tren 1.21!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin da dung!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("phattuoc")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                ItemStack torch = new ItemStack(Material.TORCH);
                p.getInventory().setItemInOffHand(torch);
                p.sendMessage("Đã ép đuốc vào tay trái!");
                return true;
            }
        }
        return false;
    }
}
