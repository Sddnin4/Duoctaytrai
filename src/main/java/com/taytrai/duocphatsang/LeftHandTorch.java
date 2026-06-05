package com.taytrai.duocphatsang;

import org.bukkit.plugin.java.JavaPlugin;

public final class LeftHandTorch extends JavaPlugin {

    @Override
    public void onEnable() {
        // Đăng ký sự kiện lắng nghe người chơi cầm đuốc
        getServer().getPluginManager().registerEvents(new TorchListener(), this);
        getLogger().info("Plugin Cam Duoc Tay Trai phat sang da hoat dong tren 1.21!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin da dung!");
    }
}
