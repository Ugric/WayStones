package dev.wbell.waystones

import com.google.gson.Gson
import org.bukkit.Bukkit
import org.bukkit.Location
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*

class WaystonePosition {
    val positions: List<WayStoneData>
        get() = Companion.positions

    companion object {
        private val gson = Gson()
        private var dataFile: File? = null
        val positions: MutableList<WayStoneData> = ArrayList()
        fun waystoneNear(pos: PositionData): WayStoneData? {
            for (height in -1..2) {
                for (width in -1..1) {
                    for (length in -1..1) {
                        val position = waystoneExists(PositionData(pos.x + width, pos.y + height, pos.z + length, pos.world))
                        if (position!=null) {
                            return position
                        }
                    }
                }
            }
            return null
        }

        fun waystoneExists(pos: PositionData): WayStoneData? {
            for (position in positions) {
                if (position.pos.x == pos.x && position.pos.y == pos.y && position.pos.z == pos.z && position.pos.world == pos.world) {
                    return position
                }
            }
            return null
        }

        fun addWaystone(position: PositionData, name: String, owner: String?) {
            val id = "waystone-"+ UUID.randomUUID().toString()
            positions.add(WayStoneData(position, name, id, owner))
            Holograms.createHologram(id, Location(Bukkit.getWorld(position.world), position.x + 0.5, (position.y + 3), position.z + 0.5), name)
            savePositions()
        }

        fun renameWaystone(position: PositionData, name: String) {
            for (i in positions.indices) {
                val pos = positions[i]
                if (position.x == pos.pos.x && position.y == pos.pos.y && position.z == pos.pos.z) {
                    positions[i] = WayStoneData(position, name, pos.id, pos.owner)
                    Holograms.editHologram(pos.id!!, 0, name)
                    savePositions()
                    return
                }
            }
            throw RuntimeException("Waystone not found")
        }

        fun removeWaystone(position: PositionData) {
            for (i in positions.indices) {
                val pos = positions[i]
                if (position.x == pos.pos.x && position.y == pos.pos.y && position.z == pos.pos.z) {
                    positions.removeAt(i)
                    Holograms.deleteHologram(pos.id!!)
                    savePositions()
                    return
                }
            }
            throw RuntimeException("Waystone not found")
        }

        fun getAllPositionNotIncluding(position: PositionData): List<WayStoneData> {
            val positionsNotIncluding: MutableList<WayStoneData> = ArrayList()
            for (pos in positions) {
                if (position.x == pos.pos.x && position.y == pos.pos.y && position.z == pos.pos.z) continue
                positionsNotIncluding.add(pos)
            }
            return positionsNotIncluding
        }

        fun loadPositions(file: File) {
            dataFile = file
            if (!file.exists()) {
                savePositions()
                return
            }
            try {
                FileReader(file).use { reader ->
                    val loadedPositions = gson.fromJson(reader, Array<WayStoneData>::class.java)
                    positions.clear()
                    positions.addAll(listOf(*loadedPositions))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun savePositions() {
            try {
                dataFile?.let { FileWriter(it).use { writer -> gson.toJson(positions.toTypedArray(), writer) } }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}