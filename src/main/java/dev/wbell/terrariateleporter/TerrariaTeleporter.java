package dev.wbell.terrariateleporter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class TerrariaTeleporter extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("hello world");
        getCommand("fly").setExecutor(new flyCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}