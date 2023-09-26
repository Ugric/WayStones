package dev.wbell.waystones

import org.bukkit.Material

class WayStoneData(
    @JvmField val pos: PositionData,
    @JvmField val name: String,
    val id: String?,
    @JvmField val owner: String?,
    @JvmField val rngBlock: Material?,
)
