package dev.wbell.waystones

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RenameWaystoneCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        if (args.isEmpty()) {
            return false
        }
        if (!sender.hasPermission("waystones.rename")) {
            sender.sendMessage("${ChatColor.DARK_RED}You do not have permission to use this command!")
            return true
        }
        val newName = args.joinToString(" ")
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
        WaystonePosition.renameWaystone(nearest.pos, newName)
        sender.sendMessage("Waystone renamed to $newName!")
        return true
    }
}