package dev.wbell.waystones

import eu.decentsoftware.holograms.api.DHAPI
import org.bukkit.Bukkit
import org.bukkit.Location

class Holograms {
    companion object {

        fun enable() {
            if (WayStones.holograms) {
                for (waystones in WaystonePosition.positions) {
                    createHologram(waystones.id!!, Location(Bukkit.getWorld(waystones.pos.world), waystones.pos.x + 0.5, (waystones.pos.y + 3), waystones.pos.z + 0.5), waystones.name)
                }
            }
        }
        fun disable() {
            for (waystones in WaystonePosition.positions) {
                try {
                    DHAPI.removeHologram(waystones.id!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

        fun createHologram(name: String, location: Location, text: String) {
            if (WayStones.holograms) {
                try {
                    DHAPI.createHologram(name, location, listOf("&5&l$text"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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