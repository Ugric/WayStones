package dev.wbell.waystones
import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.Hologram
import de.oliver.fancyholograms.api.HologramData
import de.oliver.fancyholograms.api.HologramManager
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*

class FancyHolograms {
    companion object {

        fun runEnable() {
            for (waystone in WayStones.WaystonePosition.positions) {
                try {
                    createHologram(
                        waystone.id!!,
                        Location(Bukkit.getWorld(waystone.pos.world), waystone.pos.x + 0.5, (waystone.pos.y + 3), waystone.pos.z + 0.5),
                        waystone.name
                    )
                } catch (_: Exception) {
                    // ignore
                }
            }
            WayStones.instance.logger.info("Holograms have been enabled")
        }

        fun enableHolograms() {
            if (WayStones.fancyholograms) {
                try {
                    val pluginEnabled = FancyHologramsPlugin.get().plugin.isEnabled
                    if (pluginEnabled) {
                        runEnable()
                        return
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    WayStones.fancyholograms = false
                }
            }
        }

        fun createHologram(name: String, location: Location, text: String) {
            if (WayStones.fancyholograms) {
                val hologramManager: HologramManager = FancyHologramsPlugin.get().hologramManager
                val optionalHologram: Optional<Hologram> = hologramManager.getHologram(name)

                if (optionalHologram.isPresent) {
                    val hologram = optionalHologram.get()
                    val hologramLines = mutableListOf(text)
                    hologram.data.setText(hologramLines)
                    hologram.updateHologram()
                } else {
                    // Create a new hologram
                    val newHologram = hologramManager.create(HologramData(location.toString()))
                    val hologramLines = mutableListOf(text)
                    newHologram.data.setText(hologramLines)
                    newHologram.createHologram()
                }
            }
        }


        fun deleteHologram(name: String) {
            if (WayStones.fancyholograms) {
                try {
                    val hologramManager: HologramManager = FancyHologramsPlugin.get().hologramManager
                    val optionalHologram: Optional<Hologram> = hologramManager.getHologram(name)

                    if (optionalHologram.isPresent) {
                        val hologram = optionalHologram.get()
                        hologramManager.removeHologram(hologram)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun editHologram(name: String, text: String) {
            if (WayStones.fancyholograms) {
                try {
                    val hologramManager: HologramManager = FancyHologramsPlugin.get().hologramManager
                    val optionalHologram: Optional<Hologram> = hologramManager.getHologram(name)

                    if (optionalHologram.isPresent) {
                        val hologram = optionalHologram.get()
                        val hologramLines = mutableListOf(text)
                        hologram.data.setText(hologramLines)
                        hologram.updateHologram()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
