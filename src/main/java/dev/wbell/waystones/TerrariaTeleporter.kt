package dev.wbell.waystones

import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.DecentHolograms
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class WayStones : JavaPlugin() {
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
        dev.wbell.waystones.WaystonePosition.loadPositions(File(dataFolder.absolutePath, "waystones.json"))
        PluginConfigFIle.loadConfig(config, File(dataFolder.absolutePath, "config.yml"))
        holograms = config.getBoolean("holograms")
        ambiantSound = Sound.valueOf(config.getString("ambiant-sound")!!)
    }

    override fun onDisable() {
        dev.wbell.waystones.WaystonePosition.savePositions()
        // Plugin shutdown logic
        logger.info("WayStones has been disabled")
        running = false
    }


    companion object {
        var holograms = false
        var ambiantSound = Sound.ENTITY_ENDER_DRAGON_DEATH
        fun createHologram(name: String, location: Location, text: List<String>) {
            if (holograms) {
                DHAPI.createHologram(name, location, text)
            }
        }

        fun deleteHologram(name: String) {
            if (holograms) {
                try {
                    DHAPI.removeHologram(name)
                } catch (_: Exception) {
                }

            }
        }

        fun editHologram(name: String, line: Int, text: String) {
            if (holograms) {
                try {
                    DHAPI.setHologramLine(DHAPI.getHologram(name), 0, text)
                } catch (_: Exception) {
                }
            }
        }

        var running = true
        var WaystonePosition = WaystonePosition()
        lateinit var instance: WayStones
            private set
    }
}