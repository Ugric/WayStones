package dev.wbell.waystones

import main.java.dev.wbell.waystones.EndCrystalRightClickListener
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class WayStones : JavaPlugin() {
    override fun onEnable() {
        instance = this
        running = true
        val endCrystalRightClickListener = EndCrystalRightClickListener()
        server.pluginManager.registerEvents(endCrystalRightClickListener, this)
        getCommand("renamewaystone")!!.setExecutor(RenameWaystoneCommand())
        getCommand("reiconwaystone")!!.setExecutor(ReiconWaystoneCommand())
        File(dataFolder.absolutePath).mkdirs()
        dev.wbell.waystones.WaystonePosition.loadPositions(File(dataFolder.absolutePath, "waystones.json"))
        PluginConfigFile.loadConfig(config, File(dataFolder.absolutePath, "config.yml"))
        holograms = config.getBoolean("holograms")
        fancyholograms = config.getBoolean("fancyholograms")
        val ambiantSoundString = config.getString("ambiant-sound")
        if (ambiantSoundString != null) {
            try {
                ambiantSound = Sound.valueOf(ambiantSoundString)
            } catch (e: Exception) {
                logger.warning("Ambiant sound $ambiantSoundString is not a valid sound. disabling ambiant sounds.")
            }
        }
        val ambiantParticlesString = config.getString("ambiant-particles")
        if (ambiantParticlesString != null) {
            try {
                ambiantParticles = Particle.valueOf(ambiantParticlesString)
            } catch (e: Exception) {
                logger.warning("Ambiant particles $ambiantParticlesString is not a valid particle effect. disabling ambiant particles.")
            }
        }
        endCrystalRightClickListener.playerNearbyHandler()
        FancyHolograms.enableHolograms()
    }

    override fun onDisable() {
        dev.wbell.waystones.WaystonePosition.savePositions()
        // Plugin shutdown logic
        logger.info("WayStones has been disabled")
        running = false
    }


    companion object {
        var holograms = false
        var fancyholograms = false

        var running = true
        var WaystonePosition = WaystonePosition()
        lateinit var instance: WayStones
            private set
        var ambiantSound: Sound? = null
        var ambiantParticles: Particle? = null
    }
}