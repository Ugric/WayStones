package dev.wbell.waystones

import main.java.dev.wbell.waystones.EndCrystalRightClickListener
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ReiconWaystoneCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command!")
            return true
        }
        if (args.isEmpty()) {
            return false
        }
        if (!sender.hasPermission("waystones.reicon")) {
            sender.sendMessage("${ChatColor.DARK_RED}You do not have permission to use this command!")
            return true
        }
        val location = sender.location
        var nearest: WayStoneData? = null
        for (waystone in WayStones.WaystonePosition.positions) {
            if (waystone.pos.world != location.world.name) {
                continue
            } else if (nearest == null) {
                nearest = waystone
            } else {
                if (EndCrystalRightClickListener.distance(waystone.pos, PositionData(location.x, location.y, location.z, location.world.name)) < EndCrystalRightClickListener.distance(nearest.pos, PositionData(location.x, location.y, location.z, location.world.name))) {
                    nearest = waystone
                }
            }
        }
        if (nearest == null) {
            sender.sendMessage("No waystone found!")
            return true
        }

        // Determine the item the player is holding
        val heldItem = sender.inventory.itemInMainHand.type

        WaystonePosition.reiconWaystone(nearest.pos, heldItem)

        sender.sendMessage("Waystone icon updated!")
        return true
    }
}
