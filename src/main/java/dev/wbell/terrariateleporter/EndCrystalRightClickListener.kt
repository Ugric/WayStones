package dev.wbell.terrariateleporter

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.type.Slab
import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin

class EndCrystalRightClickListener : Listener {
    private fun blockBreak(block: Block) {
        var pass = false
        for (material in waystoneBlocks) {
            if (block.type == material) {
                pass = true
                break
            }
        }
        if (!pass) return
        val location = block.location
        val x = block.x
        val y = block.y
        val z = block.z
        val position = waystonePosition.waystoneNear(PositionData(x.toDouble(), y.toDouble(), z.toDouble()))
        if (position != null) {
            waystonePosition.removeWaystone(position)
            val strikeLocation = Location(location.world, position.x + 0.5, position.y + 2, position.z + 0.5)
            strikeLocation.world.strikeLightningEffect(strikeLocation)
            for (selectPlayer in location.world.players) {
                if (selectPlayer.location.distance(location) <= 50) {
                    selectPlayer.playSound(location, Sound.ENTITY_WARDEN_DEATH, 1.0f, 1.0f)
                }
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        blockBreak(event.block)
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        val blocks = event.blockList()
        for (block in blocks) {
            blockBreak(block)
        }
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        val block = e.block
        val x = block.x
        val y = block.y
        val z = block.z
        val position = waystonePosition.waystoneNear(PositionData(x.toDouble(), y.toDouble(), z.toDouble()))
        if (position != null) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityDamagebyEntityEvent(e: EntityDamageByEntityEvent) {
        if (e.damager is Firework) {
            val fw = e.damager as Firework
            if (fw.hasMetadata("nodamage")) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock ?: return
        val location = block.location
        val x = block.x
        val y = block.y
        val z = block.z
        if (!event.action.toString().contains("RIGHT_CLICK")) return
        if (waystonePosition.waystoneExists(PositionData(x.toDouble(), y.toDouble(), z.toDouble()))) {
            val positions = waystonePosition.getAllPositionNotIncluding(PositionData(x.toDouble(), y.toDouble(), z.toDouble()))
            if (positions.isEmpty()) {
                player.sendMessage("No other waystones exist!")
                return
            }
            val position = positions[(Math.random() * positions.size).toInt()]
            val TeleportLocation = Location(location.world, position.x + 0.5, position.y, position.z - 0.5)
            val EffectLocation = Location(location.world, position.x + 0.5, position.y + 2, position.z + 0.5)
            player.teleport(TeleportLocation)
            val world = EffectLocation.world
            world.strikeLightningEffect(EffectLocation)
            val firework = world.spawn(EffectLocation, Firework::class.java)

            // Create firework meta
            val fireworkMeta = firework.fireworkMeta

            // Create a firework effect with a purple color
            val effect = FireworkEffect.builder()
                    .flicker(true)
                    .trail(true)
                    .withColor(Color.PURPLE)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .build()

            // Add the effect to the firework
            fireworkMeta.addEffect(effect)

            // Set the firework meta and detonate it immediately
            firework.fireworkMeta = fireworkMeta
            firework.setMetadata("nodamage", FixedMetadataValue(owningPluginInstance!!, true))
            firework.detonate()
            player.playSound(EffectLocation, Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.0f)
            return
        }
        val heldItem = player.inventory.itemInMainHand
        if (heldItem.type != Material.END_CRYSTAL) return
        if (block.type != Material.DEEPSLATE_BRICK_WALL) return
        run {
            val blockAbove = block.world.getBlockAt(x, y + 1, z)
            if (blockAbove.type != Material.LODESTONE) return
        }
        run {
            val blockBelow = block.world.getBlockAt(x, y - 1, z)
            if (blockBelow.type != Material.LODESTONE) return
        }
        run {
            // crying obsidian base
            for (i in 0..2) {
                for (j in 0 until 2 - i) {
                    val cryingObsidian = block.world.getBlockAt(x - 1 + i, y - 2, z - 1 + j)
                    if (cryingObsidian.type != Material.CRYING_OBSIDIAN) return
                }
            }
        }
        run {

            // deepslate brick slab bottom 4 around bottom lodestone
            val block1 = block.world.getBlockAt(x - 1, y - 1, z)
            if (block1.type != Material.DEEPSLATE_BRICK_SLAB) {
                return
            }
            val slab1 = block1.blockData as Slab
            if (slab1.type != Slab.Type.BOTTOM) return
            val block2 = block.world.getBlockAt(x + 1, y - 1, z)
            if (block2.type != Material.DEEPSLATE_BRICK_SLAB) return
            val slab2 = block2.blockData as Slab
            if (slab2.type != Slab.Type.BOTTOM) return
            val block3 = block.world.getBlockAt(x, y - 1, z - 1)
            if (block3.type != Material.DEEPSLATE_BRICK_SLAB) return
            val slab3 = block3.blockData as Slab
            if (slab3.type != Slab.Type.BOTTOM) return
            val block4 = block.world.getBlockAt(x, y - 1, z + 1)
            if (block4.type != Material.DEEPSLATE_BRICK_SLAB) return
            val slab4 = block4.blockData as Slab
            if (slab4.type != Slab.Type.BOTTOM) return
        }
        // check to make sure gaps around are air gaps
        for (height in -1..2) {
            for (i in 0..2) {
                for (j in 0..2) {
                    if (i == 1 && j == 1 && height >= 0 || (i == 0 && j == 1 || i == 1 && j == 0 || i == 1 && j == 2 || i == 2 && j == 1) && height == 2) continue
                    val air = block.world.getBlockAt(x - 1 + i, y + 1 - height, z - 1 + j)
                    if (air.type != Material.AIR) return
                }
            }
        }

        if (player.gameMode == GameMode.SURVIVAL) {
            if (heldItem.amount > 1) {
                heldItem.amount = heldItem.amount - 1
            } else {
                player.inventory.removeItem(heldItem)
            }
        }
        waystonePosition.addWaystone(PositionData(x.toDouble(), y.toDouble(), z.toDouble()))
        val world = location.world
        val EffectLocation = Location(location.world, x + 0.5, (y + 2).toDouble(), z + 0.5)
        world.strikeLightningEffect(EffectLocation)
        val firework = world.spawn(EffectLocation, Firework::class.java)

        // Create firework meta
        val fireworkMeta = firework.fireworkMeta

        // Create a firework effect with a purple color
        val effect = FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .withColor(Color.PURPLE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .build()

        // Add the effect to the firework
        fireworkMeta.addEffect(effect)

        // Set the firework meta and detonate it immediately
        firework.fireworkMeta = fireworkMeta
        firework.setMetadata("nodamage", FixedMetadataValue(owningPluginInstance!!, true))
        firework.detonate()
        for (selectPlayer in location.world.players) {
            if (selectPlayer.location.distance(location) <= 50) {
                selectPlayer.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f)
            }
        }

    }

    companion object {
        var waystoneBlocks = arrayOf(
                Material.DEEPSLATE_BRICK_WALL,
                Material.LODESTONE,
                Material.CRYING_OBSIDIAN,
                Material.DEEPSLATE_BRICK_SLAB,
                Material.AIR
        )

        @JvmField
        var owningPluginInstance: JavaPlugin? = null
    }
}