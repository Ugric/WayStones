package dev.wbell.terrariateleporter;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class flyCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        if (player.hasPermission("terrariateleport.fly")) {
            boolean allowedToFly = player.getAllowFlight();
            if (allowedToFly) {
                if (player.isFlying()) {
                    player.sendMessage("You are now falling!");
                } else {
                    player.sendMessage("You are now not flying!");
                }
                player.setFlying(false);
                player.setAllowFlight(false);
            } else {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.sendMessage("You are now flying!");
            }
        } else {
            player.sendMessage("You don't have permission to fly.");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
