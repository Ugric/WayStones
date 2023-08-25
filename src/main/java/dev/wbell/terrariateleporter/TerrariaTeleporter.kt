package dev.wbell.terrariateleporter

import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TerrariaTeleporter : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        val endCrystalRightClickListener = EndCrystalRightClickListener()
        EndCrystalRightClickListener.owningPluginInstance = this
        server.pluginManager.registerEvents(endCrystalRightClickListener, this)
        // make all the folders
        File(dataFolder.absolutePath).mkdirs()
        dev.wbell.terrariateleporter.waystonePosition.loadPositions(File(dataFolder.absolutePath, "waystones.json"))
    }

    override fun onDisable() {
        dev.wbell.terrariateleporter.waystonePosition.savePositions()
        // Plugin shutdown logic
        logger.info("TerrariaTeleporter has been disabled")
    }

    companion object {
        var waystonePosition = waystonePosition()
    }
}