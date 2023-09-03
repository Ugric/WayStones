package dev.wbell.waystones

import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.DecentHologramsAPI
import org.bukkit.Bukkit
import org.bukkit.Location

class Holograms {
    companion object {

        fun runEnable() {
            for (waystone in WayStones.WaystonePosition.positions) {
                try {
                    createHologram(waystone.id!!, Location(Bukkit.getWorld(waystone.pos.world), waystone.pos.x + 0.5, (waystone.pos.y + 3), waystone.pos.z + 0.5), waystone.name)
                } catch (_: Exception) {
                    // ignore
                }
            }
            WayStones.instance.logger.info("Holograms have been enabled")
        }

        fun enableHolograms() {
            if (WayStones.holograms) {
                try {
                    val pluginEnabled = DecentHologramsAPI.isRunning()
                    if (pluginEnabled) {
                        runEnable()
                        return
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    WayStones.holograms = false
                }
            }
        }

        fun createHologram(name: String, location: Location, text: String) {
            if (WayStones.holograms) {
                DHAPI.createHologram(name, location, listOf("&5&l$text"))
            }
        }

        fun deleteHologram(name: String) {
            if (WayStones.holograms) {
                try {
                    DHAPI.removeHologram(name)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        fun editHologram(name: String, line: Int, text: String) {
            if (WayStones.holograms) {
                try {
                    DHAPI.setHologramLine(DHAPI.getHologram(name), line, "&5&l$text")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}