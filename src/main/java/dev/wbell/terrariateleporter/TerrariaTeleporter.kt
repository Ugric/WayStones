package dev.wbell.terrariateleporter

import org.bukkit.Particle
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.File

class TerrariaTeleporter : JavaPlugin() {
    override fun onEnable() {
        instance = this
        // Plugin startup logic
        val endCrystalRightClickListener = EndCrystalRightClickListener()
        endCrystalRightClickListener.playerNearbyHandler()
        EndCrystalRightClickListener.owningPluginInstance = this
        server.pluginManager.registerEvents(endCrystalRightClickListener, this)
        getCommand("renamewaystone")!!.setExecutor(RenameWaystoneCommand())
        // make all the folders
        File(dataFolder.absolutePath).mkdirs()
        dev.wbell.terrariateleporter.waystonePosition.loadPositions(File(dataFolder.absolutePath, "waystones.json"))
    }

    override fun onDisable() {
        dev.wbell.terrariateleporter.waystonePosition.savePositions()
        // Plugin shutdown logic
        logger.info("TerrariaTeleporter has been disabled")
        running = false
    }


    companion object {
        var running = true
        var waystonePosition = waystonePosition()
            lateinit var instance: TerrariaTeleporter
                private set
    }
}