package dev.wbell.terrariateleporter

import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*

class waystonePosition {
    val positions: List<PositionData>
        get() = Companion.positions

    companion object {
        private val gson = Gson()
        private var dataFile: File? = null
        val positions: MutableList<PositionData> = ArrayList()
        fun waystoneNear(pos: PositionData): PositionData? {
            for (height in -1..2) {
                for (width in -1..1) {
                    for (length in -1..1) {
                        if (waystoneExists(PositionData(pos.x + width, pos.y + height, pos.z + length))) {
                            return PositionData(pos.x + width, pos.y + height, pos.z + length)
                        }
                    }
                }
            }
            for (position in positions) {
                if (position.x == pos.x && position.y == pos.y && position.z == pos.z) {
                    return position
                }
            }
            return null
        }

        fun waystoneExists(pos: PositionData): Boolean {
            for (position in positions) {
                if (position.x == pos.x && position.y == pos.y && position.z == pos.z) {
                    return true
                }
            }
            return false
        }

        fun addWaystone(position: PositionData) {
            positions.add(position)
            savePositions()
        }

        fun removeWaystone(position: PositionData) {
            for (i in positions.indices) {
                val pos = positions[i]
                if (position.x == pos.x && position.y == pos.y && position.z == pos.z) {
                    positions.removeAt(i)
                    return
                }
            }
            savePositions()
        }

        fun getAllPositionNotIncluding(position: PositionData): List<PositionData> {
            val positionsNotIncluding: MutableList<PositionData> = ArrayList()
            for (pos in positions) {
                if (position.x == pos.x && position.y == pos.y && position.z == pos.z) continue
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
                    val loadedPositions = gson.fromJson(reader, Array<PositionData>::class.java)
                    positions.clear()
                    positions.addAll(listOf(*loadedPositions))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun savePositions() {
            try {
                dataFile?.let { FileWriter(it).use { writer -> gson.toJson(positions.toTypedArray<PositionData>(), writer) } }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}